package krasa.grepconsole.integration;

import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;

public class GrepConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		return new InputFilter[] { applicationComponent.getGrepInputFilter(project) };
	}
}
