package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.StreamBufferSettings;
import krasa.grepconsole.plugin.ExtensionManager;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.utils.Notifier;
import krasa.grepconsole.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * every stream could have its own thread (java), or it could be all on some random pooled thread (debug in CLion)
 */
public class GrepInputFilter extends AbstractGrepFilter implements InputFilter {
	private static final Logger log = Logger.getInstance(GrepInputFilter.class);

	private static final Pair<String, ConsoleViewContentType> REMOVE_OUTPUT_PAIR = new Pair<>(null, null);
	public static final List<Pair<String, ConsoleViewContentType>> REMOVE_OUTPUT = Collections.singletonList(REMOVE_OUTPUT_PAIR);
	private static final Pattern PATTERN = Pattern.compile("(?<=\n)");

	private WeakReference<ConsoleView> console;
	private volatile boolean lastLineFiltered = false;
	private volatile boolean removeNextNewLine = false;
	private boolean blankLineWorkaround;
	private volatile GrepCopyingFilter grepFilter;
	private StreamBuffer streamBuffer;


	public GrepInputFilter(Project project, Profile profile) {
		super(project, profile);
	}

	public GrepInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	/*todo not reliable */
	public void init(WeakReference<ConsoleView> console, Profile profile) {
		this.profile = profile;
		this.console = console;
		ConsoleView consoleView = console.get();
		if (consoleView != null) {
			boolean testConsole = consoleView.getClass().getName().startsWith("com.intellij.execution.testframework.ui");
			blankLineWorkaround = testConsole;
			log.info("Initializing for " + consoleView.getClass().getName());
			if (profile.isBufferStreams()) {
				StreamBufferSettings streamBufferSettings = GrepConsoleApplicationComponent.getInstance().getState().getStreamBufferSettings();
				if (testConsole && !streamBufferSettings.isUseForTests()) {
					//no
				} else {
					streamBuffer = new StreamBuffer(consoleView, streamBufferSettings);
				}
			}
		}
		blankLineWorkaround = blankLineWorkaround || profile.isInputFilterBlankLineWorkaround();
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String text, ConsoleViewContentType consoleViewContentType) {
		if (consoleViewContentType != ConsoleViewContentType.USER_INPUT
				&& streamBuffer != null
				&& !Thread.holdsLock(streamBuffer.LOOP_GUARD)
				&& text != null) {
			boolean buffered = streamBuffer.buffer(text, consoleViewContentType);
			if (buffered) {
				return REMOVE_OUTPUT;
			}
		}


		List<Pair<String, ConsoleViewContentType>> result = filter(text, consoleViewContentType);

		grep(result, text, consoleViewContentType);

		return result;
	}

	@Nullable
	protected List<Pair<String, ConsoleViewContentType>> filter(String text, ConsoleViewContentType consoleViewContentType) {
		List<Pair<String, ConsoleViewContentType>> result = null;
		if (!grepProcessors.isEmpty()) {
			String[] split;
			try {
				split = PATTERN.split(profile.limitProcessingTime(text), 0);
			} catch (ProcessCanceledException e) {
				notifyError(text);
				return null;
			}

			result = new ArrayList<>(split.length);

			for (int i = 0; i < split.length; i++) {
				String s = split[i];
				if (lastLineFiltered && removeNextNewLine && s.equals("\n")) {
					removeNextNewLine = false;
					result.add(REMOVE_OUTPUT_PAIR);
					continue;
				}


				FilterState state = super.filter(s, -1);

				clearConsole(state, result, consoleViewContentType);

				prepareResult(result, s, state, consoleViewContentType);

			}

			//let other InputFilters work
			if (split.length == 1 && result.size() == 1) {
				if (result.get(0).first == text) {
					result = null;
				}
			}
		}

		return result;
	}

	private void notifyError(String text) {
		String message = "preparing input text for matching took too long, aborting to prevent GUI freezing.\n"
				+ "Consider reporting bug, not logging huge chunks of text, or changing following settings: 'Max processing time for a line'\n"
				+ "Text: " + Utils.toNiceLineForLog(text);
		if (showLimitNotification) {
			showLimitNotification = false;
			Notifier.notify_InputAndHighlight(project, message);
		} else {
			log.warn(message);
		}
	}

