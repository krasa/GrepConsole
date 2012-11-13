package krasa.grepconsole.console;

import krasa.grepconsole.service.GrepConsoleService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.impl.ConsoleState;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

public class GrepConsoleViewImpl extends ConsoleViewImpl implements GrepConsoleView {

	protected GrepConsoleService grepConsoleProxyService;

	public GrepConsoleViewImpl(Project project, boolean viewer) {
		super(project, viewer);
		createService(project);
	}

	public GrepConsoleViewImpl(@NotNull Project project, boolean viewer, @Nullable FileType fileType) {
		super(project, viewer, fileType);
		createService(project);
	}

	public GrepConsoleViewImpl(@NotNull Project project, @NotNull GlobalSearchScope searchScope, boolean viewer,
			@Nullable FileType fileType) {
		super(project, searchScope, viewer, fileType);
		createService(project);
	}

	protected GrepConsoleViewImpl(@NotNull Project project, @NotNull GlobalSearchScope searchScope, boolean viewer,
			@Nullable FileType fileType, @NotNull ConsoleState initialState) {
		super(project, searchScope, viewer, fileType, initialState);
		createService(project);
	}

	private void createService(Project project) {
		grepConsoleProxyService = new GrepConsoleService(project, this);
	}

	@Override
	public void print(String text, ConsoleViewContentType contentType) {
		grepConsoleProxyService.process(text, contentType, this);
	}

	@Override
	public void printProcessedResult(String text, ConsoleViewContentType contentType) {
		super.print(text, contentType);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (grepConsoleProxyService != null) {
			grepConsoleProxyService.dispose();
			grepConsoleProxyService = null;
		}
	}
}
