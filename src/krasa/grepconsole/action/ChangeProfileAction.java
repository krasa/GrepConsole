package krasa.grepconsole.action;

import javax.swing.*;

import krasa.grepconsole.model.Profile;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

public class ChangeProfileAction extends AnAction {
	public static final Icon ICON = IconLoader.getIcon("settings1.png", ChangeProfileAction.class);
	protected final ConsoleView console;

	public ChangeProfileAction(ConsoleView console) {
		super("Change profile", null, ICON);
		this.console = console;
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		// todo open combobox
		Profile profile = null;
		GrepConsoleApplicationComponent.getInstance().changeProfile(e.getProject(), profile);
	}

}
