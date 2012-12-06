package krasa.grepconsole.plugin;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import krasa.grepconsole.Cache;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.service.AbstractGrepService;
import krasa.grepconsole.service.GrepHighlightService;
import krasa.grepconsole.service.GrepInputFilterService;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

@State(name = "GrepConsole", storages = { @Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml") })
public class GrepConsoleApplicationComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginState> {

	private SettingsDialog form;
	private PluginState settings;
	private Map<Project, GrepHighlightService> cache = new HashMap<Project, GrepHighlightService>();
	private Map<Project, GrepInputFilterService> cacheInput = new HashMap<Project, GrepInputFilterService>();

	public GrepConsoleApplicationComponent() {
	}

	public void initComponent() {
	}

	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@NotNull
	public String getComponentName() {
		return "GrepConsole";
	}

	public static GrepConsoleApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(GrepConsoleApplicationComponent.class);
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

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	public JComponent createComponent() {
		// if (form == null) {
		form = new SettingsDialog(getState().clone());
		// }
		return form.getRootComponent();
	}

	public boolean isModified() {
		return form.isSettingsModified(settings);
	}

	public void apply() throws ConfigurationException {
		settings = form.getSettings().clone();
		for (AbstractGrepService listener : cache.values()) {
			listener.onChange();
		}
		for (AbstractGrepService listener : cacheInput.values()) {
			listener.onChange();
		}
		// todo this may not work properly, regenerate GrepExpressionItem id
		Cache.reset();
	}

	public void reset() {
		if (form != null) {
			form.importFrom(settings.clone());
		}
	}

	public void disposeUIResources() {
		form = null;
	}

	@NotNull
	public PluginState getState() {
		if (settings == null) {
			settings = new PluginState();
			settings.setProfiles(PluginState.createDefault());
		}
		return settings;
	}

	public void loadState(PluginState state) {
		this.settings = state;
	}

	public void changeProfile(Project project, Profile profile) {
		GrepHighlightService grepHighlightService = cache.get(project);
		if (grepHighlightService != null) {
			grepHighlightService.setProfile(profile);
		}
		GrepInputFilterService grepInputFilterService = cacheInput.get(project);
		if (grepInputFilterService != null) {
			grepInputFilterService.setProfile(profile);
		}
	}

	public void projectClosed(Project project) {
		cache.remove(project);
		cacheInput.remove(project);
	}

	public Profile getProfile(Project project) {
		return getState().getDefaultProfile();
	}

	public GrepInputFilterService getInputFilterService(Project project) {
		GrepInputFilterService service = cacheInput.get(project);
		if (service == null) {
			service = new GrepInputFilterService(project);
		}
		cacheInput.put(project, service);
		return service;
	}

	public GrepHighlightService getHighlightService(Project project) {
		GrepHighlightService service = cache.get(project);
		if (service == null) {
			service = new GrepHighlightService(project);
		}
		cache.put(project, service);
		return service;
	}
}
