package krasa.grepconsole.gui.table;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DragSource;
import java.util.*;
import java.util.List;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import krasa.grepconsole.gui.SettingsDialog;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.treeStructure.treetable.TreeTableModelAdapter;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ArrayUtil;

public class TableRowTransferHandler extends TransferHandler {
	private static final Logger log = Logger.getInstance(TableRowTransferHandler.class.getName());

	/* this seems useless */
	private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class,
			DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");
	private CheckboxTreeTable table = null;
	private SettingsDialog settingsDialog;

	public TableRowTransferHandler(CheckboxTreeTable table, SettingsDialog settingsDialog) {
		this.table = table;
		this.settingsDialog = settingsDialog;
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
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		return;
	}
	
	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	/** this sucks, we all know it */
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		try {
			final TreeTableModelAdapter model = (TreeTableModelAdapter) table.getModel();
			JTable target = (JTable) info.getComponent();
			if (!info.isDrop()) {
				return false;
			}
			JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
			int rowTo = dl.getRow();
			int indexOffset = 0;
			DefaultMutableTreeNode destinationNode = (DefaultMutableTreeNode) model.getValueAt(rowTo, 0);
			if (destinationNode == null) {
				destinationNode = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
			}
			TreeTableTree tree = table.getTree();

			int[] selectionRows = tree.getSelectionRows();
			Arrays.sort(selectionRows);
			selectionRows = ArrayUtil.reverseArray(selectionRows);

			java.util.List<DefaultMutableTreeNode> nodesToSelect = new ArrayList<DefaultMutableTreeNode>();
			java.util.List<DefaultMutableTreeNode> nodesToExpand = new ArrayList<DefaultMutableTreeNode>();
			java.util.List<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>();
			for (int selectionRow : selectionRows) {
				TreePath treePath = tree.getPathForRow(selectionRow);
				selectedNodes.add((DefaultMutableTreeNode) treePath.getLastPathComponent());
			}
			if (destinationNode instanceof GrepExpressionGroupTreeNode) {
				// reverse it back
				Collections.reverse(selectedNodes);
				for (DefaultMutableTreeNode nodeToMove : selectedNodes) {
					if (nodeToMove instanceof GrepExpressionGroupTreeNode) {
						if (destinationNode == nodeToMove) {
							continue;
						}
						DefaultMutableTreeNode parent = (DefaultMutableTreeNode) destinationNode.getParent();
						int index = parent.getIndex(destinationNode);
						int nodeToMoveIndex = parent.getIndex(nodeToMove);
						if (nodeToMoveIndex < index) {
							index--;
						}
						parent.insert(nodeToMove, index);
					} else if (nodeToMove instanceof GrepExpressionItemTreeNode) {
						TreeNode parent = destinationNode.getParent();
						int index = parent.getIndex(destinationNode);
						if (index > 0) {
							DefaultMutableTreeNode destinationNode1 = (DefaultMutableTreeNode) parent.getChildAt(index - 1);
							destinationNode1.add(nodeToMove);
						} else {
							destinationNode.add(nodeToMove);
						}
						nodesToExpand.add(nodeToMove);
					}
					nodesToSelect.add(nodeToMove);
				}
			} else if (destinationNode instanceof GrepExpressionItemTreeNode) {
				GrepExpressionGroupTreeNode destination = (GrepExpressionGroupTreeNode) destinationNode.getParent();
				int index = destination.getIndex(destinationNode) + indexOffset;
				for (DefaultMutableTreeNode nodeToMove : selectedNodes) {
					if (nodeToMove instanceof GrepExpressionGroupTreeNode) {
						if (destination == nodeToMove) {
							continue;
						}
						List<DefaultMutableTreeNode> children = getChildren(nodeToMove);
						nodesToSelect.addAll(children);
						for (DefaultMutableTreeNode node : children) {
							destination.insert(node, index);
							index++;
						}
					} else if (nodeToMove instanceof GrepExpressionItemTreeNode) {
						TreeNode parent = nodeToMove.getParent();
						if (parent == destination && index > parent.getIndex(nodeToMove)) {
							index--;
						}
						destination.insert(nodeToMove, index);

						nodesToSelect.add(nodeToMove);
						nodesToExpand.add(nodeToMove);
					}
				}
			} else {
                //destinationNode is root
				Collections.reverse(selectedNodes);
				for (DefaultMutableTreeNode nodeToMove : selectedNodes) {
					if (nodeToMove instanceof GrepExpressionGroupTreeNode) {
						destinationNode.add(nodeToMove);
						nodesToSelect.add(nodeToMove);
					} else if (nodeToMove instanceof GrepExpressionItemTreeNode) {
                        GrepExpressionGroupTreeNode lastChild = (GrepExpressionGroupTreeNode) destinationNode.getLastChild();
                        lastChild.add(nodeToMove);
                        nodesToSelect.add(nodeToMove);
					}
				}
			}

			TableUtils.reloadTree(table);
			TableUtils.expand(nodesToExpand, table);
			TableUtils.selectNodes(nodesToSelect, table);

			settingsDialog.rebuildProfile();
			target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	public List<DefaultMutableTreeNode> getChildren(DefaultMutableTreeNode parent) {
		Enumeration children = parent.children();
		List<DefaultMutableTreeNode> list = new ArrayList<DefaultMutableTreeNode>();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode o = (DefaultMutableTreeNode) children.nextElement();
			list.add(o);
		}
		return list;
	}

	@Override
	protected void exportDone(JComponent c, Transferable t, int act) {
		if (act == TransferHandler.MOVE) {
			table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

}
