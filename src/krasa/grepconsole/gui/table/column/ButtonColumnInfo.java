package krasa.grepconsole.gui.table.column;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public abstract class ButtonColumnInfo<Item> extends JavaBeanColumnInfo<Item, Item> {

	private String label;

	public ButtonColumnInfo(String name) {
		super(name, null);
		label = name;
	}

	@Override
	public void setValue(Item item, Item item2) {
	}

	@Nullable
	@Override
	public Item valueOf(Item grepExpressionItem) {
		return grepExpressionItem;
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(Item o) {
		return new ButtonEditor<Item>(new JCheckBox()) {
			@Override
			protected String getLabel(Object value) {
				return label;
			}

			@Override
			protected void onButtonClicked(Item item) {
				ButtonColumnInfo.this.onButtonClicked(item);
			}
		};
	}

	@Override
	public boolean isCellEditable(Item grepExpressionItem) {
		return true;
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(Item aVoid) {
		return new ButtonRenderer() {
			@Override
			protected String getText(Object value) {
				return label;
			}
		};
	}

	abstract void onButtonClicked(Item item);
}
