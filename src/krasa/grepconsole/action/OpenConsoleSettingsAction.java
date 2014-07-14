package krasa.grepconsole.action;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import javax.swing.*;

public class OpenConsoleSettingsAction extends HighlightManipulationAction {
	public static final Icon ICON = IconLoader.getIcon("highlight.gif", OpenConsoleSettingsAction.class);
	public static final Icon STATS = IconLoader.getIcon("stats.gif", OpenConsoleSettingsAction.class);
	private ConsoleView console;

	public OpenConsoleSettingsAction() {
	}

	public OpenConsoleSettingsAction(ConsoleView console) {
		super("Open Grep Console settings", null, ICON);
		this.console = console;
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		GrepConsoleApplicationComponent instance = GrepConsoleApplicationComponent.getInstance();
		instance.setCurrentAction(this);
		ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), instance);
		instance.setCurrentAction(null);

	}

	@Override
	public void applySettings() {
		if (console != null) {
			resetHighlights(console);
		}
	}
}
