package krasa.grepconsole.plugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.model.DomainObject;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.GrepStyle;
import krasa.grepconsole.model.Profile;

import com.rits.cloning.Cloner;

public class PluginState extends DomainObject implements Cloneable {

	private List<Profile> profiles = new ArrayList<Profile>();
	private boolean enabled;

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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}
}
