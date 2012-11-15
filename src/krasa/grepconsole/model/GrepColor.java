package krasa.grepconsole.model;

import java.awt.*;


public class GrepColor extends DomainObject {

	private boolean enabled;
	private Integer color;

	public GrepColor(boolean enabled, Color color) {
		this.enabled = enabled;
		if (color == null) {
			color = Color.BLACK;
		}
		this.color = color.getRGB();
	}

	public GrepColor(boolean enabled, Integer color) {
		this.enabled = enabled;
		this.color = color;
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

	public Color getColorAsAWT() {
		if (color == null) {
			return null;
		}
		return new Color(color);
	}
}
