package krasa.grepconsole.integration;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.action.AddHighlightAction;
import krasa.grepconsole.action.MoveErrorStreamToTheBottomAction;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.grep.OpenGrepConsoleAction;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsManager;
import krasa.grepconsole.stats.action.ShowHideStatisticsConsolePanelAction;
import krasa.grepconsole.stats.action.ShowHideStatisticsStatusBarPanelAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class MyConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.registerConsole(console);
		serviceManager.createHighlightFilterIfMissing(console);

		if (console instanceof ConsoleViewImpl) {
			StatisticsManager.createStatisticsPanels((com.intellij.execution.impl.ConsoleViewImpl) console);
		}

		ArrayList<AnAction> anActions = new ArrayList<>();
		anActions.add(new OpenConsoleSettingsAction(console));
		if (console instanceof ConsoleViewImpl) {
			try {
				//API check}
				if (MoveErrorStreamToTheBottomAction.getConsoleViewContentTypeKey() != null) {
					anActions.add(new MoveErrorStreamToTheBottomAction((ConsoleViewImpl) console));
				}
			} catch (Throwable e) {
				//ok
			}
		}
		anActions.addAll(Arrays.asList(actions));
		replaceClearAction(anActions);
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	/**
	 * not cached
	 */
	@NotNull
	@Override
	public AnAction[] postProcessPopupActions(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		ServiceManager manager = ServiceManager.getInstance();
		ArrayList<AnAction> anActions = new ArrayList<>();
		anActions.add(new OpenGrepConsoleAction("Grep", "Open a new filter/grep console", AllIcons.General.Filter));
		anActions.add(new AddHighlightAction("Add highlight", "Add highlight for this selected text", IconLoader.findIcon("/krasa/grepconsole/action/highlight.gif")));
		if (manager.isRegistered(console)) {
			anActions.add(new ShowHideStatisticsConsolePanelAction(console));
			anActions.add(new ShowHideStatisticsStatusBarPanelAction(console));
		} else {
			anActions.add(new OpenConsoleSettingsAction(console));
		}
		anActions.addAll(Arrays.asList(super.postProcessPopupActions(console, actions)));
		replaceClearAction(anActions);
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	private void replaceClearAction(ArrayList<AnAction> anActions) {
		for (int i = 0; i < anActions.size(); i++) {
			AnAction anAction = anActions.get(i);
			if (anAction instanceof ConsoleViewImpl.ClearAllAction) {
				anActions.set(i, clearAction());
			}
		}
	}

	private ConsoleViewImpl.ClearAllAction clearAction() {
		return new ConsoleViewImpl.ClearAllAction() {
			@Override
			public void actionPerformed(AnActionEvent e) {
				super.actionPerformed(e);
				final ConsoleView consoleView = e.getData(LangDataKeys.CONSOLE_VIEW);
				if (consoleView != null) {
					try {
						StatisticsManager.clearCount(consoleView);
					} catch (Exception e1) {
						// tough luck
					}
				}
			}
		};
	}

}
