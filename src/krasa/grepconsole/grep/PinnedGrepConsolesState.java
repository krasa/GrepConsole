package krasa.grepconsole.grep;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.GrepProjectComponent;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.*;

public class PinnedGrepConsolesState {

	@Transient
	private int MAX_SIZE;
	@Transient
	private List<WeakReference<OpenGrepConsoleAction.PinAction>> actions = new ArrayList<>();

	private Map<RunConfigurationRef, Pins> map = new LinkedHashMap<>();

	public PinnedGrepConsolesState() {
		//noinspection UnresolvedPropertyKey
		MAX_SIZE = Registry.intValue("krasa.grepconsole.grep.PinnedGrepConsolesState.MAX_SIZE", 50);
	}

	public static PinnedGrepConsolesState getInstance(Project project) {
		return GrepProjectComponent.getInstance(project).getPinnedGreps();
	}

	public void register(OpenGrepConsoleAction.PinAction pinAction) {
		actions.add(new WeakReference<>(pinAction));
		if (GrepConsoleApplicationComponent.getInstance().getProfile().isAlwaysPinGrepConsoles()) {
			pin(pinAction);
		}
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

	public void update(@NotNull RunConfigurationRef runContentDescriptor, String parentConsoleUUID, @NotNull String consoleUUID, @NotNull GrepModel grepModel, boolean add) {
		Pins pins = map.get(runContentDescriptor);

		if (pins == null) {
			if (add) {
				map.put(runContentDescriptor, new Pins(parentConsoleUUID, consoleUUID, grepModel));
				clean();
			}
		} else {
			boolean updated = false;
			for (Pin pin : pins.pins) {
				if (pin.consoleUUID.equals(consoleUUID)) {
					pin.grepModel = grepModel;
					updated = true;
				}
			}
			if (!updated && add) {
				pins.pins.add(new Pin(parentConsoleUUID, consoleUUID, grepModel));
			}
		}
	}

	private void clean() {
		if (isFull()) {
			Iterator<RunConfigurationRef> iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				if (!isFull()) {
					break;
				}
				iterator.remove();
			}
		}
	}

	private boolean isFull() {
		return map.size() > MAX_SIZE;
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



	public Pins getPins(RunConfigurationRef key) {
		return map.get(key);
	}

	public static class Pins {
		private List<Pin> pins = new ArrayList<>();

		public Pins(@Nullable String parentConsoleUUID, @NotNull String consoleUUID, @NotNull GrepModel grepModel) {
			pins.add(new Pin(parentConsoleUUID, consoleUUID, grepModel));
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
		private GrepModel grepModel;

		public Pin() {
		}

		public Pin(@Nullable String parentConsoleUUID, @NotNull String consoleUUID, @NotNull GrepModel grepModel) {
			this.parentConsoleUUID = parentConsoleUUID;
			this.consoleUUID = consoleUUID;
			this.grepModel = grepModel;
		}

		public void setGrepModel(@NotNull GrepModel grepModel) {
			this.grepModel = grepModel;
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

		public GrepModel getGrepModel() {
			return grepModel;
		}

		@Override
		public String toString() {
			return "Pin{" +
					"parentConsoleUUID='" + parentConsoleUUID + '\'' +
					", consoleUUID='" + consoleUUID + '\'' +
					", grepModel=" + grepModel +
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
