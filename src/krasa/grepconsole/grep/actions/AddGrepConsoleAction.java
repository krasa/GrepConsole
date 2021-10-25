package krasa.grepconsole.grep.actions;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.grep.gui.GrepPanel;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

//import krasa.grepconsole.grep.listener.GrepFilterAsyncListener;

public class AddGrepConsoleAction extends DumbAwareAction {

	private static final Logger LOG = Logger.getInstance(AddGrepConsoleAction.class);

	public AddGrepConsoleAction() {
	}

	public AddGrepConsoleAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		ConsoleView parentConsoleView = getConsoleView(e);
		if (parentConsoleView == null) {
			return;
		}
		List<MyConsoleViewImpl> childGreps = ServiceManager.getInstance().findChildGreps(parentConsoleView);
//		if (childGreps.isEmpty()) {
//			new OpenGrepConsoleAction().actionPerformed(e);
//			return;
//		}

		DefaultActionGroup actionGroup = new DefaultActionGroup();
		if (parentConsoleView instanceof MyConsoleViewImpl) {
			actionGroup.add(new MyAnAction(((MyConsoleViewImpl) parentConsoleView).getGrepPanel(), "[This Console]"));
			actionGroup.add(new Separator());
		}
		add(actionGroup, childGreps);
		actionGroup.add(new Separator());
		actionGroup.add(new OpenGrepConsoleAction("[New]", null, null));


		ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup("Select Console", actionGroup, e.getDataContext(), JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, true, (Runnable) null, -1);
//		InputEvent inputEvent = e.getInputEvent();
//		if (inputEvent != null) {
//			popup.showInCenterOf(inputEvent.getComponent());
//		} else {
		popup.showInBestPositionFor(e.getDataContext());
//		}
	}

	@Override
	public void update(AnActionEvent e) {
		Presentation presentation = e.getPresentation();
		boolean enabled = false;

		Project eventProject = getEventProject(e);
		ConsoleView parentConsoleView = OpenGrepConsoleAction.getTopParentConsoleView(e.getData(LangDataKeys.CONSOLE_VIEW));
		if (parentConsoleView != null) {
			GrepFilter grepFilter = ServiceManager.getInstance().getGrepFilter(parentConsoleView);
			if (eventProject != null && grepFilter != null) {
				RunContentDescriptor runContentDescriptor = OpenGrepConsoleAction.getRunContentDescriptor(eventProject, parentConsoleView);
				if (runContentDescriptor != null) {
					RunnerLayoutUi runnerLayoutUi = OpenGrepConsoleAction.getRunnerLayoutUi(eventProject, runContentDescriptor, parentConsoleView);
					enabled = runnerLayoutUi != null;
				}
			}
		}

		presentation.setEnabled(enabled || e.getData(PlatformDataKeys.TOOL_WINDOW) != null);
	}

	private void add(DefaultActionGroup actionGroup, List<MyConsoleViewImpl> list) {
		for (MyConsoleViewImpl consoleView : list) {
			GrepPanel grepPanel = consoleView.getGrepPanel();
			GrepCompositeModel model = grepPanel.getModel();
			String expression = model.getTitle();
			actionGroup.add(new MyAnAction(grepPanel, expression));
			add(actionGroup, ServiceManager.getInstance().findChildGreps(consoleView));
			actionGroup.add(new Separator());
		}
	}

	private ConsoleView getConsoleView(AnActionEvent e) {
		return e.getData(LangDataKeys.CONSOLE_VIEW);
	}

	private class MyAnAction extends AnAction {
		private GrepPanel grepPanel;

		public MyAnAction(GrepPanel grepPanel, String name) {
			super(name);
			this.grepPanel = grepPanel;
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			String expression = getExpression(e);
			grepPanel.addExpression(expression);
		}
	}

	@NotNull
	protected String getExpression(AnActionEvent e) {
		String s = Utils.getSelectedString(e);
		if (s == null)
			s = "";
		if (s.endsWith("\n")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
}
