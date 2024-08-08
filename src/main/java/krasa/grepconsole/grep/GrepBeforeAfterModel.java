package krasa.grepconsole.grep;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Pair;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.MyConsoleViewImpl;
import krasa.grepconsole.integration.LookAndFeelListener;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class GrepBeforeAfterModel {

	private int before = 0;
	private int after = 0;

	@Transient
	transient Queue<Line> queue;
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

	public void flushBeforeMatched(MyConsoleViewImpl consoleView) {
		for (Line line : getQueue()) {
			print(consoleView, line);
		}
	}

	public void buffer(MyConsoleViewImpl consoleView, Line line) {
		if (after > linesAfterActual) {
			print(consoleView, line);
			linesAfterActual++;
		} else {
			getQueue().add(line);
		}
	}

	private static void print(MyConsoleViewImpl consoleView, Line line) {
		for (Pair<String, ConsoleViewContentType> next : line.tokens) {
			ConsoleViewContentType type = next.second;
			if (type == ConsoleViewContentType.NORMAL_OUTPUT) {
				type = LookAndFeelListener.getContentType();
			}
			consoleView.print(next.first, type);
		}
	}

	private Queue<Line> getQueue() {
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

	public static class Line {
		List<Pair<String, ConsoleViewContentType>> tokens;

		public Line(List<Pair<String, ConsoleViewContentType>> tokens) {
			this.tokens = tokens;
		}
	}
}
