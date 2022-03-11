package krasa.grepconsole.grep;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import krasa.grepconsole.plugin.GrepProjectComponent;
import org.jetbrains.annotations.NotNull;

public class PinnedGrepsReopenerEnabler implements StartupActivity {
	@Override
	public void runActivity(@NotNull Project project) {
		GrepProjectComponent.getInstance(project).pinReopenerEnabled = true;
	}
}
