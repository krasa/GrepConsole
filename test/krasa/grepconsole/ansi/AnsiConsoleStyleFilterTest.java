package krasa.grepconsole.ansi;

import java.util.regex.Matcher;

import krasa.grepconsole.model.Profile;

import org.fusesource.jansi.Ansi;
import org.junit.Assert;
import org.junit.Test;

public class AnsiConsoleStyleFilterTest {

	protected final AnsiConsoleStyleFilter ansiConsoleStyleFilter;

	public AnsiConsoleStyleFilterTest() {
		ansiConsoleStyleFilter = new AnsiConsoleStyleFilter(new Profile());
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
		Assert.assertTrue(ansi1.toString(), matcher.matches());
	}

}
