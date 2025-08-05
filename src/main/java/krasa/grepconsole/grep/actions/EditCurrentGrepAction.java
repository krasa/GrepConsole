package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.grep.gui.GrepPanel;
import org.jetbrains.annotations.NotNull;


public class EditCurrentGrepAction extends MyDumbAwareAction {


	public EditCurrentGrepAction() {
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		GrepPanel grepPanel = ApplyAction.getGrepPanel(e);
		if (grepPanel != null) {
			grepPanel.requestFocusToLastTextArea();
		}
	}


	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabledAndVisible(ApplyAction.getGrepPanel(e) != null);
	}
}
