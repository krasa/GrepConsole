package krasa.grepconsole.integration;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class GrepConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		AnsiInputFilter lastAnsi = ServiceManager.getInstance().getLastAnsi();
		if (lastAnsi != null) {
			lastAnsi.setConsole(console);
		}
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.addAll(Arrays.asList(actions));
		return anActions.toArray(new AnAction[anActions.size()]);
	}
}
