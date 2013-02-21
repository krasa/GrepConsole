package krasa.grepconsole.integration;

import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import krasa.grepconsole.service.AnsiFilterService;
import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;

public class GrepConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		System.err.println("GrepConsoleInputFilterProvider");
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		return new InputFilter[]{applicationComponent.getInputFilterService(project), new AnsiFilterService(project)};
	}
}
