package krasa.grepconsole.utils;

import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
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

	public static ActionCallback selectAndFocusSubTab(RunnerLayoutUi runnerLayoutUi, ConsoleView originalConsole) {
		JComponent originalConsoleComponent = null;
		if (originalConsole instanceof JComponent) {
			originalConsoleComponent = (JComponent) originalConsole;
		} else {
			LOG.error("console not supported " + originalConsole);
		} 
		
		
		Content[] contents = runnerLayoutUi.getContents();
		for (Content content : contents) {
			JComponent component = content.getComponent();
			if (component == originalConsoleComponent) {
				return runnerLayoutUi.selectAndFocus(content, true, true);
			} else if (isChild(component, originalConsoleComponent, -1)) {
				return runnerLayoutUi.selectAndFocus(content, true, true);
			}
		}
		// for testng console
		for (Content content : contents) {
			JComponent component = content.getComponent();
			if (isChild(component, originalConsoleComponent, -2)) {
				return runnerLayoutUi.selectAndFocus(content, true, true);
			}
		}
		return null;
	}

	private static boolean isChild(JComponent component, JComponent originalConsole, int i) {
		return component.getComponentZOrder(originalConsole) != i;
	}

	public static void navigate(Project project, @Nullable ConsoleView consoleView) {
		Collection<RunContentDescriptor> descriptors = ExecutionHelper.findRunningConsole(project, dom -> OpenGrepConsoleAction.isSameConsole(dom, consoleView, true));
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

}
