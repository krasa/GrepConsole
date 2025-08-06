package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.grep.gui.MyGrepSearchTextArea;
import org.jetbrains.annotations.NotNull;

public class JumpToConsoleOrRemoveExpressionAction extends MyDumbAwareAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		MyGrepSearchTextArea grepSearchTextArea = MyGrepSearchTextArea.GREP_PANEL_TEXT_AREA.getData(e.getDataContext());
		if (grepSearchTextArea != null) {
			grepSearchTextArea.escapeAction(e);
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabledAndVisible(MyGrepSearchTextArea.GREP_PANEL_TEXT_AREA.getData(e.getDataContext()) != null);
	}
}
