package krasa.grepconsole.stats;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ui.UIUtil;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Vojtech Krasa
 */
public class StatisticsConsolePanel extends JPanel implements Disposable {
	private List<Pair<JLabel, GrepProcessor>> pairs = new ArrayList<>();
	private java.util.Timer timer;
	private final JPanel jPanel;
	private HighlightingFilter highlightingFilter;
	private ConsoleViewImpl consoleView;

	public StatisticsConsolePanel(HighlightingFilter filter, ConsoleViewImpl consoleView) {
		super(new BorderLayout());
		this.highlightingFilter = filter;
		this.consoleView = consoleView;
		setBorder(new EmptyBorder(0, 0, 0, 0));
		final FlowLayout layout = new FlowLayout();
		layout.setVgap(-4);
		layout.setHgap(0);
		jPanel = new JPanel(layout);
		jPanel.setBackground(getBackground());
		add(jPanel, BorderLayout.WEST);
		init();
		startUpdater();
	}

	public void reset() {
		pairs.clear();
		jPanel.removeAll();
		init();
		revalidate();
		repaint();
	}

	public Project getProject() {
		return highlightingFilter.getProject();
	}

	private void init() {
		final List<GrepProcessor> grepProcessors = highlightingFilter.getGrepProcessors();

		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isShowCountInConsole()) {
				add(grepProcessor, new MyMouseInputAdapter(grepProcessor));
			}
		}
		addButtons();
	}

	public void addButtons() {
		jPanel.add(createActionLabel("Reset", new Runnable() {
			@Override
			public void run() {
				StatisticsManager.clearCount(highlightingFilter);
				reset();
			}
		}));

		jPanel.add(createActionLabel("Hide", new Runnable() {
			@Override
			public void run() {
				StatisticsConsolePanel.this.dispose();
			}
		}));
	}

	public void add(GrepProcessor processor, final MouseInputAdapter mouseInputAdapter) {
		jPanel.add(createCounterPanel(processor, mouseInputAdapter));
	}

	private JPanel createCounterPanel(GrepProcessor processor,
									  final MouseInputAdapter mouseInputAdapter) {
		GrepColor backgroundColor = processor.getGrepExpressionItem().getStyle().getBackgroundColor();
		GrepColor foregroundColor = processor.getGrepExpressionItem().getStyle().getForegroundColor();
		final JPanel panel = new JPanel(new FlowLayout());
		panel.setBackground(getBackground());

		final JLabel label = new JLabel("0");
		label.setForeground(JBColor.BLACK);
		pairs.add(Pair.create(label, processor));

		final krasa.grepconsole.stats.common.ColorPanel color = new krasa.grepconsole.stats.common.ColorPanel(
				processor.getGrepExpressionItem().getGrepExpression());
		color.setSelectedColor(backgroundColor.getColorAsAWT());
		color.setBorderColor(foregroundColor.getColorAsAWT());
		color.setBackground(getBackground());
		color.addMouseListener(mouseInputAdapter);
		panel.addMouseListener(mouseInputAdapter);
		panel.add(color);
		panel.add(label);
		return panel;
	}

	@Override
	public Color getBackground() {
		Color color = EditorColorsManager.getInstance().getGlobalScheme().getColor(
				EditorColors.NOTIFICATION_BACKGROUND);
		return color == null ? UIUtil.getToolTipBackground() : color;
	}

	private HyperlinkLabel createActionLabel(final String text, final Runnable action) {
		HyperlinkLabel label = new HyperlinkLabel(text, JBColor.BLACK, getBackground(), JBColor.BLUE);
		label.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			protected void hyperlinkActivated(HyperlinkEvent e) {
				action.run();
			}
		});

		return label;
	}

	public boolean hasItems() {
		return !pairs.isEmpty();
	}

	public void startUpdater() {
		timer = new java.util.Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!pairs.isEmpty()) {
					ApplicationManager.getApplication().invokeLater(new Runnable() {
						@Override
						public void run() {
							for (Pair<JLabel, GrepProcessor> pair : pairs) {
								pair.getFirst().setText(String.valueOf(pair.second.getMatches()));
							}
						}
					});
				}
			}
		}, 100, 100);
	}

	private void cancelTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public void dispose() {
		cancelTimer();
		consoleView = null;
		Container parent = getParent();
		if (parent != null) {
			parent.remove(this);
			if (parent instanceof JPanel) {
				((JPanel) parent).revalidate();
			}
		}
	}

	private class MyMouseInputAdapter extends MouseInputAdapter {
		private final GrepProcessor grepProcessor;
		private final GrepExpressionItem grepExpressionItem;

		public MyMouseInputAdapter(GrepProcessor grepProcessor) {
			this.grepProcessor = grepProcessor;
			this.grepExpressionItem = grepProcessor.getGrepExpressionItem();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				Editor myEditor = consoleView.getEditor();
				MarkupModelEx model = (MarkupModelEx) myEditor.getMarkupModel();
				CommonProcessors.CollectProcessor<RangeHighlighter> processor = new CommonProcessors.CollectProcessor<RangeHighlighter>() {
					@Override
					protected boolean accept(RangeHighlighter rangeHighlighter) {
						if (rangeHighlighter.isValid() && rangeHighlighter.getLayer() == HighlighterLayer.CONSOLE_FILTER) {
							//won't work when multiple processorItems combine attributes
							return rangeHighlighter.getTextAttributes() == grepExpressionItem.getConsoleViewContentType(null).getAttributes();
						}
						return false;
					}
				};
				int to = myEditor.getCaretModel().getPrimaryCaret().getOffset() - 1;
				model.processRangeHighlightersOverlappingWith(0, to, processor);
				List<RangeHighlighter> highlighters = (List<RangeHighlighter>) processor.getResults();

				if (highlighters.isEmpty() && to + 1 < myEditor.getDocument().getTextLength()) {
					model.processRangeHighlightersOverlappingWith(to, myEditor.getDocument().getTextLength(), processor);
					highlighters = (List<RangeHighlighter>) processor.getResults();
				}

				if (!highlighters.isEmpty()) {
					RangeHighlighter rangeHighlighter = highlighters.get(highlighters.size() - 1);
					myEditor.getCaretModel().getPrimaryCaret().moveToOffset(rangeHighlighter.getStartOffset());
					myEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
					IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myEditor.getContentComponent(), true));
				}
			}
		}
	}
}
