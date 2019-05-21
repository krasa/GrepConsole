package krasa.grepconsole.gui.table;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBComboBoxLabel;
import com.intellij.ui.components.editors.JBComboBoxTableCellEditorComponent;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import krasa.grepconsole.gui.ProfileDetailForm;
import krasa.grepconsole.gui.table.column.*;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.plugin.ExtensionManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class TransformerTableBuilder extends GrepTableBuilder {

	public TransformerTableBuilder(final ProfileDetailForm profileDetailForm) {
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

				return new AbstractTableCellEditor() {
					private final JBComboBoxTableCellEditorComponent myProfilesChooser = new JBComboBoxTableCellEditorComponent();

					public Object getCellEditorValue() {
						return myProfilesChooser.getEditorValue();
					}

					public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
						myProfilesChooser.setCell(table, row, column);
						List<String> options = getOptions();
						myProfilesChooser.setOptions(options.toArray());
						myProfilesChooser.setOptions(options.toArray());

						int i = options.indexOf(value);
						if (i >= 0) {   //that stupid thing 
							myProfilesChooser.setDefaultValue(options.get(i));
						} else {
							myProfilesChooser.setDefaultValue(value);
						}
						return myProfilesChooser;
					}
				};
			}

			@Nullable
			@Override
			public TableCellRenderer getRenderer(final GrepExpressionItem scopeSetting) {
				return new DefaultTableCellRenderer() {
					boolean modifyForeground = false;
					private final JBComboBoxLabel myLabel = new JBComboBoxLabel() {

						@Override
						public void setForeground(Color color) {
							if (modifyForeground) {       //com.intellij.ui.dualView.TreeTableView hack
								super.setForeground(color);
							}
						}
					};

					public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
						if (value != null) {
							myLabel.setText((String) value);
							boolean contains = getOptions().contains(scopeSetting.getAction());
							if (!contains) {
								modifyForeground = true;
								myLabel.setForeground(contains ? UIUtil.getTableForeground() : JBColor.RED);
								modifyForeground = false;
							} else {
								modifyForeground = true;
							}

							if (isSelected) {
								myLabel.setSelectionIcon();
							} else {
								myLabel.setRegularIcon();
							}
						}
						return myLabel;

					}
				};
			}

			@NotNull
			private List<String> getOptions() {
				ArrayList<String> options = new ArrayList<>();
				options.addAll(GrepExpressionItem.getOptions());
				options.addAll(ExtensionManager.references());
				return options;
			}

		};

		columns.add(new FolderColumnInfoWrapper(action));
		action.preferedStringValue("___________________________________");
		columns.add(new FolderColumnInfoWrapper(
				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>("Continue matching", "continueMatching").tooltipText("If true, match a line against the next configured items to apply multiple actions")));

		columns.add(new FolderColumnInfoWrapper(new ClearColumn("Clear Console")));

//		columns.add(new FolderColumnInfoWrapper(
//				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(STATUS_BAR_COUNT, "showCountInStatusBar").tooltipText("Show count of occurrences in Status Bar statistics panel\n(the number may not be right for test executions)")));
//		columns.add(new FolderColumnInfoWrapper(
//				new CheckBoxJavaBeanColumnInfo<GrepExpressionItem>(CONSOLE_COUNT, "showCountInConsole").tooltipText("Show count of occurrences in Console statistics panel\n(the number may not be right for test executions)")));
		columns.add(new FolderColumnInfoWrapper(new SoundColumn("Sound", profileDetailForm)));

		CheckboxTree.CheckboxTreeCellRenderer renderer = new CheckboxTree.CheckboxTreeCellRenderer() {
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
		table = new MyCheckboxTreeTable(createRoot(), renderer, columns, null, profileDetailForm);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table, profileDetailForm));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(table.getTree());
		treeExpander.expandAll();
	}


}
