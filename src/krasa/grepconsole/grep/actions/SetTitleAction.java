package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import krasa.grepconsole.grep.gui.GrepPanel;
import org.jetbrains.annotations.NotNull;

public class SetTitleAction extends DumbAwareAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		GrepPanel grepPanel = ApplyAction.getGrepPanel(e);
		if (grepPanel != null) {

			String s = Messages.showInputDialog(grepPanel, null, "Tab Title", null, grepPanel.createModel().getTitle(), new NonEmptyInputValidator());
			if (s != null) {
				grepPanel.setCustomTitle(s);
				grepPanel.apply();
			}
		}
	}
}
