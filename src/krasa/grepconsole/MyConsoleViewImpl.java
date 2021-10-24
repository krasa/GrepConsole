package krasa.grepconsole;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.grep.gui.GrepPanel;
import org.jetbrains.annotations.NotNull;

public class MyConsoleViewImpl extends ConsoleViewImpl {
	private final ConsoleView parentConsoleView;
	private GrepPanel grepPanel;

	public MyConsoleViewImpl(Project project, boolean viewer, ConsoleView parentConsoleView) {
		super(project, viewer);
		this.parentConsoleView = parentConsoleView;
	}

	public ConsoleView getParentConsoleView() {
		return parentConsoleView;
	}

	public void setGrepPanel(GrepPanel grepPanel) {
		this.grepPanel = grepPanel;
	}

	public GrepPanel getGrepPanel() {
		return grepPanel;
	}

	@Override
	public Object getData(@NotNull String dataId) {
		if (GrepPanel.GREP_PANEL.is(dataId)) {
			return grepPanel;
		}

		return super.getData(dataId);
	}
}
