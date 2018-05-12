package krasa.grepconsole.model;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.GrepProcessorImpl;
import krasa.grepconsole.filter.support.ThreadUnsafeGrepProcessor;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GrepExpressionItem extends AbstractGrepModelElement {

	public static final String ACTION_REMOVE_UNLESS_MATCHED = "REMOVE_UNLESS_PREVIOUSLY_MATCHED";
	public static final String ACTION_REMOVE = "REMOVE";
	public static final String ACTION_NO_ACTION = "NO_ACTION";
	
	private boolean enabled = true;
	/**
	 * filter out text if matches
	 */
	@Deprecated
	private boolean inputFilter = false;
	private boolean fold = false;
	private String grepExpression;
	private String unlessGrepExpression;
	private boolean caseInsensitive;
	private GrepStyle style = new GrepStyle();
	private Sound sound = new Sound();
	@Deprecated
	private boolean clearConsole;

	private transient Pattern pattern;
	private transient Pattern unlessPattern;

	private Operation operationOnMatch = Operation.EXIT;
	private String action;
	private boolean highlightOnlyMatchingText = false;

	private boolean showCountInConsole = false;
	private boolean showCountInStatusBar = false;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isClearConsole() {
		return clearConsole;
	}

	public void setClearConsole(boolean clearConsole) {
		this.clearConsole = clearConsole;
	}

	@Transient
	public boolean isWholeLine() {
		return !highlightOnlyMatchingText;
	}

	public void setWholeLine(boolean wholeLine) {
		this.highlightOnlyMatchingText = !wholeLine;
	}

	public boolean isFold() {
		return fold;
	}

	public void setFold(boolean fold) {
		this.fold = fold;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public Sound getSound() {
		return sound;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isInputFilter() {
		return inputFilter;
	}

	public void setInputFilter(boolean inputFilter) {
		this.inputFilter = inputFilter;
	}

	public String getGrepExpression() {
		return grepExpression;
	}

	public void setGrepExpression(String grepExpression) {
		if (this.grepExpression == null || grepExpression == null || !this.grepExpression.equals(grepExpression)) {
			this.grepExpression = grepExpression;
			this.pattern = null;
		}
	}

	public String getUnlessGrepExpression() {
		return unlessGrepExpression;
	}

	public void setUnlessGrepExpression(String unlessGrepExpression) {
		if (this.unlessGrepExpression == null || unlessGrepExpression == null
				|| !this.unlessGrepExpression.equals(unlessGrepExpression)) {
			this.unlessGrepExpression = unlessGrepExpression;
			this.unlessPattern = null;
		}
	}

	public GrepStyle getStyle() {
		return style;
	}

	public void setStyle(GrepStyle style) {
		this.style = style;
	}

	public Pattern getPattern() {
		if (pattern == null && grepExpression != null) {
			pattern = compilePattern(grepExpression);
		}

		return pattern;
	}

	public Pattern getUnlessPattern() {
		if (unlessPattern == null && !StringUtils.isEmpty(unlessGrepExpression)) {
			unlessPattern = compilePattern(unlessGrepExpression);
		}

		return unlessPattern;
	}

	/**
	 * Compiles the specified grep expression. Swallows exceptions caused by invalid expressions.
	 *
	 * @param expression
	 * @return The compiled pattern, or <code>null</code> if an error occurs.
	 */
	private Pattern compilePattern(String expression) {
		Pattern pattern;

		try {
			pattern = Pattern.compile(expression, computeFlags()); // $NON-NLS-1$ //$NON-NLS-2$
		} catch (PatternSyntaxException ex) {
			pattern = null;
		}

		return pattern;
	}

	/**
	 * Computes flags for the regular expression pattern.
	 *
	 * @return Flags.
	 */
	private int computeFlags() {
		return caseInsensitive ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
	}

	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		if (caseInsensitive != this.caseInsensitive) {
			this.caseInsensitive = caseInsensitive;
			this.pattern = null;
			this.unlessPattern = null;
		}
	}

	public GrepProcessor createProcessor() {
		return new GrepProcessorImpl(this);
	}

	/**
	 * do not use in input filters
	 */
	public GrepProcessor createThreadUnsafeProcessor() {
		return new ThreadUnsafeGrepProcessor(this);
	}

	public GrepExpressionItem grepExpression(final String grepExpression) {
		this.grepExpression = grepExpression;
		return this;
	}

	public GrepExpressionItem unlessGrepExpression(final String unlessGrepExpression) {
		this.unlessGrepExpression = unlessGrepExpression;
		return this;
	}

	public GrepExpressionItem caseInsensitive(final boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
		return this;
	}

	public GrepExpressionItem style(final GrepStyle style) {
		this.style = style;
		return this;
	}


	public Operation getOperationOnMatch() {
		return operationOnMatch;
	}

	public void setOperationOnMatch(Operation operationOnMatch) {
		this.operationOnMatch = operationOnMatch;
	}

	/* TODO this all need to be changed */
	public ConsoleViewContentType getConsoleViewContentType(ConsoleViewContentType consoleViewContentType) {
		ConsoleViewContentType result;
		if (consoleViewContentType != null) {
			// TODO maybe tree would have better performance?
			final String newId = consoleViewContentType.toString() + "-" + getId();
			result = Cache.getInstance().get(newId);
			if (result == null) {
				result = createConsoleViewContentType(newId, consoleViewContentType.getAttributes().clone());
			}
		} else {
			String cacheIdentifier = getId();
			result = Cache.getInstance().get(cacheIdentifier);
			if (result == null) {
				result = createConsoleViewContentType(cacheIdentifier, new TextAttributes());
			}
		}
		return result;
	}

	private ConsoleViewContentType createConsoleViewContentType(String newId, TextAttributes newTextAttributes) {
		ConsoleViewContentType result;
		style.applyTo(newTextAttributes);
		result = new ConsoleViewContentType(newId, newTextAttributes);
		Cache.getInstance().put(newId, result);
		return result;
	}

	public GrepExpressionItem enabled(final boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public GrepExpressionItem pattern(final Pattern pattern) {
		this.pattern = pattern;
		return this;
	}

	public GrepExpressionItem unlessPattern(final Pattern unlessPattern) {
		this.unlessPattern = unlessPattern;
		return this;
	}

	public GrepExpressionItem operationOnMatch(final Operation operationOnMatch) {
		this.operationOnMatch = operationOnMatch;
		return this;
	}

	public boolean isHighlightOnlyMatchingText() {
		return highlightOnlyMatchingText;
	}

	public void setHighlightOnlyMatchingText(boolean highlightOnlyMatchingText) {
		this.highlightOnlyMatchingText = highlightOnlyMatchingText;
	}

	public GrepExpressionItem highlightOnlyMatchingText(final boolean highlightOnlyMatchingText) {
		this.highlightOnlyMatchingText = highlightOnlyMatchingText;
		return this;
	}

	public boolean isShowCountInConsole() {
		return showCountInConsole;
	}

	public void setShowCountInConsole(boolean showCountInConsole) {
		this.showCountInConsole = showCountInConsole;
	}

	public boolean isShowCountInStatusBar() {
		return showCountInStatusBar;
	}

	public void setShowCountInStatusBar(boolean showCountInStatusBar) {
		this.showCountInStatusBar = showCountInStatusBar;
	}

	public boolean isContinueMatching() {
		return operationOnMatch == Operation.CONTINUE_MATCHING;
	}

	public void setContinueMatching(boolean continueMatching) {
		this.operationOnMatch = continueMatching ? Operation.CONTINUE_MATCHING : Operation.EXIT;
	}

	public GrepExpressionItem fold(boolean fold) {
		this.fold = fold;
		return this;
	}

	public GrepExpressionItem sound(Sound sound) {
		this.sound = sound;
		return this;
	}

	public GrepExpressionItem clearConsole(boolean clearConsole) {
		this.clearConsole = clearConsole;
		return this;
	}


	public GrepExpressionItem showCountInConsole(boolean showCountInConsole) {
		this.showCountInConsole = showCountInConsole;
		return this;
	}

	public GrepExpressionItem showCountInStatusBar(boolean showCountInStatusBar) {
		this.showCountInStatusBar = showCountInStatusBar;
		return this;
	}

	public GrepExpressionItem action(String action) {
		this.action = action;
		return this;
	}
}
