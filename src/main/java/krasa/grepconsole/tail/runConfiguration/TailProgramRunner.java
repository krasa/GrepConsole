package krasa.grepconsole.tail.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import org.jetbrains.annotations.NotNull;

public class TailProgramRunner implements ProgramRunner<RunnerSettings> {

	public static final String ID = "GrepConsoleTail";

	@Override
	public @NotNull
	String getRunnerId() {
		return ID;
	}

	@Override
	public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile) {
		return s.equals("Run") && runProfile instanceof TailRunConfiguration;
	}

	@Override
	public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
		RunProfileState state = environment.getState();
		state.execute(environment.getExecutor(), this);
	}
}
