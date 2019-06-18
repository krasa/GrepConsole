package krasa.grepconsole.folding;

import com.intellij.execution.ConsoleFolding;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.text.StringUtil;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
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

	@Override
	public boolean shouldFoldLine(String line) {
		try {
			List<GrepExpressionItem> foldings = grepConsoleApplicationComponent.getCachedFoldingItems();
			int cachedMaxLengthToMatch = grepConsoleApplicationComponent.getCachedMaxLengthToMatch();
			Integer maxProcessingTimeAsInt = grepConsoleApplicationComponent.getCachedMaxProcessingTimeAsInt();

			line = line.substring(0, Math.min(line.length(), cachedMaxLengthToMatch));
			CharSequence input = StringUtil.newBombedCharSequence(line, maxProcessingTimeAsInt);

			for (int i = 0; i < foldings.size(); i++) {
				GrepExpressionItem grepExpressionItem = foldings.get(i);
				Pattern unlessPattern = grepExpressionItem.getUnlessPattern();
				if (unlessPattern != null && unlessPattern.matcher(input).matches()) {
					continue;
				}
				Pattern pattern = grepExpressionItem.getPattern();
				if (pattern != null && pattern.matcher(input).matches()) {
					return true;
				}
			}
		} catch (ProcessCanceledException e) {
		}
		return false;
	}

	@Nullable
	@Override
	public String getPlaceholderText(List<String> lines) {
		String s = lines.size() > 1 ? "s" : "";
		return " <" + lines.size() + " folded line" + s + ">";
	}
}
