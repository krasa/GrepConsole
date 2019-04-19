package krasa.grepconsole.plugin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.plugin.MyConfigurable;
import krasa.grepconsole.plugin.PluginState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GrepSettingsEditor extends SettingsEditor<RunConfigurationBase> {
	private final RunConfigurationBase configuration;
	private MyConfigurable myConfigurable;

	public GrepSettingsEditor(RunConfigurationBase configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void resetEditorFrom(RunConfigurationBase runConfigurationBase) {
	}

	@Override
	protected void applyEditorTo(RunConfigurationBase runConfigurationBase) throws ConfigurationException {
		myConfigurable.apply();
	}

	@NotNull
	@Override
	protected JComponent createEditor() {
		myConfigurable = new MyConfigurable(configuration) {
			@Override
			protected void refreshServices(@Nullable HighlightManipulationAction currentAction) {
			}

			protected JPanel getRootComponent() {
				return form.getProfiles();
			}
			@Override
			protected PluginState getClone(PluginState formSettings) {
				return formSettings;
			}
		};
		return myConfigurable.createComponent();
	}

}
