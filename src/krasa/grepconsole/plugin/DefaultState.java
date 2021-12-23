package krasa.grepconsole.plugin;

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

	public static List<GrepExpressionItem> createDefaultItems(boolean dark, boolean enabled) {
		List<GrepExpressionItem> items = new ArrayList<>();
		JBColor fatalBackground = JBColor.namedColor("GrepConsole.fatal.background",
				dark ? JBColor.BLACK : JBColor.RED
		);
		JBColor errorBackground = JBColor.namedColor("GrepConsole.error.background",
				dark ? new Color(55, 0, 0, 200) : JBColor.ORANGE
		);
		JBColor warnBackground = JBColor.namedColor("GrepConsole.warn.background",
				dark ? new Color(26, 0, 55, 200) : JBColor.YELLOW
		);
		JBColor infoForeground = JBColor.namedColor("GrepConsole.info.foreground",
				JBColor.GRAY
		);
		JBColor traceForeground = JBColor.namedColor("GrepConsole.trace.foreground",
				dark ? JBColor.BLACK : JBColor.LIGHT_GRAY
		);
		items.add(newItem(enabled).style(
				getGrepStyle(fatalBackground, null).bold(true)).grepExpression(
				".*FATAL.*"));
		items.add(newItem(enabled).style(getGrepStyle(errorBackground, null)).grepExpression(
				".*ERROR.*"));
		items.add(newItem(enabled).style(getGrepStyle(warnBackground, null)).grepExpression(
				".*WARN.*"));
		items.add(newItem(enabled).enabled(false).style(getGrepStyle(null, null)).grepExpression(
				".*INFO.*"));
		items.add(newItem(enabled).style(getGrepStyle(null, infoForeground)).grepExpression(
				".*DEBUG.*"));
		items.add(newItem(enabled).style(getGrepStyle(null, traceForeground)).grepExpression(
				".*TRACE.*"));
		return items;
	}

	public static GrepExpressionItem newItem(boolean enabled) {
		GrepExpressionItem grepExpressionItem = new GrepExpressionItem();
		grepExpressionItem.setEnabled(enabled);
		return grepExpressionItem;
	}

	public static GrepStyle getGrepStyle(Color color, Color foreground) {
		GrepStyle grepStyle = new GrepStyle().backgroundColor(new GrepColor(true, color));
		if (foreground != null) {
			grepStyle = grepStyle.foregroundColor(new GrepColor(true, foreground));
		}
		return grepStyle;
	}

}
