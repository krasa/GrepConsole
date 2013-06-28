package krasa.grepconsole.service;

import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.Mode;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

public abstract class AbstractService {

	protected Project project;
	protected Profile profile;
	protected Mode mode = Mode.DEFAULT;

	public AbstractService(Project project) {
		this.project = project;
		profile = GrepConsoleApplicationComponent.getInstance().getProfile(project);
	}

	public AbstractService(Profile profile) {
		this.profile = profile;
	}

	protected void refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		profile = applicationComponent.getState().getProfile(profile);
	}

	public void onChange() {
		refreshProfile();
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}
}
