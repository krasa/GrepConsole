package krasa.grepconsole.builder;

import krasa.grepconsole.console.GrepConsoleViewImpl;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

public class GrepTextConsoleBuilderImpl extends TextConsoleBuilderImpl {

	public GrepTextConsoleBuilderImpl(Project project) {
		this(project, GlobalSearchScope.allScope(project));
	}

	public GrepTextConsoleBuilderImpl(@NotNull Project project, @NotNull GlobalSearchScope scope) {
		super(project, scope);
	}

	@Override
	protected ConsoleView createConsole() {
		return new GrepConsoleViewImpl(this.getProject(), this.getScope(), this.isViewer(), null);
	}
}
