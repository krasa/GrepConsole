package krasa.grepconsole.gui.table.column;

import com.intellij.util.ui.table.ComboBoxTableCellEditor;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public abstract class DropDownColumnInfo<Item, Value> extends JavaBeanColumnInfo<Item, Value> {
	public DropDownColumnInfo(String name, String propertyName) {
		super(name, propertyName);
	}
//
//	@Override
//	public void setValue(Item item, Item value) {
//		System.err.println(value);
//	}

//	@Nullable
//	@Override
//	public Item valueOf(Item grepExpressionItem) {
//		return grepExpressionItem;
//	}

	@Nullable
	@Override
	public TableCellEditor getEditor(Item o) {
		return ComboBoxTableCellEditor.INSTANCE;
	}

	@Nullable
	@Override
	public abstract TableCellRenderer getRenderer(Item aVoid);

}
