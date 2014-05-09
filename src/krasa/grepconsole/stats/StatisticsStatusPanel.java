package krasa.grepconsole.stats;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.GrepColor;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

/**
 * @author Vojtech Krasa
 */
public class StatisticsStatusPanel extends JPanel {
	List<Pair<JLabel, GrepProcessor>> pairs = new ArrayList<Pair<JLabel, GrepProcessor>>();
	java.util.Timer timer;
	private final JPanel jPanel;
	private WeakReference<ConsoleView> consoleView;
	private GrepHighlightFilter grepHighlightFilter;

	public StatisticsStatusPanel(final ConsoleView consoleView, GrepHighlightFilter filter) {
		super(new BorderLayout());
		this.consoleView = new WeakReference<ConsoleView>(consoleView);
		this.grepHighlightFilter = filter;
		setBorder(new EmptyBorder(0, 0, 0, 0));
		final FlowLayout layout = new FlowLayout();
		layout.setVgap(-4);
		layout.setHgap(0);
		jPanel = new JPanel(layout);
		// jPanel.setBackground(getBackground());
		add(jPanel, BorderLayout.WEST);
		init();
		startUpdater();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				activateConsole();
			}
		});
	}

	private void activateConsole() {
		final ConsoleViewImpl consoleViewImpl = (ConsoleViewImpl) this.consoleView.get();
		if (consoleViewImpl != null) {
			activate(consoleViewImpl);
		}
	}

	private void activate(ConsoleViewImpl consoleViewImpl) {
		final Project project = consoleViewImpl.getProject();
		final RunContentManager runContentManager = ExecutionManager.getInstance(project).getContentManager();
		for (RunContentDescriptor descriptor : runContentManager.getAllDescriptors()) {
			final ExecutionConsole executionConsole = descriptor.getExecutionConsole();
			if (executionConsole == consoleViewImpl) {
				final ToolWindow toolWindowByDescriptor = runContentManager.getToolWindowByDescriptor(descriptor);
				if (toolWindowByDescriptor != null) {
					final ContentManager contentManager = toolWindowByDescriptor.getContentManager();
					for (Content content : contentManager.getContents()) {
						if (content == descriptor.getAttachedContent()) {
							toolWindowByDescriptor.activate(null);
							contentManager.setSelectedContent(content, true);
							return;
						}
					}
				}
			}
		}
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
		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isStats()) {
				add(grepProcessor);
			}
		}
	}

	public void add(GrepProcessor processor) {
		jPanel.add(createCounterPanel(processor));
	}

	private JPanel createCounterPanel(GrepProcessor processor) {
		GrepColor backgroundColor = processor.getGrepExpressionItem().getStyle().getBackgroundColor();
		GrepColor foregroundColor = processor.getGrepExpressionItem().getStyle().getForegroundColor();
		final JPanel panel = new JPanel(new FlowLayout());
		// panel.setBackground(getBackground());

		final JLabel label = new JLabel("0");
		label.setForeground(JBColor.BLACK);
		pairs.add(new Pair<JLabel, GrepProcessor>(label, processor));

		final ColorPanel color = new ColorPanel(processor.getGrepExpressionItem().getGrepExpression()) {
			@Override
			protected void onMousePressed(MouseEvent e) {
				activateConsole();
			}
		};
		color.setSelectedColor(backgroundColor.getColorAsAWT());
		color.setBorderColor(foregroundColor.getColorAsAWT());
		// color.setBackground(getBackground());
		panel.add(color);
		panel.add(label);
		return panel;
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

	@Override
	public void setVisible(boolean aFlag) {
		if (aFlag) {
			cancelTimer();
			startUpdater();
		} else {
			cancelTimer();
		}
		super.setVisible(aFlag);
	}

	public void cancelTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
