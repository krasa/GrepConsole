package krasa.grepconsole.action;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.JBColor;
import krasa.grepconsole.model.*;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class AddHighlightAction extends HighlightManipulationAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final ConsoleView consoleView = getConsoleView(e);
		if (consoleView != null) {
			try {
				String string = getString(e);
				if (string == null)
					return;
				GrepConsoleApplicationComponent instance = GrepConsoleApplicationComponent.getInstance();
				addExpressionItem(string, instance.getProfile(e.getProject()));
				ServiceManager.getInstance().resetSettings();
				resetHighlights(consoleView);

				OpenConsoleSettingsAction openConsoleSettingsAction = new OpenConsoleSettingsAction(consoleView);
				openConsoleSettingsAction.actionPerformed(e);
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		}
	}

	private void addExpressionItem(String string, final Profile profile1) {
		Profile profile = profile1;
		GrepStyle style = new GrepStyle();
		style.setForegroundColor(new GrepColor(Color.BLACK));
		style.setBackgroundColor(new GrepColor(JBColor.CYAN));
		profile.getGrepExpressionItems().add(new GrepExpressionItem().grepExpression(".*" + string + ".*").style(style));
	}

	private String getString(AnActionEvent e) throws UnsupportedFlavorException, IOException {
		DataContext dataContext = e.getDataContext();
		CopyProvider provider = PlatformDataKeys.COPY_PROVIDER.getData(dataContext);
		if (provider == null) {
			return null;
		}
		provider.performCopy(dataContext);
		Transferable contents = CopyPasteManager.getInstance().getContents();
		return contents == null ? null : (String) contents.getTransferData(DataFlavor.stringFlavor);
	}

	private ConsoleView getConsoleView(AnActionEvent e) {
		return e.getData(LangDataKeys.CONSOLE_VIEW);
	}

	@Override
	public void update(AnActionEvent e) {
		Presentation presentation = e.getPresentation();
		final boolean enabled = getConsoleView(e) != null;
		boolean selectedText = isSelectedText(e);
		presentation.setEnabled(selectedText && enabled);
		presentation.setVisible(selectedText && enabled);

	}

	private boolean isSelectedText(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		CopyProvider provider = PlatformDataKeys.COPY_PROVIDER.getData(dataContext);
		return provider != null && provider.isCopyEnabled(dataContext) && provider.isCopyVisible(dataContext);
	}

	@Override
	public void applySettings() {
	}
}
