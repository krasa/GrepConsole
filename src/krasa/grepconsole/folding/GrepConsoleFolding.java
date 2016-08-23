package krasa.grepconsole.folding;

import java.util.List;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ConsoleFolding;
import com.intellij.openapi.util.text.StringUtil;

import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

/**
 * @author Vojtech Krasa
 */
public class GrepConsoleFolding extends ConsoleFolding {
	private GrepConsoleApplicationComponent grepConsoleApplicationComponent;

	public GrepConsoleFolding(GrepConsoleApplicationComponent grepConsoleApplicationComponent) {
		this.grepConsoleApplicationComponent = grepConsoleApplicationComponent;
	}

	@Override
	public boolean shouldFoldLine(String line) {
		List<GrepExpressionItem> foldings = grepConsoleApplicationComponent.getCachedFoldingItems();
		int cachedMaxLengthToMatch = grepConsoleApplicationComponent.getCachedMaxLengthToMatch();
		line = line.substring(0, Math.min(line.length(), cachedMaxLengthToMatch));
		CharSequence input = StringUtil.newBombedCharSequence(line, 10000);

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
		return false;
	}

	@Nullable
	@Override
	public String getPlaceholderText(List<String> lines) {
		String s = lines.size() > 1 ? "s" : "";
		return " <" + lines.size() + " folded line" + s + ">";
	}
}
