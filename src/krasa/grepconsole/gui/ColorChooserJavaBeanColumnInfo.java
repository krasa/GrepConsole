package krasa.grepconsole.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
            public Object getCellEditorValue() {
                return new GrepColor(checkBoxWithColorChooser.isSelected(), checkBoxWithColorChooser.getColor());
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                         int column) {
                return checkBoxWithColorChooser = getCheckBoxWithColorChooser((GrepColor) value, this);
            }
        };
        return abstractTableCellEditor;
    }

    private CheckBoxWithColorChooser getCheckBoxWithColorChooser(GrepColor color, final AbstractTableCellEditor abstractTableCellEditor) {
        if (color == null) {
            color = new GrepColor();
        }
        CheckBoxWithColorChooser checkBoxWithColorChooser = new CheckBoxWithColorChooser(null, color.isEnabled(), color.getColorAsAWT());
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
