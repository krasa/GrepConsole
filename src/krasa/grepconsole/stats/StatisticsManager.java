package krasa.grepconsole.stats;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Vojtech Krasa
 */
public class StatisticsManager {
	private static final Logger log = Logger.getInstance(StatisticsManager.class);

	public static void createStatisticsPanels(final ConsoleViewImpl console) {
		HighlightingFilter highlightingFilter = ServiceManager.getInstance().getHighlightFilter(console);
		if (highlightingFilter == null) {
			return;
		}

		Profile profile = ServiceManager.getInstance().getProfile(console);
		if (profile.isShowStatsInStatusBarByDefault()) {
			createStatusBarPanel(console, highlightingFilter);
		}

		if (profile.isShowStatsInConsoleByDefault()) {
			createConsolePanel(console, highlightingFilter);
		}
	}

	public static void resetStatisticsPanels(ConsoleViewImpl consoleViewImpl) {
		resetConsolePanel(consoleViewImpl);
		resetStatusBarPanel(consoleViewImpl);
	}

	private static void resetStatusBarPanel(ConsoleViewImpl console) {
		ConsoleStatusBarWidget statusBarPanel = getStatusBarPanel(console);
		if (statusBarPanel != null) {
			StatisticsStatusBarPanel statisticsPanel = statusBarPanel.getStatisticsPanel();
			statisticsPanel.reset();
			if (!statisticsPanel.hasItems()) {
				statusBarPanel.dispose();
			} else {
				statisticsPanel.revalidate();
			}
		} else {
			Profile profile = ServiceManager.getInstance().getProfile(console);
			HighlightingFilter highlightingFilter = ServiceManager.getInstance().getHighlightFilter(console);
			if (highlightingFilter != null && profile.isShowStatsInStatusBarByDefault()) {
				createStatusBarPanel(console, highlightingFilter);
			}
		}
	}

	public static void createStatusBarPanel(@NotNull ConsoleViewImpl consoleView,
											@NotNull HighlightingFilter highlightingFilter) {
		if (!highlightingFilter.hasGrepProcessorsForStatusBar()) {
			return;
		}
		try {
			final Project project = consoleView.getProject();
			final StatusBar statusBar = WindowManager.getInstance().getIdeFrame(project).getStatusBar();
			final ConsoleStatusBarWidget statusBarWidget = new ConsoleStatusBarWidget(consoleView, highlightingFilter);
			statusBar.getComponent().revalidate();
			Disposer.register(consoleView, statusBarWidget);
			statusBar.addWidget(statusBarWidget);
		} catch (Throwable e) {
			log.error(e);
		}
	}

	public static void resetConsolePanel(ConsoleViewImpl consoleView) {
		StatisticsConsolePanel statisticsConsolePanel = getConsolePanel(consoleView);
		if (statisticsConsolePanel != null) {
			statisticsConsolePanel.reset();
			if (!statisticsConsolePanel.hasItems()) {
				statisticsConsolePanel.dispose();
			} else {
				statisticsConsolePanel.revalidate();
			}
		} else {
			Profile profile = ServiceManager.getInstance().getProfile(consoleView);
			HighlightingFilter highlightingFilter = ServiceManager.getInstance().getHighlightFilter(consoleView);
			if (profile.isShowStatsInConsoleByDefault() && highlightingFilter != null) {
				createConsolePanel(consoleView, highlightingFilter);
			}
		}
	}

	public static void createConsolePanel(@NotNull ConsoleViewImpl consoleView,
										  @NotNull HighlightingFilter highlightingFilter) {
		if (!highlightingFilter.hasGrepProcessorsForConsolePanel()) {
			return;
		}
		StatisticsConsolePanel statisticsConsolePanel = new StatisticsConsolePanel(highlightingFilter, consoleView);
		if (statisticsConsolePanel.hasItems()) {
			consoleView.add(statisticsConsolePanel, BorderLayout.SOUTH);
			consoleView.revalidate();
			Disposer.register(consoleView, statisticsConsolePanel);
		} else {
			statisticsConsolePanel.dispose();
		}
	}

	@Nullable
	public static ConsoleStatusBarWidget getStatusBarPanel(@NotNull ConsoleViewImpl consoleView) {
		final Project project = consoleView.getProject();
		IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
		if (ideFrame == null) {
			return null;
		}
		StatusBar statusBar = ideFrame.getStatusBar();
		StatusBarWidget widget = statusBar.getWidget(ConsoleStatusBarWidget.createId(consoleView));
		return (ConsoleStatusBarWidget) widget;
	}

	@Nullable
	public static StatisticsConsolePanel getConsolePanel(JPanel consolePanel) {
		final BorderLayout layout = (BorderLayout) consolePanel.getLayout();
		final Component layoutComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
		return (StatisticsConsolePanel) layoutComponent;
	}

	public static void clearCount(ConsoleView console) {
		final HighlightingFilter highlightingFilter = ServiceManager.getInstance().getHighlightFilter(console);
		clearCount(highlightingFilter);
	}

	public static void clearCount(@Nullable HighlightingFilter highlightingFilter) {
		if (highlightingFilter != null) {
			for (GrepProcessor grepProcessor : highlightingFilter.getGrepProcessors()) {
				grepProcessor.resetMatches();
			}
		}
	}

}
