package krasa.grepconsole.grep.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import krasa.grepconsole.action.MyToggleAction;
import krasa.grepconsole.plugin.PluginState;

public class AutoApplyToggleAction extends MyToggleAction implements DumbAware {

	@Override
	public boolean isSelected(AnActionEvent anActionEvent) {
		return PluginState.getInstance().isAutoApplyGrepModel();
	}

	@Override
	public void setSelected(AnActionEvent anActionEvent, boolean b) {
		PluginState.getInstance().setAutoApplyGrepModel(b);
	}
}
