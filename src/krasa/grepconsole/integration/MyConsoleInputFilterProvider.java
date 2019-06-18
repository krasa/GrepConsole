package krasa.grepconsole.integration;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.filter.LockingInputFilterWrapper;
import krasa.grepconsole.filter.MainInputFilter;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

public class MyConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		Profile defaultProfile = GrepConsoleApplicationComponent.getInstance().getState().getDefaultProfile();
		GrepFilter grepFilter = ServiceManager.getInstance().createGrepFilter(project, defaultProfile);
		MainInputFilter inputFilter = ServiceManager.getInstance().createInputFilter(project, defaultProfile, grepFilter);

		if (inputFilter != null) {
			return new InputFilter[]{new LockingInputFilterWrapper(inputFilter)};
		} else {
			return new InputFilter[]{new LockingInputFilterWrapper(grepFilter)};
		}
	}

}
