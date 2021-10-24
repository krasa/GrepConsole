package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
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
}
