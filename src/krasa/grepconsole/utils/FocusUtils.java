package krasa.grepconsole.utils;

import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.testframework.ui.TestResultsPanel;
import com.intellij.execution.ui.*;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import krasa.grepconsole.MyConsoleViewImpl;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Vojtech Krasa
 */
public class FocusUtils {
	private static final Logger LOG = Logger.getInstance(FocusUtils.class);

	public static void requestFocus(Project project) {
		JFrame frame = WindowManager.getInstance().getFrame(project);

		// the only reliable way I found to bring it to the top
		boolean aot = frame.isAlwaysOnTop();
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(aot);

		int frameState = frame.getExtendedState();
		if ((frameState & Frame.ICONIFIED) == Frame.ICONIFIED) {
			// restore the frame if it is minimized
			frame.setExtendedState(frameState ^ Frame.ICONIFIED);
		}
		frame.toFront();
		frame.requestFocus();
	}

	public static ActionCallback selectAndFocusSubTab(RunnerLayoutUi runnerLayoutUi, ConsoleViewImpl originalConsole) {
		Content[] contents = runnerLayoutUi.getContents();
		for (Content content : contents) {
			JComponent component = content.getComponent();
			if (component == originalConsole) {
				return runnerLayoutUi.selectAndFocus(content, true, true);
			} else if (isChild(component, originalConsole, -1)) {
				return runnerLayoutUi.selectAndFocus(content, true, true);
			}
		}
		// for testng console
		for (Content content : contents) {
			JComponent component = content.getComponent();
			if (isChild(component, originalConsole, -2)) {
				return runnerLayoutUi.selectAndFocus(content, true, true);
			}
		}
		return null;
	}

	private static boolean isChild(JComponent component, ConsoleViewImpl originalConsole, int i) {
		return component.getComponentZOrder(originalConsole) != i;
	}

	public static void navigate(Project project, @Nullable ConsoleView consoleView) {
		Collection<RunContentDescriptor> descriptors = ExecutionHelper.findRunningConsole(project, dom -> isSameConsole(dom, consoleView, true));
		if (descriptors.size() == 1) {
			RunContentDescriptor o = (RunContentDescriptor) descriptors.toArray()[0];
			RunnerLayoutUi runnerLayoutUi = o.getRunnerLayoutUi();

			selectAndFocusWindowTab(project, o);

			if (runnerLayoutUi != null) {
				ActionCallback actionCallback = selectAndFocusSubTab(runnerLayoutUi, (ConsoleViewImpl) consoleView);
			}
		}

	}

	public static void selectAndFocusWindowTab(Project project, RunContentDescriptor o) {
		final RunContentManager runContentManager = ExecutionManager.getInstance(project).getContentManager();
		final ToolWindow toolWindowByDescriptor = runContentManager.getToolWindowByDescriptor(o);
		if (toolWindowByDescriptor != null) {
			final ContentManager contentManager = toolWindowByDescriptor.getContentManager();
			toolWindowByDescriptor.activate(null);
			Content attachedContent = o.getAttachedContent();
			if (attachedContent != null) {
				contentManager.setSelectedContent(attachedContent, true);
			}
		}
	}

	public static boolean isSameConsole(RunContentDescriptor dom, ExecutionConsole consoleView, boolean orChild) {
		ExecutionConsole executionConsole = dom.getExecutionConsole();
		if (executionConsole instanceof BaseTestsOutputConsoleView) {
			executionConsole = ((BaseTestsOutputConsoleView) executionConsole).getConsole();
		}
		if (consoleView instanceof MyConsoleViewImpl && orChild) {
			ConsoleViewImpl parentConsoleView = ((MyConsoleViewImpl) consoleView).getParentConsoleView();
			return isSameConsole(dom, parentConsoleView, orChild);
		}
		return executionConsole == consoleView;
	}

	public static boolean isSameConsole(Content dom, ExecutionConsole consoleView) {
		JComponent actionsContextComponent = dom.getActionsContextComponent();
		if (actionsContextComponent == consoleView) {
			return true;
		} else if (actionsContextComponent instanceof SMTestRunnerResultsForm) {
			SMTestRunnerResultsForm resultsForm = (SMTestRunnerResultsForm) actionsContextComponent;
			try {
				Field myConsole = TestResultsPanel.class.getDeclaredField("myConsole");
				myConsole.setAccessible(true);
				ConsoleView data = DataManager.getInstance().getDataContext((Component) myConsole.get(resultsForm)).getData(LangDataKeys.CONSOLE_VIEW);
				return data == consoleView;
			} catch (Throwable e) {
				LOG.error(e);
			}
		}
		return false;
	}
}