	//if we change output, then next InputFilter won't get processed, so Grep it here.
	private void grep(List<Pair<String, ConsoleViewContentType>> pairs, String text, ConsoleViewContentType consoleViewContentType) {
		if (grepFilter != null) {
			if (pairs == null) {
				grepFilter.applyFilter(text, consoleViewContentType);
			} else {
				for (int i = 0; i < pairs.size(); i++) {
					Pair<String, ConsoleViewContentType> pair = pairs.get(i);
					if (pair == REMOVE_OUTPUT_PAIR) {
					} else if (pair != null) {
						grepFilter.applyFilter(pair.first, pair.second);
					}
				}
			}
		}
	}

	public void clearConsole(FilterState state, List<Pair<String, ConsoleViewContentType>> pairs, ConsoleViewContentType consoleViewContentType) {
		if (state != null && state.isClearConsole()) {
			ConsoleView consoleView = console.get();
			if (consoleView != null) {
				consoleView.clear();

				grep(pairs, null, consoleViewContentType);
				pairs.clear();
			}
		}
	}

	private List<Pair<String, ConsoleViewContentType>> prepareResult(List<Pair<String, ConsoleViewContentType>> result, String s, FilterState state, ConsoleViewContentType consoleViewContentType) {
		boolean previousLineFiltered = lastLineFiltered;
		lastLineFiltered = false;
		removeNextNewLine = false;

		if (state != null) {
			if (state.isExclude()) {
				result.add(REMOVE_OUTPUT_PAIR);
				lastLineFiltered = true;
				removeNextNewLine = blankLineWorkaround && state.notTerminatedWithNewline();
				return result;
			} else if (profile.isMultilineInputFilter() && !state.isMatchesSomething() && previousLineFiltered) {
				result.add(REMOVE_OUTPUT_PAIR);
				lastLineFiltered = true;
				removeNextNewLine = blankLineWorkaround && state.notTerminatedWithNewline();
				return result;
			}
		}

		if (state != null && state.isTextChanged()) {
			result.add(new Pair<>(state.getText(), consoleViewContentType));
			return result;
		} else {
			// input is not changed
			result.add(new Pair<>(s, consoleViewContentType));
			return result;
		}
	}

	@Override
	public void onChange() {
		super.onChange();
		lastLineFiltered = false;
	}

	@Override
	protected void initProcessors() {
		grepProcessors = new ArrayList<>();
		if (profile.isEnabledInputFiltering()) {
			boolean inputFilterExists = false;

			if (profile.isTestHighlightersInInputFilter()) {
				grepProcessors.add(new HighlighterTestProcessor(profile.getAllGrepExpressionItems()));
			}

			for (GrepExpressionItem grepExpressionItem : profile.getAllInputFilterExpressionItems()) {
				if (!grepExpressionItem.isEnabled()) {
					continue;
				}
				GrepProcessor processor = createProcessor(grepExpressionItem);

				validate(processor);

				grepProcessors.add(processor);
				inputFilterExists = true;
			}
			if (!inputFilterExists) {
				grepProcessors.clear();
			}
		}
	}

	private void validate(GrepProcessor processor) {
		String action = processor.getGrepExpressionItem().getAction();
		if (StringUtils.isNotBlank(action) //blank == no action
				&& !GrepExpressionItem.ACTION_REMOVE_UNLESS_MATCHED.equals(action)
				&& !GrepExpressionItem.ACTION_BUFFER_UNTIL_NEWLINE.equals(action)
				&& !GrepExpressionItem.ACTION_REMOVE.equals(action)
				&& !GrepExpressionItem.ACTION_NO_ACTION.equals(action)) {
			if (ExtensionManager.getFunction(action) == null) {
				Notifier.notify_MissingExtension(action, project);
			}
		}
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem item) {
		// return profile.isEnabledInputFiltering() && (item.isInputFilter() || item.isClearConsole());
		throw new UnsupportedOperationException();
	}

	public GrepCopyingFilter getGrepFilter() {
		return grepFilter;
	}

	public void setGrepFilter(GrepCopyingFilter copyingFilter) {
		this.grepFilter = copyingFilter;
	}
}
