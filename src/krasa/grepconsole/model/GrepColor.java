package krasa.grepconsole.model;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@SuppressWarnings("UseJBColor")
public class GrepColor extends DomainObject {

	private boolean enabled;
	private Integer color;
	private String colorKey;

	public GrepColor() {
	}

	public GrepColor(Color color) {
		this(true, color);
	}

	public GrepColor(boolean enabled, Color color) {
		this.enabled = enabled;
		if (color != null) {
			this.color = color.getRGB();
		}
	}

	public GrepColor(boolean enabled, String colorKey) {
		this.colorKey = colorKey;
		this.enabled = enabled;
	}

	public GrepColor(@NotNull ColorKey colorKey) {
		this.colorKey = colorKey.getExternalName();
		this.enabled = colorKey.getDefaultColor() != null;
	}

	public Integer getColor() {
		return color;
	}

	public void setColor(Integer color) {
		this.color = color;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getColorKey() {
		return colorKey;
	}

	public void setColorKey(String colorKey) {
		this.colorKey = colorKey;
	}

	public Color getColorAsAWT() {
		if (colorKey != null) {
			return EditorColorsUtil.getGlobalOrDefaultColor(ColorKey.createColorKey(colorKey));
		}

		if (color == null) {
			return null;
		}
		return new Color(color);
	}

	/*ColorChooser returns Color under Darcula - equals with JBColor does not work*/
	public boolean isSameAsColorKey(Color color) {
		if (colorKey != null) {
			Color colorAsAWT = EditorColorsUtil.getGlobalOrDefaultColor(ColorKey.createColorKey(colorKey));
			return color != null && colorAsAWT != null && color.getBlue() == colorAsAWT.getBlue() && color.getRed() == colorAsAWT.getRed() && color.getGreen() == colorAsAWT.getGreen();
		}
		return false;
	}
}
