package krasa.grepconsole.stats;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.gui.SettingsContext;
import krasa.grepconsole.model.GrepColor;

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
	private GrepHighlightFilter grepHighlightFilter;
	private ConsoleViewImpl consoleView;

	public StatisticsConsolePanel(GrepHighlightFilter filter, ConsoleViewImpl consoleView) {
		super(new BorderLayout());
		this.grepHighlightFilter = filter;
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
	}

	public Project getProject() {
		return grepHighlightFilter.getProject();
	}

	private void init() {
		final List<GrepProcessor> grepProcessors = grepHighlightFilter.getGrepProcessors();
		MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					new OpenConsoleSettingsAction(consoleView).actionPerformed(getProject(), SettingsContext.NONE);
				}
			}
		};
		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isShowCountInConsole()) {
				add(grepProcessor, mouseInputAdapter);
			}
		}
		addButtons();
	}

	public void addButtons() {
		jPanel.add(createActionLabel("Reset", new Runnable() {
			@Override
			public void run() {
				StatisticsManager.clearCount(grepHighlightFilter);
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
		pairs.add(new Pair<>(label, processor));

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

}
