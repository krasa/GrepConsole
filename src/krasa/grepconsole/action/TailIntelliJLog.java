package krasa.grepconsole.action;

import java.io.File;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.DumbAwareAction;

/**
 * @author Vojtech Krasa
 */
public class TailIntelliJLog extends DumbAwareAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final File logFile = new File(PathManager.getLogPath(), "idea.log");
		new OpenFileInConsoleAction().openFileInConsole(getEventProject(e), logFile);
	}
}
