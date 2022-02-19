package krasa.grepconsole.grep;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GrepModelTest {
	private GrepModel.Matcher matcher;

	@Test
	public void matchTest() throws Exception {
		matcher = new GrepModel(false, false, false, "a", false).createMatcher();
		_true("a");
		_true("A");
		_true("aa");
		_true("aA");
		false_("b");

		matcher = new GrepModel(false, false, false, "a", false).createMatcher();
		_true("a");
		_true("A");
//		false_("aa");
//		false_("aA");
		false_("b");

		matcher = new GrepModel(false, false, true, "a", false).createMatcher();
		_true("a");
		_true("A");
//		false_("aa");
//		false_("aA");
		false_("b");

		matcher = new GrepModel(false, false, false, "a[a]", false).createMatcher();
		false_("a");
		false_("A");
		false_("aa");
		false_("aA");
		false_("b");

		matcher = new GrepModel(false, false, true, "a[a]", false).createMatcher();
		false_("a");
		false_("A");
		_true("aa");
		_true("aA");
		false_("b");

		matcher = new GrepModel(false, false, true, "a", false).createMatcher();
		_true("a");
		_true("A");
		_true("aa");
		_true("aA");
		false_("b");

		matcher = new GrepModel(false, true, false, "a", false).createMatcher();
		_true("a");
		_true("A");
		false_("aa");
		false_("aA");
		false_("b");

		matcher = new GrepModel(false, true, true, "a", false).createMatcher();
		_true("a");
		_true("A");
		false_("aa");
		false_("aA");
		false_("b");

		matcher = new GrepModel(true, false, false, "a", false).createMatcher();
		_true("a");
		false_("A");
		_true("aa");
		_true("aA");
		false_("b");

		matcher = new GrepModel(true, false, true, "a", false).createMatcher();
		_true("a");
		false_("A");
		_true("aa");
		_true("aA");
		false_("b");
	}

	protected void _true(String a) {
		assertTrue(matcher.matches(a));
	}

	protected void false_(String b) {
		false_(matcher.matches(b));
	}

	protected void false_(boolean a) {
		assertFalse(a);
	}
}