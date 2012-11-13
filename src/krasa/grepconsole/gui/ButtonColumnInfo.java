package krasa.grepconsole.gui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jetbrains.annotations.Nullable;

import com.intellij.util.ui.ColumnInfo;

public abstract class ButtonColumnInfo<Item> extends ColumnInfo<Item, Item> {

	private String label;

	public ButtonColumnInfo(String name) {
		super(null);
		label = name;
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
		return new ButtonRenderer(){
			@Override
			protected String getText(Object value) {
				return label;
			}
		};
	}

	abstract void onButtonClicked(Item item);
}
