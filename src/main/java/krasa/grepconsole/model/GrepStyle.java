package krasa.grepconsole.model;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.intellij.lang.annotations.JdkConstants;

import java.awt.*;

public class GrepStyle extends DomainObject {
	private String name;

	private GrepColor foregroundColor = new GrepColor();
	private GrepColor backgroundColor = new GrepColor();

	private GrepColor effectColor = new GrepColor();
	private EffectType effectType;

	@JdkConstants.FontStyle
	private boolean bold;
	private boolean italic;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EffectType getEffectType() {
		return effectType;
	}

	public void setEffectType(EffectType effectType) {
		this.effectType = effectType;
	}

	public boolean isBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	public void applyTo(TextAttributes attributes) {
		if (foregroundColor != null && foregroundColor.isEnabled()) {
			Color colorAsAWT = foregroundColor.getColorAsAWT();
			if (colorAsAWT != null) {
				attributes.setForegroundColor(colorAsAWT);
			}
		}
		if (backgroundColor != null && backgroundColor.isEnabled()) {
			Color colorAsAWT = backgroundColor.getColorAsAWT();
			if (colorAsAWT != null) {
				attributes.setBackgroundColor(colorAsAWT);
			}
		}
		if (effectType != null) {
			attributes.setEffectType(effectType);
		}
		if (effectColor != null && effectColor.isEnabled()) {
			Color colorAsAWT = effectColor.getColorAsAWT();
			if (colorAsAWT != null) {
				attributes.setEffectColor(colorAsAWT);
			}
		}
		int fontType = 0 + (bold ? 1 : 0) + (italic ? 2 : 0);
		attributes.setFontType(fontType);
	}

	public GrepStyle name(final String name) {
		this.name = name;
		return this;
	}

	public GrepStyle foregroundColor(final GrepColor foregroundColor) {
		this.foregroundColor = foregroundColor;
		return this;
	}

	public GrepStyle backgroundColor(final GrepColor backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public GrepStyle effectColor(final GrepColor effectColor) {
		this.effectColor = effectColor;
		return this;
	}

	public GrepStyle effectType(final EffectType effectType) {
		this.effectType = effectType;
		return this;
	}

	public GrepColor getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(GrepColor foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public GrepColor getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(GrepColor backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public GrepColor getEffectColor() {
		return effectColor;
	}

	public void setEffectColor(GrepColor effectColor) {
		this.effectColor = effectColor;
	}

	public GrepStyle bold(final boolean bold) {
		this.bold = bold;
		return this;
	}

	public GrepStyle italic(final boolean italic) {
		this.italic = italic;
		return this;
	}

	public boolean isResetable() {
		if (foregroundColor.isResetable()) {
			return true;
		}
		if (backgroundColor.isResetable()) {
			return true;
		}

		return false;
	}

	public void reset() {
		foregroundColor.reset();
		backgroundColor.reset();
	}

	public boolean hasColor() {
		return backgroundColor.getColorAsAWT() != null || foregroundColor.getColorAsAWT() != null;
	}
}
