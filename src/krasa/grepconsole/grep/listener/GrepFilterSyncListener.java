package krasa.grepconsole.grep.listener;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.grep.GrepContextModel;
import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.utils.Notifier;
import krasa.grepconsole.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GrepFilterSyncListener implements GrepFilterListener {
	private static final Logger log = Logger.getInstance(GrepFilterSyncListener.class);

	private final OpenGrepConsoleAction.LightProcessHandler myProcessHandler;
	private final Project project;
	private volatile Profile profile;
	private volatile boolean showLimitNotification = true;
	private GrepCompositeModel grepModel;

	private final ThreadLocal<String> previousIncompleteToken = new ThreadLocal<>();
	private final ThreadLocal<Long> previousTimestamp = new ThreadLocal<>();

	public GrepFilterSyncListener(OpenGrepConsoleAction.LightProcessHandler myProcessHandler, Project project, Profile profile) {
		this.myProcessHandler = myProcessHandler;
		this.project = project;
		this.profile = profile;
	}

	@Override
	public void modelUpdated(@NotNull GrepCompositeModel grepModel) {
		this.grepModel = grepModel;
	}

	@Override
	public void profileUpdated(@NotNull Profile profile) {
		this.profile = profile;
	}

	@Override
	public void process(String text, ConsoleViewContentType type) {
		if (grepModel == null || StringUtils.isEmpty(text)) {
			return;
		}
		List<String> split = StringUtil.split(text, "\n", false, false);
		for (int i = 0; i < split.size(); i++) {
			String s = split.get(i);

			if (!s.endsWith("\n")) {
				Long lastTimestamp = previousTimestamp.get();
				if (lastTimestamp == null || System.currentTimeMillis() - lastTimestamp < 1000) {
					if (previousIncompleteToken.get() != null) {
						previousIncompleteToken.set(previousIncompleteToken.get() + s);
						previousTimestamp.set(System.currentTimeMillis());
					} else {
						previousIncompleteToken.set(s);
						previousTimestamp.set(System.currentTimeMillis());
					}
					continue;
				}
			}

			if (previousIncompleteToken.get() != null) {
				s = previousIncompleteToken.get() + s;
				previousIncompleteToken.set(null);
				previousTimestamp.set(null);
			}

			Key stdout = ProcessOutputTypes.STDOUT;
			if (type == ConsoleViewContentType.ERROR_OUTPUT) {
				stdout = ProcessOutputTypes.STDERR;
			} else if (type == ConsoleViewContentType.SYSTEM_OUTPUT) {
				stdout = ProcessOutputTypes.SYSTEM;
			}

			String substring = profile.limitInputGrepLength_andCutNewLine(s);
			CharSequence charSequence = profile.limitProcessingTime(substring);
			try {

				GrepContextModel contextModel = grepModel.getGrepContextModel();
				if (grepModel.matches(charSequence)) {
					if (!s.endsWith("\n")) {
						s = s + "\n";
					}

					contextModel.flushBeforeMatched(myProcessHandler);

					myProcessHandler.notifyTextAvailable(s, stdout);

					contextModel.matched();
				} else {
					contextModel.buffer(myProcessHandler, s, stdout);
				}
			} catch (ProcessCanceledException e) {
				String message = "Grep to a subconsole took too long, aborting to prevent input freezing.\n"
						+ "Consider changing following settings: 'Match only first N characters on each line' or 'Max processing time for a line'\n"
						+ "Matcher: " + grepModel + "\n" + "Line: " + Utils.toNiceLineForLog(substring);
				if (showLimitNotification) {
					showLimitNotification = false;
					Notifier.notify_GrepFilter(project, message);
				} else {
					log.warn(message);
				}
			}

		}

	}

	@Override
	public void clear() {
		previousTimestamp.remove();
		previousIncompleteToken.remove();
		GrepContextModel grepContextModel = grepModel.getGrepContextModel();
		grepContextModel.clear();
	}

	@Override
	public void dispose() {
		previousIncompleteToken.set(null);
	}

	@Override
	public String toString() {
		return "GrepFilterSyncListener{" +
				"grepModel=" + grepModel +
				'}';
	}
}
