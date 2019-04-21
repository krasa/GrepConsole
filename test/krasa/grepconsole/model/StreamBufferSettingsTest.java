package krasa.grepconsole.model;

import krasa.grepconsole.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

public class StreamBufferSettingsTest {

	@Test
	public void toInt() {
	}

	@Test
	public void toNano() {
		Assert.assertEquals(5111111_000_000L, Utils.toNano("5111111", "1"));
	}
}