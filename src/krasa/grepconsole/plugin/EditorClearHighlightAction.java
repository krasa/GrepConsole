package krasa.grepconsole.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;

public class EditorClearHighlightAction extends HighlightManipulationAction {

	public EditorClearHighlightAction() {
	}

	@Override
	public void applySettings() {
	}

	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if (editor != null) {
			editor.getMarkupModel().removeAllHighlighters();
		}
	}
}
