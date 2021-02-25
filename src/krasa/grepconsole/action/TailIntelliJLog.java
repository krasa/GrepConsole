package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class TailIntelliJLog extends DumbAwareAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = getEventProject(e);
		if (project == null) return;

		final File logFile = new File(PathManager.getLogPath(), "idea.log");
		new TailFileInConsoleAction().openFileInConsole(project, logFile, TailFileInConsoleAction.resolveEncoding(logFile));
	}
}
