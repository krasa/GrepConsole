package krasa.grepconsole.gui.table.builder;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.ColumnInfo;
import krasa.grepconsole.gui.MainSettingsForm;
import krasa.grepconsole.gui.table.CheckboxTreeTable;
import krasa.grepconsole.gui.table.TableRowTransferHandler;
import krasa.grepconsole.gui.table.column.*;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Vojtech Krasa
 */
public class FoldingTableBuilder extends GrepTableBuilder {

	public FoldingTableBuilder(final MainSettingsForm mainSettingsForm) {
		AtomicReference<CheckboxTreeTable> treeTableAtomicReference = new AtomicReference<>();
		List<ColumnInfo> columns = new ArrayList<>();
		columns.add(new TreeColumnInfo("") {
			@Nullable
			@Override
			public String getPreferredStringValue() {
				return "________________";
			}

//			@Override
//			public int getWidth(JTable table) {
//				return 64;
//			}
		});
		columns.add(new GroupNameAdapter(new JavaBeanColumnInfo<GrepExpressionItem, String>("Expression",
				"grepExpression").preferedStringValue("___________________________")));

		JavaBeanColumnInfo<GrepExpressionItem, String> unless = new JavaBeanColumnInfo<>(
				"Unless expression", "unlessGrepExpression");
		columns.add(new FolderColumnInfoWrapper(unless.preferedStringValue("___________")));

		unless.addListener(new ValueChangedListener<GrepExpressionItem, String>() {
			@Override
			public void onValueChanged(GrepExpressionItem grepExpressionItem, String newValue) {
				if (!StringUtils.isEmpty(newValue)) {
					grepExpressionItem.setWholeLine(true);
				}
			}
		});

		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Whole line", "wholeLine").tooltipText("Match a whole line, otherwise find a matching substrings - 'Unless expression' works only for whole lines.")));

		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Case insensitive",
				"caseInsensitive")));

		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Fold", "fold", (grepExpressionItem, newValue) -> {
			if (newValue && !mainSettingsForm.currentProfile.isEnableFoldings()) {
				mainSettingsForm.currentProfile.setEnableFoldings(true);
				mainSettingsForm.setData(mainSettingsForm.currentProfile);
			}
		})));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Start", "startFolding", (grepExpressionItem, newValue) -> {
			if (newValue && !mainSettingsForm.currentProfile.isEnableFoldings()) {
				mainSettingsForm.currentProfile.setEnableFoldings(true);
				mainSettingsForm.setData(mainSettingsForm.currentProfile);
			}

			if (newValue) {
				grepExpressionItem.setFold(true);
				grepExpressionItem.setStopFolding(false);
				treeTableAtomicReference.get().repaint();
			}
		}).tooltipText("Start folding")));
		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Stop", "stopFolding", (grepExpressionItem, newValue) -> {


			if (newValue) {
				grepExpressionItem.setStartFolding(false);
				treeTableAtomicReference.get().repaint();
			}
		}).tooltipText("Stop folding")));


		JavaBeanColumnInfo<GrepExpressionItem, String> foldPlaceholderTextPrefix = new JavaBeanColumnInfo<>(
				"Placeholder", "foldPlaceholderTextPrefix");
		columns.add(new FolderColumnInfoWrapper(foldPlaceholderTextPrefix.preferedStringValue("______________")));


		CheckboxTree.CheckboxTreeCellRenderer renderer = new CheckboxTree.CheckboxTreeCellRenderer() {
			@Override
			public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
										  int row, boolean hasFocus) {
				if (value instanceof CheckedTreeNode) {
					CheckedTreeNode checkedTreeNode = (CheckedTreeNode) value;
					Object userObject = checkedTreeNode.getUserObject();
					if (userObject instanceof GrepExpressionGroup) {
						Icon icon = PlatformIcons.FOLDER_ICON;
						final ColoredTreeCellRenderer textRenderer = getTextRenderer();
						textRenderer.setIcon(icon);
					}
				}

			}
		};
		table = new MyCheckboxTreeTable(createRoot(), renderer, columns, null, mainSettingsForm);
		treeTableAtomicReference.set(table);

		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table, mainSettingsForm));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(table.getTree());
		treeExpander.expandAll();
	}


}
