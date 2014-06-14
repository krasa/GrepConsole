package krasa.grepconsole.integration;

import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;

public class GrepConsoleFilterProvider implements ConsoleFilterProvider {

	@NotNull
	@Override
	public Filter[] getDefaultFilters(@NotNull Project project) {
		return new Filter[] { ServiceManager.getInstance().createHighlightFilter(project) };
	}
}
