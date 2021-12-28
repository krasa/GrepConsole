package krasa.grepconsole.gui.table.column;

import com.intellij.util.ui.AbstractTableCellEditor;
import krasa.grepconsole.model.GrepColor;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

public class ColorChooserJavaBeanColumnInfo<Item> extends JavaBeanColumnInfo<Item, GrepColor> {

	public ColorChooserJavaBeanColumnInfo(String name, String propertyName) {
		super(name, propertyName);
	}

	@Nullable
	@Override
	public GrepColor valueOf(Item o) {
		return getProperty(o);
	}

	@Override
	public void setValue(Item o, GrepColor value) {
		setPropertyValue(o, value);
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(final Item o) {
		return new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
														   boolean hasFocus, int row, int column) {
				return getCheckBoxWithColorChooser((GrepColor) value, null);
			}
		};
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(final Item o) {
		AbstractTableCellEditor abstractTableCellEditor = new AbstractTableCellEditor() {

			protected CheckBoxWithColorChooser checkBoxWithColorChooser;

			@Override
			public boolean shouldSelectCell(EventObject anEvent) {
				return true;
			}

			@Override
			public Object getCellEditorValue() {
				GrepColor originalGrepColor = checkBoxWithColorChooser.getOriginalGrepColor();
				Color color = checkBoxWithColorChooser.getColor();
				return new GrepColor(checkBoxWithColorChooser.isSelected(), color, originalGrepColor);
			}

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
														 int column) {
				return checkBoxWithColorChooser = getCheckBoxWithColorChooser((GrepColor) value, this);
			}
		};
		return abstractTableCellEditor;
	}

	private CheckBoxWithColorChooser getCheckBoxWithColorChooser(GrepColor grepColor, final AbstractTableCellEditor abstractTableCellEditor) {
		if (grepColor == null) {
			grepColor = new GrepColor();
		}
		CheckBoxWithColorChooser checkBoxWithColorChooser = new CheckBoxWithColorChooser(null, grepColor) {
			@Override
			public void onColorChanged() {
				abstractTableCellEditor.stopCellEditing();
			}
		};
		// hack for updating color in the table after it has been selected in the dialog
		Component[] components = checkBoxWithColorChooser.getComponents();
		for (Component component : components) {
			if (component instanceof JCheckBox) {
				((JCheckBox) component).addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (abstractTableCellEditor != null) {
							abstractTableCellEditor.stopCellEditing();
						}
					}
				});
			}
		}

		return checkBoxWithColorChooser;
	}

}
