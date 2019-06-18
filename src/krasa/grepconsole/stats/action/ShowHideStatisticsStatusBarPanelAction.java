package krasa.grepconsole.stats.action;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.ConsoleStatusBarWidget;
import krasa.grepconsole.stats.StatisticsManager;
import org.jetbrains.annotations.NotNull;

public class ShowHideStatisticsStatusBarPanelAction extends DumbAwareAction {
	private final ConsoleView console;

	public ShowHideStatisticsStatusBarPanelAction(ConsoleView console) {
		this.console = console;
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		ConsoleStatusBarWidget statusBarPanel = StatisticsManager.getStatusBarPanel(
				(com.intellij.execution.impl.ConsoleViewImpl) console);

		if (statusBarPanel == null) {
			HighlightingFilter highlightingFilter = ServiceManager.getInstance().getHighlightFilter(console);
			if (highlightingFilter == null) {
				return;
			}
			if (!hasStatusBarItems(highlightingFilter)) {
				new OpenConsoleSettingsAction(console).actionPerformed(getEventProject(anActionEvent),
						SettingsContext.STATUS_BAR);
			}
			if (!hasStatusBarItems(highlightingFilter)) {
				return;
			}
			StatisticsManager.createStatusBarPanel((ConsoleViewImpl) console, highlightingFilter);
		} else {
			final StatusBar statusBar = WindowManager.getInstance().getIdeFrame(
					getEventProject(anActionEvent)).getStatusBar();
			statusBar.removeWidget(statusBarPanel.ID());
		}
	}

	public boolean hasStatusBarItems(@NotNull HighlightingFilter highlightingFilter) {
		boolean showCountInStatusBar = false;
		for (GrepProcessor grepProcessor : highlightingFilter.getGrepProcessors()) {
			showCountInStatusBar = grepProcessor.getGrepExpressionItem().isShowCountInStatusBar();
			if (showCountInStatusBar) {
				break;
			}
		}
		return showCountInStatusBar;
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		if (console instanceof ConsoleViewImpl) {
			ConsoleStatusBarWidget statusBarPanel = StatisticsManager.getStatusBarPanel(
					(com.intellij.execution.impl.ConsoleViewImpl) console);
			if (statusBarPanel != null) {
				e.getPresentation().setText("Hide Grep Console Statistics in StatusBar");
			}
			if (statusBarPanel == null) {
				e.getPresentation().setText("Show Grep Console Statistics in StatusBar");
			}
		} else {
			e.getPresentation().setVisible(false);

		}
	}
}
