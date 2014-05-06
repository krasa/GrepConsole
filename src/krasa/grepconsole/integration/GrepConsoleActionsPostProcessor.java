package krasa.grepconsole.integration;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class GrepConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		AnsiInputFilter lastAnsi = ServiceManager.getInstance().getLastAnsi();
		if (lastAnsi != null) {
			lastAnsi.setConsole(console);
		}
		createStatisticsPanel(console);

		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.addAll(Arrays.asList(actions));

		return anActions.toArray(new AnAction[anActions.size()]);
	}

	@NotNull
	@Override
	public AnAction[] postProcessPopupActions(@NotNull final ConsoleView console, @NotNull AnAction[] actions) {
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();

		anActions.add(new DumbAwareAction("Show Grep Console Statistics") {
			@Override
			public void actionPerformed(AnActionEvent anActionEvent) {
				StatisticsPanel statisticsPanel = getStatisticsPanel((JPanel) console);
				statisticsPanel.showStatistics();
			}
		});

		anActions.addAll(Arrays.asList(super.postProcessPopupActions(console, actions)));
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	public static void createStatisticsPanel(ConsoleView console) {
		JPanel consolePanel = (JPanel) console;
		StatisticsPanel statisticsPanel = getStatisticsPanel(consolePanel);
		if (statisticsPanel != null) {
			statisticsPanel.reset();
			statisticsPanel.setVisible(statisticsPanel.hasItems());
			statisticsPanel.revalidate();
		} else {
			GrepHighlightFilter lastGrepHighlightFilter = ServiceManager.getInstance().getLastGrepHighlightFilter();
			if (lastGrepHighlightFilter != null) {
				statisticsPanel = new StatisticsPanel(console, lastGrepHighlightFilter);
				statisticsPanel.setVisible(statisticsPanel.hasItems());
				consolePanel.add(statisticsPanel, BorderLayout.SOUTH);
	}
		}

	}

	private static StatisticsPanel getStatisticsPanel(JPanel consolePanel) {
		final BorderLayout layout = (BorderLayout) consolePanel.getLayout();
		final Component layoutComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
		return (StatisticsPanel) layoutComponent;
	}
}
