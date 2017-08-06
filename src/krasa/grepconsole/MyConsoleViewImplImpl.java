package krasa.grepconsole;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.openapi.project.Project;

public class MyConsoleViewImplImpl extends ConsoleViewImpl {
	private final ConsoleViewImpl parentConsoleView;

	public MyConsoleViewImplImpl(Project project, boolean viewer, ConsoleViewImpl parentConsoleView) {
		super(project, viewer);
		this.parentConsoleView = parentConsoleView;
	}

	public ConsoleViewImpl getParentConsoleView() {
		return parentConsoleView;
	}
}
