package krasa.grepconsole.stats;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SeparatorComponent;
import krasa.grepconsole.action.MyDumbAwareAction;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.stats.common.ColorPanel;
import krasa.grepconsole.utils.FocusUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static com.intellij.ui.ColorUtil.brighter;

/**
 * @author Vojtech Krasa
 */
public abstract class StatisticsStatusBarPanel extends JPanel {
	private final JPanel jPanel;
	private List<Pair<JLabel, GrepProcessor>> pairs = new ArrayList<>();
	private java.util.Timer timer;
	private WeakReference<ConsoleView> consoleView;
	private HighlightingFilter highlightingFilter;
	private static Color[] bgs = getColors();
	static int i = 5;

	public StatisticsStatusBarPanel(ConsoleView consoleView, HighlightingFilter filter) {
		super(new BorderLayout());
		this.consoleView = new WeakReference<>(consoleView);
		this.highlightingFilter = filter;
		add(new SeparatorComponent(7), BorderLayout.WEST);
		final FlowLayout layout = new FlowLayout();
		layout.setVgap(0);
		layout.setHgap(4);
		jPanel = new JPanel(new GridBagLayout());
		jPanel.setBorder(new LineBorder(Color.BLACK, 1));
		setBackground(getBgColor());
		jPanel.setBackground(getBackground());
		
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
				StatisticsStatusBarPanel.this.mouse(e);
			}
		});
	}

	@NotNull
	public static Color getBgColor() {
		if (++i >= bgs.length) {
			i = 0;
		}
		return bgs[i];
	}

	@NotNull
	public static Color[] getColors() {
		Color[] bgs;
		if (!JBColor.isBright()) {
			bgs = new Color[]{
					new Color(114, 120, 125),
					new Color(255, 255, 255),
					new Color(105, 175, 128),					
					new Color(248, 209, 72),
					new Color(207, 83, 41),
					new Color(204, 114, 189),
					new Color(2, 169, 226),
			};
		} else {
			bgs = new Color[]{
					new Color(232, 232, 232),
					new Color(162, 162, 162),
					new Color(105, 175, 128),
					new Color(248, 209, 72),
					new Color(207, 83, 41),
					new Color(204, 114, 189),
					new Color(2, 169, 226)
			};
		}
		return bgs;
	}

	public static Color darkerDarkulaBrighterIJ(Color orange) {
		if (!JBColor.isBright()) {
			return orange.darker().darker();
		} else {
			return brighter(orange, 1);
		}
	}

	public static Color brighterDarkulaDarkerIJ(Color orange) {
		if (!JBColor.isBright()) {
			return brighter(orange, 1);
		} else {
			return orange.darker().darker();
		}
	}

	private void showPopup(Component comp, int x, int y) {
		final ActionGroup actionGroup = new ActionGroup() {
			@NotNull
			@Override
			public AnAction[] getChildren(@Nullable AnActionEvent e) {
				return new AnAction[]{new ResetAction(), new HideAction()};
			}
		};
		ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent().show(comp, x, y);
	}

	private JPanel createItem(GrepProcessor processor,
							  final MouseInputAdapter mouseInputAdapter) {
		final JPanel panel = getItemPanel();
		ColorPanel colorPanel = getColorPanel(processor);
		JLabel label = getLabel(processor);
		colorPanel.addMouseListener(mouseInputAdapter);
		label.addMouseListener(mouseInputAdapter);
		panel.add(colorPanel);
		panel.add(label);
		panel.setBackground(null);
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
		label.setForeground(Color.BLACK);
		pairs.add(new Pair<>(label, processor));
		return label;
	}

	private krasa.grepconsole.stats.common.ColorPanel getColorPanel(final GrepProcessor processor) {
		final krasa.grepconsole.stats.common.ColorPanel color = new krasa.grepconsole.stats.common.ColorPanel(
				processor.getGrepExpressionItem().getGrepExpression(), new Dimension(14, 14)) {
			@Override
			protected void onMousePressed(MouseEvent e) {
				mouse(e);
			}
		};

		GrepColor backgroundColor = processor.getGrepExpressionItem().getStyle().getBackgroundColor();
		GrepColor foregroundColor = processor.getGrepExpressionItem().getStyle().getForegroundColor();
		color.setSelectedColor(backgroundColor.getColorAsAWT());
		color.setBorderColor(foregroundColor.getColorAsAWT());
		return color;
	}

	private void mouse(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			showPopup(e.getComponent(), e.getX(), e.getY());
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			StatisticsStatusBarPanel.this.hideStatusBar();
		} else {
			showToolWindow();
		}
	}

	private void showToolWindow() {
		final ConsoleViewImpl consoleViewImpl = (ConsoleViewImpl) this.consoleView.get();
		if (consoleViewImpl != null) {
			FocusUtils.navigate(getProject(), consoleViewImpl);
		}
	}

	public void reset() {
		pairs.clear();
		jPanel.removeAll();
		initComponents();
	}

	public Project getProject() {
		return highlightingFilter.getProject();
	}

	private void initComponents() {
		MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				mouse(e);
			}
		};
		final List<GrepProcessor> grepProcessors = highlightingFilter.getGrepProcessors();
		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isShowCountInStatusBar()) {
				add(grepProcessor, mouseInputAdapter);
			}
		}
	}

	public void add(GrepProcessor processor, final MouseInputAdapter mouseInputAdapter) {
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

	private class ResetAction extends MyDumbAwareAction {
		public ResetAction() {
			super("Reset");
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			StatisticsManager.clearCount(consoleView.get());
			reset();
		}
	}

	private class HideAction extends MyDumbAwareAction {
		public HideAction() {
			super("Hide");
		}

		@Override
		public void actionPerformed(AnActionEvent e) {
			hideStatusBar();
		}
	}

}
