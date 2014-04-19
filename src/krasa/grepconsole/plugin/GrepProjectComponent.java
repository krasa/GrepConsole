package krasa.grepconsole.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GrepProjectComponent implements ProjectComponent {
	private Project project;

	public GrepProjectComponent(Project project) {
		this.project = project;
	}

	public void initComponent() {
		// TODO: insert component initialization logic here
	}

	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@NotNull
	public String getComponentName() {
		return "GrepProjectComponent";
	}

	public void projectOpened() {
		// called when project is opened
	}

	public void projectClosed() {
	}
}
