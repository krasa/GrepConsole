package krasa.grepconsole;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.grep.PinnedGrepsReopener;

public class MyConsoleViewImpl extends ConsoleViewImpl {
	private final ConsoleView parentConsoleView;

	public MyConsoleViewImpl(Project project, boolean viewer, ConsoleView parentConsoleView) {
		super(project, viewer);
		this.parentConsoleView = parentConsoleView;
		PinnedGrepsReopener.ignore(this);
	}

	public ConsoleView getParentConsoleView() {
		return parentConsoleView;
	}
}
