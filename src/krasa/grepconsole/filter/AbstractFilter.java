package krasa.grepconsole.filter;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

public abstract class AbstractFilter implements DumbAware {

	protected Project project;
	protected volatile Profile profile;

	public AbstractFilter(Project project, Profile profile) {
		this.project = project;
		this.profile = profile;
	}

	public AbstractFilter(Profile profile) {
		this.profile = profile;
	}

	protected void refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		profile = applicationComponent.getState().getProfile(profile);
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		onChange();
	}

	public Project getProject() {
		return project;
	}

	public void onChange() {
		refreshProfile();
	}

}
