package krasa.grepconsole.tail;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithActions;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.GrepProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy of com.intellij.execution.RunContentExecutor Runs a process and prints the output in a content tab within the
 * Run toolwindow.
 *
 * @author yole
 */
public class TailContentExecutor implements Disposable {
	private final Project myProject;
	private final ProcessHandler myProcess;
	private final List<Filter> myFilterList = new ArrayList<>();
	private Runnable myRerunAction;
	private Runnable myStopAction;
	private Runnable myAfterCompletion;
	private Computable<Boolean> myStopEnabled;
	private String myTitle = "Output";
	private String myHelpId = null;
	private boolean myActivateToolWindow = true;
	private File file;

	public TailContentExecutor(@NotNull Project project, @NotNull ProcessHandler process) {
		myProject = project;
		myProcess = process;
	}

	public TailContentExecutor withFilter(Filter filter) {
		myFilterList.add(filter);
		return this;
	}

	public TailContentExecutor withTitle(String title) {
		myTitle = title;
		return this;
	}

	public TailContentExecutor withRerun(Runnable rerun) {
		myRerunAction = rerun;
		return this;
	}

	public TailContentExecutor withStop(@NotNull Runnable stop, @NotNull Computable<Boolean> stopEnabled) {
		myStopAction = stop;
		myStopEnabled = stopEnabled;
		return this;
	}

	public TailContentExecutor withAfterCompletion(Runnable afterCompletion) {
		myAfterCompletion = afterCompletion;
		return this;
	}

	public TailContentExecutor withHelpId(String helpId) {
		myHelpId = helpId;
		return this;
	}

	public TailContentExecutor withActivateToolWindow(boolean activateToolWindow) {
		myActivateToolWindow = activateToolWindow;
		return this;
	}

	private ConsoleView createConsole(@NotNull Project project, @NotNull ProcessHandler processHandler) {
		TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
		consoleBuilder.filters(myFilterList);
		ConsoleView console = consoleBuilder.getConsole();
		console.attachToProcess(processHandler);
		return console;
	}

	public void run() {
		FileDocumentManager.getInstance().saveAllDocuments();

		ConsoleView consoleView = createConsole(myProject, myProcess);

		if (myHelpId != null) {
			consoleView.setHelpId(myHelpId);
		}
		Executor executor = TailRunExecutor.getRunExecutorInstance();
		DefaultActionGroup actions = new DefaultActionGroup();

		// Create runner UI layout
		final RunnerLayoutUi.Factory factory = RunnerLayoutUi.Factory.getInstance(myProject);
		final RunnerLayoutUi layoutUi = factory.create("Tail", "Tail", "Tail", this);

		final JComponent consolePanel = createConsolePanel(consoleView, actions);
		RunContentDescriptor descriptor = new RunContentDescriptor(new RunProfile() {
			@Nullable
			@Override
			public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
				return null;
			}

			@Override
			public String getName() {
				return myTitle;
			}

			@Nullable
			@Override
			public Icon getIcon() {
				return AllIcons.Process.DisabledRun;
			}
		}, new DefaultExecutionResult(consoleView, myProcess), layoutUi);
		descriptor.setExecutionId(System.nanoTime());

		ComponentWithActions componentWithActions = new MyImpl(null, null, (JComponent) consoleView, null, consolePanel);
		final Content content = layoutUi.createContent(ExecutionConsole.CONSOLE_CONTENT_ID, componentWithActions, myTitle, AllIcons.Debugger.Console, consolePanel);
		layoutUi.addContent(content, 0, PlaceInGrid.right, false);
		layoutUi.getOptions().setLeftToolbar(createActionToolbar(consolePanel, consoleView, layoutUi, descriptor, executor), "RunnerToolbar");

		Disposer.register(descriptor, this);
		Disposer.register(descriptor, content);

		Disposer.register(content, consoleView);
		if (myStopAction != null) {
			Disposer.register(consoleView, new Disposable() {
				@Override
				public void dispose() {
					myStopAction.run();
				}
			});
		}

		for (AnAction action : consoleView.createConsoleActions()) {
			actions.add(action);
		}

		ExecutionManager.getInstance(myProject).getContentManager().showRunContent(executor, descriptor);

		if (myActivateToolWindow) {
			activateToolWindow();
		}

		if (myAfterCompletion != null) {
			myProcess.addProcessListener(new ProcessAdapter() {
				@Override
				public void processTerminated(ProcessEvent event) {
					SwingUtilities.invokeLater(myAfterCompletion);
				}
			});
		}

