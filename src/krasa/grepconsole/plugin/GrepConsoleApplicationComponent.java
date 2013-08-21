package krasa.grepconsole.plugin;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import krasa.grepconsole.action.HighlightManipulationAction;
import krasa.grepconsole.filter.AbstractGrepFilter;
import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.filter.GrepInputFilter;
import krasa.grepconsole.filter.support.GuiContext;
import krasa.grepconsole.grep.Cache;
import krasa.grepconsole.gui.SettingsDialog;
import krasa.grepconsole.model.Profile;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ExportableApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

@State(name = "GrepConsole", storages = { @Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml") })
public class GrepConsoleApplicationComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginState>, ExportableApplicationComponent {

	private SettingsDialog form;
	private PluginState settings;
	private Map<Project, GrepHighlightFilter> cacheHighlight = new HashMap<Project, GrepHighlightFilter>();
	private Map<Project, GrepInputFilter> cacheInput = new HashMap<Project, GrepInputFilter>();
	private List<WeakReference<AnsiInputFilter>> cacheAnsi = new ArrayList<WeakReference<AnsiInputFilter>>();
	public static AnsiInputFilter lastAnsi;

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
		for (AbstractGrepFilter listener : cacheHighlight.values()) {
			listener.setGuiContext(guiContext);
		}
	}

	public void resetCache() {
		for (AbstractGrepFilter listener : cacheHighlight.values()) {
			listener.onChange();
		}
		for (AbstractGrepFilter listener : cacheInput.values()) {
			listener.onChange();
		}

		Iterator<WeakReference<AnsiInputFilter>> iterator = cacheAnsi.iterator();
		while (iterator.hasNext()) {
			WeakReference<AnsiInputFilter> next = iterator.next();
			AnsiInputFilter ansiInputFilter = next.get();
			if (ansiInputFilter == null) {
				iterator.remove();
			} else {
				ansiInputFilter.onChange();
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
		GrepHighlightFilter grepHighlightFilter = cacheHighlight.get(project);
		if (grepHighlightFilter != null) {
			grepHighlightFilter.setProfile(profile);
		}
		GrepInputFilter grepInputFilter = cacheInput.get(project);
		if (grepInputFilter != null) {
			grepInputFilter.setProfile(profile);
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

	public GrepInputFilter getInputFilterService(Project project) {
		GrepInputFilter service = cacheInput.get(project);
		if (service == null) {
			service = new GrepInputFilter(project);
		}
		cacheInput.put(project, service);
		return service;
	}

	public AnsiInputFilter getAnsiFilterService(Project project) {
		AnsiInputFilter service = new AnsiInputFilter(project);
		cacheAnsi.add(new WeakReference<AnsiInputFilter>(service));
		lastAnsi = service;
		return service;
	}

	public GrepHighlightFilter getHighlightService(Project project) {
		GrepHighlightFilter service = cacheHighlight.get(project);
		if (service == null) {
			service = new GrepHighlightFilter(project);
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
