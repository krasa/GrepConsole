package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
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

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		Project project = e.getProject();
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		e.getPresentation().setEnabled(project != null && editor != null);
	}
}
