package krasa.grepconsole.plugin;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class OpenSettingsAction extends AnAction {
	public static final Icon ICON = IconLoader.getIcon("settings1.png", OpenSettingsAction.class);

	public OpenSettingsAction(ConsoleView console) {
		super("Open Grep Console settings", null, ICON);
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), GrepConsoleApplicationComponent.getInstance());
	}

}
