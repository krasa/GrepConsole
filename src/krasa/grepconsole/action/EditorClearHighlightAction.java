package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.PluginState;

public class EditorClearHighlightAction extends HighlightManipulationAction {

	public EditorClearHighlightAction() {
	}

	@Override
	public void applySettings() {
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		PluginState pluginState = GrepConsoleApplicationComponent.getInstance().getState();
		pluginState.getDonationNagger().actionExecuted();
		
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if (editor != null) {
			editor.getMarkupModel().removeAllHighlighters();
		}
	}
}
