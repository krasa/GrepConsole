package krasa.grepconsole.gui;

import org.apache.commons.beanutils.PropertyUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.util.ui.ColumnInfo;

public class JavaBeanColumnInfo<Item, Aspect> extends ColumnInfo<Item, Aspect> {
	protected String propertyName;

	public JavaBeanColumnInfo(String name, String propertyName) {
		super(name);
		this.propertyName = propertyName;
	}

	@Nullable
	@Override
	public Aspect valueOf(Item item) {
		return getProperty(item);
	}

	@Override
	public void setValue(Item item, Aspect value) {
		setPropertyValue(item, value);
	}

	@Override
	public boolean isCellEditable(Item item) {
		return true;
	}

    protected void setPropertyValue(Item item, Aspect value) {
        try {
            PropertyUtils.setProperty(item, propertyName, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	protected Aspect getProperty(Item item) {
		try {
			return (Aspect) PropertyUtils.getProperty(item, propertyName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
