package krasa.grepconsole.gui.table;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.ColumnInfo;
import krasa.grepconsole.gui.ProfileDetail;
import krasa.grepconsole.gui.table.column.*;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.plugin.ExtensionManager;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class TransformerTableBuilder extends GrepTableBuilder {

	public TransformerTableBuilder(final ProfileDetail profileDetail) {
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


		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(
				"Whole line", "wholeLine").tooltipText("Match a whole line, otherwise find a matching substrings - 'Unless expression' works only for whole lines.")));

		columns.add(new FolderColumnInfoWrapper(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Case insensitive",
				"caseInsensitive")));


		DropDownColumnInfo<GrepExpressionItem, String> action = new DropDownColumnInfo<GrepExpressionItem, String>("Action", "action") {

			@Nullable
			@Override
			public TableCellEditor getEditor(GrepExpressionItem o) {
				return new krasa.grepconsole.gui.table.column.ComboBoxTableCellEditor() {
					@Override
					public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
						//noinspection unchecked
						comboBox.setModel(new ListComboBoxModel(getOptions(o)));

						comboBox.setSelectedItem(value);

						return comboBox;
					}
				};
			}

			@Nullable
			@Override
			public TableCellRenderer getRenderer(GrepExpressionItem aVoid) {
				return krasa.grepconsole.gui.table.column.ComboBoxTableCellRenderer.COMBO_WHEN_SELECTED_RENDERER;
			}

			@NotNull
			private List<String> getOptions(GrepExpressionItem o) {
				ArrayList<String> options = new ArrayList<>();

				options.add(GrepExpressionItem.ACTION_REMOVE_UNLESS_MATCHED);
				options.add(GrepExpressionItem.ACTION_REMOVE);
				options.add(GrepExpressionItem.ACTION_NO_ACTION);
				options.addAll(ExtensionManager.references());
//				if (!options.contains(o.getAction())) {
//					options.add(o.getAction());
//				}
				return options;
			}

		};

		columns.add(new FolderColumnInfoWrapper(action));
		action.tooltipText("A line will not be filtered out if any previous expression matches first");
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Continue matching", "continueMatching").tooltipText("If true, match a line against the next configured items to apply multiple highlights")));

		columns.add(new FolderColumnInfoWrapper(new ClearColumn("Clear Console", profileDetail).tooltipText("Will not work if any previous non-filtering expression is matched first.")));

		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(STATUS_BAR_COUNT, "showCountInStatusBar").tooltipText("Show count of occurrences in Status Bar statistics panel\n(the number may not be right for test executions)")));
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(CONSOLE_COUNT, "showCountInConsole").tooltipText("Show count of occurrences in Console statistics panel\n(the number may not be right for test executions)")));
		columns.add(new FolderColumnInfoWrapper(new SoundColumn("Sound", profileDetail)));

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
		table = new MyCheckboxTreeTable(createRoot(), renderer, columns, null, profileDetail);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table, profileDetail));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(table.getTree());
		treeExpander.expandAll();
	}


}
