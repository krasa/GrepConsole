package krasa.grepconsole.ansi;

import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.Matcher;

import krasa.grepconsole.model.Profile;

import org.fusesource.jansi.Ansi;
import org.junit.Test;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Pair;

public class AnsiConsoleStyleFilterTest {

	protected final AnsiConsoleStyleFilter ansiConsoleStyleFilter;

	public AnsiConsoleStyleFilterTest() {
		ansiConsoleStyleFilter = new AnsiConsoleStyleFilter(new Profile());
	}

	@Test
	public void testProcess() throws Exception {
		process("foo\u001B[0;36mbar", "foo\u001B[0;36mbar", 3);
		process("\u001B[0;36mbar", "\u001B[0;36mbar", 2);
		process("\u001B[0;36m", "\u001B[0;36m", 1);
	}

	@Test
	public void testHideAnsi() throws Exception {
		Profile profile = new Profile();
		profile.setHideAnsiCommands(true);
		ansiConsoleStyleFilter.setProfile(profile);

		process("foo\u001B[0m\u001B[36mbar", "foobar", 2);
		process("\u001B[0m\u001B[36mbar", "bar", 1);
		process("\u001B[0m\u001B[36m", "", 1);
	}

	private void process(final String input, final String expected, final int numberOfItems) {
		List<Pair<String, ConsoleViewContentType>> list = ansiConsoleStyleFilter.process(input,
				ConsoleViewContentType.NORMAL_OUTPUT);
		assertNotNull(list);
		assertEquals(numberOfItems, list.size());

		StringBuilder sb = new StringBuilder();
		for (Pair<String, ConsoleViewContentType> pair : list) {
			sb.append(pair.first);
		}
		assertEquals(expected, sb.toString());
	}

	@Test
	public void testMatcher() throws Exception {
		matches(Ansi.ansi().bg(Ansi.Color.BLUE));
		matches(Ansi.ansi().fg(Ansi.Color.BLUE));
		matches(Ansi.ansi().eraseLine());
		matches(Ansi.ansi().reset());
		matches(Ansi.ansi().scrollUp(1));
		matches(Ansi.ansi().eraseScreen());
		matches(Ansi.ansi().cursorDown(1));
		matches(Ansi.ansi().eraseScreen(Ansi.Erase.ALL));
	}

	private void matches(final Ansi ansi1) {
		Matcher matcher = this.ansiConsoleStyleFilter.getMatcher(ansi1.toString());
		assertTrue(ansi1.toString(), matcher.matches());
	}

}
