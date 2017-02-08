package krasa.grepconsole.stats;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.content.ContentManager;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.stats.common.ColorPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Vojtech Krasa
 */
public abstract class StatisticsStatusBarPanel extends JPanel {
	private final JPanel jPanel;
	private List<Pair<JLabel, GrepProcessor>> pairs = new ArrayList<>();
	private java.util.Timer timer;
	private WeakReference<ConsoleView> consoleView;
	private GrepHighlightFilter grepHighlightFilter;

	public StatisticsStatusBarPanel(ConsoleView consoleView, GrepHighlightFilter filter) {
		super(new BorderLayout());
		this.consoleView = new WeakReference<>(consoleView);
		this.grepHighlightFilter = filter;
		add(new SeparatorComponent(7), BorderLayout.WEST);

		final FlowLayout layout = new FlowLayout();
		layout.setVgap(0);
		layout.setHgap(4);
		jPanel = new JPanel(new GridBagLayout());

		// jPanel.setBackground(Color.BLUE);
		add(jPanel, BorderLayout.CENTER);
		initComponents();
		startUpdater();
		addMouseListener(new PopupHandler() {
			@Override
			public void invokePopup(Component comp, int x, int y) {
				showPopup(comp, x, y);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) {
					showToolWindow();
				}
			}
		});
	}

	private void showPopup(Component comp, int x, int y) {
		final ActionGroup actionGroup = new ActionGroup() {
			@NotNull
			@Override
			public AnAction[] getChildren(@Nullable AnActionEvent e) {
				return new AnAction[] { new ResetCountAction(), new HideAction() };
			}
		};
		ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent().show(comp, x, y);
	}

	private JPanel createItem(GrepProcessor processor,
			final OpenConsoleSettingsActionMouseInputAdapter mouseInputAdapter) {
		final JPanel panel = getItemPanel();
		ColorPanel colorPanel = getColorPanel(processor);
		JLabel label = getLabel(processor);
		colorPanel.addMouseListener(mouseInputAdapter);
		label.addMouseListener(mouseInputAdapter);
		panel.add(colorPanel);
		panel.add(label);
		return panel;
	}

	private JPanel getItemPanel() {
		final FlowLayout layout = new FlowLayout();
		layout.setVgap(0);
		layout.setHgap(4);
		return new JPanel(layout);
	}

	private JLabel getLabel(GrepProcessor processor) {
		final JLabel label = new JLabel("0");
		label.setForeground(JBColor.BLACK);
		pairs.add(new Pair<>(label, processor));
		return label;
	}

	private krasa.grepconsole.stats.common.ColorPanel getColorPanel(final GrepProcessor processor) {
		final krasa.grepconsole.stats.common.ColorPanel color = new krasa.grepconsole.stats.common.ColorPanel(
				processor.getGrepExpressionItem().getGrepExpression(), new Dimension(14, 14)) {
			@Override
			protected void onMousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					showPopup(e.getComponent(), e.getX(), e.getY());
				} else {
					showToolWindow();
				}
			}
		};

		GrepColor backgroundColor = processor.getGrepExpressionItem().getStyle().getBackgroundColor();
		GrepColor foregroundColor = processor.getGrepExpressionItem().getStyle().getForegroundColor();
		color.setSelectedColor(backgroundColor.getColorAsAWT());
		color.setBorderColor(foregroundColor.getColorAsAWT());
		return color;
	}

	private void showToolWindow() {
		final ConsoleViewImpl consoleViewImpl = (ConsoleViewImpl) this.consoleView.get();
		if (consoleViewImpl != null) {
			activate(consoleViewImpl.getProject(), grepHighlightFilter.getExecutionId());
		}
	}

	private void activate(Project project, long executionId) {
		final RunContentManager runContentManager = ExecutionManager.getInstance(project).getContentManager();
		for (RunContentDescriptor descriptor : runContentManager.getAllDescriptors()) {
			if (descriptor.getExecutionId() == executionId) {
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
		OpenConsoleSettingsActionMouseInputAdapter mouseInputAdapter = new OpenConsoleSettingsActionMouseInputAdapter(
				consoleView.get(), getProject());
		final List<GrepProcessor> grepProcessors = grepHighlightFilter.getGrepProcessors();
		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isShowCountInStatusBar()) {
				add(grepProcessor, mouseInputAdapter);
			}
		}
	}

	public void add(GrepProcessor processor, final OpenConsoleSettingsActionMouseInputAdapter mouseInputAdapter) {
		jPanel.add(createItem(processor, mouseInputAdapter));
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

	protected abstract void hideStatusBar();

	private class ResetCountAction extends DumbAwareAction {
		public ResetCountAction() {
			super("Reset count");
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			StatisticsManager.clearCount(consoleView.get());
		}
	}

	private class HideAction extends DumbAwareAction {
		public HideAction() {
			super("Hide");
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			hideStatusBar();
		}
	}

}
