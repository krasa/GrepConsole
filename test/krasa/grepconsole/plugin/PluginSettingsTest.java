package krasa.grepconsole.plugin;

import junit.framework.Assert;
import org.junit.Test;

public class PluginSettingsTest {
	@Test
	public void testClone() throws Exception {
		PluginSettings pluginSettings = new PluginSettings();
		Assert.assertTrue(pluginSettings.equals(pluginSettings.clone()));
	}
}
