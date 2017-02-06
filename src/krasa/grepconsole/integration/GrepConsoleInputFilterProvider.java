package krasa.grepconsole.integration;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.GrepCopyingFilter;
import krasa.grepconsole.filter.GrepInputFilter;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

public class GrepConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		GrepInputFilter inputFilter = ServiceManager.getInstance().createInputFilter(project);
		GrepCopyingFilter copyingFilter = ServiceManager.getInstance().createCopyingFilter(project);
		if (inputFilter != null) {
			return new InputFilter[] { inputFilter, copyingFilter };
		} else {
			return new InputFilter[] { copyingFilter };
		}
	}

}
