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
		grepExpressionItems.add(newItem().enabled(false).grepExpression(".*unwanted line.*").action(GrepExpressionItem.ACTION_REMOVE));
		grepExpressionItems.add(newItem().enabled(false).grepExpression(".*unwanted line.*").action(GrepExpressionItem.ACTION_REMOVE_UNLESS_MATCHED));
		return grepExpressionItems;
	}

	public static void resetToDefault(Profile profile) {
		List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		grepExpressionGroups.clear();
		grepExpressionGroups.add(new GrepExpressionGroup("default"));
		boolean underDarcula = UIUtil.isUnderDarcula();
		if (underDarcula) {
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.DARK, createDefaultItems(true)));
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.LIGHT, createDefaultItems(false)));
		} else {
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.LIGHT, createDefaultItems(false)));
			grepExpressionGroups.add(new GrepExpressionGroup(krasa.grepconsole.model.Profile.DARK, createDefaultItems(true)));
		}

	}

	private static List<GrepExpressionItem> createDefaultItems(boolean dark) {
		List<GrepExpressionItem> items = new ArrayList<>();
		if (dark) {
			items.add(newItem().style(
					getGrepStyle(new Color(0, 0, 0, 255), null).bold(true)).grepExpression(
					".*FATAL.*"));
			items.add(newItem().style(getGrepStyle(new Color(55, 0, 0, 200), null)).grepExpression(
					".*ERROR.*"));
			Color warnColor;
//			warnColor = new Color(0, 55, 55, 200);
//			warnColor = new Color(0, 55, 0, 200);
//			warnColor = new Color(22, 22, 0, 230);
			warnColor = new Color(26, 0, 55, 200);
			items.add(newItem().style(getGrepStyle(warnColor, null)).grepExpression(
					".*WARN.*"));
			items.add(newItem().enabled(false).style(getGrepStyle(null, null)).grepExpression(
					".*INFO.*"));
			items.add(newItem().style(getGrepStyle(null, dark ? Color.GRAY : Color.GRAY)).grepExpression(
					".*DEBUG.*"));
			items.add(newItem().style(getGrepStyle(null, dark ? Color.BLACK : Color.LIGHT_GRAY)).grepExpression(
					".*TRACE.*"));
		} else {
			items.add(newItem().style(
					getGrepStyle(JBColor.RED, null).bold(true)).grepExpression(
					".*FATAL.*"));
			items.add(newItem().style(getGrepStyle(JBColor.ORANGE, null)).grepExpression(
					".*ERROR.*"));
			items.add(newItem().style(getGrepStyle(JBColor.YELLOW, null)).grepExpression(
					".*WARN.*"));
			items.add(newItem().enabled(false).style(getGrepStyle(null, null)).grepExpression(
					".*INFO.*"));
			items.add(newItem().style(getGrepStyle(null, dark ? Color.GRAY : Color.GRAY)).grepExpression(
					".*DEBUG.*"));
			items.add(newItem().style(getGrepStyle(null, dark ? Color.BLACK : Color.LIGHT_GRAY)).grepExpression(
					".*TRACE.*"));
		}
		return items;
	}

	public static GrepExpressionItem newItem() {
		return new GrepExpressionItem();
	}

	public static GrepStyle getGrepStyle(Color color, Color foreground) {
		GrepStyle grepStyle = new GrepStyle().backgroundColor(new GrepColor(true, color));
		if (foreground != null) {
			grepStyle = grepStyle.foregroundColor(new GrepColor(true, foreground));
		}
		return grepStyle;
	}

}
