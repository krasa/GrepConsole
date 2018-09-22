package krasa.grepconsole.action;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import krasa.grepconsole.utils.Utils;

import java.awt.*;

public class QuickToggleEditorHighlightAction extends ToggleEditorHighlightAction {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(QuickToggleEditorHighlightAction.class);

	@Override
	protected Color chooseColor(Editor editor) {
		return Utils.nextColor();
	}
}
