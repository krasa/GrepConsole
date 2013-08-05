package krasa.grepconsole.integration;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class GrepConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		return new InputFilter[]{applicationComponent.getInputFilterService(project),
				applicationComponent.getAnsiFilterService(project)};
	}

}
