package krasa.grepconsole.plugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.intellij.execution.ui.ConsoleView;
import com.rits.cloning.Cloner;
import krasa.grepconsole.model.DomainObject;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class PluginSettings extends DomainObject implements Cloneable {
	private List<Profile> profiles;

	public PluginSettings() {
		if (profiles == null) {
			createDefault();
		}
	}

	private void createDefault() {
		profiles = new ArrayList<Profile>();
		Profile profile = new Profile();
		profile.setDefaultProfile(true);
		profiles.add(profile);
		List<GrepExpressionItem> grepExpressionItems = new ArrayList<GrepExpressionItem>();
		grepExpressionItems.add(newItem().style(getGrepStyle(Color.RED)).grepExpression(".*ERROR"));
		grepExpressionItems.add(newItem().style(getGrepStyle(Color.YELLOW)).grepExpression(
				".*WARN"));
		grepExpressionItems.add(newItem().style(getGrepStyle(Color.RED)).grepExpression(".*FATAL"));
		profile.setGrepExpressionItems(grepExpressionItems);
	}

	private GrepExpressionItem newItem() {
		return new GrepExpressionItem();
	}

	private GrepStyle getGrepStyle(Color color) {
		return new GrepStyle().backgroundColor(new GrepColor(true, color));
	}

	public Profile getProfile(ConsoleView consoleView) {
		//todo determine profile somehow
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

	@Override
	protected PluginSettings clone()  {
		Cloner cloner = new Cloner();
		cloner.nullInsteadOfClone();
		return cloner.deepClone(this);
	}
}
