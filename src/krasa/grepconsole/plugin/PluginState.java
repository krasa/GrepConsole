package krasa.grepconsole.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.model.DomainObject;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PluginState extends DomainObject implements Cloneable {
	private static final Logger LOG = Logger.getInstance(PluginState.class);
	
	private List<Profile> profiles = new ArrayList<>();
	private TailSettings tailSettings;
	private boolean enabled;

	public Profile getDefaultProfile() {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.isDefaultProfile()) {
				result = profile;
			}
		}

		if (result == null) {
			if (profiles.isEmpty()) {
				profiles.add(DefaultState.getDefaultProfile());
			}
			Profile profile = profiles.get(0);
			profile.setDefaultProfile(true);
			result = profile;
		}
		return result;
	}

	@NotNull
	public Profile getProfile(long selectedProfileId) {
		Profile result = null;
		for (Profile profile : profiles) {
			if (profile.getId() == selectedProfileId) {
				result = profile;
			}
		}
		if (result == null) {
			result = getDefaultProfile();
		}
		return result;
	}
	public TailSettings getTailSettings() {
		if (tailSettings == null) {
			tailSettings = new TailSettings();
		}
		return tailSettings;
	}

	public void setTailSettings(TailSettings tailSettings) {
		this.tailSettings = tailSettings;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	@Override
	public PluginState clone() {
		return krasa.grepconsole.Cloner.deepClone(this);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	@Transient
	public boolean isSynchronousHighlighting() {
		return getDefaultProfile().isSynchronous();
	}

	public Profile createProfile() {
		Profile profile = DefaultState.getDefaultProfile();
		profile.setDefaultProfile(false);
		profile.setName(Utils.generateName(profiles, "new"));
		profiles.add(profile);
		return profile;
	}

	public void setDefault(Profile selectedProfile) {
		for (Profile profile : profiles) {
			profile.setDefaultProfile(false);
		}
		selectedProfile.setDefaultProfile(true);
	}

	public Profile copyProfile(Profile selectedProfile) {
		Profile profile = selectedProfile.clone();
		profile.setName(Utils.generateName(profiles, profile.getPresentableName()));
		profile.setDefaultProfile(false);
		profile.setId(System.currentTimeMillis());
		profiles.add(profile);
		return profile;
	}


	public Profile delete(Profile selectedProfile) {
		int index = profiles.indexOf(selectedProfile);
		profiles.remove(selectedProfile);

		if (selectedProfile.isDefaultProfile()) {
			if (profiles.isEmpty()) {
				profiles.add(DefaultState.getDefaultProfile());
			} else {
				profiles.get(0).setDefaultProfile(true);
			}
			return profiles.get(0);
		} else {
			if (profiles.size() > index) {
				return profiles.get(index);
			}
			if (profiles.size() > index - 1) {
				return profiles.get(index - 1);
			}
			return getDefaultProfile();
		}
	}

	@Override
	public String toString() {
		return "PluginState{" +
				"profiles=" + profiles +
				", tailSettings=" + tailSettings +
				", enabled=" + enabled +
				"} " + super.toString();
	}
	          

}
