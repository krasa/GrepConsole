package krasa.grepconsole.integration;

import com.intellij.execution.filters.ConsoleDependentInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.filter.LockingInputFilterWrapper;
import krasa.grepconsole.filter.MainInputFilter;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyConsoleInputFilterProvider extends ConsoleDependentInputFilterProvider {

	@Override
	public @NotNull
	List<InputFilter> getDefaultFilters(@NotNull ConsoleView consoleView, @NotNull Project project, @NotNull GlobalSearchScope globalSearchScope) {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.registerConsole(consoleView);
		Profile profile = serviceManager.getProfile(consoleView);
		GrepFilter grepFilter = serviceManager.createGrepFilter(project, profile, consoleView);
		MainInputFilter inputFilter = serviceManager.createInputFilter(project, profile, grepFilter, consoleView);

		if (inputFilter != null) {
			return List.of(new LockingInputFilterWrapper(inputFilter));
		} else {
			return List.of(new LockingInputFilterWrapper(grepFilter));
		}
	}


}
