package krasa.grepconsole.grep;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class MyCloseAction extends DumbAwareAction {
	private Content tab;
	private ContentManager contentManager;
	private RunnerLayoutUi runnerLayoutUi;

	public MyCloseAction(Content tab, ContentManager contentManager) {
		this.tab = tab;
		this.contentManager = contentManager;
		init();
	}

	public MyCloseAction(Content tab, RunnerLayoutUi runnerLayoutUi) {
		this.tab = tab;
		this.runnerLayoutUi = runnerLayoutUi;
		init();
	}

	public void init() {
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(AllIcons.Actions.Cancel);
		templatePresentation.setText(ExecutionBundle.messagePointer("close.tab.action.name"));
		templatePresentation.setDescription(Presentation.NULL_STRING);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		if (runnerLayoutUi != null) {
			boolean removedOk = runnerLayoutUi.removeContent(tab, true);
			if (removedOk) {
				runnerLayoutUi = null;
				tab = null;
			}
		}
		if (contentManager != null && tab != null) {
			final boolean removedOk = contentManager.removeContent(tab, true);
			if (removedOk) {
				contentManager = null;
				tab = null;
			}
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(runnerLayoutUi != null || contentManager != null);
	}
}
