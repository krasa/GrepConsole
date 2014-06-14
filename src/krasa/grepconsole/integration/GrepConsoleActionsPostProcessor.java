package krasa.grepconsole.integration;

import java.util.ArrayList;
import java.util.Arrays;

import krasa.grepconsole.action.AddHighlightAction;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsManager;
import krasa.grepconsole.stats.action.ShowHideStatisticsConsolePanelAction;
import krasa.grepconsole.stats.action.ShowHideStatisticsStatusBarPanelAction;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.*;
import org.jetbrains.annotations.Nullable;

public class GrepConsoleActionsPostProcessor implements ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull final ConsoleView console, @NotNull AnAction[] actions) {
		AnsiInputFilter lastAnsi = ServiceManager.getInstance().getLastAnsi();
		if (lastAnsi != null) {
			lastAnsi.setConsole(console);
		}

		registerConsole(console);
		if (console instanceof ConsoleViewImpl) {
			StatisticsManager.createStatisticsPanels((com.intellij.execution.impl.ConsoleViewImpl) console);
		}

		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		ActionGroup e = new ActionGroup("Console statistics", true ) {
			@NotNull
			@Override
			public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
				return new AnAction[]{new OpenConsoleSettingsAction(console),
						new ShowHideStatisticsStatusBarPanelAction(console),
						new ShowHideStatisticsConsolePanelAction(console)};
			}
		};
		e.getTemplatePresentation().setIcon(OpenConsoleSettingsAction.ICON);
		anActions.add(e);
		anActions.addAll(Arrays.asList(actions));

		replaceClearAction(anActions);
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	public void registerConsole(ConsoleView console) {
		GrepHighlightFilter lastGrepHighlightFilter = ServiceManager.getInstance().getLastGrepHighlightFilter();
		if (lastGrepHighlightFilter != null) {
			ServiceManager.getInstance().register(console, lastGrepHighlightFilter);
		}
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
					StatisticsManager.clearCount(consoleView);
				}
			}
		};
	}

}
