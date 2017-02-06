package krasa.grepconsole.filter;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

public abstract class AbstractFilter implements DumbAware {

	protected Project project;
	protected volatile Profile profile;

	public AbstractFilter(Project project) {
		this.project = project;
		profile = GrepConsoleApplicationComponent.getInstance().getProfile();
	}

	public AbstractFilter(Profile profile) {
		this.profile = profile;
	}

	protected void refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		profile = applicationComponent.getState().getProfile(profile);
	}

	public Project getProject() {
		return project;
	}

	public void onChange() {
		refreshProfile();
	}

}
