package krasa.grepconsole.gui;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.ColumnInfo;
import krasa.grepconsole.gui.table.CheckboxTreeCellRendererBase;
import krasa.grepconsole.gui.table.CheckboxTreeTable;
import krasa.grepconsole.gui.table.TableRowTransferHandler;
import krasa.grepconsole.gui.table.column.*;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

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
		table = new CheckboxTreeTable(createRoot(), renderer, columns.toArray(new ColumnInfo[columns.size()]), foldC);
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
}
