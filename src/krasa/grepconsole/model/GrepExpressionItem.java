package krasa.grepconsole.model;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import krasa.grepconsole.decorators.ConsoleTextDecorator;
import krasa.grepconsole.decorators.Operation;
import krasa.grepconsole.service.Cache;

import com.intellij.execution.ui.ConsoleViewContentType;

public class GrepExpressionItem extends AbstractGrepModelElement {

	private String grepExpression;
	private String unlessGrepExpression;
	private boolean caseInsensitive;
	private GrepStyle style = new GrepStyle();

	private Pattern pattern;
	private Pattern unlessPattern;

	private Operation operationOnMatch = Operation.PRINT_IMMEDIATELY;

	public GrepExpressionItem() {
		this(null);
	}

	public GrepExpressionItem(String id) {
		super(id);

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
		if (unlessPattern == null && unlessGrepExpression != null) {
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
			pattern = Pattern.compile(".*" + expression + ".*", computeFlags()); //$NON-NLS-1$ //$NON-NLS-2$
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

	public ConsoleTextDecorator createDecorator() {
		return new ConsoleTextDecorator(this);
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

	@Override
	public void findStyleUses(GrepStyle style, Set<GrepExpressionItem> items) {
		if (this.style == style) {
			items.add(this);
		}
	}

	@Override
	protected void refreshStyles() {
		if (style != null) {
			style = getRoot().getStyle(style.getId());
		}
	}

	public ModifiableConsoleViewContentType getStyle(ConsoleViewContentType contentType) {
		String cacheIdentifier = getCacheIdentifier(contentType);
		ModifiableConsoleViewContentType result = Cache.getInstance().get(cacheIdentifier);
		if (result == null) {
			result = ModifiableConsoleViewContentType.create(cacheIdentifier, contentType);
			style.applyTo(result.getAttributes());
			Cache.getInstance().put(cacheIdentifier, result);
		}
		return result;
	}

	private String getCacheIdentifier(ConsoleViewContentType contentType) {
		return getId() + contentType.toString();
	}

	public Operation getOperationOnMatch() {
		return operationOnMatch;
	}

	public void setOperationOnMatch(Operation operationOnMatch) {
		this.operationOnMatch = operationOnMatch;
	}
}
