package krasa.grepconsole.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import krasa.grepconsole.gui.table.*;
import krasa.grepconsole.gui.table.column.*;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.ColumnInfo;

/**
 * @author Vojtech Krasa
 */
public class SettingsTableBuilder {
	private CheckboxTreeTable table;
	private SettingsDialog settingsDialog;

	public SettingsTableBuilder(SettingsDialog settingsDialog) {
		this.settingsDialog = settingsDialog;
		List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
		columns.add(new TreeColumnInfo(""));
		columns.add(new GroupNameAdapter(new JavaBeanColumnInfo<GrepExpressionItem, String>("Expression",
				"grepExpression").preferedStringValue("___________________________________")));
		columns.add(new FolderColumnInfoWrapper(new JavaBeanColumnInfo<GrepExpressionItem, String>("Unless expression",
				"unlessGrepExpression").preferedStringValue("______________")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>(
				"Filter out", "inputFilter")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>(
				"Case insensitive", "caseInsensitive")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Bold",
				"style.bold")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Italic",
				"style.italic")));
		columns.add(new FolderColumnInfoWrapper(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Background",
				"style.backgroundColor")));
		columns.add(new FolderColumnInfoWrapper(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Foreground",
				"style.foregroundColor")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Continue matching", "continueMatching").tooltipText("If not checked, the first match will end highlighting")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>(
				"Highlight only matching text", "highlightOnlyMatchingText")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("StatusBar count", "showCountInStatusBar").tooltipText("Show count of occurrences in Status Bar statistics panel\n(the number may not be right for test executions)")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Console count", "showCountInConsole").tooltipText("Show count of occurrences in Console statistics panel\n(the number may not be right for test executions)")));
		columns.add(new FolderColumnInfoWrapper(new SoundColumn("Sound", settingsDialog)));

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
		table = new CheckboxTreeTable(createRoot(), renderer, columns.toArray(new ColumnInfo[columns.size()]));
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(table.getTree());
		treeExpander.expandAll();
	}

	private CheckedTreeNode createRoot() {
		CheckedTreeNode root = new CheckedTreeNode(null);
		for (GrepExpressionGroup group : settingsDialog.getProfile().getGrepExpressionGroups()) {
			GroupTreeNode newChild = new GroupTreeNode(group);
			for (GrepExpressionItem grepExpressionItem : group.getGrepExpressionItems()) {
				newChild.add(new GrepExpressionItemTreeNode(grepExpressionItem));
			}
			root.add(newChild);
		}
		return root;
	}

	public CheckboxTreeTable getTable() {
		return table;
	}
}
