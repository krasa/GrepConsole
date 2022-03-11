package krasa.grepconsole.grep.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.grep.gui.GrepPanel;
import org.jetbrains.annotations.NotNull;

public class SourceAction extends DumbAwareAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		GrepPanel grepPanel = ApplyAction.getGrepPanel(e);
		if (grepPanel != null) {
			grepPanel.selectSource();
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		Presentation presentation = e.getPresentation();
		GrepPanel grepPanel = ApplyAction.getGrepPanel(e);
		if (grepPanel != null) {
			ConsoleView sourceConsole = grepPanel.getSourceConsole();
			if (sourceConsole instanceof MyConsoleViewImpl) {
				GrepPanel panel = ((MyConsoleViewImpl) sourceConsole).getGrepPanel();
				presentation.setText("Source: '" + panel.getCachedFullTitle() + "'");
			} else {
				presentation.setText("Source: Main Console");
			}
		}
	}
}
