package krasa.grepconsole.tail;

import javax.swing.*;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.execution.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

/**
 * @author Vojtech Krasa
 */
public class TailRunExecutor extends Executor {
	public static final Icon ToolWindowRun = IconLoader.getIcon("/krasa/grepconsole/tail/tail.png"); // 13x13

	public static final String TOOLWINDOWS_ID = "Tail";
	@NonNls
	public static final String EXECUTOR_ID = TOOLWINDOWS_ID;

	@Override
	@NotNull
	public String getStartActionText() {
		return ExecutionBundle.message("default.runner.start.action.text");
	}

	@Override
	public String getToolWindowId() {
		return TOOLWINDOWS_ID;
	}

	@Override
	public Icon getToolWindowIcon() {
		return ToolWindowRun;
	}

	@Override
	@NotNull
	public Icon getIcon() {
		return AllIcons.Actions.Execute;
	}

	@Override
	public Icon getDisabledIcon() {
		return AllIcons.Process.DisabledRun;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	@NotNull
	public String getActionName() {
		return "Tail";
	}

	@Override
	@NotNull
	public String getId() {
		return EXECUTOR_ID;
	}

	@Override
	public String getContextActionId() {
		return "GrepConsoleTailFile";
	}

	@Override
	public String getHelpId() {
		return null;
	}

	public static Executor getRunExecutorInstance() {
		return ExecutorRegistry.getInstance().getExecutorById(EXECUTOR_ID);
	}
}
