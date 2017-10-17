package krasa.grepconsole.utils;

import krasa.grepconsole.model.Profile;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

	@Test
	public void generateName() {
		ArrayList<Profile> settingsList = new ArrayList<>();
		settingsList.add(new Profile("new"));
		settingsList.add(new Profile("default"));

		String aNew = Utils.generateName(settingsList, "new");
		assertEquals("new (0)", aNew);
		settingsList.add(new Profile(aNew));

		aNew = Utils.generateName(settingsList, "new");
		assertEquals("new (1)", aNew);
		settingsList.add(new Profile(aNew));

		aNew = Utils.generateName(settingsList, "new");
		assertEquals("new (2)", aNew);
		settingsList.add(new Profile(aNew));
	}
}