		myProcess.startNotify();
	}

	@NotNull
	private ActionGroup createActionToolbar(JComponent consolePanel, ConsoleView consoleView, @NotNull final RunnerLayoutUi myUi,
											RunContentDescriptor contentDescriptor, Executor runExecutorInstance) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.add(new RerunAction(consolePanel));
		actionGroup.add(new StopAction());
		actionGroup.add(new PinAction());
		actionGroup.add(myUi.getOptions().getLayoutActions());
		CloseAction closeAction = new CloseAction(runExecutorInstance, contentDescriptor, myProject) {
			@Override
			public void actionPerformed(AnActionEvent e) {
				super.actionPerformed(e);
				GrepProjectComponent.getInstance(myProject).unpin(file);
			}
		};
		GrepProjectComponent.getInstance(myProject).register(closeAction);
		actionGroup.add(closeAction);
		actionGroup.add(new CloseAll());
		return actionGroup;
	}

	public void activateToolWindow() {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				ToolWindowManager.getInstance(myProject).getToolWindow(TailRunExecutor.TOOLWINDOWS_ID).activate(null);
			}
		});
	}

	private static JComponent createConsolePanel(ConsoleView view, ActionGroup actions) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(view.getComponent(), BorderLayout.CENTER);
		panel.add(createToolbar(actions), BorderLayout.WEST);
		return panel;
	}

	private static JComponent createToolbar(ActionGroup actions) {
		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, false);
		return actionToolbar.getComponent();
	}

	@Override
	public void dispose() {
	}

	public void forFile(File file) {
		this.file = file;
	}

	private class RerunAction extends AnAction implements DumbAware {

		public RerunAction(JComponent consolePanel) {
			super("Rerun", "Rerun", AllIcons.Actions.Restart);
			registerCustomShortcutSet(CommonShortcuts.getRerun(), consolePanel);
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			try {
				myRerunAction.run();
			} catch (Exception e1) {
				Notification notification = GrepConsoleApplicationComponent.NOTIFICATION.createNotification(e1.getMessage(), MessageType.WARNING);
				Notifications.Bus.notify(notification, e.getProject());
			}
		}

		@Override
		public void update(AnActionEvent e) {
			e.getPresentation().setVisible(myRerunAction != null);
		}
	}

	private class StopAction extends AnAction implements DumbAware {
		public StopAction() {
			super("Stop", "Stop", AllIcons.Actions.Suspend);
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			myStopAction.run();
		}

		@Override
		public void update(AnActionEvent e) {
			e.getPresentation().setVisible(myStopAction != null);
			e.getPresentation().setEnabled(myStopEnabled != null && myStopEnabled.compute());
		}
	}

	private class CloseAll extends AnAction implements DumbAware {
		public CloseAll() {
			super("Close All", "Close All", AllIcons.Actions.Exit);
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			GrepProjectComponent.getInstance(myProject).closeAllTails(e);
		}

	}

	public class PinAction extends ToggleAction implements DumbAware {
		private boolean pinned;

		public PinAction() {
			super("Pin", "Reopen with project", AllIcons.General.Pin_tab);
			GrepProjectComponent projectComponent = GrepProjectComponent.getInstance(myProject);
			projectComponent.register(this);
			pinned = projectComponent.isPinned(file.getAbsoluteFile());
		}

		@Override
		public boolean isSelected(AnActionEvent anActionEvent) {
			return pinned;
		}

		public void refreshPinStatus(GrepProjectComponent projectComponent) {
			pinned = projectComponent.isPinned(file.getAbsoluteFile());
		}

		@Override
		public void setSelected(AnActionEvent anActionEvent, boolean b) {
			pinned = b;
			GrepProjectComponent projectComponent = GrepProjectComponent.getInstance(myProject);
			if (pinned) {
				projectComponent.pin(file);
			} else {
				projectComponent.unpin(file);
			}
		}

	}


	static class MyImpl implements ComponentWithActions {
		private final ActionGroup myToolbar;
		private final String myToolbarPlace;
		private final JComponent myToolbarContext;
		private final JComponent mySearchComponent;
		private final JComponent myComponent;

		public MyImpl(final ActionGroup toolbar, final String toolbarPlace, final JComponent toolbarContext,
					  final JComponent searchComponent,
					  final JComponent component) {
			myToolbar = toolbar;
			myToolbarPlace = toolbarPlace;
			myToolbarContext = toolbarContext;
			mySearchComponent = searchComponent;
			myComponent = component;
		}

		@Override
		public boolean isContentBuiltIn() {
			return false;
		}

		public MyImpl(final JComponent component) {
			this(null, null, null, null, component);
		}

		@Override
		public ActionGroup getToolbarActions() {
			return myToolbar;
		}

		@Override
		public JComponent getSearchComponent() {
			return mySearchComponent;
		}

		@Override
		public String getToolbarPlace() {
			return myToolbarPlace;
		}

		@Override
		public JComponent getToolbarContextComponent() {
			return myToolbarContext;
		}

		@Override
		@NotNull
		public JComponent getComponent() {
			return myComponent;
		}
	}

}
