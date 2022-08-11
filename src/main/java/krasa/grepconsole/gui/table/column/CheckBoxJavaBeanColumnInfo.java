package krasa.grepconsole.gui.table.column;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class CheckBoxJavaBeanColumnInfo<T> extends JavaBeanColumnInfo<T, Boolean> {
	public CheckBoxJavaBeanColumnInfo(String name, String propertyName) {
		super(name, propertyName);
	}

	public CheckBoxJavaBeanColumnInfo(String name, String propertyName, ValueChangedListener<T, Boolean> listener) {
		this(name, propertyName);
		addListener(listener);
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(Object o) {
		final BooleanTableCellRenderer booleanTableCellRenderer = new BooleanTableCellRenderer();
		booleanTableCellRenderer.setToolTipText(getTooltipText());
		return booleanTableCellRenderer;
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(Object o) {
		return new BooleanTableCellEditor();
	}
}
