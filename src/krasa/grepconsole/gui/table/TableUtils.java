package krasa.grepconsole.gui.table;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class TableUtils {
	public static void reloadTree(final CheckboxTreeTable treeTable) {
		final List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(treeTable.getTree());
		((ListTreeTableModelOnColumns) treeTable.getTree().getModel()).reload();
		TreeUtil.restoreExpandedPaths(treeTable.getTree(), expandedPaths);
	}

	public static void selectNode(DefaultMutableTreeNode newChild, final CheckboxTreeTable table) {
		table.getTree().setSelectionPath(new TreePath(newChild.getPath()));
	}

	public static void selectNodes(List<DefaultMutableTreeNode> selectedNodes, CheckboxTreeTable table) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (DefaultMutableTreeNode selectedNode : selectedNodes) {
			treePaths.add(new TreePath(selectedNode.getPath()));
		}
		table.getTree().setSelectionPaths(treePaths.toArray(new TreePath[treePaths.size()]));
	}
}
