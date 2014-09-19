package krasa.grepconsole.plugin;

import java.io.File;
import java.util.*;

import javax.swing.*;

import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.filter.support.SoundMode;
import krasa.grepconsole.gui.*;
import krasa.grepconsole.model.*;

import org.jetbrains.annotations.*;

import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.options.*;
import com.intellij.openapi.project.Project;

@State(name = "GrepConsole", storages = { @Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml") })
public class GrepConsoleApplicationComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginState>, ExportableApplicationComponent {

	protected List<GrepExpressionItem> foldingsCache;
	private SettingsDialog form;
	private PluginState settings;
	private HighlightManipulationAction currentAction;
	private ServiceManager serviceManager = ServiceManager.getInstance();

	public GrepConsoleApplicationComponent() {
	}

	public static GrepConsoleApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(GrepConsoleApplicationComponent.class);
	}

	public List<GrepExpressionItem> getFoldingItems() {
		if (foldingsCache == null) {
			synchronized (this) {
				if (foldingsCache == null) {
					initFoldingItems();
				}
			}
		}
		return foldingsCache;
	}

	private void initFoldingItems() {
		List<GrepExpressionItem> list = new ArrayList<GrepExpressionItem>();
		Profile profile = getInstance().getState().getDefaultProfile();
		List<GrepExpressionItem> grepExpressionItems = profile.getAllGrepExpressionItems();
		for (GrepExpressionItem grepExpressionItem : grepExpressionItems) {
			if (profile.isEnableFoldings() && grepExpressionItem.isFold()
					&& !(profile.isEnabledInputFiltering() && grepExpressionItem.isInputFilter())) {
				list.add(grepExpressionItem);
			}
		}
		foldingsCache = list;
	}

	@Override
	public void initComponent() {
	}

	@Override
	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@Override
	@NotNull
	public String getComponentName() {
		return "GrepConsole";
	}

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
			form = new SettingsDialog(getState().clone());
		}
		return form.getRootComponent();
	}

	public void prepareForm(SettingsContext settingsContext) {
		form = new SettingsDialog(getState().clone(), settingsContext);
	}

	@Override
	public boolean isModified() {
		return form.isSettingsModified(settings);
	}

	@Override
	public void apply() throws ConfigurationException {
		settings = form.getSettings().clone();
		serviceManager.resetSettings();
		initFoldingItems();
		Sound.soundMode = SoundMode.DISABLED;
		if (currentAction != null) {
			currentAction.applySettings();
		}
		Sound.soundMode = SoundMode.ENABLED;
	}

	@Override
	public void reset() {
		if (form != null) {
			form.importFrom(settings.clone());
		}
	}

	@Override
	public void disposeUIResources() {
		form = null;
	}

	@Override
	@NotNull
	public PluginState getState() {
		if (settings == null) {
			settings = new PluginState();
			settings.setProfiles(DefaultState.createDefault());
		}
		return settings;
	}

	@Override
	public void loadState(PluginState state) {
		this.settings = state;
	}

	public Profile getProfile(Project project) {
		return getState().getDefaultProfile();
	}

	public void setCurrentAction(HighlightManipulationAction currentEditor) {
		this.currentAction = currentEditor;
	}

	@NotNull
	@Override
	public File[] getExportFiles() {
		return new File[] { PathManager.getOptionsFile("grepConsole") };
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return "Grep Console";
	}
}
