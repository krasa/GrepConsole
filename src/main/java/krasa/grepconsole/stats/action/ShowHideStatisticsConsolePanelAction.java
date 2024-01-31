package krasa.grepconsole.stats.action;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsConsolePanel;
import krasa.grepconsole.stats.StatisticsManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Vojtech Krasa
 */
public class ShowHideStatisticsConsolePanelAction extends MyDumbAwareAction {
	private final ConsoleView console;

	public ShowHideStatisticsConsolePanelAction(ConsoleView console) {
		this.console = console;
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		HighlightingFilter highlightingFilter = ServiceManager.getInstance().getHighlightFilter(console);
		if (highlightingFilter == null) {
			return;
		}
		StatisticsConsolePanel statisticsConsolePanel = StatisticsManager.getConsolePanel((JPanel) console);
		if (statisticsConsolePanel == null) {

			if (!hasStatusItems(highlightingFilter)) {
				new OpenConsoleSettingsAction(console).actionPerformed(getEventProject(anActionEvent),
						SettingsContext.CONSOLE_BAR);
			}
			if (!hasStatusItems(highlightingFilter)) {
				return;
			}
			StatisticsManager.createConsolePanel((ConsoleViewImpl) console, highlightingFilter);
		} else {
			statisticsConsolePanel.dispose();
		}
	}

	public boolean hasStatusItems(@NotNull HighlightingFilter highlightingFilter) {
		boolean showCountInStatusBar = false;
		for (GrepProcessor grepProcessor : highlightingFilter.getGrepProcessors()) {
			showCountInStatusBar = grepProcessor.getGrepExpressionItem().isShowCountInConsole();
			if (showCountInStatusBar) {
				break;
			}
		}
		return showCountInStatusBar;
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		if (console instanceof JPanel) {
			StatisticsConsolePanel statisticsConsolePanel = StatisticsManager.getConsolePanel((JPanel) console);
			e.getPresentation().setText(statisticsConsolePanel != null ? "Hide Grep Console Statistics in Console"
					: "Show Grep Console Statistics in Console");
		} else {
			e.getPresentation().setVisible(false);
		}
	}
}
