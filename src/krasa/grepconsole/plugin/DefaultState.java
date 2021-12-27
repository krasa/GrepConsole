package krasa.grepconsole.plugin;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import krasa.grepconsole.model.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DefaultState {
	public static List<Profile> createDefault() {
		List<Profile> profiles = new ArrayList<>();
		Profile profile = getDefaultProfile();
		profiles.add(profile);
		return profiles;
	}

	@NotNull
	public static Profile getDefaultProfile() {
		Profile profile = new Profile();
		profile.setDefaultProfile(true);
		profile.setName("default");
		resetToDefault(profile);

		List<GrepExpressionGroup> inputFilters = profile.getInputFilterGroups();
		inputFilters.clear();
		inputFilters.add(new GrepExpressionGroup("default", createDefaultInputFilter()));
		return profile;
	}

	private static List<GrepExpressionItem> createDefaultInputFilter() {
		ArrayList<GrepExpressionItem> grepExpressionItems = new ArrayList<>();
		grepExpressionItems.add(newItem(false).grepExpression(".*unwanted line.*").action(GrepExpressionItem.ACTION_REMOVE));
		grepExpressionItems.add(newItem(false).grepExpression(".*unwanted line.*").action(GrepExpressionItem.ACTION_REMOVE_UNLESS_MATCHED));
		return grepExpressionItems;
	}

	public static void resetToDefault(Profile profile) {
		List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		grepExpressionGroups.clear();
		grepExpressionGroups.add(new GrepExpressionGroup("default"));
		boolean underDarcula = UIUtil.isUnderDarcula();
		if (underDarcula) {
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.DARK, createDefaultItems(true, true)));
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.LIGHT, createDefaultItems(false, true)));
		} else {
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.LIGHT, createDefaultItems(false, true)));
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.DARK, createDefaultItems(true, true)));
		}

	}

	private static final ColorKey FATAL_BACKGROUND = ColorKey.createColorKey("GrepConsole.fatal.background", new JBColor(
			JBColor.RED, new Color(0, 0, 0, 255)
	));
	private static final ColorKey ERROR_BACKGROUND = ColorKey.createColorKey("GrepConsole.error.background", new JBColor(
			JBColor.ORANGE, new Color(55, 0, 0, 200)
	));
	private static final ColorKey WARN_BACKGROUND = ColorKey.createColorKey("GrepConsole.warn.background", new JBColor(
			JBColor.YELLOW, new Color(26, 0, 55, 200)
	));
	private static final ColorKey DEBUG_FOREGROUND = ColorKey.createColorKey("GrepConsole.debug.foreground", new JBColor(
			JBColor.GRAY, JBColor.GRAY
	));
	private static final ColorKey TRACE_FOREGROUND = ColorKey.createColorKey("GrepConsole.trace.foreground", new JBColor(
			JBColor.LIGHT_GRAY, JBColor.BLACK
	));

	public static List<GrepExpressionItem> createDefaultItems(boolean dark, boolean enabled) {
		List<GrepExpressionItem> items = new ArrayList<>();
		items.add(newItem(enabled).style(
				getGrepDefaultStyle(FATAL_BACKGROUND, null).bold(true)).grepExpression(
				".*FATAL.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(ERROR_BACKGROUND, null)).grepExpression(
				".*ERROR.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(WARN_BACKGROUND, null)).grepExpression(
				".*WARN.*"));
		items.add(newItem(enabled).enabled(false).style(getGrepDefaultStyle(null, null)).grepExpression(
				".*INFO.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(null, DEBUG_FOREGROUND)).grepExpression(
				".*DEBUG.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(null, TRACE_FOREGROUND)).grepExpression(
				".*TRACE.*"));
		return items;
	}

	public static GrepExpressionItem newItem(boolean enabled) {
		GrepExpressionItem grepExpressionItem = new GrepExpressionItem();
		grepExpressionItem.setEnabled(enabled);
		return grepExpressionItem;
	}

	private static GrepStyle getGrepDefaultStyle(ColorKey color, ColorKey foreground) {
		GrepStyle grepStyle = new GrepStyle().backgroundColor(new GrepColor(true, color));
		if (foreground != null) {
			grepStyle = grepStyle.foregroundColor(new GrepColor(true, foreground));
		}
		return grepStyle;
	}

}
