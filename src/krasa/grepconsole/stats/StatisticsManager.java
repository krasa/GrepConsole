package krasa.grepconsole.stats;

import java.awt.*;

import javax.swing.*;

import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.*;

/**
 * @author Vojtech Krasa
 */
public class StatisticsManager {
	public static void createStatisticsPanel(final ConsoleView console) {
		JPanel consolePanel = (JPanel) console;
		ConsoleViewImpl consoleView1 = (ConsoleViewImpl) console;
		StatisticsConsolePanel statisticsConsolePanel = getStatisticsPanel(consolePanel);
		if (statisticsConsolePanel != null) {
			statisticsConsolePanel.reset();
			statisticsConsolePanel.setVisible(statisticsConsolePanel.hasItems() && statisticsConsolePanel.isVisible());
			statisticsConsolePanel.revalidate();
		} else {
			final GrepHighlightFilter lastGrepHighlightFilter = ServiceManager.getInstance().getLastGrepHighlightFilter();
			if (lastGrepHighlightFilter != null) {
				// todo optimize it, no need to create it always i guess
				Profile profile = GrepConsoleApplicationComponent.getInstance().getProfile(
						lastGrepHighlightFilter.getProject());
				statisticsConsolePanel = new StatisticsConsolePanel(lastGrepHighlightFilter);
				statisticsConsolePanel.setVisible(statisticsConsolePanel.hasItems()
						&& profile.isShowStatsInConsoleByDefault());
				consolePanel.add(statisticsConsolePanel, BorderLayout.SOUTH);

				createStatusPanel(console, consoleView1, lastGrepHighlightFilter);
			}
		}

	}

	private static void createStatusPanel(final ConsoleView console, ConsoleViewImpl consoleView1,
			final GrepHighlightFilter lastGrepHighlightFilter) {
		final Project project = consoleView1.getProject();
		final StatusBar statusBar = WindowManager.getInstance().getIdeFrame(project).getStatusBar();
		final MyCustomStatusBarWidget statusBarWidget = new MyCustomStatusBarWidget((ConsoleViewImpl) console,
				lastGrepHighlightFilter);
		statusBar.addWidget(statusBarWidget);

		Disposer.register(console, statusBarWidget);
	}

	@Nullable
	public static StatisticsConsolePanel getStatisticsPanel(JPanel consolePanel) {
		final BorderLayout layout = (BorderLayout) consolePanel.getLayout();
		final Component layoutComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
		return (StatisticsConsolePanel) layoutComponent;
	}

	private static class MyCustomStatusBarWidget implements CustomStatusBarWidget {

		private GrepHighlightFilter lastGrepHighlightFilter;
		protected StatisticsStatusPanel statisticsPanel;

		public MyCustomStatusBarWidget(ConsoleViewImpl console, GrepHighlightFilter lastGrepHighlightFilter) {
			this.lastGrepHighlightFilter = lastGrepHighlightFilter;
			statisticsPanel = new StatisticsStatusPanel(console, lastGrepHighlightFilter);
		}

		@Override
		public JComponent getComponent() {
			Profile profile = GrepConsoleApplicationComponent.getInstance().getProfile(
					lastGrepHighlightFilter.getProject());
			statisticsPanel.setVisible(statisticsPanel.hasItems() && profile.isShowStatsInStatusBarByDefault());
			return statisticsPanel;
		}

		@NotNull
		@Override
		public String ID() {
			return "StatisticsPanel";
		}

		@Nullable
		@Override
		public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
			return null;
		}

		@Override
		public void install(@NotNull StatusBar statusBar) {
		}

		@Override
		public void dispose() {
			System.err.println("dispose");
			lastGrepHighlightFilter = null;
			statisticsPanel.cancelTimer();
			statisticsPanel.getParent().remove(statisticsPanel);
			statisticsPanel = null;

		}
	}
}
