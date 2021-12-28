package krasa.grepconsole.plugin;

import com.intellij.openapi.editor.colors.ColorKey;
import krasa.grepconsole.integration.ThemeColors;
import krasa.grepconsole.model.*;
import org.jetbrains.annotations.NotNull;

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
		grepExpressionItems.add(newItem(".*unwanted line.*", null).action(GrepExpressionItem.ACTION_REMOVE));
		grepExpressionItems.add(newItem(".*unwanted line.*", null).action(GrepExpressionItem.ACTION_REMOVE_UNLESS_MATCHED));
		return grepExpressionItems;
	}

	public static void resetToDefault(Profile profile) {
		List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		grepExpressionGroups.clear();
		grepExpressionGroups.add(new GrepExpressionGroup("default", createDefaultItems()));
		grepExpressionGroups.add(new GrepExpressionGroup("@Theme Name@"));
	}


	public static List<GrepExpressionItem> createDefaultItems() {
		List<GrepExpressionItem> items = new ArrayList<>();
		items.add(newItem(".*FATAL.*", style(ThemeColors.FATAL_BACKGROUND, ThemeColors.FATAL_FOREGROUND).bold(true)));
		items.add(newItem(".*ERROR.*", style(ThemeColors.ERROR_BACKGROUND, ThemeColors.ERROR_FOREGROUND)));
		items.add(newItem(".*WARN.*", style(ThemeColors.WARN_BACKGROUND, ThemeColors.WARN_FOREGROUND)));
		items.add(newItem(".*INFO.*", style(ThemeColors.INFO_BACKGROUND, ThemeColors.INFO_FOREGROUND)));
		items.add(newItem(".*DEBUG.*", style(ThemeColors.DEBUG_FOREGROUND, ThemeColors.DEBUG_BACKGROUND)));
		items.add(newItem(".*TRACE.*", style(ThemeColors.TRACE_FOREGROUND, ThemeColors.TRACE_BACKGROUND)));
		return items;
	}


	public static GrepExpressionItem newItem(String grepExpression, GrepStyle style) {
		GrepExpressionItem grepExpressionItem = new GrepExpressionItem();
		grepExpressionItem.setStyle(style);
		grepExpressionItem.setGrepExpression(grepExpression);
		grepExpressionItem.setEnabled(true);
		return grepExpressionItem;
	}

	private static GrepStyle style(ColorKey backgroundColor, ColorKey foregroundColor) {
		GrepStyle grepStyle = new GrepStyle();
		grepStyle.backgroundColor(new GrepColor(backgroundColor));
		grepStyle.foregroundColor(new GrepColor(foregroundColor));
		return grepStyle;
	}

}
