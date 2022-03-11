package krasa.grepconsole.tail.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.Consumer;
import krasa.grepconsole.action.TailFileInConsoleAction;
import krasa.grepconsole.tail.TailContentExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class TailRunProfileState implements RunProfileState {
	private final ExecutionEnvironment myEnvironment;

	TailRunProfileState(ExecutionEnvironment environment) {
		myEnvironment = environment;
	}

	@Override
	public @Nullable
	ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
		final RunProfile profile = myEnvironment.getRunProfile();
		if (profile instanceof TailRunConfiguration) {
			final TailRunConfiguration runConfig = (TailRunConfiguration) profile;
			boolean allowRunningInParallel = runConfig.isAllowRunningInParallel();

			TailRunConfigurationSettings mySettings = runConfig.mySettings;
			List<String> paths = mySettings.getPaths();
			for (String path : paths) {
				Consumer<File> fileConsumer = file -> open(file, allowRunningInParallel, mySettings, runConfig.getProject());
				TailUtils.openAllMatching(path, runConfig.mySettings.isSelectNewestMatchingFile(), fileConsumer);
			}
		}
		return null;
	}

	public void open(File file, boolean allowRunningInParallel, TailRunConfigurationSettings mySettings, @NotNull Project project) {
		if (file == null || !file.exists() || !file.isFile()) {
			return;
		}

		if (!allowRunningInParallel) {
			if (showExistingContent(file, project)) return;
		}

		TailFileInConsoleAction.openFileInConsole(project, file, TailFileInConsoleAction.resolveEncoding(file, mySettings.isAutodetectEncoding(), mySettings.getEncoding()));
	}

	public static boolean showExistingContent(File file, @NotNull Project project) {
		ToolWindow tail = ToolWindowManager.getInstance(project).getToolWindow("Tail");
		if (tail != null) {
			ContentManager contentManager = tail.getContentManager();
			for (Content content : contentManager.getContents()) {
				if (content.isValid()) {
					RunContentDescriptor contentDescriptor = content.getUserData(RunContentDescriptor.DESCRIPTOR_KEY);
					if (contentDescriptor != null) {
						ProcessHandler processHandler = contentDescriptor.getProcessHandler();
						if (processHandler != null && !processHandler.isProcessTerminated()) {
							if (FileUtil.pathsEqual(file.getAbsolutePath(), processHandler.getUserData(TailContentExecutor.FILE_PATH))) {
//										TailContentExecutor.PinAction pinAction = processHandler.getUserData(TailContentExecutor.PIN_ACTION);
//										if (pinAction != null && pinAction.isSelected(null)) {
//											continue;
//										}
//										contentManager.removeContent(content, true);
								tail.activate(null);
								contentManager.setSelectedContent(content, true);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

}
