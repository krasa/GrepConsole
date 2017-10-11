package krasa.grepconsole.plugin;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@State(name = "GrepConsole", storages = { @Storage(id = "GrepConsole", file = "$APP_CONFIG$/GrepConsole.xml") })
public class GrepConsoleApplicationComponent
		implements ApplicationComponent,
		PersistentStateComponent<PluginState>, ExportableApplicationComponent {

	public static final NotificationGroup NOTIFICATION = new NotificationGroup("Grep Console",
			NotificationDisplayType.BALLOON, true);

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
		
		List<GrepExpressionItem> grepExpressionItems = profile.getAllGrepExpressionItems();
		for (GrepExpressionItem grepExpressionItem : grepExpressionItems) {
			boolean enableFoldings = profile.isEnableFoldings();
			boolean enabled = grepExpressionItem.isEnabled();
			boolean fold = grepExpressionItem.isFold();
			boolean enabledInputFilter = isEnabledInputFilter(profile, grepExpressionItem);
			if (enableFoldings && enabled && fold && !enabledInputFilter) {
				list.add(grepExpressionItem);
			}
		}
		foldingsCache = list;
	}

	private boolean isEnabledInputFilter(Profile profile, GrepExpressionItem grepExpressionItem) {
		return profile.isEnabledInputFiltering() && grepExpressionItem.isInputFilter();
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

	@Deprecated            //TODO
	public Profile getProfile() {
		return getState().getDefaultProfile();
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
