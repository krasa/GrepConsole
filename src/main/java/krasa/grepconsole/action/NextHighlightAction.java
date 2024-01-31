package krasa.grepconsole.action;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ex.MarkupIterator;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NextHighlightAction extends MyDumbAwareAction {

	private ConsoleView consoleView;

	public NextHighlightAction() {
	}

	public NextHighlightAction(ConsoleView consoleView) {
		this.consoleView = consoleView;
		ActionUtil.copyFrom(this, "krasa.grepconsole.action.NextHighlight");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		Editor myEditor = getEditor(anActionEvent);

		MarkupModelEx model = (MarkupModelEx) myEditor.getMarkupModel();
		int from = myEditor.getCaretModel().getPrimaryCaret().getOffset() + 1;
		int textLength = myEditor.getDocument().getTextLength();
		MarkupIterator<RangeHighlighterEx> iterator = model.overlappingIterator(from, textLength);
		RangeHighlighterEx result = null;
		try {
			while (iterator.hasNext()) {
				RangeHighlighterEx next = iterator.next();
				if (next.isValid() && next.getLayer() == HighlighterLayer.CONSOLE_FILTER) {
					if (next.getStartOffset() > from) {
						result = next;
						break;
					}
				}
			}
		} finally {
			iterator.dispose();
		}
//		List<RangeHighlighter> highlighters = (List<RangeHighlighter>) processor.getResults();

//		if (highlighters.isEmpty() && from + 1 < textLength) {
//			model.processRangeHighlightersOverlappingWith(from, textLength, processor);
//			highlighters = (List<RangeHighlighter>) processor.getResults();
//		}

		if (result != null) {
			RangeHighlighter rangeHighlighter = result;
			myEditor.getCaretModel().getPrimaryCaret().moveToOffset(rangeHighlighter.getStartOffset());
			myEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
			IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myEditor.getContentComponent(), true));
		}
	}

	@Nullable
	private Editor getEditor(@NotNull AnActionEvent anActionEvent) {
		Editor myEditor;
		if (consoleView instanceof ConsoleViewImpl) {
			myEditor = (((ConsoleViewImpl) consoleView).getEditor());
		} else {
			myEditor = anActionEvent.getData(CommonDataKeys.EDITOR);
		}
		return myEditor;
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(consoleView != null || e.getData(LangDataKeys.CONSOLE_VIEW) != null);
	}
}
