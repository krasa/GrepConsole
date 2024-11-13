package krasa.grepconsole.action;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import krasa.grepconsole.utils.Utils;

import java.awt.*;

public class QuickToggleEditorHighlightCSAction extends ToggleEditorHighlightAction {
	private static final Logger LOG = Logger.getInstance(QuickToggleEditorHighlightCSAction.class);

    public QuickToggleEditorHighlightCSAction() {
        super(true);
    }

    @Override
	protected Color chooseColor(Editor editor) {
		return Utils.nextColor();
	}
}
