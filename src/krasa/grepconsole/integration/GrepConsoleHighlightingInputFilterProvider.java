package krasa.grepconsole.integration;

import krasa.grepconsole.filter.GrepHighlightingInputFilter;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleHighlightingInputFilterProvider;
import com.intellij.execution.filters.HighlightingInputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

public class GrepConsoleHighlightingInputFilterProvider extends ConsoleHighlightingInputFilterProvider {

	protected GrepHighlightingInputFilter createHighlightingFilter(@NotNull Project project, ServiceManager manager) {
		if (GrepConsoleApplicationComponent.getInstance().getState().isSynchronousHighlighting()) {
			return manager.createHighlightInputFilter(project);
		}
		return null;
	}

	@NotNull
	@Override
	public HighlightingInputFilter[] getFilters(@NotNull ConsoleView consoleView,
			@NotNull Project project, @NotNull GlobalSearchScope globalSearchScope) {
		ServiceManager manager = ServiceManager.getInstance();

		GrepHighlightingInputFilter highlightingFilter = createHighlightingFilter(project, manager);
		if (highlightingFilter == null) {
			return new HighlightingInputFilter[0];
		}

		return new HighlightingInputFilter[] { highlightingFilter };
	}
}
