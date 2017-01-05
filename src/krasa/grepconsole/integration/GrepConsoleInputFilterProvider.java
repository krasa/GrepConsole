package krasa.grepconsole.integration;

import com.intellij.execution.filters.ConsoleInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

public class GrepConsoleInputFilterProvider implements ConsoleInputFilterProvider {

	@NotNull
	@Override
	public InputFilter[] getDefaultFilters(@NotNull Project project) {
		ServiceManager manager = ServiceManager.getInstance();
		if (GrepConsoleApplicationComponent.getInstance().getState().isSynchronousHighlighting()) {
			return new InputFilter[]{manager.createInputFilter(project),
					manager.createAnsiFilter(project),
					manager.createHighlightInputFilter(project),
					manager.createCopyingFilter(project)};
		} else {
			return new InputFilter[]{manager.createInputFilter(project),
					manager.createAnsiFilter(project),
					manager.createCopyingFilter(project)};
		}
	}

}
