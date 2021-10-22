package krasa.grepconsole.plugin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import krasa.grepconsole.gui.ProfilesForm;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.PluginState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * {@link GrepRunConfigurationExtensionNew}
 */
public class GrepSettingsEditor extends SettingsEditor<RunConfigurationBase> {
	private final RunConfigurationBase configuration;
	private ProfilesForm form;

	public GrepSettingsEditor(RunConfigurationBase configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void resetEditorFrom(RunConfigurationBase runConfigurationBase) {
		long selectedProfileId = GrepConsoleData.getGrepConsoleData(runConfigurationBase).getSelectedProfileId();
		selectedProfileId = setDefaultProfileId(PluginState.getInstance(), selectedProfileId);
		form.setOriginallySelectedProfileId(selectedProfileId);
	}

	@Override
	protected void applyEditorTo(RunConfigurationBase runConfigurationBase) throws ConfigurationException {
		Profile selectedProfile = form.getSelectedProfile();
		long id = selectedProfile != null ? selectedProfile.getId() : 0;
		GrepConsoleData.getGrepConsoleData(runConfigurationBase).setSelectedProfileId_ifAllowed(id);
	}

	@NotNull
	@Override
	protected JComponent createEditor() {
		PluginState state = PluginState.getInstance();
		long selectedProfileId = GrepConsoleData.getGrepConsoleData(configuration).getSelectedProfileId();
		selectedProfileId = setDefaultProfileId(state, selectedProfileId);
		form = new ProfilesForm(state.clone(), selectedProfileId, false);
		JPanel profiles = form.getRootComponent();
		profiles.setBorder(new TitledBorder("Grep Console"));
		return profiles;
	}

	protected long setDefaultProfileId(PluginState state, long selectedProfileId) {
		if (selectedProfileId == 0) {
			selectedProfileId = state.getDefaultProfile().getId();
		}
		return selectedProfileId;
	}
}
