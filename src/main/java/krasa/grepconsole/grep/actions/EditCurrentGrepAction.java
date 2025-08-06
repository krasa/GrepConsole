package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.grep.gui.MyGrepSearchTextArea;
import org.jetbrains.annotations.NotNull;


public class EditCurrentGrepAction extends MyDumbAwareAction {


	public EditCurrentGrepAction() {
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		ApplyAction.getGrepPanel(e).requestFocusToLastTextArea();
	}


	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabledAndVisible(ApplyAction.getGrepPanel(e) != null
				&& MyGrepSearchTextArea.GREP_PANEL_TEXT_AREA.getData(e.getDataContext()) == null //not focused
		);
	}
}
