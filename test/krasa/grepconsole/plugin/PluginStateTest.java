package krasa.grepconsole.plugin;

import junit.framework.Assert;

import org.junit.Test;

public class PluginStateTest {
	@Test
	public void testClone() throws Exception {
		PluginState pluginState = new PluginState();
		Assert.assertTrue(pluginState.equals(pluginState.clone()));
	}
}
