package krasa.grepconsole.action;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

import javax.swing.*;

import krasa.grepconsole.model.*;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColorPicker;

public class AddHighlightAction extends HighlightManipulationAction {
	public AddHighlightAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final ConsoleView consoleView = getConsoleView(e);
		if (consoleView != null) {
			try {
				String string = getString(e);
				if (string == null)
					return;
				GrepConsoleApplicationComponent instance = GrepConsoleApplicationComponent.getInstance();
				Color color = ColorPicker.showDialog(rootComponent(getEventProject(e)), "Background color", Color.CYAN,
						true, null, true);
				if (color == null) {
					return;
				}

				addExpressionItem(string, color, instance.getProfile(e.getProject()));
				ServiceManager.getInstance().resetSettings();
				resetHighlights(consoleView);

			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		}
	}

	private static JComponent rootComponent(Project project) {
		if (project != null) {
			IdeFrame frame = WindowManager.getInstance().getIdeFrame(project);
			if (frame != null)
				return frame.getComponent();
		}

		JFrame frame = WindowManager.getInstance().findVisibleFrame();
		return frame != null ? frame.getRootPane() : null;
	}

	private void addExpressionItem(String string, Color color, final Profile profile) {
		GrepStyle style = new GrepStyle();
		style.setForegroundColor(new GrepColor(Color.BLACK));
		style.setBackgroundColor(new GrepColor(color));
		profile.getGrepExpressionItems().add(
				0,
				new GrepExpressionItem().grepExpression(string).style(style).highlightOnlyMatchingText(true).operationOnMatch(
						Operation.CONTINUE_MATCHING));
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
