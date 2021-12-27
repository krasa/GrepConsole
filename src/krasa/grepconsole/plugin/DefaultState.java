package krasa.grepconsole.plugin;

import com.intellij.ui.JBColor;
import krasa.grepconsole.model.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.editor.colors.ColorKey.createColorKey;

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
		grepExpressionGroups.add(new GrepExpressionGroup("default", createDefaultItems(true)));
		grepExpressionGroups.add(new GrepExpressionGroup("@Theme Name@"));
	}

	private static final GrepColor FATAL_BACKGROUND = new GrepColor(createColorKey("GrepConsole.fatal.background", new JBColor(
			JBColor.RED, new Color(0, 0, 0, 255)
	)));
	private static final GrepColor FATAL_FOREGROUND = new GrepColor(createColorKey("GrepConsole.fatal.foreground", null));

	private static final GrepColor ERROR_BACKGROUND = new GrepColor(createColorKey("GrepConsole.error.background", new JBColor(
			JBColor.ORANGE, new Color(55, 0, 0, 200)
	)));
	private static final GrepColor ERROR_FOREGROUND = new GrepColor(createColorKey("GrepConsole.error.foreground", null));

	private static final GrepColor WARN_BACKGROUND = new GrepColor(createColorKey("GrepConsole.warn.background", new JBColor(
			JBColor.YELLOW, new Color(26, 0, 55, 200)
	)));
	private static final GrepColor WARN_FOREGROUND = new GrepColor(createColorKey("GrepConsole.warn.foreground", null));


	private static final GrepColor INFO_BACKGROUND = new GrepColor(createColorKey("GrepConsole.info.background", null));
	private static final GrepColor INFO_FOREGROUND = new GrepColor(createColorKey("GrepConsole.info.foreground", null));

	private static final GrepColor DEBUG_FOREGROUND = new GrepColor(createColorKey("GrepConsole.debug.foreground", new JBColor(
			JBColor.GRAY, JBColor.GRAY
	)));
	private static final GrepColor DEBUG_BACKGROUND = new GrepColor(createColorKey("GrepConsole.debug.background", null));

	public static final GrepColor TRACE_FOREGROUND = new GrepColor(createColorKey("GrepConsole.trace.foreground", new JBColor(
			JBColor.LIGHT_GRAY, JBColor.BLACK
	)));
	private static final GrepColor TRACE_BACKGROUND = new GrepColor(createColorKey("GrepConsole.trace.background", null));

	public static List<GrepExpressionItem> createDefaultItems(boolean enabled) {
		List<GrepExpressionItem> items = new ArrayList<>();
		items.add(newItem(enabled).style(
				getGrepDefaultStyle(FATAL_BACKGROUND, FATAL_FOREGROUND).bold(true)).grepExpression(
				".*FATAL.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(ERROR_BACKGROUND, ERROR_FOREGROUND)).grepExpression(
				".*ERROR.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(WARN_BACKGROUND, WARN_FOREGROUND)).grepExpression(
				".*WARN.*"));
		items.add(newItem(enabled).enabled(false).style(getGrepDefaultStyle(INFO_BACKGROUND, INFO_FOREGROUND)).grepExpression(
				".*INFO.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(DEBUG_FOREGROUND, DEBUG_BACKGROUND)).grepExpression(
				".*DEBUG.*"));
		items.add(newItem(enabled).style(getGrepDefaultStyle(TRACE_FOREGROUND, TRACE_BACKGROUND)).grepExpression(
				".*TRACE.*"));
		return items;
	}

	public static GrepExpressionItem newItem(boolean enabled) {
		GrepExpressionItem grepExpressionItem = new GrepExpressionItem();
		grepExpressionItem.setEnabled(enabled);
		return grepExpressionItem;
	}

	private static GrepStyle getGrepDefaultStyle(GrepColor backgroundColor, GrepColor foregroundColor) {
		GrepStyle grepStyle = new GrepStyle();
		grepStyle.backgroundColor(backgroundColor);
		grepStyle.foregroundColor(foregroundColor);
		return grepStyle;
	}

}
