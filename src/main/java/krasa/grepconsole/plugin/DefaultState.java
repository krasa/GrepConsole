package krasa.grepconsole.plugin;

import com.intellij.openapi.editor.colors.ColorKey;
import krasa.grepconsole.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static krasa.grepconsole.integration.ThemeColors.*;

public class DefaultState {
	private static final Logger log = LoggerFactory.getLogger(DefaultState.class);

	public static List<Profile> createDefault() {
		List<Profile> profiles = new ArrayList<>();
		profiles.add(getDefaultProfile());
		return profiles;
	}

	/**
	 * this must never fail, catch all logic
	 */
	@NotNull
	public static Profile getDefaultProfile() {
		Profile profile = new Profile();
		profile.setDefaultProfile(true);
		profile.setName("default");

		try {
			resetToDefault(profile);

			List<GrepExpressionGroup> inputFilters = profile.getInputFilterGroups();
			inputFilters.clear();
			inputFilters.add(new GrepExpressionGroup("default", createDefaultInputFilter()));
		} catch (Throwable e) {
			log.error("failed to create default profile", e);
		}
		return profile;
	}

	private static List<GrepExpressionItem> createDefaultInputFilter() {
		ArrayList<GrepExpressionItem> grepExpressionItems = new ArrayList<>();
		grepExpressionItems.add(new GrepExpressionItem().enabled(false).grepExpression(".*unwanted line.*").action(GrepExpressionItem.ACTION_REMOVE));
		grepExpressionItems.add(new GrepExpressionItem().enabled(false).grepExpression(".*unwanted line.*").action(GrepExpressionItem.ACTION_REMOVE_UNLESS_MATCHED));
		return grepExpressionItems;
	}

	public static void resetToDefault(@NotNull Profile profile) {
		List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		grepExpressionGroups.clear();
		grepExpressionGroups.add(new GrepExpressionGroup("default", createDefaultItems()));
		grepExpressionGroups.add(new GrepExpressionGroup("@Theme Name@"));
	}


	private static List<GrepExpressionItem> createDefaultItems() {
		List<GrepExpressionItem> items = new ArrayList<>();
		// Some langauges use 'CRITICAL' instead, e.g Python
		items.add(newItem(".*\\b(FATAL|CRITICAL)\\b.*", style(FATAL_BACKGROUND, FATAL_FOREGROUND).bold(true)));
		items.add(newItem(".*\\bERROR\\b.*", style(ERROR_BACKGROUND, ERROR_FOREGROUND)));
		// Some languages spell out the full word, e.g. Python
		items.add(newItem(".*\\bWARN(ING)?\\b.*", style(WARN_BACKGROUND, WARN_FOREGROUND)));
		items.add(newItem(".*\\bINFO\\b.*", style(INFO_BACKGROUND, INFO_FOREGROUND)));
		items.add(newItem(".*\\bDEBUG\\b.*", style(DEBUG_BACKGROUND, DEBUG_FOREGROUND)));
		items.add(newItem(".*\\bTRACE\\b.*", style(TRACE_BACKGROUND, TRACE_FOREGROUND)));
		return items;
	}


	private static GrepExpressionItem newItem(String grepExpression, @NotNull GrepStyle style) {
		GrepExpressionItem grepExpressionItem = new GrepExpressionItem();
		grepExpressionItem.setGrepExpression(grepExpression);
		grepExpressionItem.setStyle(style);
		grepExpressionItem.setEnabled(style.hasColor());
		return grepExpressionItem;
	}

	private static GrepStyle style(@NotNull ColorKey backgroundColor, @NotNull ColorKey foregroundColor) {
		GrepStyle grepStyle = new GrepStyle();
		grepStyle.backgroundColor(new GrepColor(backgroundColor));
		grepStyle.foregroundColor(new GrepColor(foregroundColor));
		return grepStyle;
	}

}
