package krasa.grepconsole.stats;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.gui.SettingsContext;

import com.intellij.execution.impl.ConsoleViewImpl;

/**
 * @author Vojtech Krasa
 */
class OpenConsoleSettingsActionMouseInputAdapter extends MouseInputAdapter {

    private ConsoleView consoleView;
    private Project project;

    public OpenConsoleSettingsActionMouseInputAdapter(ConsoleView consoleView, Project project) {
		this.consoleView = consoleView;
        this.project = project;
    }

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			new OpenConsoleSettingsAction(consoleView).actionPerformed(project, SettingsContext.NONE);
		}
	}
}
