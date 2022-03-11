package krasa.grepconsole.grep;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.awt.*;
import java.util.Objects;
import java.util.Queue;

public class GrepBeforeAfterModel {
	public static final Key GREP_BEFORE_AFTER = new Key("grepBeforeAfter");

	public static TextAttributesKey TEXT_ATTRIBUTES_KEY;

	public static void lookAndFeelChanged() {
		if (TEXT_ATTRIBUTES_KEY == null) {
			TextAttributes defaultAttributes = ConsoleViewContentType.getConsoleViewType(ProcessOutputTypes.STDOUT).getAttributes().clone();
			defaultAttributes.setFontType(Font.ITALIC);
			TEXT_ATTRIBUTES_KEY = TextAttributesKey.createTextAttributesKey("Grep Console - Before/After", defaultAttributes);
			ConsoleViewContentType.registerNewConsoleViewType(GrepBeforeAfterModel.GREP_BEFORE_AFTER, new ConsoleViewContentType(GrepBeforeAfterModel.GREP_BEFORE_AFTER.toString(), TEXT_ATTRIBUTES_KEY));
		} else {
			TextAttributes defaultAttributes = TEXT_ATTRIBUTES_KEY.getDefaultAttributes();
			defaultAttributes.copyFrom(ConsoleViewContentType.getConsoleViewType(ProcessOutputTypes.STDOUT).getAttributes());
			defaultAttributes.setFontType(Font.ITALIC);
		}
	}


	private int before = 0;
	private int after = 0;

	@Transient
	transient Queue<Pair<String, Key>> queue;
	@Transient
	private transient int linesAfterActual = Integer.MAX_VALUE;


	public int getBefore() {
		return before;
	}

	public void setBefore(int before) {
		this.before = before;
	}

	public int getAfter() {
		return after;
	}

	public void setAfter(int after) {
		this.after = after;
	}

	public void flushBeforeMatched(OpenGrepConsoleAction.LightProcessHandler myProcessHandler) {
		for (Pair<String, Key> next : getQueue()) {
			print(myProcessHandler, next.first, next.second);
		}
	}

	public void buffer(OpenGrepConsoleAction.LightProcessHandler myProcessHandler, String s, Key stdout) {
		if (after > linesAfterActual) {
			print(myProcessHandler, s, stdout);
			linesAfterActual++;
		} else {
			getQueue().add(Pair.create(s, stdout));
		}
	}

	private void print(OpenGrepConsoleAction.LightProcessHandler myProcessHandler, String s, Key stdout) {
		myProcessHandler.notifyTextAvailable(s, GREP_BEFORE_AFTER);
	}

	private Queue<Pair<String, Key>> getQueue() {
		if (queue == null) {
			if (before == 0) {
				queue = new FakeQueue();
			} else {
				queue = new CircularFifoQueue<>(before);
			}
		}
		return queue;
	}

	public void matched() {
		getQueue().clear();
		linesAfterActual = 0;
	}

	public void clear() {
		linesAfterActual = Integer.MAX_VALUE;
		if (queue != null) {
			queue.clear();
		}
	}

	@Override
	public String toString() {
		return "GrepBeforeAfterModel{" +
				"before='" + before + '\'' +
				", after='" + after + '\'' +
				'}';
	}

	public String toPresentationString() {
		return before + "/" + after;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GrepBeforeAfterModel that = (GrepBeforeAfterModel) o;
		return before == that.before && after == that.after;
	}

	@Override
	public int hashCode() {
		return Objects.hash(before, after);
	}
}
