package krasa.grepconsole.plugin.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.WriteExternalException;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.PluginState;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class GrepConsoleData implements JDOMExternalizable {
	public static final Key<GrepConsoleData> GREP_CONSOLE_DATA = Key.create("GrepConsoleData");
	public static final String SELECTED_PROFILE_ID = "selectedProfileId";
	private long selectedProfileId;

	@NotNull
	public static <T extends RunConfigurationBase> GrepConsoleData getGrepConsoleData(T t) {
		GrepConsoleData userData = t.getCopyableUserData(GREP_CONSOLE_DATA);
		if (userData == null) {
			userData = new GrepConsoleData();
		}
		t.putCopyableUserData(GREP_CONSOLE_DATA, userData);
		return userData;
	}


	@Override
	public void readExternal(Element element) throws InvalidDataException {
		final String selectedProfileIdStr = element.getAttributeValue(SELECTED_PROFILE_ID);
		if (selectedProfileIdStr != null) {
			selectedProfileId = Long.valueOf(selectedProfileIdStr);
		}
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException {
		PluginState state = GrepConsoleApplicationComponent.getInstance().getState();
		boolean runConfigurationChanges = state.isAllowRunConfigurationChanges();
		if (runConfigurationChanges && selectedProfileId != 0 && selectedProfileId != state.getDefaultProfile().getId()) {
			element.setAttribute(SELECTED_PROFILE_ID, String.valueOf(selectedProfileId));
		}
	}

	public void setSelectedProfileId(long selectedProfileId) {
		if (!GrepConsoleApplicationComponent.getInstance().getState().isAllowRunConfigurationChanges()) {
			return;
		}
		this.selectedProfileId = selectedProfileId;
	}

	public long getSelectedProfileId() {
		return selectedProfileId;
	}

}
