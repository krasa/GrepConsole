package krasa.grepconsole.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import krasa.grepconsole.Cloner;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.tail.remotecall.RemoteCallService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@State(name = "GrepConsole", storages = {@Storage("GrepConsole.xml")})
public class GrepConsoleApplicationComponent
		implements ApplicationComponent,
		PersistentStateComponent<PluginState> {

	protected List<GrepExpressionItem> foldingsCache;
	private PluginState settings;
	protected int cachedMaxLengthToMatch = Integer.MAX_VALUE;
	private Integer maxProcessingTimeAsInt;

	public GrepConsoleApplicationComponent() {
	}

	public static GrepConsoleApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(GrepConsoleApplicationComponent.class);
	}

	public Integer getCachedMaxProcessingTimeAsInt() {
		return maxProcessingTimeAsInt;
	}

	public int getCachedMaxLengthToMatch() {
		return cachedMaxLengthToMatch;
	}

	public List<GrepExpressionItem> getCachedFoldingItems() {
		if (foldingsCache == null) {
			synchronized (this) {
				if (foldingsCache == null) {
					initFoldingCache();
				}
			}
		}
		return foldingsCache;
	}

	void initFoldingCache() {
		List<GrepExpressionItem> list = new ArrayList<>();
		Profile profile = getInstance().getState().getDefaultProfile();
		maxProcessingTimeAsInt = profile.getMaxProcessingTimeAsInt();
		if (profile.isEnableMaxLengthLimit()) {
			cachedMaxLengthToMatch = profile.getMaxLengthToMatchAsInt();
		} else {
			cachedMaxLengthToMatch = Integer.MAX_VALUE;
		}

		List<GrepExpressionItem> grepExpressionItems = profile.getAllFoldingExpressionItems();
		for (GrepExpressionItem grepExpressionItem : grepExpressionItems) {
			boolean enableFoldings = profile.isEnableFoldings();
			boolean enabled = grepExpressionItem.isEnabled();
			boolean fold = grepExpressionItem.isFold() || grepExpressionItem.isStartFolding() || grepExpressionItem.isStopFolding();
			if (enableFoldings && enabled && fold) {
				list.add(grepExpressionItem);
			}
		}
		foldingsCache = list;
	}

	@Override
	public void initComponent() {
		final TailSettings tailSettings = getState().getTailSettings();
		if (tailSettings.isEnabled()) {
			RemoteCallService.getInstance().rebind(tailSettings);
		}
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
		foldingsCache = null;

		migrate();
	}

	protected void migrate() {
		if (settings.getVersion() < 1) {
			List<Profile> profiles = settings.getProfiles();
			for (Profile profile : profiles) {
				for (GrepExpressionGroup grepExpressionGroup : profile.getGrepExpressionGroups()) {
					for (Iterator<GrepExpressionItem> iterator = grepExpressionGroup.getGrepExpressionItems().iterator(); iterator.hasNext(); ) {
						GrepExpressionItem grepExpressionItem = iterator.next();

						if (grepExpressionItem.isInputFilter()) {
							GrepExpressionItem newItem = Cloner.deepClone(grepExpressionItem);
							newItem.action(GrepExpressionItem.ACTION_REMOVE);

							String name = grepExpressionGroup.getName();
							if (StringUtils.isBlank(name)) {
								name = "default";
							}
							GrepExpressionGroup group = profile.getOrCreateInputFilterGroup(name);
							group.add(newItem);

							iterator.remove();
						}
					}
				}

			}
			settings.setVersion(1);
		}


		if (settings.getVersion() < 2) {
			Profile profile = settings.getDefaultProfile();
			for (GrepExpressionGroup grepExpressionGroup : profile.getGrepExpressionGroups()) {
				for (Iterator<GrepExpressionItem> iterator = grepExpressionGroup.getGrepExpressionItems().iterator(); iterator.hasNext(); ) {
					GrepExpressionItem grepExpressionItem = iterator.next();
					if (grepExpressionItem.isFold()) {
						GrepExpressionItem newItem = Cloner.deepClone(grepExpressionItem);

						String name = grepExpressionGroup.getName();
						if (StringUtils.isBlank(name)) {
							name = "default";
						}
						GrepExpressionGroup group = profile.getOrCreateFoldingGroup(name);
						group.add(newItem);

						grepExpressionItem.setEnabled(false);
					}
				}

			}
			settings.setVersion(2);
		}
	}

}
