package krasa.grepconsole.gui;

import com.intellij.ide.CopyProvider;
import com.intellij.ide.CutProvider;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.ColumnInfo;
import krasa.grepconsole.gui.table.*;
import krasa.grepconsole.gui.table.column.*;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static krasa.grepconsole.Cloner.deepClone;

/**
 * @author Vojtech Krasa
 */
public class SettingsTableBuilder {
	public static final String STATUS_BAR_COUNT = "StatusBar count";
	public static final String CONSOLE_COUNT = "Console count";
	private CheckboxTreeTable table;

	public SettingsTableBuilder(final ProfileDetail profileDetail) {
		List<ColumnInfo> columns = new ArrayList<>();
		columns.add(new TreeColumnInfo("") {
			@Nullable
			@Override
			public String getPreferredStringValue() {
				return "________________";
			}

			@Override
			public int getWidth(JTable table) {
				return 64;
			}
		});
		columns.add(new GroupNameAdapter(new JavaBeanColumnInfo<GrepExpressionItem, String>("Expression",
				"grepExpression").preferedStringValue("___________________________________")));

		JavaBeanColumnInfo<GrepExpressionItem, String> unless = new JavaBeanColumnInfo<>(
				"Unless expression", "unlessGrepExpression");
		columns.add(new FolderColumnInfoWrapper(unless.preferedStringValue("______________")));
		unless.addListener(new ValueChangedListener<GrepExpressionItem, String>() {
			@Override
			public void onValueChanged(GrepExpressionItem grepExpressionItem, String newValue) {
				if (!StringUtils.isEmpty(newValue)) {
					grepExpressionItem.setWholeLine(true);
				}
			}
		});

		CheckBoxJavaBeanColumnInfo<GrepExpressionItem> inputFilter = new CheckBoxJavaBeanColumnInfo<>(
				"Filter out", "inputFilter");
		inputFilter.tooltipText("A line will not be filtered out if any previous expression matches first");
		inputFilter.addListener(new ValueChangedListener<GrepExpressionItem, Boolean>() {
			@Override
			public void onValueChanged(GrepExpressionItem grepExpressionItem, Boolean newValue) {
				if (newValue && !profileDetail.profile.isEnabledInputFiltering()) {
					profileDetail.profile.setEnabledInputFiltering(true);
					profileDetail.setData(profileDetail.profile);
				}
			}
		});
		columns.add(new FolderColumnInfoWrapper(inputFilter));
		CheckBoxJavaBeanColumnInfo<GrepExpressionItem> fold = new CheckBoxJavaBeanColumnInfo<>(
				"Fold", "fold");
		fold.addListener(new ValueChangedListener<GrepExpressionItem, Boolean>() {
			@Override
			public void onValueChanged(GrepExpressionItem grepExpressionItem, Boolean newValue) {
				if (newValue && !profileDetail.profile.isEnableFoldings()) {
					profileDetail.profile.setEnableFoldings(true);
					profileDetail.setData(profileDetail.profile);
				}
			}
		});
		FolderColumnInfoWrapper foldC = new FolderColumnInfoWrapper(fold);
		columns.add(foldC);
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Whole line", "wholeLine").tooltipText("Match a whole line, otherwise find a matching substrings - 'Unless expression' works only for whole lines.")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Continue matching", "continueMatching").tooltipText("If true, match a line against the next configured items to apply multiple highlights")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Case insensitive",
				"caseInsensitive")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Bold", "style.bold")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Italic",
				"style.italic")));
		columns.add(new FolderColumnInfoWrapper(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Background",
				"style.backgroundColor")));
		columns.add(new FolderColumnInfoWrapper(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Foreground",
				"style.foregroundColor")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(STATUS_BAR_COUNT, "showCountInStatusBar").tooltipText("Show count of occurrences in Status Bar statistics panel\n(the number may not be right for test executions)")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(CONSOLE_COUNT, "showCountInConsole").tooltipText("Show count of occurrences in Console statistics panel\n(the number may not be right for test executions)")));
		columns.add(new FolderColumnInfoWrapper(new SoundColumn("Sound", profileDetail)));
		columns.add(new FolderColumnInfoWrapper(new ClearColumn("Clear Console", profileDetail).tooltipText("Will not work if any previous non-filtering expression is matched first.")));

		CheckboxTreeCellRendererBase renderer = new CheckboxTreeCellRendererBase() {
			@Override
			public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
										  int row, boolean hasFocus) {
				if (value instanceof CheckedTreeNode) {
					CheckedTreeNode checkedTreeNode = (CheckedTreeNode) value;
					Object userObject = checkedTreeNode.getUserObject();
					if (userObject instanceof GrepExpressionGroup) {
						Icon icon = PlatformIcons.DIRECTORY_CLOSED_ICON;
						final ColoredTreeCellRenderer textRenderer = getTextRenderer();
						textRenderer.setIcon(icon);
					}
				}

			}
		};
		table = new MyCheckboxTreeTable(createRoot(), renderer, columns, foldC, profileDetail);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table, profileDetail));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(table.getTree());
		treeExpander.expandAll();
	}

	private CheckedTreeNode createRoot() {
		CheckedTreeNode root = new CheckedTreeNode(null);

		return root;
	}

	public CheckboxTreeTable getTable() {
		return table;
	}

	private static class MyCheckboxTreeTable extends CheckboxTreeTable implements CopyProvider, CutProvider, DataProvider, PasteProvider {
		private static final Logger LOG = Logger.getInstance(MyCheckboxTreeTable.class);
		private static final DataFlavor ARRAY_LIST = new DataFlavor(List.class, "List");

		private final ProfileDetail profileDetail;

		public MyCheckboxTreeTable(CheckedTreeNode root, CheckboxTreeCellRendererBase renderer, List<ColumnInfo> columns, FolderColumnInfoWrapper foldC, ProfileDetail profileDetail) {
			super(root, renderer, columns.toArray(new ColumnInfo[columns.size()]), foldC);
			this.profileDetail = profileDetail;
		}


		@Override
		public void performCopy(@NotNull DataContext dataContext) {
			try {
				CopyPasteManager.getInstance().setContents(new ListTransferable(getSelection()));
			} catch (Exception ex) {
				// probably don't have clipboard access or something
				LOG.info(ex);
			}
		}

		@Override
		public void performCut(@NotNull DataContext dataContext) {
			try {
				List<DefaultMutableTreeNode> selection = getSelection();
				List<DefaultMutableTreeNode> selectionCopy = cloneNodes(selection);

				CopyPasteManager.getInstance().setContents(new ListTransferable(selectionCopy));

				for (DefaultMutableTreeNode node : selection) {
					node.removeFromParent();
				}
			} catch (Exception ex) {
				// probably don't have clipboard access or something
				LOG.info(ex);
			}
			TableUtils.reloadTree(this);
			profileDetail.rebuildProfile();
		}

		@NotNull
		protected List<DefaultMutableTreeNode> cloneNodes(List<DefaultMutableTreeNode> selection) {
			List<DefaultMutableTreeNode> selectionCopy = new ArrayList<>();

			DefaultMutableTreeNode lastGroup = null;
			for (DefaultMutableTreeNode toAdd : selection) {
				if (toAdd instanceof GrepExpressionGroupTreeNode) {
					lastGroup = toAdd;
					DefaultMutableTreeNode clone = (DefaultMutableTreeNode) toAdd.clone();
					Enumeration children = toAdd.children();
					while (children.hasMoreElements()) {
						DefaultMutableTreeNode o = (DefaultMutableTreeNode) children.nextElement();
						clone.add((MutableTreeNode) o.clone());
					}
					selectionCopy.add(clone);
				} else if (toAdd instanceof GrepExpressionItemTreeNode) {
					if (lastGroup != null && lastGroup.isNodeChild(toAdd)) {
						continue;
					}
					selectionCopy.add((DefaultMutableTreeNode) toAdd.clone());
				} else
					throw new RuntimeException(String.valueOf(toAdd));
			}
			return selectionCopy;
		}

		@Override
		public void performPaste(@NotNull DataContext dataContext) {
			List<DefaultMutableTreeNode> listToAdd = CopyPasteManager.getInstance().getContents(ARRAY_LIST);
			if (listToAdd == null) {
				return;
			}
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getTree().getLastSelectedPathComponent();

			DefaultMutableTreeNode parent = null;
			int index = 0;
			int rootIndex = 0;
			if (selectedNode instanceof GrepExpressionItemTreeNode) {
				parent = (DefaultMutableTreeNode) selectedNode.getParent();
				index = parent.getIndex(selectedNode);
			} else if (selectedNode instanceof GrepExpressionGroupTreeNode) {
				parent = selectedNode;
				index = parent.getChildCount();
			}
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) parent.getParent();
			rootIndex = root.getIndex(parent);

			java.util.List<DefaultMutableTreeNode> nodesToSelect = new ArrayList<>();
			java.util.List<DefaultMutableTreeNode> nodesToExpand = new ArrayList<>();
			DefaultMutableTreeNode lastGroup = null;

			for (DefaultMutableTreeNode toAdd : listToAdd) {
				if (toAdd instanceof GrepExpressionGroupTreeNode) {
					GrepExpressionGroup grepExpressionGroup = deepClone(((GrepExpressionGroupTreeNode) toAdd).getGrepExpressionGroup());
					GrepExpressionGroupTreeNode newChild = new GrepExpressionGroupTreeNode(grepExpressionGroup);
					for (GrepExpressionItem item : grepExpressionGroup.getGrepExpressionItems()) {
						GrepExpressionItemTreeNode newItem = new GrepExpressionItemTreeNode(item);
						newChild.add(newItem);
					}
					root.insert(newChild, rootIndex++);
					nodesToExpand.add(newChild);
					nodesToSelect.add(newChild);
					lastGroup = toAdd;
				} else if (toAdd instanceof GrepExpressionItemTreeNode) {
					if (lastGroup != null && lastGroup.isNodeChild(toAdd)) {
						continue;
					}
					GrepExpressionItem grepExpressionItem = deepClone(((GrepExpressionItemTreeNode) toAdd).getGrepExpressionItem());
					GrepExpressionItemTreeNode newChild = new GrepExpressionItemTreeNode(grepExpressionItem);
					parent.insert(newChild, index++);
					nodesToSelect.add(newChild);
				} else
					throw new RuntimeException(String.valueOf(toAdd));
			}
			TableUtils.reloadTree(this);
			TableUtils.expand(nodesToExpand, this);
			TableUtils.selectNodes(nodesToSelect, this);

			profileDetail.rebuildProfile();
		}

		@Override
		public boolean isCopyEnabled(@NotNull DataContext dataContext) {
			return !getSelection().isEmpty();
		}

		@Override
		public boolean isCopyVisible(@NotNull DataContext dataContext) {
			return true;
		}

		@Override
		public boolean isPastePossible(@NotNull DataContext dataContext) {
			return !getSelection().isEmpty();

		}

		@Override
		public boolean isPasteEnabled(@NotNull DataContext dataContext) {
			return true;
		}

		@Override
		public boolean isCutEnabled(@NotNull DataContext dataContext) {
			return !getSelection().isEmpty();
		}

		@Override
		public boolean isCutVisible(@NotNull DataContext dataContext) {
			return true;
		}

		@Nullable
		@Override
		public Object getData(String dataId) {
			if (PlatformDataKeys.COPY_PROVIDER.is(dataId) || PlatformDataKeys.CUT_PROVIDER.is(dataId) || PlatformDataKeys.PASTE_PROVIDER.is(dataId)) {
				return this;
			}
			return null;
		}

		public class ListTransferable implements Transferable {
			List data;

			public ListTransferable(List alist) {
				data = alist;
			}

			public List getData() {
				return data;
			}

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException {
				if (!isDataFlavorSupported(flavor)) {
					throw new UnsupportedFlavorException(flavor);
				}
				return data;
			}

			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{ARRAY_LIST};
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				if (ARRAY_LIST.equals(flavor)) {
					return true;
				}
				return false;
			}
		}
	}

}
