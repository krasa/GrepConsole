package krasa.grepconsole.plugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.model.DomainObject;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;

import com.intellij.execution.ui.ConsoleView;
import com.rits.cloning.Cloner;

public class PluginState extends DomainObject implements Cloneable {
	private List<Profile> profiles;


	public static List<Profile> createDefault() {
		List<Profile> profiles = new ArrayList<Profile>();
		Profile profile = new Profile();
		profile.setDefaultProfile(true);
		profiles.add(profile);
		profile.setGrepExpressionItems(createDefaultItems());

		return profiles;
	}

	public static List<GrepExpressionItem> createDefaultItems() {
		List<GrepExpressionItem> grepExpressionItems = new ArrayList<GrepExpressionItem>();
		grepExpressionItems.add(newItem().style(getGrepStyle(Color.RED, Color.WHITE)).grepExpression(".*FATAL"));
		grepExpressionItems.add(newItem().style(getGrepStyle(Color.ORANGE, null)).grepExpression(".*ERROR"));
		grepExpressionItems.add(newItem().style(getGrepStyle(Color.YELLOW, null)).grepExpression(".*WARN"));
		return grepExpressionItems;
	}

	private static GrepExpressionItem newItem() {
		return new GrepExpressionItem();
	}

	private static GrepStyle getGrepStyle(Color color, Color foreground) {
		GrepStyle grepStyle = new GrepStyle().backgroundColor(new GrepColor(true, color));
		if (foreground != null) {
			grepStyle = grepStyle.foregroundColor(new GrepColor(true, foreground));
		}
		return grepStyle;
	}

	public Profile getProfile(ConsoleView consoleView) {
		// todo determine profile somehow
		return getDefaultProfile();
	}

	public Profile getDefaultProfile() {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.isDefaultProfile()) {
				result = profile;
			}
		}

		if (result == null) {
			Profile profile = profiles.get(0);
			profile.setDefaultProfile(true);
			result = profile;
		}
		return result;
	}

	public Profile getProfile(Profile oldProfile) {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.getId() == oldProfile.getId()) {
				result = profile;
			}
		}
		if (result == null) {
			result = getDefaultProfile();
		}
		return result;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	@Override
	protected PluginState clone() {
		Cloner cloner = new Cloner();
		cloner.nullInsteadOfClone();
		return cloner.deepClone(this);
	}
}
