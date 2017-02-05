package krasa.grepconsole.gui.table.column;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.util.ui.ColumnInfo;

public class JavaBeanColumnInfo<Item, Value> extends ColumnInfo<Item, Value> {
	private String maxStringValue = null;
	private String tooltipText = null;
	private String preferedStringValue = null;
	private int additionalWidth = 0;
	private String propertyName;
	private int width = -1;
	private List<ValueChangedListener<Item, Value>> valueChangedListeners;

	public JavaBeanColumnInfo(String name, String propertyName) {
		super(name);
		this.propertyName = propertyName;
	}

	public JavaBeanColumnInfo tooltipText(final String tooltipText) {
		this.tooltipText = tooltipText;
		return this;
	}

	@Nullable
	@Override
	public String getTooltipText() {
		if (tooltipText == null) {
			return getName();
		}
		return tooltipText;
	}

	@Nullable
	@Override
	public String getMaxStringValue() {
		return maxStringValue;
	}

	@Nullable
	@Override
	public String getPreferredStringValue() {
		return preferedStringValue;
	}

	@Override
	public int getAdditionalWidth() {
		return additionalWidth;
	}

	@Override
	public int getWidth(JTable table) {
		return width;
	}

	@Nullable
	@Override
	public Value valueOf(Item item) {
		return getProperty(item);
	}

	@Override
	public void setValue(Item item, Value value) {
		setPropertyValue(item, value);
		if (valueChangedListeners != null) {
			for (ValueChangedListener<Item, Value> lister : valueChangedListeners) {
				lister.onValueChanged(item, value);
			}
		}
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

	public JavaBeanColumnInfo width(final int width) {
		this.width = width;
		return this;
	}

	public JavaBeanColumnInfo additionalWidth(final int additionalWidth) {
		this.additionalWidth = additionalWidth;
		return this;
	}

	public JavaBeanColumnInfo propertyName(final String propertyName) {
		this.propertyName = propertyName;
		return this;
	}

	public JavaBeanColumnInfo preferedStringValue(final String preferedStringValue) {
		this.preferedStringValue = preferedStringValue;
		return this;
	}

	public JavaBeanColumnInfo maxStringValue(final String maxStringValue) {
		this.maxStringValue = maxStringValue;
		return this;
	}

	@Override
	public String toString() {
		return "JavaBeanColumnInfo{" + "tooltipText='" + tooltipText + '\'' + "} " + super.toString();
	}

	public boolean addListener(ValueChangedListener<Item, Value> listener) {
		if (valueChangedListeners == null) {
			valueChangedListeners = new ArrayList<ValueChangedListener<Item, Value>>();
		}
		return valueChangedListeners.add(listener);
	}

	public boolean removeListener(ValueChangedListener<Item, Value> o) {
		if (valueChangedListeners != null) {
			return valueChangedListeners.remove(o);
		}
		return false;
	}
}
