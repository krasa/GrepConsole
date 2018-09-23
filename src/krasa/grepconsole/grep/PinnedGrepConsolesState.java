package krasa.grepconsole.grep;

import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepProjectComponent;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.*;

public class PinnedGrepConsolesState {
	private static final Logger LOG = Logger.getInstance(PinnedGrepConsolesState.class);

	public static int MAX_SIZE = 50;
	@Transient
	private List<WeakReference<OpenGrepConsoleAction.PinAction>> actions = new ArrayList<>();

	private Map<RunConfigurationRef, Pins> map = new LinkedHashMap<>();


	public static PinnedGrepConsolesState getInstance(Project project) {
		return GrepProjectComponent.getInstance(project).getPinnedGreps();
	}

	public void register(OpenGrepConsoleAction.PinAction pinAction, Profile profile) {
		actions.add(new WeakReference<>(pinAction));
		if (profile.isAlwaysPinGrepConsoles()) {
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
		if (LOG.isDebugEnabled()) {
			LOG.debug(">pin " + "pinAction = [" + pinAction + "]");
		}
		update(pinAction.getRunConfigurationRef(), pinAction.getParentConsoleUUID(), pinAction.getConsoleUUID(), pinAction.getModel(), pinAction.getContentType(), true);
	}

	public void update(@NotNull RunConfigurationRef runContentDescriptor, String parentConsoleUUID, @NotNull String consoleUUID, @NotNull GrepModel grepModel, String contentType, boolean add) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(">update " + "runContentDescriptor = [" + runContentDescriptor + "], parentConsoleUUID = [" + parentConsoleUUID + "], consoleUUID = [" + consoleUUID + "], grepModel = [" + grepModel + "], contentType = [" + contentType + "], add = [" + add + "]");
		}
		Pins pins = map.get(runContentDescriptor);

		if (LOG.isDebugEnabled()) {
			LOG.debug("#update found: " + pins);
		}
		if (pins == null) {
			if (add) {
				map.put(runContentDescriptor, new Pins(parentConsoleUUID, consoleUUID, contentType, grepModel));
				clean();
			}
		} else {
			boolean updated = false;
			for (Pin pin : pins.pins) {
				if (pin.consoleUUID.equals(consoleUUID)) {
					LOG.debug("#update grepModel updated for pin=" + pin);
					pin.grepModel = grepModel;
					updated = true;
				}
			}
			if (!updated && add) {
				Pin e = new Pin(parentConsoleUUID, consoleUUID, contentType, grepModel);
				if (LOG.isDebugEnabled()) {
					LOG.debug("#update adding new pin =" + e);
				}
				pins.pins.add(e);
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
		if (LOG.isDebugEnabled()) {
			LOG.debug(">unpin " + "pinAction = [" + pinAction + "]");
		}
		String consoleUUID = pinAction.getConsoleUUID();
		Pins pins = map.get(pinAction.getRunConfigurationRef());

		if (LOG.isDebugEnabled()) {
			LOG.debug("found pins =" + pins);
		}
		if (pins != null) {
			for (Iterator<Pin> iterator = pins.pins.iterator(); iterator.hasNext(); ) {
				Pin pin = iterator.next();
				if (pin.consoleUUID.equals(consoleUUID)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("removing pin =" + pin);
					}
					iterator.remove();
				} else if (pin.parentConsoleUUID != null && pin.parentConsoleUUID.equals(consoleUUID)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("removing pin =" + pin);
					}
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
		if (LOG.isDebugEnabled()) {
			LOG.debug(">getPins " + "key = [" + key + "]");
		}
		Pins pins = map.get(key);
		if (LOG.isDebugEnabled()) {
			LOG.debug("<getPins " + "pins = [" + pins + "]");
		}
		return pins;
	}

	public static class Pins {
		private List<Pin> pins = new ArrayList<>();

		public Pins(@Nullable String parentConsoleUUID, @NotNull String consoleUUID, String contentType, @NotNull GrepModel grepModel) {
			pins.add(new Pin(parentConsoleUUID, consoleUUID, contentType, grepModel));
		}

		public Pins() {
		}

		public void setPins(List<Pin> pins) {
			this.pins = pins;
		}

		public List<Pin> getPins() {
			return pins;
		}

		@Override
		public String toString() {
			return "Pins{" +
					"pins=" + pins +
					'}';
		}
	}

	/**
	 * TODO add profileId
	 */
	public static class Pin {
		@Nullable
		private String parentConsoleUUID;
		private String consoleUUID;
		private String contentType;
		private GrepModel grepModel;

		public Pin() {
		}

		public Pin(@Nullable String parentConsoleUUID, @NotNull String consoleUUID, String contentType, @NotNull GrepModel grepModel) {
			this.parentConsoleUUID = parentConsoleUUID;
			this.consoleUUID = consoleUUID;
			this.contentType = contentType;
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
		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
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
					", contentType='" + contentType + '\'' +
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

		@NotNull
		static protected RunConfigurationRef toKey(RunContentDescriptor runContentDescriptor) {
			return new RunConfigurationRef(
					runContentDescriptor.getDisplayName(), runContentDescriptor.getIcon());
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
