package krasa.grepconsole.grep.actions;

import com.intellij.execution.ExecutionBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import krasa.grepconsole.action.MyDumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class MyCloseAction extends MyDumbAwareAction {
	private Content tab;
	private ContentManager contentManager;

	public MyCloseAction(Content tab, ContentManager contentManager) {
		this.tab = tab;
		this.contentManager = contentManager;
		init();
	}


	public void init() {
		final Presentation templatePresentation = getTemplatePresentation();
		ActionUtil.copyFrom(this, "CloseContent"); // for shortcut in text
		templatePresentation.setIcon(AllIcons.Actions.Cancel);
		templatePresentation.setText(ExecutionBundle.messagePointer("close.tab.action.name", new Object[0]));
		templatePresentation.setDescription(Presentation.NULL_STRING);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		if (contentManager != null && tab != null) {
			ActionCallback actionCallback = contentManager.removeContent(tab, true, true, true);
			actionCallback.doWhenDone(() -> {
				if (actionCallback.isDone()) {
					contentManager = null;
					tab = null;
				}
			});

		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(contentManager != null);
	}
}
