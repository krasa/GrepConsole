package krasa.grepconsole.gui.table;

import com.intellij.ui.CheckedTreeNode;
import krasa.grepconsole.model.GrepExpressionGroup;

/**
 * @author Vojtech Krasa
 */
public class GrepExpressionGroupTreeNode extends CheckedTreeNode {

	public GrepExpressionGroupTreeNode(GrepExpressionGroup userObject) {
		super(userObject);
		setEnabled(true);
	}

	@Override
	public boolean isChecked() {
		return getObject().isEnabled();
	}

	@Override
	public void setChecked(boolean checked) {
		getObject().setEnabled(checked);
	}

	public GrepExpressionGroup getObject() {
		return (GrepExpressionGroup) userObject;
	}
}
