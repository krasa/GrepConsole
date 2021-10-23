package krasa.grepconsole.gui.table;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.treeStructure.treetable.TreeTableModelAdapter;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ArrayUtil;
import krasa.grepconsole.gui.MainSettingsForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class TableRowTransferHandler extends TransferHandler {
	private static final Logger log = Logger.getInstance(TableRowTransferHandler.class);

	static DataFlavor flavor = new DataFlavor(State.class, "krasa.grepconsole.gui.table.TableRowTransferHandler.State");

	static class State implements Transferable {
		private final CheckboxTreeTable table;

		public State(CheckboxTreeTable table) {
			this.table = table;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{flavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return TableRowTransferHandler.flavor.equals(flavor);
		}

		@NotNull
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor.equals(TableRowTransferHandler.flavor)) {
				return this;
			}
			return null;
		}
	}

	private CheckboxTreeTable table = null;
	private MainSettingsForm mainSettingsForm;

	public TableRowTransferHandler(CheckboxTreeTable table, MainSettingsForm mainSettingsForm) {
		this.table = table;
		this.mainSettingsForm = mainSettingsForm;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		assert (c == table);
		return new State(table);
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
		return TransferHandler.MOVE;
	}

	/**
	 * this sucks, I know it, you know it, everybody knows it
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!info.isDrop()) {
			return false;
		}

		try {
			State transferData = (State) info.getTransferable().getTransferData(flavor);
			CheckboxTreeTable sourceTable = transferData.table;
			CheckboxTreeTable destinationTable = (CheckboxTreeTable) info.getComponent();
			final TreeTableModelAdapter destinationModel = (TreeTableModelAdapter) destinationTable.getModel();
			JTable target = (JTable) info.getComponent();
			JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
			int rowTo = dl.getRow();
			int indexOffset = 0;


			DefaultMutableTreeNode destinationNode = (DefaultMutableTreeNode) destinationModel.getValueAt(rowTo, 0);
			if (destinationNode == null) {
				destinationNode = (DefaultMutableTreeNode) destinationTable.getTree().getModel().getRoot();
			}
			java.util.List<DefaultMutableTreeNode> nodesToSelect = new ArrayList<>();
			java.util.List<DefaultMutableTreeNode> nodesToExpand = new ArrayList<>();
			List<DefaultMutableTreeNode> selectedNodes = getSelectedNodes(sourceTable);

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

			TableUtils.reloadTree(destinationTable);
			TableUtils.expand(nodesToExpand, destinationTable);
			TableUtils.selectNodes(nodesToSelect, destinationTable);
			TableUtils.reloadTree(sourceTable);
			TableUtils.expand(nodesToExpand, sourceTable);
			TableUtils.selectNodes(nodesToSelect, sourceTable);

			mainSettingsForm.rebuildProfile();
			target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			sourceTable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			target.grabFocus();
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}

	@NotNull
	protected List<DefaultMutableTreeNode> getSelectedNodes(CheckboxTreeTable sourceTable) {
		TreeTableTree sourceTree = sourceTable.getTree();
		int[] selectionRows = sourceTree.getSelectionRows();
		Arrays.sort(selectionRows);
		selectionRows = ArrayUtil.reverseArray(selectionRows);
		List<DefaultMutableTreeNode> selectedNodes = new ArrayList<>();
		for (int selectionRow : selectionRows) {
			TreePath treePath = sourceTree.getPathForRow(selectionRow);
			selectedNodes.add((DefaultMutableTreeNode) treePath.getLastPathComponent());
		}
		return selectedNodes;
	}

	public List<DefaultMutableTreeNode> getChildren(DefaultMutableTreeNode parent) {
		Enumeration children = parent.children();
		List<DefaultMutableTreeNode> list = new ArrayList<>();
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
