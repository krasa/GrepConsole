package krasa.grepconsole.model;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@SuppressWarnings("UseJBColor")
public class GrepColor extends DomainObject {

	private boolean enabled;
	private Integer color;
	private String colorKey;

	public GrepColor() {
	}

	public GrepColor(Color color) {
		this.enabled = true;
		if (color != null) {
			this.color = color.getRGB();
		} else {
			this.enabled = false;
		}
	}

	public GrepColor(boolean enabled, Color color) {
		this.enabled = enabled;
		if (color != null) {
			this.color = color.getRGB();
		} else {
			this.enabled = false;
		}
	}

	public GrepColor(@NotNull ColorKey colorKey) {
		this.colorKey = colorKey.getExternalName();
		this.enabled = true;
	}

	public GrepColor(boolean selected, Color newColor, GrepColor originalGrepColor) {
		if (originalGrepColor.isSameAsColorKey(newColor)) {
			newColor = null;
		}
		if (newColor != null) {
			this.color = newColor.getRGB();
		}
		this.colorKey = originalGrepColor.getColorKey();
		this.enabled = selected;
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

	@Nullable
	public Color getColorAsAWT() {
		if (color != null) {
			return new Color(color);
		}
		if (colorKey != null) {
			return EditorColorsUtil.getGlobalOrDefaultColor(ColorKey.createColorKey(colorKey));
		}
		return null;
	}

	/*ColorChooser returns Color under Darcula - equals with JBColor does not work*/
	public boolean isSameAsColorKey(Color color) {
		if (colorKey != null && color != null) {
			Color colorAsAWT = EditorColorsUtil.getGlobalOrDefaultColor(ColorKey.createColorKey(colorKey));
			return colorAsAWT != null && color.getBlue() == colorAsAWT.getBlue() && color.getRed() == colorAsAWT.getRed() && color.getGreen() == colorAsAWT.getGreen();
		}
		return false;
	}

	public boolean isResetable() {
		return colorKey != null && color != null;
	}

	public void reset() {
		color = null;
	}
}
