package krasa.grepconsole.grep.gui;

import org.junit.Assert;
import org.junit.Test;

public class GrepOptionsItemTest {

	@Test
	public void test() throws Exception {
		GrepOptionsItem grepOptionsItem = new GrepOptionsItem();
		grepOptionsItem.version = 1;
		grepOptionsItem.wholeLine = true;
		grepOptionsItem.regex = false;
		grepOptionsItem.caseSensitive = true;
		grepOptionsItem.expression = "foo";
		Assert.assertEquals("1|true|false|true|foo", grepOptionsItem.asString());
		Assert.assertEquals(grepOptionsItem, GrepOptionsItem.fromString(grepOptionsItem.asString()));
	}

}