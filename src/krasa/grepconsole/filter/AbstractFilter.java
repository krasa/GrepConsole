package krasa.grepconsole.filter;

import krasa.grepconsole.filter.support.GuiContext;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import com.intellij.openapi.project.Project;

public abstract class AbstractFilter {

	protected Project project;
	protected Profile profile;
	protected GuiContext guiContext = GuiContext.DEFAULT;

	public AbstractFilter(Project project) {
		this.project = project;
		profile = GrepConsoleApplicationComponent.getInstance().getProfile(project);
	}

	public AbstractFilter(Profile profile) {
		this.profile = profile;
	}

	protected void refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		profile = applicationComponent.getState().getProfile(profile);
	}

	public void onChange() {
		refreshProfile();
	}

	public void setGuiContext(GuiContext guiContext) {
		this.guiContext = guiContext;
	}
}
