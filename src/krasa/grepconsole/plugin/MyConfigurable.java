package krasa.grepconsole.plugin;

import javax.swing.*;

import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.model.Sound;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

public class MyConfigurable implements Configurable {

	private SettingsDialog form;
	private ServiceManager serviceManager = ServiceManager.getInstance();
	public GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
	HighlightManipulationAction currentAction;

	@Nls
	@Override
	public String getDisplayName() {
		return "Grep Console";
	}

	@Nullable
	public Icon getIcon() {
		return null;
	}

	@Override
	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	@Override
	public JComponent createComponent() {
		if (form == null) {
			form = new SettingsDialog(applicationComponent.getState().clone());
		}
		return form.getRootComponent();
	}

	@Override
	public boolean isModified() {
		return form != null && form.isSettingsModified(applicationComponent.getState());
	}

	@Override
	public void apply() throws ConfigurationException {
		PluginState formSettings = form.getSettings();
		applicationComponent.loadState(formSettings.clone());
		serviceManager.resetSettings();
		applicationComponent.initFoldingCache();
		Sound.soundMode = SoundMode.DISABLED;
		if (currentAction != null) {
			currentAction.applySettings();
		}
		Sound.soundMode = SoundMode.ENABLED;
	}

	@Override
	public void reset() {
		if (form != null) {
			form.importFrom(applicationComponent.getState().clone());
		}
	}

	@Override
	public void disposeUIResources() {
		form = null;
	}

	public void prepareForm(SettingsContext settingsContext) {
		form = new SettingsDialog(applicationComponent.getState().clone(), settingsContext);
	}

	public void setCurrentAction(HighlightManipulationAction currentEditor) {
		this.currentAction = currentEditor;
	}

}
