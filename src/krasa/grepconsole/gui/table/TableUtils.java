package krasa.grepconsole.gui.table;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jetbrains.annotations.NotNull;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.util.ui.tree.TreeUtil;

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

	public static void expand(GrepExpressionGroupTreeNode newChild, CheckboxTreeTable table) {
		expand(table.getTree(), new TreePath(newChild.getPath()), 1);
	}

	public static void expand(List<DefaultMutableTreeNode> nodes, CheckboxTreeTable table) {
		for (DefaultMutableTreeNode node : nodes) {
			expand(table.getTree(), new TreePath(node.getPath()), 1);
		}
	}

	private static void expand(@NotNull JTree tree, @NotNull TreePath path, int levels) {
		if (levels != 0) {
			tree.expandPath(path);
			TreeNode node = (TreeNode) path.getLastPathComponent();
			Enumeration children = node.children();

			while (children.hasMoreElements()) {
				expand(tree, path.pathByAddingChild(children.nextElement()), levels - 1);
			}

		}
	}
}
