package krasa.grepconsole.tail;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Vojtech Krasa
 */
public class TailRunExecutor extends Executor {
	public static final String TAIL_SVG = "/krasa/grepconsole/icons/tail.svg";
	public static final String DISABLED_RUN_SVG = "/krasa/grepconsole/icons/disabledRun.svg";

	public static final String TOOLWINDOWS_ID = "Tail";
	@NonNls
	public static final String EXECUTOR_ID = "GrepConsoleTail";

	@Override
	@NotNull
	public String getStartActionText() {
		return "Tail";
	}

	@Override
	public String getToolWindowId() {
		return TOOLWINDOWS_ID;
	}

	@Override
	public Icon getToolWindowIcon() {
		return IconLoader.getIcon(TAIL_SVG, this.getClass());
	}

	@Override
	@NotNull
	public Icon getIcon() {
		return AllIcons.Actions.Execute;
	}

	@Override
	public Icon getDisabledIcon() {
		return IconLoader.getIcon(DISABLED_RUN_SVG, this.getClass());
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
