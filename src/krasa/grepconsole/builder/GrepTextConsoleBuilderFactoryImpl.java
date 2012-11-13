package krasa.grepconsole.builder;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

public class GrepTextConsoleBuilderFactoryImpl extends TextConsoleBuilderFactoryImpl {
	@Override
	public TextConsoleBuilder createBuilder(@NotNull Project project) {
		return new GrepTextConsoleBuilderImpl(project);
	}

	@Override
	public TextConsoleBuilder createBuilder(@NotNull Project project, @NotNull GlobalSearchScope scope) {
		return new GrepTextConsoleBuilderImpl(project, scope);
	}
}
