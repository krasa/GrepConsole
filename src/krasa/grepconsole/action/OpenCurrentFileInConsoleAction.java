package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class OpenCurrentFileInConsoleAction extends OpenFileInConsoleAction {

	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if (editor != null) {
			final Project project = getEventProject(e);
			final PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
			final PsiFile psiFile = manager.getPsiFile(editor.getDocument());
			if (psiFile != null) {
				final VirtualFile virtualFile = psiFile.getVirtualFile();
				final String path = virtualFile.getPath();
				openFileInConsole(project, new File(path));
			}
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if (editor != null) {
			final Project project = getEventProject(e);
			final PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
			final PsiFile psiFile = manager.getPsiFile(editor.getDocument());
			e.getPresentation().setEnabled(psiFile != null);
		} else {
			e.getPresentation().setEnabled(false);
		}
	}
}
