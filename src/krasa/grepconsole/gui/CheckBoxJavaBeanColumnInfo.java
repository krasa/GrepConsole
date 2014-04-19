package krasa.grepconsole.gui;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class CheckBoxJavaBeanColumnInfo<T, Boolean> extends JavaBeanColumnInfo {
	public CheckBoxJavaBeanColumnInfo(String name, String propertyName) {
		super(name, propertyName);
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(Object o) {
		final BooleanTableCellRenderer booleanTableCellRenderer = new BooleanTableCellRenderer();
		booleanTableCellRenderer.setToolTipText(getName());
		return booleanTableCellRenderer;
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(Object o) {
		return new BooleanTableCellEditor();
	}
}
