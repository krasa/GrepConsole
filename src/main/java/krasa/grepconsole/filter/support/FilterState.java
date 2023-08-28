package krasa.grepconsole.filter.support;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Operation;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.ExtensionManager;
import krasa.grepconsole.utils.Notifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;

public class FilterState {
	private static final Logger LOG = Logger.getInstance(FilterState.class);

	private int offset;
	private Operation nextOperation = Operation.CONTINUE_MATCHING;
	protected ConsoleViewContentType consoleViewContentType;
	protected List<MyResultItem> resultItemList;
	private boolean exclude;
	private boolean matchesSomething;
	private String text;
	private final Profile profile;
	private CharSequence charSequence;
	private final Project project;
	private boolean clearConsole;
	private boolean textChanged;
	private boolean multiline;

	public FilterState(int offset, String text, Profile profile, CharSequence charSequence, Project project) {
		this.offset = offset;
		this.text = text;
		this.profile = profile;
		this.charSequence = charSequence;
		this.project = project;
	}

	@NotNull
	public CharSequence getCharSequence() {
		return charSequence;
	}

	public Operation getNextOperation() {
		return nextOperation;
	}

	public void setNextOperation(Operation nextOperation) {
		this.nextOperation = nextOperation;
	}

	public void setConsoleViewContentType(ConsoleViewContentType consoleViewContentType) {
		this.consoleViewContentType = consoleViewContentType;
	}

	public ConsoleViewContentType getConsoleViewContentType() {
		return consoleViewContentType;
	}

	public TextAttributes getTextAttributes() {
		if (consoleViewContentType == null) {
			return null;
		}
		return consoleViewContentType.getAttributes();
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

	public boolean isExclude() {
		return exclude;
	}

	public void setMatchesSomething(boolean matchesSomething) {
		this.matchesSomething = matchesSomething;
	}

	public boolean isMatchesSomething() {
		return matchesSomething;
	}

	public boolean add(MyResultItem resultItem) {
		if (resultItemList == null) {
			resultItemList = new ArrayList<>();
		}
		return resultItemList.add(resultItem);
	}

	@Nullable
	public List<MyResultItem> getResultItemList() {
		return resultItemList;
	}

	public int getOffset() {
		return offset;
	}

	public void setClearConsole(boolean clearConsole) {
		this.clearConsole |= clearConsole;
	}

	public boolean isClearConsole() {
		return clearConsole;
	}

	public boolean notTerminatedWithNewline() {
		if (charSequence.length() == 0) {
			return false;
		}
		return charSequence.charAt(charSequence.length() - 1) != '\n';
	}

	public void executeAction(GrepExpressionItem grepExpressionItem, Matcher matcher) {
		setNextOperation(grepExpressionItem.getOperationOnMatch());
		setClearConsole(grepExpressionItem.isClearConsole());
		setMultiline(grepExpressionItem.isMultiline());

		String action = grepExpressionItem.getAction();
		if (GrepExpressionItem.ACTION_NO_ACTION.equals(action)) {
//		} else if (GrepExpressionItem.ACTION_BUFFER_UNTIL_NEWLINE.equals(action)) {
//			String originalText = text;
//			text = grepProcessor.bufferUntilNewLine(text);
//			update(originalText);
		} else if (GrepExpressionItem.ACTION_REMOVE.equals(action)) {
			setExclude(true);
		} else if (GrepExpressionItem.ACTION_REMOVE_UNLESS_MATCHED.equals(action)) {
			if (!isMatchesSomething()) {
				setExclude(true);
			}
		} else if (action != null && !StringUtil.isEmpty(text)) {
			executeFunction(action, matcher);
		}

		setMatchesSomething(true);

		if (grepExpressionItem.getSound().isEnabled()) {
			grepExpressionItem.getSound().play();
		}
	}

	@SuppressWarnings("unchecked")
	protected void executeFunction(String action, Matcher matcher) {
		Object function = ExtensionManager.getFunction(action);

		if (function != null) {
			long t0 = System.currentTimeMillis();

			String originalText = text;
			try {
				if (function instanceof Function) {
					text = ((Function<String, String>) function).apply(originalText);
				} else if (function instanceof BiFunction) {
					text = ((BiFunction<String, Matcher, String>) function).apply(originalText, matcher);
				} else {
					Notifier.notify_BrokenExtension(action, project);
				}
			} catch (Throwable e) {
				LOG.error("Script '" + action + "' error for text: '" + originalText + "'", e);
				return;
			}


			t0 = System.currentTimeMillis() - t0;
			if (t0 > 1000) {
				LOG.warn("GrepConsole: script '" + action + "'took " + t0 + " ms on '''" + originalText + "'''");
			}

			update(originalText);
		} else {
			Notifier.notify_MissingExtension(action, project);
		}
	}

	private void update(String originalText) {
		if (text == null) {
			setExclude(true);
		}
		//noinspection StringEquality
		boolean textChanged = this.textChanged || text != originalText;
		if (textChanged) {
			setTextChanged(true);

			if (text != null && nextOperation == Operation.CONTINUE_MATCHING) {
				String substring = profile.limitInputLength_andCutNewLine(text);
				charSequence = profile.limitProcessingTime(substring);
			}
		}
	}

	public String getText() {
		return text;
	}

	public void setTextChanged(boolean textChanged) {
		this.textChanged = textChanged;
	}

	public boolean isTextChanged() {
		return textChanged;
	}

	public boolean getTextChanged() {
		return textChanged;
	}

	public boolean isMultiline() {
		return multiline;
	}

	public void setMultiline(boolean multiline) {
		this.multiline = multiline;
	}
}
