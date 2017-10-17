package krasa.grepconsole.filter;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFilter implements DumbAware {
	protected Project project;
	@NotNull
	protected volatile Profile profile;

	public AbstractFilter(@NotNull Project project, @NotNull Profile profile) {
		this.project = project;
		this.profile = profile;
	}

	public AbstractFilter(@NotNull Profile profile) {
		this.profile = profile;
	}

	protected void refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		profile = applicationComponent.getState().getProfile(profile.getId());
	}

	public void setProfile(@NotNull Profile profile) {
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
