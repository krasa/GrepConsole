package krasa.grepconsole.gui.table;

import krasa.grepconsole.model.GrepExpressionGroup;

import com.intellij.ui.CheckedTreeNode;

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
		return getGrepExpressionGroup().isEnabled();
	}

	@Override
	public void setChecked(boolean checked) {
		getGrepExpressionGroup().setEnabled(checked);
	}

	public GrepExpressionGroup getGrepExpressionGroup() {
		return (GrepExpressionGroup) userObject;
	}
}
