package krasa.grepconsole.stats.action;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsConsolePanel;
import krasa.grepconsole.stats.StatisticsManager;

/**
 * @author Vojtech Krasa
 */
public class ShowHideStatisticsConsolePanelAction extends DumbAwareAction {
	private final ConsoleView console;

	public ShowHideStatisticsConsolePanelAction(ConsoleView console) {
		this.console = console;
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		GrepHighlightFilter highlightFilter = ServiceManager.getInstance().getHighlightFilter(console);
		if (highlightFilter == null) {
			return;
		}
		StatisticsConsolePanel statisticsConsolePanel = StatisticsManager.getConsolePanel((JPanel) console);
		if (statisticsConsolePanel == null) {

			if (!hasStatusItems(highlightFilter)) {
				new OpenConsoleSettingsAction(console).actionPerformed(getEventProject(anActionEvent),
						SettingsContext.CONSOLE);
			}
			if (!hasStatusItems(highlightFilter)) {
				return;
			}
			StatisticsManager.createConsolePanel((ConsoleViewImpl) console, highlightFilter);
		} else {
			statisticsConsolePanel.dispose();
		}
	}

	public boolean hasStatusItems(@NotNull GrepHighlightFilter highlightFilter) {
		boolean showCountInStatusBar = false;
		for (GrepProcessor grepProcessor : highlightFilter.getGrepProcessors()) {
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
