package krasa.grepconsole.integration;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

import krasa.grepconsole.action.AddHighlightAction;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class GrepConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	public static final String HIDE_GREP_CONSOLE_STATISTICS = "Hide Grep Console Statistics";
	public static final String SHOW_GREP_CONSOLE_STATISTICS = "Show Grep Console Statistics";

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		AnsiInputFilter lastAnsi = ServiceManager.getInstance().getLastAnsi();
		if (lastAnsi != null) {
			lastAnsi.setConsole(console);
		}
		StatisticsPanel.createStatisticsPanel(console);

		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.addAll(Arrays.asList(actions));

		return anActions.toArray(new AnAction[anActions.size()]);
	}

	@NotNull
	@Override
	public AnAction[] postProcessPopupActions(@NotNull final ConsoleView console, @NotNull AnAction[] actions) {
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new AddHighlightAction("Add highlight", "Add highlight for this selected text", null));

		anActions.add(new DumbAwareAction(SHOW_GREP_CONSOLE_STATISTICS) {
			@Override
			public void actionPerformed(AnActionEvent anActionEvent) {
				StatisticsPanel statisticsPanel = StatisticsPanel.getStatisticsPanel((JPanel) console);
				if (statisticsPanel.isVisible()) {
					statisticsPanel.setVisible(false);
				} else {
					if (!statisticsPanel.hasItems()) {
						new OpenConsoleSettingsAction(console).actionPerformed(anActionEvent);
					}
					if (statisticsPanel.hasItems()) {
						statisticsPanel.setVisible(true);
					}
				}

			}

			@Override
			public void update(AnActionEvent e) {
				super.update(e);
				StatisticsPanel statisticsPanel = StatisticsPanel.getStatisticsPanel((JPanel) console);
				e.getPresentation().setText(
						statisticsPanel.isVisible() ? HIDE_GREP_CONSOLE_STATISTICS : SHOW_GREP_CONSOLE_STATISTICS);
			}
		});
		anActions.addAll(Arrays.asList(super.postProcessPopupActions(console, actions)));

		return anActions.toArray(new AnAction[anActions.size()]);
	}
}
