package krasa.grepconsole.gui.table;

import com.intellij.ui.CheckedTreeNode;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;

import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class GrepExpressionGroupTreeNode extends CheckedTreeNode {

	public GrepExpressionGroupTreeNode(GrepExpressionGroup userObject) {
		super(userObject);
		setEnabled(true);
		for (GrepExpressionItem item : userObject.getGrepExpressionItems()) {
			add(new GrepExpressionItemTreeNode(item));
		}
	}

	public boolean hasUncheckedChildren() {
		List<GrepExpressionItem> grepExpressionItems = getObject().getGrepExpressionItems();
		for (GrepExpressionItem grepExpressionItem : grepExpressionItems) {
			if (!grepExpressionItem.isEnabled()) {
				return true;
			}
		}
		return false;
	}


	public GrepExpressionGroup getObject() {
		return (GrepExpressionGroup) userObject;
	}
}
