package krasa.grepconsole.gui;

import org.apache.commons.beanutils.PropertyUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.util.ui.ColumnInfo;

public class JavaBeanColumnInfo<Item, Value> extends ColumnInfo<Item, Value> {
	protected String propertyName;

	public JavaBeanColumnInfo(String name, String propertyName) {
		super(name);
		this.propertyName = propertyName;
	}

	@Nullable
	@Override
	public Value valueOf(Item item) {
		return getProperty(item);
	}

	@Override
	public void setValue(Item item, Value value) {
		setPropertyValue(item, value);
	}

	@Override
	public boolean isCellEditable(Item item) {
		return true;
	}

	protected void setPropertyValue(Item item, Value value) {
		try {
			PropertyUtils.setProperty(item, propertyName, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Value getProperty(Item item) {
		try {
			return (Value) PropertyUtils.getProperty(item, propertyName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
