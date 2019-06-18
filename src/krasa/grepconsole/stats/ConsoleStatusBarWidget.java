package krasa.grepconsole.stats;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.ui.UIUtil;
import krasa.grepconsole.filter.HighlightingFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConsoleStatusBarWidget implements CustomStatusBarWidget {

	private final String id;
	private final Project project;
	protected StatisticsStatusBarPanel statisticsPanel;

	public ConsoleStatusBarWidget(ConsoleViewImpl console, HighlightingFilter lastHighlightingFilter) {
		statisticsPanel = new StatisticsStatusBarPanel(console, lastHighlightingFilter) {
			@Override
			protected void hideStatusBar() {
				dispose();
			}
		};
		id = createId(console);
		project = console.getProject();
	}

	public static String createId(ConsoleView console) {
		return "GrepConsole" + Integer.toHexString(console.hashCode());
	}

	@Override
	public JComponent getComponent() {
		return statisticsPanel;
	}

	@NotNull
	@Override
	public String ID() {
		return id;
	}

	@Nullable
	@Override
	public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
		return null;
	}

	@Override
	public void install(@NotNull StatusBar statusBar) {
	}

	public StatisticsStatusBarPanel getStatisticsPanel() {
		return statisticsPanel;
	}

	@Override
	public void dispose() {
		UIUtil.invokeLaterIfNeeded(() -> {
			IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
			if (ideFrame != null) {
				final StatusBar statusBar = ideFrame.getStatusBar();
				statusBar.removeWidget(ID());
				statusBar.getComponent().revalidate();
			}

			if (statisticsPanel != null) {
				statisticsPanel.cancelTimer();
				statisticsPanel = null;
			}
		});
	}
}
