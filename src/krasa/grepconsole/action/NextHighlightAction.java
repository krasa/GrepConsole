package krasa.grepconsole.action;

import com.intellij.execution.impl.*;
import com.intellij.execution.ui.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.wm.*;
import org.jetbrains.annotations.*;

public class NextHighlightAction extends DumbAwareAction {

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
