package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import krasa.grepconsole.plugin.PluginState;

public class AutoReloadToggleAction extends ToggleAction implements DumbAware {

	@Override
	public boolean isSelected(AnActionEvent anActionEvent) {
		return PluginState.getInstance().isAutoReloadGrepModel();
	}

	@Override
	public void setSelected(AnActionEvent anActionEvent, boolean b) {
		PluginState.getInstance().setAutoReloadGrepModel(b);
	}
}
