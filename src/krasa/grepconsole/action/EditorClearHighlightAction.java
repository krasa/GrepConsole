package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;

public class EditorClearHighlightAction extends HighlightManipulationAction {

	public EditorClearHighlightAction() {
	}

	@Override
	public void applySettings() {
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if (editor != null) {
			editor.getMarkupModel().removeAllHighlighters();
		}
	}
}
