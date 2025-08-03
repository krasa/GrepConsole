package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.grep.gui.GrepPanel;
import krasa.grepconsole.plugin.PluginState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ReloadAction extends MyDumbAwareAction {
	public final Icon RELOAD_GRAY = IconLoader.getIcon("/krasa/grepconsole/icons/buildLoadChangesGray.svg", ReloadAction.class);
	public final Icon RELOAD_SELECTED = IconLoader.getIcon("/krasa/grepconsole/icons/buildLoadChanges.svg", ReloadAction.class);

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (e.getInputEvent().isControlDown() || e.getInputEvent().isAltDown()) {
			PluginState instance = PluginState.getInstance();
			instance.setAutoReloadGrepModel(!instance.isAutoReloadGrepModel());
		}
		GrepPanel grepPanel = ApplyAction.getGrepPanel(e);
		if (grepPanel != null) {
			grepPanel.reload();
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setIcon(PluginState.getInstance().isAutoReloadGrepModel() ? RELOAD_SELECTED : RELOAD_GRAY);
	}
}
