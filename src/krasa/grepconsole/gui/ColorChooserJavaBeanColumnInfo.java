package krasa.grepconsole.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import krasa.grepconsole.model.GrepColor;
import org.jetbrains.annotations.Nullable;

import com.intellij.ui.CheckBoxWithColorChooser;
import com.intellij.util.ui.AbstractTableCellEditor;

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
				return getCheckBoxWithColorChooser((GrepColor) value);
			}


		};
	}

	@Nullable
	@Override
	public String getTooltipText() {
		return "Left click on cell, and right click on color to choose ";
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(final Item o) {
		return new AbstractTableCellEditor() {

			protected CheckBoxWithColorChooser checkBoxWithColorChooser;

			@Override
			public Object getCellEditorValue() {
				return new GrepColor(checkBoxWithColorChooser.isSelected(), checkBoxWithColorChooser.getColor());
			}

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
														 int column) {
				return checkBoxWithColorChooser = getCheckBoxWithColorChooser((GrepColor) value);
			}
		};
	}

	private CheckBoxWithColorChooser getCheckBoxWithColorChooser(GrepColor color) {
		if (color == null) {
			color=new GrepColor();
		}
		return new CheckBoxWithColorChooser(null, color.isEnabled(), color.getColorAsAWT()) {
			@Override
			public void setColor(Color color) {
				super.setColor(color);
			}

			@Override
			public void setSelected(boolean selected) {
				super.setSelected(selected);
			}
		};
	}
}
