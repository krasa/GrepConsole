package krasa.grepconsole.action;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.plugin.MyConfigurable;
import krasa.grepconsole.utils.Rehighlighter;

public class OpenConsoleSettingsAction extends HighlightManipulationAction {
	private ConsoleView console;


	public OpenConsoleSettingsAction(ConsoleView console) {
		super("Open Grep Console settings", null, IconLoader.getIcon("/krasa/grepconsole/icons/highlight.svg", OpenConsoleSettingsAction.class));
		this.console = console;
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		Project project = e.getProject();
		actionPerformed(project, SettingsContext.NONE);
	}

	public void actionPerformed(Project project, SettingsContext settingsContext) {
		MyConfigurable instance = new MyConfigurable(project, console);
		instance.setCurrentAction(this);
		instance.prepareForm(settingsContext);
		ShowSettingsUtil.getInstance().editConfigurable(project, "GrepConsoleSettings", instance, true);
		instance.setCurrentAction(null);
	}

	@Override
	public void applySettings() {
		if (console != null) {
			new Rehighlighter().resetHighlights(console);
		}
	}
}
