package krasa.grepconsole.model;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsUtil;

import java.awt.*;

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

	public GrepColor(boolean enabled, Integer color) {
		this.enabled = enabled;
		this.color = color;
	}

	public GrepColor(boolean enabled, ColorKey colorKey) {
		this.enabled = enabled;
		if(colorKey != null) {
			this.colorKey = colorKey.getExternalName();
		} else {
			this.enabled = false;
			color = Color.black.getRGB();
		}
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
    if(colorKey != null) {
      return EditorColorsUtil.getGlobalOrDefaultColor(
          ColorKey.createColorKey(colorKey)
      );
    }

		if (color == null) {
			return null;
		}
		return new Color(color);
	}
}
