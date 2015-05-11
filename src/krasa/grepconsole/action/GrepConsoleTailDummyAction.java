package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.*;

public class GrepConsoleTailDummyAction extends DumbAwareAction {

	public GrepConsoleTailDummyAction() {
		getTemplatePresentation().setVisible(false);
	}

	@Override public void actionPerformed(AnActionEvent e) {
		
	}
	
	
	
}
