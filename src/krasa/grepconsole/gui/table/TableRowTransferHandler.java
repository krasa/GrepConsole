package krasa.grepconsole.gui.table;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.util.*;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ArrayUtil;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.treeStructure.treetable.TreeTableModelAdapter;

public class TableRowTransferHandler extends TransferHandler {
	private static final Logger log = Logger.getInstance(TableRowTransferHandler.class.getName());

	/* this is useless */
	private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class,
			DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");
	private CheckboxTreeTable table = null;

	public TableRowTransferHandler(CheckboxTreeTable table) {
	this.table = table;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		assert (c == table);
		return new DataHandler(table.getTree().getSelectionPaths(), localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		boolean b = info.getComponent() == table && info.isDrop();
		table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		return b;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		try {
			final TreeTableModelAdapter model = (TreeTableModelAdapter) table.getModel();
			JTable target = (JTable) info.getComponent();
			JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
			int rowTo = dl.getRow();
			DefaultMutableTreeNode destinationNode = (DefaultMutableTreeNode) model.getValueAt(rowTo, 0);
			if (destinationNode == null) {
				destinationNode = (DefaultMutableTreeNode) model.getValueAt(rowTo - 1, 0);
				if (destinationNode instanceof GrepExpressionItemTreeNode) {
					destinationNode = (DefaultMutableTreeNode) destinationNode.getParent();
		}
			}
			TreeTableTree tree = table.getTree();

			int[] selectionRows = tree.getSelectionRows();
			Arrays.sort(selectionRows);
			selectionRows = ArrayUtil.reverseArray(selectionRows);
			java.util.List<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>();
			for (int selectionRow : selectionRows) {
				TreePath treePath = tree.getPathForRow(selectionRow);
				selectedNodes.add((DefaultMutableTreeNode) treePath.getLastPathComponent());
			}

			if (destinationNode instanceof GroupTreeNode) {
				for (DefaultMutableTreeNode nodeToMove : selectedNodes) {
					if (nodeToMove instanceof GroupTreeNode) {
						Enumeration children = nodeToMove.children();
						while (children.hasMoreElements()) {
							DefaultMutableTreeNode o = (DefaultMutableTreeNode) children.nextElement();
							destinationNode.add(o);
						}
					} else if (nodeToMove instanceof GrepExpressionItemTreeNode) {
						destinationNode.add(nodeToMove);
					}
				}
			} else {
				GroupTreeNode parent = (GroupTreeNode) destinationNode.getParent();
				int index = parent.getIndex(destinationNode);
				for (DefaultMutableTreeNode nodeToMove : selectedNodes) {
					if (nodeToMove instanceof GroupTreeNode) {
						Enumeration children = nodeToMove.children();
						while (children.hasMoreElements()) {
							DefaultMutableTreeNode o = (DefaultMutableTreeNode) children.nextElement();
							parent.add(o);
						}
					} else if (nodeToMove instanceof GrepExpressionItemTreeNode) {
						TreeNode parent1 = nodeToMove.getParent();
						if (parent1 == parent && index > parent1.getIndex(nodeToMove)) {
							index--;
						}
						parent.insert(nodeToMove, index);
					}
				}
			}
			TableUtils.reloadTree(table);
			TableUtils.selectNodes(selectedNodes, table);

			target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable t, int act) {
		if (act == TransferHandler.MOVE) {
			table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

}
