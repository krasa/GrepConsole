package krasa.grepconsole.action;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.service.GrepHighlightService;
import org.jetbrains.annotations.Nullable;

public class EditorHighlightAction extends HighlightManipulationAction {

	protected Editor editor;
	protected Project project;

	public void actionPerformed(AnActionEvent e) {
		editor = e.getData(PlatformDataKeys.EDITOR);
		project = e.getProject();
		if (editor != null && project != null) {
			GrepConsoleApplicationComponent instance = GrepConsoleApplicationComponent.getInstance();
			instance.setCurrentAction(this);
			boolean b = ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), instance);
			if (b) {
				applySettings();
			}
			instance.setCurrentAction(null);
		}
	}

	private void highlight(Editor editor, Project project) {
		EditorHyperlinkSupport myHyperlinks = new EditorHyperlinkSupport(editor, project);
		int lineCount = editor.getDocument().getLineCount();
		if (lineCount > 0) {
			myHyperlinks.highlightHyperlinks(getGrepFilter(project), getPredefinedMessageFilter(), 0, lineCount - 1);
		}
	}

	private GrepHighlightService getGrepFilter(Project project) {
		return GrepConsoleApplicationComponent.getInstance().getHighlightService(project);
	}

	private Filter getPredefinedMessageFilter() {
		return new Filter() {
			@Nullable
			@Override
			public Result applyFilter(String line, int entireLength) {
				return null;
			}
		};
	}

	@Override
	public void applySettings() {
		removeAllHighlighters(editor);
		highlight(editor, project);
	}
}
