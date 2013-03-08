package krasa.grepconsole.service;

import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import com.intellij.openapi.project.Project;

public abstract class AbstractService {

	protected Project project;
	protected Profile profile;

	public AbstractService(Project project) {
		this.project = project;
		profile = GrepConsoleApplicationComponent.getInstance().getProfile(project);
	}

	public AbstractService(Profile profile) {
		this.profile = profile;
	}

	protected Profile refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		return applicationComponent.getState().getProfile(profile);
	}

}
