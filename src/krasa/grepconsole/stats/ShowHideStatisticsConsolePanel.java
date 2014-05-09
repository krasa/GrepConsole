package krasa.grepconsole.stats;

import javax.swing.*;

import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.integration.GrepConsoleActionsPostProcessor;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

/**
 * @author Vojtech Krasa
 */
public class ShowHideStatisticsConsolePanel extends DumbAwareAction {
	private final ConsoleView console;

	public ShowHideStatisticsConsolePanel(ConsoleView console) {
		super(GrepConsoleActionsPostProcessor.SHOW_GREP_CONSOLE_STATISTICS);
		this.console = console;
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		StatisticsConsolePanel statisticsConsolePanel = StatisticsManager.getStatisticsPanel((JPanel) console);
		if (statisticsConsolePanel != null) {
			if (statisticsConsolePanel.isVisible()) {
				statisticsConsolePanel.setVisible(false);
			} else {
				if (!statisticsConsolePanel.hasItems()) {
					new OpenConsoleSettingsAction(console).actionPerformed(anActionEvent);
				}
				if (statisticsConsolePanel.hasItems()) {
					statisticsConsolePanel.setVisible(true);
				}
			}
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		StatisticsConsolePanel statisticsConsolePanel = StatisticsManager.getStatisticsPanel((JPanel) console);
		if (statisticsConsolePanel != null) {
			e.getPresentation().setText(
					statisticsConsolePanel.isVisible() ? GrepConsoleActionsPostProcessor.HIDE_GREP_CONSOLE_STATISTICS
							: GrepConsoleActionsPostProcessor.SHOW_GREP_CONSOLE_STATISTICS);
		}
		if (statisticsConsolePanel == null) {
			e.getPresentation().setVisible(false);
		}
	}
}
