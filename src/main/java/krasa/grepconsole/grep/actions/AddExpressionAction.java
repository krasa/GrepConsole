package krasa.grepconsole.grep.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.grep.gui.GrepPanel;
import org.jetbrains.annotations.NotNull;

public class AddExpressionAction extends MyDumbAwareAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		GrepPanel grepPanel = getGrepPanel(e);
		if (grepPanel != null) {
			grepPanel.addExpression("");
		}
	}

	public static GrepPanel getGrepPanel(AnActionEvent e) {
		GrepPanel grepPanel = GrepPanel.GREP_PANEL.getData(e.getDataContext());
		if (grepPanel != null) {
			return grepPanel;
		}

		ConsoleView data = e.getData(LangDataKeys.CONSOLE_VIEW);
		if (data instanceof MyConsoleViewImpl) {
			grepPanel = ((MyConsoleViewImpl) data).getGrepPanel();
		}
		return grepPanel;
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabledAndVisible(getGrepPanel(e) != null);
	}
}
