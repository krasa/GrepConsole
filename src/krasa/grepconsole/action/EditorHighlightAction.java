package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.plugin.MyConfigurable;
import krasa.grepconsole.utils.Rehighlighter;

public class EditorHighlightAction extends HighlightManipulationAction {

	protected Editor editor;
	protected Project project;

	@Override
	public void actionPerformed(AnActionEvent e) {
		editor = e.getData(PlatformDataKeys.EDITOR);
		project = e.getProject();
		if (editor != null && project != null) {
			MyConfigurable instance = new MyConfigurable();
			instance.setCurrentAction(this);
			boolean b = ShowSettingsUtil.getInstance().editConfigurable(e.getProject(), instance);
			if (b) {
				applySettings();
			}
			instance.setCurrentAction(null);
		}
	}
	@Override
	public void applySettings() {
		Rehighlighter rehighlighter = new Rehighlighter();
		rehighlighter.removeAllHighlighters(editor);
		rehighlighter.highlight(editor, project);
	}
}
