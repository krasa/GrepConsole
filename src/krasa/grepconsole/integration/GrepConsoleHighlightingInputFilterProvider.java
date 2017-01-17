package krasa.grepconsole.integration;

import krasa.grepconsole.filter.GrepHighlightingInputFilter;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleHighlightingInputFilterExProvider;
import com.intellij.execution.filters.HighlightingInputFilterEx;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

public class GrepConsoleHighlightingInputFilterProvider extends ConsoleHighlightingInputFilterExProvider {

	protected GrepHighlightingInputFilter createHighlightingFilter(@NotNull Project project, ServiceManager manager) {
		if (GrepConsoleApplicationComponent.getInstance().getState().isSynchronousHighlighting()) {
			return manager.createHighlightInputFilter(project);
		}
		return null;
	}

	@NotNull
	@Override
	public HighlightingInputFilterEx[] getHighlightingFilters(@NotNull ConsoleView consoleView,
			@NotNull Project project, @NotNull GlobalSearchScope globalSearchScope) {
		ServiceManager manager = ServiceManager.getInstance();

		GrepHighlightingInputFilter highlightingFilter = createHighlightingFilter(project, manager);
		if (highlightingFilter == null) {
			return new HighlightingInputFilterEx[0];
		}

		return new HighlightingInputFilterEx[] { highlightingFilter };
	}
}
