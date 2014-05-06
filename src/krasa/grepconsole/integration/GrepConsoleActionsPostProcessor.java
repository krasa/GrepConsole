package krasa.grepconsole.integration;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;

import krasa.grepconsole.action.OpenConsoleSettingsAction;
import krasa.grepconsole.filter.AnsiInputFilter;
import krasa.grepconsole.plugin.ServiceManager;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.PlatformColors;
import com.intellij.util.ui.UIUtil;

public class GrepConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		AnsiInputFilter lastAnsi = ServiceManager.getInstance().getLastAnsi();
		if (lastAnsi != null) {
			lastAnsi.setConsole(console);
		}
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new OpenConsoleSettingsAction(console));
		anActions.addAll(Arrays.asList(actions));

		javax.swing.JPanel jPanel = (javax.swing.JPanel) console;
		final StatisticsPanel comp = new StatisticsPanel();
		jPanel.add(comp, BorderLayout.SOUTH);

		return anActions.toArray(new AnAction[anActions.size()]);
	}

	public class StatisticsPanel extends JPanel {

		public StatisticsPanel() {
			super(new BorderLayout());
			setBorder(new EmptyBorder(0, 0, 0, 0));

			final FlowLayout layout = new FlowLayout();
			layout.setVgap(-5);
			layout.setHgap(0);
			final JPanel jPanel = new JPanel(layout);
			jPanel.setBackground(getBackground());

			jPanel.add(getjPanel(Color.BLUE, "312421421421"));
			jPanel.add(getjPanel(Color.RED, "4"));
			jPanel.add(getjPanel(Color.YELLOW, "4"));
			jPanel.add(createActionLabel("Reset", new Runnable() {
				@Override
				public void run() {
					System.err.println("www");
				}
			}));
			add(jPanel, BorderLayout.WEST);
		}

		private JPanel getjPanel(final Color blue, final String text) {
			final ColorPanel comp = new ColorPanel();
			comp.setSelectedColor(blue);
			final JLabel comp1 = new JLabel(text);
			comp1.setForeground(Color.WHITE);

			final JPanel jPanel = new JPanel(new FlowLayout());
			jPanel.setBackground(getBackground());
			comp.setBackground(getBackground());
			jPanel.add(comp);
			jPanel.add(comp1);
			return jPanel;
		}

		@Override
		public Color getBackground() {
			Color color = EditorColorsManager.getInstance().getGlobalScheme().getColor(
					EditorColors.NOTIFICATION_BACKGROUND);
			return color == null ? UIUtil.getToolTipBackground() : color;
		}

		public HyperlinkLabel createActionLabel(final String text, @NonNls final String actionId) {
			return createActionLabel(text, new Runnable() {
				@Override
				public void run() {
					executeAction(actionId);
				}
			});
		}

		public HyperlinkLabel createActionLabel(final String text, final Runnable action) {
			HyperlinkLabel label = new HyperlinkLabel(text, Color.WHITE, getBackground(), PlatformColors.BLUE);
			label.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				protected void hyperlinkActivated(HyperlinkEvent e) {
					action.run();
				}
			});

			return label;
		}

		protected void executeAction(final String actionId) {
			final AnAction action = ActionManager.getInstance().getAction(actionId);
			final AnActionEvent event = new AnActionEvent(null, DataManager.getInstance().getDataContext(this),
					ActionPlaces.UNKNOWN, action.getTemplatePresentation(), ActionManager.getInstance(), 0);
			action.beforeActionPerformedUpdate(event);
			action.update(event);

			if (event.getPresentation().isEnabled() && event.getPresentation().isVisible()) {
				action.actionPerformed(event);
			}
		}

		@Override
		public Dimension getMinimumSize() {
			return new Dimension(0, 0);
		}
	}

}
