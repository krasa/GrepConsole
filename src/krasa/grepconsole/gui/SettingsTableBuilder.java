package krasa.grepconsole.gui;

import java.util.*;

import javax.swing.*;

import krasa.grepconsole.gui.table.*;
import krasa.grepconsole.gui.table.CheckboxTreeTable;
import krasa.grepconsole.gui.table.column.*;
import krasa.grepconsole.model.*;

import org.jetbrains.annotations.Nullable;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.ColumnInfo;

/**
 * @author Vojtech Krasa
 */
public class SettingsTableBuilder {
	public static final String STATUS_BAR_COUNT = "StatusBar count";
	public static final String CONSOLE_COUNT = "Console count";
	private CheckboxTreeTable table;

	public SettingsTableBuilder(final SettingsDialog settingsDialog) {
		List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
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
		columns.add(new FolderColumnInfoWrapper(new JavaBeanColumnInfo<GrepExpressionItem, String>("Unless expression",
				"unlessGrepExpression").preferedStringValue("______________")));
		CheckBoxJavaBeanColumnInfo<GrepExpressionItem> inputFilter = new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Filter out", "inputFilter");
		inputFilter.addListener(new ValueChangedListener<Boolean>() {
			@Override
			public void onValueChanged(Boolean newValue) {
				if (newValue && !settingsDialog.getProfile().isEnabledInputFiltering()) {
					settingsDialog.getProfile().setEnabledInputFiltering(true);
					settingsDialog.setData(settingsDialog.getProfile());
				}
			}
		});
		columns.add(new FolderColumnInfoWrapper(inputFilter));
		CheckBoxJavaBeanColumnInfo<GrepExpressionItem> fold = new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Fold", "fold");
		fold.addListener(new ValueChangedListener<Boolean>() {
			@Override
			public void onValueChanged(Boolean newValue) {
				if (newValue && !settingsDialog.getProfile().isEnableFoldings()) {
					settingsDialog.getProfile().setEnableFoldings(true);
					settingsDialog.setData(settingsDialog.getProfile());
				}
			}
		});
		columns.add(new FolderColumnInfoWrapper(fold));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
						"Whole line", "wholeLine").tooltipText("Highlights a whole line if it matches, or only matching substrings. Only for highlighting.")));
		columns.add(new FolderColumnInfoWrapper(
						new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Continue matching", "continueMatching").tooltipText("If true, match a line against next configured items to apply multiple styles")));
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
		table.setTransferHandler(new TableRowTransferHandler(table, settingsDialog));
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
}
