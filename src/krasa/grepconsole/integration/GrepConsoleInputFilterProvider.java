package krasa.grepconsole.integration;

import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.ConsoleTextInputFilterProvider;
import com.intellij.execution.filters.TextInputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

public class GrepConsoleInputFilterProvider extends ConsoleTextInputFilterProvider {

	@NotNull
	@Override
	public TextInputFilter[] getFilters(@NotNull ConsoleView consoleView, @NotNull Project project,
			@NotNull GlobalSearchScope globalSearchScope) {
		ServiceManager manager = ServiceManager.getInstance();
		return new TextInputFilter[] { manager.createCopyingFilter(project), manager.createInputFilter(project) };

	}

}
