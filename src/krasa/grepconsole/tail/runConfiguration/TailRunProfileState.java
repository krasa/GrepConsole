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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import krasa.grepconsole.action.TailFileInConsoleAction;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.tail.TailContentExecutor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
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
				open(path, allowRunningInParallel, mySettings, runConfig.getProject());

			}
			return null;
		}
		return null;
	}

	public void open(String path, boolean allowRunningInParallel, TailRunConfigurationSettings mySettings, @NotNull Project project) {
		if (StringUtils.isBlank(path)) {
			return;
		}
		File file = new File(path);
//			if (!file.exists() || !file.isFile()) {
//				return null;
//			}

		if (!allowRunningInParallel) {
			ToolWindow tail = ToolWindowManager.getInstance(project).getToolWindow("Tail");
			if (tail != null) {
				ContentManager contentManager = tail.getContentManager();
				for (Content content : contentManager.getContents()) {
					if (content.isValid()) {
						RunContentDescriptor contentDescriptor = content.getUserData(RunContentDescriptor.DESCRIPTOR_KEY);
						if (contentDescriptor != null) {
							ProcessHandler processHandler = contentDescriptor.getProcessHandler();
							if (processHandler != null && !processHandler.isProcessTerminated()) {
								if (file.getAbsolutePath().equals(processHandler.getUserData(TailContentExecutor.FILE_PATH))) {
//										TailContentExecutor.PinAction pinAction = processHandler.getUserData(TailContentExecutor.PIN_ACTION);
//										if (pinAction != null && pinAction.isSelected(null)) {
//											continue;
//										}
//										contentManager.removeContent(content, true);
									tail.activate(null);
									contentManager.setSelectedContent(content, true);
									return;
								}
							}
						}
					}
				}
			}

		}

		new TailFileInConsoleAction().openFileInConsole(project, file, resolveEncoding(file, mySettings));
	}

	@NotNull
	public static Charset resolveEncoding(File file, TailRunConfigurationSettings mySettings) {
		String encoding = null;
		if (mySettings.isAutodetectEncoding()) {
			encoding = TailFileInConsoleAction.detectEncoding(file);
		}
		if (org.apache.commons.lang.StringUtils.isEmpty(encoding)) {
			encoding = mySettings.getEncoding();
		}
		if (org.apache.commons.lang.StringUtils.isEmpty(encoding)) {
			final TailSettings tailSettings = GrepConsoleApplicationComponent.getInstance().getState().getTailSettings();
			encoding = tailSettings.getDefaultEncoding();
		}
		return Charset.forName(encoding);
	}

}
