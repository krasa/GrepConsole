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

	public GrepColor(boolean enabled, Color color) {
		this.enabled = enabled;
		if (color == null) {
			color = Color.BLACK;
			this.enabled = false;
		}
		this.color = color.getRGB();
	}

	public GrepColor(@NotNull ColorKey colorKey) {
		this.colorKey = colorKey.getExternalName();
		this.enabled = colorKey.getDefaultColor() != null;
	}

	public GrepColor() {
		this(false, Color.BLACK);
	}

	public GrepColor(Color color) {
		this(true, color);
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
			Color color = EditorColorsUtil.getGlobalOrDefaultColor(
					ColorKey.createColorKey(colorKey)
			);
			if (color == null) {
				return Color.BLACK;
			}
			return color;
		}

		if (color == null) {
			return null;
		}
		return new Color(color);
	}
}
