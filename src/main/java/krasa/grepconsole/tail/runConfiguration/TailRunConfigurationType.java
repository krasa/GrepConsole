package krasa.grepconsole.tail.runConfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.tail.TailRunExecutor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TailRunConfigurationType implements ConfigurationType {
	public static final String ID = "TailRunConfiguration";

	@Override
	public String getDisplayName() {
		return "Tail";
	}

	@Override
	public String getConfigurationTypeDescription() {
		return "Tail by Grep Console plugin";
	}

	@Override
	public Icon getIcon() {
		return IconLoader.getIcon(TailRunExecutor.TAIL_SVG, this.getClass());
	}

	@NotNull
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ConfigurationFactory[] getConfigurationFactories() {
		return new ConfigurationFactory[]{new TailConfigurationFactory(this)};
	}
}