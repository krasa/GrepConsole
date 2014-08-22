package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class TailIntelliJLog extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        final File logFile = new File(PathManager.getLogPath(), "idea.log");
        new OpenFileInConsoleAction().openFileInConsole(getEventProject(e), logFile);
        
    }
}
