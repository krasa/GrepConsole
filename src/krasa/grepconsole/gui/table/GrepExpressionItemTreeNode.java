package krasa.grepconsole.gui.table;

import krasa.grepconsole.model.GrepExpressionItem;

import com.intellij.ui.CheckedTreeNode;

/**
 * @author Vojtech Krasa
 */
public class GrepExpressionItemTreeNode extends CheckedTreeNode {

	public GrepExpressionItemTreeNode(GrepExpressionItem userObject) {
		super(userObject);
		setEnabled(true);
	}

	@Override
	public boolean isChecked() {
		return getGrepExpressionItem().isEnabled();
	}

	@Override
	public void setChecked(boolean checked) {
		getGrepExpressionItem().setEnabled(checked);
	}

	public GrepExpressionItem getGrepExpressionItem() {
		return (GrepExpressionItem) userObject;
	}
}
