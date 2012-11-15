package krasa.grepconsole.integration;

import krasa.grepconsole.GrepFilter;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;

public class GrepConsoleFilterProvider implements ConsoleFilterProvider {

	@NotNull
	@Override
	public Filter[] getDefaultFilters(@NotNull Project project) {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		GrepFilter grepFilter = applicationComponent.getGrepFilter(project);
		return new Filter[] { grepFilter };
	}
}
