package krasa.grepconsole.stats.action;

import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.GrepConsoleStatusBarWidget;
import krasa.grepconsole.stats.StatisticsManager;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

public class ShowHideStatisticsStatusBarPanelAction extends DumbAwareAction {
	private final ConsoleView console;

	public ShowHideStatisticsStatusBarPanelAction(ConsoleView console) {
		this.console = console;
	}

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		GrepConsoleStatusBarWidget statusBarPanel = StatisticsManager.getStatusBarPanel((com.intellij.execution.impl.ConsoleViewImpl) console);

		if (statusBarPanel == null) {
			GrepHighlightFilter highlightFilter = ServiceManager.getInstance().getHighlightFilter(console);

			if (!hasStatusBarItems(highlightFilter)) {
				new OpenConsoleSettingsAction(console).actionPerformed(anActionEvent);
			}
			if (!hasStatusBarItems(highlightFilter)) {
				return;
			}
			StatisticsManager.createStatusBarPanel((ConsoleViewImpl) console, highlightFilter);
		} else {
			final StatusBar statusBar = WindowManager.getInstance().getIdeFrame(getEventProject(anActionEvent)).getStatusBar();
			statusBar.removeWidget(statusBarPanel.ID());
		}
	}

	public boolean hasStatusBarItems(@NotNull GrepHighlightFilter highlightFilter) {
		boolean showCountInStatusBar = false;
		for (GrepProcessor grepProcessor : highlightFilter.getGrepProcessors()) {
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
			GrepConsoleStatusBarWidget statusBarPanel = StatisticsManager.getStatusBarPanel((com.intellij.execution.impl.ConsoleViewImpl) console);
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
