package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.ExtensionManager;
import krasa.grepconsole.utils.Notifier;
import org.apache.commons.lang.StringUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrepInputFilter extends AbstractGrepFilter implements InputFilter {

	private static final List<Pair<String, ConsoleViewContentType>> REMOVE_OUTPUT = Collections.singletonList(new Pair<>(null, null));

	private WeakReference<ConsoleView> console;
	private volatile boolean lastLineFiltered = false;
	private volatile boolean removeNextNewLine = false;
	private boolean testConsole;
	private volatile GrepCopyingFilter grepFilter;

	public GrepInputFilter(Project project, Profile profile) {
		super(project, profile);
	}

	public GrepInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	public void init(WeakReference<ConsoleView> console, Profile profile) {
		this.profile = profile;
		this.console = console;
		ConsoleView consoleView = console.get();
		if (consoleView != null) {
			testConsole = consoleView.getClass().getName().startsWith("com.intellij.execution.testframework.ui");
		}
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s, ConsoleViewContentType consoleViewContentType) {
		if (lastLineFiltered && removeNextNewLine && s.equals("\n")) {
			removeNextNewLine = false;
			return REMOVE_OUTPUT;
		}

		FilterState state = super.filter(s, -1);

		clearConsole(state);

		List<Pair<String, ConsoleViewContentType>> pairs = prepareResult(state, consoleViewContentType);


		grep(pairs, s, consoleViewContentType);

		return pairs;
	}

	//if we change output, then next InputFilter won't get processed, so Grep it here.
	private void grep(List<Pair<String, ConsoleViewContentType>> pairs, String s, ConsoleViewContentType consoleViewContentType) {
		if (grepFilter != null) {
			if (pairs != REMOVE_OUTPUT) {
				if (pairs != null) {
					Pair<String, ConsoleViewContentType> pair = pairs.get(0);
					grepFilter.applyFilter(pair.first, pair.second);
				} else {
					grepFilter.applyFilter(s, consoleViewContentType);
				}
			}
		}
	}

	public void clearConsole(FilterState state) {
		if (state != null && state.isClearConsole()) {
			ConsoleView consoleView = console.get();
			if (consoleView != null) {
				consoleView.clear();
			}
		}
	}

	private List<Pair<String, ConsoleViewContentType>> prepareResult(FilterState state, ConsoleViewContentType consoleViewContentType) {
		List<Pair<String, ConsoleViewContentType>> result = null;
		if (state != null) {
			if (state.isExclude()) {
				result = REMOVE_OUTPUT;
				lastLineFiltered = true;
			} else if (profile.isMultilineInputFilter() && !state.isMatchesSomething() && lastLineFiltered) {
				result = REMOVE_OUTPUT;
			}
		}
		if (result == null) {
			lastLineFiltered = false;
			removeNextNewLine = false;
			if (state != null && state.isTextChanged()) {
				return Collections.singletonList(new Pair<>(state.getText(), consoleViewContentType));
			}
			return null;// input is not changed
		} else {
			removeNextNewLine = testConsole && state.notTerminatedWithNewline();
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

	public void setGrepFilter(GrepCopyingFilter copyingFilter) {
		this.grepFilter = copyingFilter;
	}
}
