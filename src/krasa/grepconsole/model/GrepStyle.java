package krasa.grepconsole.model;

import java.util.UUID;

import org.intellij.lang.annotations.JdkConstants;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;

public class GrepStyle extends DomainObject {
	private String id;
	private String name;

	private GrepColor foregroundColor;
	private GrepColor backgroundColor;

	private GrepColor effectColor;
	private EffectType effectType;

	@JdkConstants.FontStyle
	private boolean bold;
	private boolean italic;

	public GrepStyle() {
		this(null);
	}

	public GrepStyle(String id) {
		if (id == null) {
			this.id = UUID.randomUUID().toString();
		} else {
			this.id = id;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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
			attributes.setForegroundColor(foregroundColor.getColorAsAWT());
		}
		if (backgroundColor != null && backgroundColor.isEnabled()) {
			attributes.setBackgroundColor(backgroundColor.getColorAsAWT());
		}
		if (effectType != null) {
			attributes.setEffectType(effectType);
		}
		if (effectColor != null && effectColor.isEnabled()) {
			attributes.setEffectColor(effectColor.getColorAsAWT());
		}
		int fontType = 0 + (bold ? 1 : 0) + (italic ? 2 : 0);
		attributes.setFontType(fontType);
	}

	public GrepStyle id(final String id) {
		this.id = id;
		return this;
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

}
