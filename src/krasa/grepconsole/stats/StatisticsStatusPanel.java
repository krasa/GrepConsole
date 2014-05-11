package krasa.grepconsole.stats;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;

import javax.swing.*;

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
import com.intellij.ui.SeparatorComponent;
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
		add(new SeparatorComponent(7), BorderLayout.WEST);

		final FlowLayout layout = new FlowLayout();
		layout.setVgap(0);
		layout.setHgap(0);
		jPanel = new JPanel(new GridBagLayout());

		// jPanel.setBackground(Color.BLUE);
		add(jPanel, BorderLayout.CENTER);
		initComponents();
		startUpdater();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showToolWindow();
			}
		});
	}

	private JPanel createCounterPanel(GrepProcessor processor) {
		GrepColor backgroundColor = processor.getGrepExpressionItem().getStyle().getBackgroundColor();
		GrepColor foregroundColor = processor.getGrepExpressionItem().getStyle().getForegroundColor();
		// panel.setBackground(getBackground());
		final GridBagLayout layout = new GridBagLayout();
		// layout.setVgap(0);
		// layout.setHgap(0);
		final JPanel panel = new JPanel(layout);
		// panel.setBackground(Color.RED);

		final JLabel label = new JLabel("0");
		label.setForeground(JBColor.BLACK);
		pairs.add(new Pair<JLabel, GrepProcessor>(label, processor));

		final ColorPanel color = new ColorPanel(processor.getGrepExpressionItem().getGrepExpression(), new Dimension(
				14, 14)) {
			@Override
			protected void onMousePressed(MouseEvent e) {
				showToolWindow();
			}
		};
		color.setSelectedColor(backgroundColor.getColorAsAWT());
		color.setBorderColor(foregroundColor.getColorAsAWT());
		// color.setBackground(getBackground());
		panel.add(color);
		panel.add(label);
		return panel;
	}

	private void showToolWindow() {
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
					toolWindowByDescriptor.activate(null);
					contentManager.setSelectedContent(descriptor.getAttachedContent(), true);
					return;
				}
		}
		}
}

	public void reset() {
		pairs.clear();
		jPanel.removeAll();
		initComponents();
	}

	public Project getProject() {
		return grepHighlightFilter.getProject();
	}

	private void initComponents() {
		final List<GrepProcessor> grepProcessors = grepHighlightFilter.getGrepProcessors();
		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isShowCountInStatusBar()) {
				add(grepProcessor);
			}
		}
	}

	public void add(GrepProcessor processor) {
		jPanel.add(createCounterPanel(processor));
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
