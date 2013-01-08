package krasa.grepconsole.plugin;

import javax.swing.*;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.util.IconLoader;

public class OpenSettingsAction extends HighlightManipulationAction {
	public static final Icon ICON = IconLoader.getIcon("highlight.gif", OpenSettingsAction.class);
	private ConsoleView console;

	public OpenSettingsAction(ConsoleView console) {
		super("Open Grep Console settings", null, ICON);
		this.console = console;
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		boolean b = ShowSettingsUtil.getInstance().editConfigurable(e.getProject(),
				GrepConsoleApplicationComponent.getInstance());
		if (b) {
			if (console != null) {
				resetHighlights(console);
			}
		}
	}

}
