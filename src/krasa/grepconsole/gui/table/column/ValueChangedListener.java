package krasa.grepconsole.gui.table.column;

public interface ValueChangedListener<Item, T> {
	void onValueChanged(Item item, T newValue);
}
