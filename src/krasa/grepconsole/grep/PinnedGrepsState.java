package krasa.grepconsole.grep;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.*;

@State(name = "PinnedGrepsState", storages = {
		@Storage(file = StoragePathMacros.PROJECT_FILE),
		@Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/grepConsolePins.xml", scheme = StorageScheme.DIRECTORY_BASED)})
public class PinnedGrepsState implements PersistentStateComponent<PinnedGrepsState> {

	@com.intellij.util.xmlb.annotations.Transient
	private List<WeakReference<OpenGrepConsoleAction.PinAction>> actions = new ArrayList<>();

	public static PinnedGrepsState getInstance(Project project) {
		return ServiceManager.getService(project, PinnedGrepsState.class);
	}

	@Nullable
	@Override
	public PinnedGrepsState getState() {
		return this;
	}

	@Override
	public void loadState(PinnedGrepsState state) {
		XmlSerializerUtil.copyBean(state, this);
	}


	public void register(OpenGrepConsoleAction.PinAction pinAction) {
		actions.add(new WeakReference<>(pinAction));
	}

	public boolean isPinned(OpenGrepConsoleAction.PinAction pinAction) {
		String consoleUUID = pinAction.getConsoleUUID();
		RunConfigurationRef key = pinAction.getRunConfigurationRef();
		Pins pins = map.get(key);

		if (pins != null) {
			for (Pin pin : pins.pins) {
				if (pin.consoleUUID.equals(consoleUUID)) {
					return true;
				}
			}
		}
		return false;
	}

	public void pin(OpenGrepConsoleAction.PinAction pinAction) {
		update(pinAction.getRunConfigurationRef(), pinAction.getParentConsoleUUID(), pinAction.getConsoleUUID(), pinAction.getModel(), true);
	}

	public void update(RunConfigurationRef runContentDescriptor, String parentConsoleUUID, String consoleUUID, CopyListenerModel copyListenerModel, boolean add) {
		Pins pins = map.get(runContentDescriptor);

		if (pins == null) {
			if (add) {
				map.put(runContentDescriptor, new Pins(parentConsoleUUID, consoleUUID, copyListenerModel));
			}
		} else {
			boolean updated = false;
			for (Pin pin : pins.pins) {
				if (pin.consoleUUID.equals(consoleUUID)) {
					pin.copyListenerModel = copyListenerModel;
					updated = true;
				}
			}
			if (!updated && add) {
				pins.pins.add(new Pin(parentConsoleUUID, consoleUUID, copyListenerModel));
			}
		}
	}

	public void unpin(OpenGrepConsoleAction.PinAction pinAction) {
		String consoleUUID = pinAction.getConsoleUUID();
		Pins pins = map.get(pinAction.getRunConfigurationRef());

		if (pins != null) {
			for (Iterator<Pin> iterator = pins.pins.iterator(); iterator.hasNext(); ) {
				Pin pin = iterator.next();
				if (pin.consoleUUID.equals(consoleUUID)) {
					iterator.remove();
				} else if (pin.parentConsoleUUID != null && pin.parentConsoleUUID.equals(consoleUUID)) {
					iterator.remove();
				}
			}
		}
		refresh();
	}

	private void refresh() {
		for (Iterator<WeakReference<OpenGrepConsoleAction.PinAction>> iterator = actions.iterator(); iterator.hasNext(); ) {
			WeakReference<OpenGrepConsoleAction.PinAction> listener = iterator.next();
			OpenGrepConsoleAction.PinAction pinAction = listener.get();
			if (pinAction == null) {
				iterator.remove();
			} else {
				pinAction.refreshPinStatus(this);
			}
		}
	}


	private Map<RunConfigurationRef, Pins> map = new HashMap<>();

	public Pins getPins(RunConfigurationRef key) {
		return map.get(key);
	}

	public static class Pins {
		private List<Pin> pins = new ArrayList<>();

		public Pins(String parentConsoleUUID, String consoleUUID, CopyListenerModel copyListenerModel) {
			pins.add(new Pin(parentConsoleUUID, consoleUUID, copyListenerModel));
		}

		public Pins() {
		}

		public void setPins(List<Pin> pins) {
			this.pins = pins;
		}

		public List<Pin> getPins() {
			return pins;
		}
	}

	public static class Pin {
		@Nullable
		private String parentConsoleUUID;
		private String consoleUUID;
		private CopyListenerModel copyListenerModel;

		public Pin() {
		}

		public Pin(@Nullable String parentConsoleUUID, @NotNull String consoleUUID, @NotNull CopyListenerModel copyListenerModel) {
			this.parentConsoleUUID = parentConsoleUUID;
			this.consoleUUID = consoleUUID;
			this.copyListenerModel = copyListenerModel;
		}

		public void setCopyListenerModel(@NotNull CopyListenerModel copyListenerModel) {
			this.copyListenerModel = copyListenerModel;
		}

		public void setParentConsoleUUID(@Nullable String parentConsoleUUID) {
			this.parentConsoleUUID = parentConsoleUUID;
		}

		public void setConsoleUUID(@NotNull String consoleUUID) {
			this.consoleUUID = consoleUUID;
		}

		@Nullable
		public String getParentConsoleUUID() {
			return parentConsoleUUID;
		}

		public String getConsoleUUID() {
			return consoleUUID;
		}

		public CopyListenerModel getCopyListenerModel() {
			return copyListenerModel;
		}

		@Override
		public String toString() {
			return "Pin{" +
					"parentConsoleUUID='" + parentConsoleUUID + '\'' +
					", consoleUUID='" + consoleUUID + '\'' +
					", grepModel=" + copyListenerModel +
					'}';
		}
	}

	public static class RunConfigurationRef {
		private String name;
		@Nullable
		private String icon;

		public RunConfigurationRef() {
		}

		public String getName() {
			return name;
		}

		public void setName(@NotNull String name) {
			this.name = name;
		}

		@Nullable
		public String getIcon() {
			return icon;
		}

		public void setIcon(@Nullable String icon) {
			this.icon = icon;
		}

		public RunConfigurationRef(@NotNull String name, @Nullable Icon icon) {
			this.name = name;
			if (icon != null) {
				String iconPath = icon.toString();
				String iconName = StringUtils.substringAfterLast(iconPath, "/");
				if (!StringUtils.isEmpty(iconName)) {
					iconPath = iconName;
				}
				this.icon = iconPath;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			RunConfigurationRef that = (RunConfigurationRef) o;

			if (name != null ? !name.equals(that.name) : that.name != null) return false;
			return icon != null ? icon.equals(that.icon) : that.icon == null;
		}

		@Override
		public int hashCode() {
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + (icon != null ? icon.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "RunConfigurationRef{" +
					"name='" + name + '\'' +
					", icon='" + icon + '\'' +
					'}';
		}
	}

	public Map<RunConfigurationRef, Pins> getMap() {
		return map;
	}

	public void setMap(Map<RunConfigurationRef, Pins> map) {
		this.map = map;
	}
}
