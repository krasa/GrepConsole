package krasa.grepconsole.integration;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.GrepCopyingFilter;
import krasa.grepconsole.filter.GrepInputFilter;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

public class GrepConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		Profile defaultProfile = GrepConsoleApplicationComponent.getInstance().getState().getDefaultProfile();
		GrepInputFilter inputFilter = ServiceManager.getInstance().createInputFilter(project, defaultProfile);
		GrepCopyingFilter copyingFilter = ServiceManager.getInstance().createCopyingFilter(project, defaultProfile);

		if (inputFilter != null) {
			if (defaultProfile.isFilterOutBeforeGrep()) {
				return new InputFilter[]{inputFilter, copyingFilter};
			} else {
				return new InputFilter[]{copyingFilter, inputFilter};
			} 
		} else {
			return new InputFilter[]{copyingFilter};
		}
	}

}
