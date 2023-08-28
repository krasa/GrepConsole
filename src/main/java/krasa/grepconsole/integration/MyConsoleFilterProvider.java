package krasa.grepconsole.integration;

import com.intellij.execution.filters.ConsoleDependentFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

public class MyConsoleFilterProvider extends ConsoleDependentFilterProvider {

	@NotNull
	@Override
	public Filter[] getDefaultFilters(@NotNull ConsoleView consoleView, @NotNull Project project,
									  @NotNull GlobalSearchScope globalSearchScope) {
		return new Filter[]{ServiceManager.getInstance().createOrGetHighlightFilter(project, consoleView)};
	}

}
