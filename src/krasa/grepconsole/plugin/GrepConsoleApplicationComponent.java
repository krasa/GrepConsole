package krasa.grepconsole.plugin;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ExportableApplicationComponent;
import krasa.grepconsole.Cache;
import krasa.grepconsole.Mode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.filter.Cache;
import krasa.grepconsole.filter.GuiContext;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.service.AbstractGrepService;
import krasa.grepconsole.service.AnsiFilterService;
import krasa.grepconsole.service.GrepHighlightService;
import krasa.grepconsole.service.GrepInputFilterService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@State(name = "GrepConsole", storages = {@Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml")})
public class GrepConsoleApplicationComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginState>, ExportableApplicationComponent {

	private SettingsDialog form;
	private PluginState settings;
	private Map<Project, GrepHighlightService> cacheHighlight = new HashMap<Project, GrepHighlightService>();
	private Map<Project, GrepInputFilterService> cacheInput = new HashMap<Project, GrepInputFilterService>();
	private List<WeakReference<AnsiFilterService>> cacheAnsi = new ArrayList<WeakReference<AnsiFilterService>>();
	public static AnsiFilterService lastAnsi;

	private HighlightManipulationAction currentAction;

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
		resetCache();
		setMode(GuiContext.NO_SOUND);
		if (currentAction != null) {
			currentAction.applySettings();
		}
		setMode(GuiContext.DEFAULT);
	}

	private void setMode(GuiContext guiContext) {
		for (AbstractGrepService listener : cacheHighlight.values()) {
			listener.setGuiContext(guiContext);
		}
	}

	public void resetCache() {
		for (AbstractGrepService listener : cacheHighlight.values()) {
			listener.onChange();
		}
		for (AbstractGrepService listener : cacheInput.values()) {
			listener.onChange();
		}

		Iterator<WeakReference<AnsiFilterService>> iterator = cacheAnsi.iterator();
		while (iterator.hasNext()) {
			WeakReference<AnsiFilterService> next = iterator.next();
			AnsiFilterService ansiFilterService = next.get();
			if (ansiFilterService == null) {
				iterator.remove();
			} else {
				ansiFilterService.onChange();
			}
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
			settings.setProfiles(DefaultState.createDefault());
		}
		return settings;
	}

	public void loadState(PluginState state) {
		this.settings = state;
	}

	public void changeProfile(Project project, Profile profile) {
		GrepHighlightService grepHighlightService = cacheHighlight.get(project);
		if (grepHighlightService != null) {
			grepHighlightService.setProfile(profile);
		}
		GrepInputFilterService grepInputFilterService = cacheInput.get(project);
		if (grepInputFilterService != null) {
			grepInputFilterService.setProfile(profile);
		}
	}

	public void projectClosed(Project project) {
		cacheHighlight.remove(project);
		cacheAnsi.remove(project);
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

	public AnsiFilterService getAnsiFilterService(Project project) {
		AnsiFilterService service = new AnsiFilterService(project);
		cacheAnsi.add(new WeakReference<AnsiFilterService>(service));
		lastAnsi = service;
		return service;
	}

	public GrepHighlightService getHighlightService(Project project) {
		GrepHighlightService service = cacheHighlight.get(project);
		if (service == null) {
			service = new GrepHighlightService(project);
		}
		cacheHighlight.put(project, service);
		return service;
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
