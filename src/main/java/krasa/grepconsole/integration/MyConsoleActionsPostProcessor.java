package krasa.grepconsole.integration;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import krasa.grepconsole.action.MoveErrorStreamToTheBottomAction;
import krasa.grepconsole.action.NextHighlightAction;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.action.PreviousHighlightAction;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsManager;
import krasa.grepconsole.stats.action.ShowHideStatisticsConsolePanelAction;
import krasa.grepconsole.stats.action.ShowHideStatisticsStatusBarPanelAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class MyConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.createHighlightFilterIfMissing(console);

		if (console instanceof ConsoleViewImpl) {
			StatisticsManager.createStatisticsPanels((com.intellij.execution.impl.ConsoleViewImpl) console);
		}

		ArrayList<AnAction> anActions = new ArrayList<>();
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.add(new PreviousHighlightAction(console));
		anActions.add(new NextHighlightAction(console));
		if (console instanceof ConsoleViewImpl) {
			try {
				//API check}
				if (MoveErrorStreamToTheBottomAction.findConsoleViewContentTypeKey() != null) {
					anActions.add(new MoveErrorStreamToTheBottomAction((ConsoleViewImpl) console));
				}
			} catch (Throwable e) {
				//ok
			}
		}
		anActions.addAll(Arrays.asList(actions));
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	/**
	 * not cached
	 */
	@NotNull
	@Override
	public AnAction[] postProcessPopupActions(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		ServiceManager manager = ServiceManager.getInstance();
		ArrayList<AnAction> anActions = new ArrayList<>();
		anActions.add(CustomActionsSchema.getInstance().getCorrectedAction("krasa.grepconsole.actions"));
		if (manager.isRegistered(console)) {
			anActions.add(new ShowHideStatisticsConsolePanelAction(console));
			anActions.add(new ShowHideStatisticsStatusBarPanelAction(console));
		}
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.add(new Separator());
		anActions.addAll(Arrays.asList(super.postProcessPopupActions(console, actions)));
		return anActions.toArray(new AnAction[anActions.size()]);
	}


}
