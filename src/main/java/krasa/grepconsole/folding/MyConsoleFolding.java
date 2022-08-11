package krasa.grepconsole.folding;

import com.intellij.execution.ConsoleFolding;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Vojtech Krasa
 */
public class MyConsoleFolding extends ConsoleFolding {
	private GrepConsoleApplicationComponent grepConsoleApplicationComponent;

	public MyConsoleFolding() {
		this.grepConsoleApplicationComponent = GrepConsoleApplicationComponent.getInstance();
	}

	boolean startFolding = false;
	String placeholderTextPrefix = "";

	public boolean shouldFoldLine(@NotNull Project project, @NotNull String line) {
		try {
			List<GrepExpressionItem> foldings = grepConsoleApplicationComponent.getCachedFoldingItems();
			int cachedMaxLengthToMatch = grepConsoleApplicationComponent.getCachedMaxLengthToMatch();
			Integer maxProcessingTimeAsInt = grepConsoleApplicationComponent.getCachedMaxProcessingTimeAsInt();

			line = line.substring(0, Math.min(line.length(), cachedMaxLengthToMatch));
			CharSequence input = StringUtil.newBombedCharSequence(line, maxProcessingTimeAsInt);

			for (int i = 0; i < foldings.size(); i++) {
				GrepExpressionItem grepExpressionItem = foldings.get(i);
				boolean wholeLine = !grepExpressionItem.isHighlightOnlyMatchingText();
				Pattern unlessPattern = grepExpressionItem.getUnlessPattern();
				if (unlessPattern != null && unlessPattern.matcher(input).matches()) {
					continue;
				}
				Pattern pattern = grepExpressionItem.getPattern();
				if (pattern != null && ((wholeLine && pattern.matcher(input).matches()) || (!wholeLine && pattern.matcher(input).find()))) {
					placeholderTextPrefix = grepExpressionItem.getFoldPlaceholderTextPrefix();

					if (grepExpressionItem.isStartFolding()) {
						startFolding = true;
					} else if (grepExpressionItem.isStopFolding()) {
						startFolding = false;
					}

					return grepExpressionItem.isFold();


				}
			}
		} catch (ProcessCanceledException e) {
		}
		return startFolding;
	}

	@Override
	public boolean shouldBeAttachedToThePreviousLine() {
		return false;
	}

	@Nullable
	public String getPlaceholderText(@NotNull Project project, @NotNull List<String> lines) {
		String s = lines.size() > 1 ? "s" : "";
		return " " + placeholderTextPrefix + " <" + lines.size() + " folded line" + s + ">";
	}

}
