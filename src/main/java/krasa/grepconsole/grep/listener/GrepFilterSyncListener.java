package krasa.grepconsole.grep.listener;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.grep.GrepBeforeAfterModel;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.utils.Notifier;
import krasa.grepconsole.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GrepFilterSyncListener implements GrepFilterListener {
	private static final Logger log = Logger.getInstance(GrepFilterSyncListener.class);

	private MyConsoleViewImpl newConsole;
	private final OpenGrepConsoleAction.LightProcessHandler myProcessHandler;
	private final Project project;
	private volatile Profile profile;
	private volatile boolean showLimitNotification = true;
	private GrepCompositeModel grepModel;

	private final ThreadLocal<List<Pair<String, ConsoleViewContentType>>> incompleteLine = ThreadLocal.withInitial(ArrayList::new);

	public GrepFilterSyncListener(MyConsoleViewImpl newConsole,
								  OpenGrepConsoleAction.LightProcessHandler myProcessHandler,
								  Project project,
								  Profile profile) {
		this.newConsole = newConsole;
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
			String token = split.get(i);

			if (StringUtils.isEmpty(token)) {
				continue;
			}

			List<Pair<String, ConsoleViewContentType>> oldTokens = incompleteLine.get();
			oldTokens.add(Pair.pair(token, type));

			//print only complete lines
			if (!token.endsWith("\n")) {
				continue;
			}


			StringBuilder sb = new StringBuilder();
			for (Pair<String, ConsoleViewContentType> t : oldTokens) {
				sb.append(t.first);
			}
			String substring = profile.limitInputGrepLength_andCutNewLine(sb.toString());
			CharSequence charSequence = profile.limitProcessingTime(substring);

			try {
				GrepBeforeAfterModel beforeAfterModel = grepModel.getBeforeAfterModel();
				if (grepModel.matches(charSequence)) {
					beforeAfterModel.flushBeforeMatched(newConsole);

					for (Pair<String, ConsoleViewContentType> t : oldTokens) {
						newConsole.print(t.first, t.second);
					}

					beforeAfterModel.matched();
				} else {
					beforeAfterModel.buffer(newConsole, new GrepBeforeAfterModel.Line(new ArrayList<>(oldTokens)));
				}

				oldTokens.clear();
			} catch (ProcessCanceledException e) {
				String message = "Grep to a subconsole took too long, aborting to prevent input freezing.\n"
						+
						"Consider changing following settings: 'Match only first N characters on each line' or 'Max processing time for a line'\n"
						+
						"Matcher: " +
						grepModel +
						"\n" +
						"Line: " +
						Utils.toNiceLineForLog(substring);
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
		incompleteLine.get().clear();
		GrepBeforeAfterModel beforeAfterModel = grepModel.getBeforeAfterModel();
		beforeAfterModel.clear();
	}

	@Override
	public void dispose() {
		incompleteLine.set(null);
		newConsole = null;
	}

	@Override
	public String toString() {
		return "GrepFilterSyncListener{" +
				"grepModel=" + grepModel +
				'}';
	}
}
