package krasa.grepconsole.grep.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.grep.gui.GrepPanel;
import krasa.grepconsole.plugin.PluginState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ApplyAction extends MyDumbAwareAction {
	public final Icon COMMIT_GRAY = IconLoader.getIcon("/krasa/grepconsole/icons/commitGray.svg", ApplyAction.class);
	public final Icon COMMIT_SELECTED = IconLoader.getIcon("/krasa/grepconsole/icons/commitBlue.svg", ApplyAction.class);

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (e.getInputEvent().isControlDown() || e.getInputEvent().isAltDown()) {
			PluginState instance = PluginState.getInstance();
			instance.setAutoApplyGrepModel(!instance.isAutoApplyGrepModel());
		}

		GrepPanel grepPanel = getGrepPanel(e);
		if (grepPanel != null) {
			grepPanel.apply();
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
		e.getPresentation().setIcon(PluginState.getInstance().isAutoApplyGrepModel() ? COMMIT_SELECTED : COMMIT_GRAY);
	}
}
