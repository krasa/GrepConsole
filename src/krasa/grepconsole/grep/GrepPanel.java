package krasa.grepconsole.grep;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.ui.TextFieldWithStoredHistory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;

public class GrepPanel extends JPanel implements Disposable {
	@Nullable
	private ConsoleViewImpl originalConsole;
	private final ConsoleViewImpl newConsole;
	private final GrepCopyingListener copyingListener;
	private final RunnerLayoutUi runnerLayoutUi;
	private TextFieldWithStoredHistory expression;
	private TextFieldWithStoredHistory unlessExpression;
	private OpenGrepConsoleAction.ApplyCallback applyCallback;
	private JBCheckBox caseSensitive;
	private JButton reload;
	private JButton source;
	private JButton apply;

	public GrepPanel(ConsoleViewImpl originalConsole, ConsoleViewImpl newConsole, GrepCopyingListener copyingListener,
			String expression, RunnerLayoutUi runnerLayoutUi) {
		this.originalConsole = originalConsole;
		this.newConsole = newConsole;
		this.copyingListener = copyingListener;
		this.runnerLayoutUi = runnerLayoutUi;
		setBorder(new EmptyBorder(0, 0, 0, 0));
		final FlowLayout layout = new FlowLayout();
		layout.setVgap(0);
		layout.setAlignment(FlowLayout.LEFT);
		// layout.setHgap(0);
		setLayout(layout);
		init(expression);
	}

	public void reset() {
		init(".*");
	}

	private void init(String expression) {
		add(new JLabel("Expression: "));
		this.expression = new TextFieldWithStoredHistory("ConsoleQuickFilterPanel-expression");
		this.expression.setText(expression);
		this.expression.setBorder(JBUI.Borders.empty());
		this.expression.setMinimumAndPreferredWidth(300);
		add(this.expression);
		add(new JLabel("Unless: "));
		unlessExpression = new TextFieldWithStoredHistory("ConsoleQuickFilterPanel-unlessExpression");
		unlessExpression.setMinimumAndPreferredWidth(150);
		this.unlessExpression.setBorder(JBUI.Borders.empty());
		add(unlessExpression);
		caseSensitive = new JBCheckBox("Case Sensitive", false);
		add(caseSensitive);
		addButtons();
	}

	public void addButtons() {
		apply = newButton("Apply", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply(expression.getText(), unlessExpression.getText());

			}
		});
		add(apply);
		reload = newButton("Reload", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply(expression.getText(), unlessExpression.getText());
				newConsole.clear();
				if (originalConsole != null) {
					Editor editor = originalConsole.getEditor();
					Document document = editor.getDocument();
					String text = document.getText();
					for (String s : text.split("\n")) {
						copyingListener.process(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
					}
				}
			}
		});
		add(reload);
		source = newButton("Source", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Content[] contents = runnerLayoutUi.getContents();
				for (Content content : contents) {
					JComponent component = content.getComponent();
					if (component == originalConsole) {
						runnerLayoutUi.selectAndFocus(content, true, true);
					} else if (component.getClass() == OpenGrepConsoleAction.MyJPanel.class) {
						JPanel s = (JPanel) component;
						if (s.getComponentZOrder(originalConsole) != -1) {
							runnerLayoutUi.selectAndFocus(content, true, true);
						}

					}
				}

			}
		});
		add(source);
	}

	protected JButton newButton(String s, ActionListener l) {
		JButton jButton = new JButton(s);
		Dimension oldSize = jButton.getPreferredSize();
		JBDimension newSize = new JBDimension(oldSize.width, oldSize.height - 5);
		jButton.setPreferredSize(newSize);
		jButton.addActionListener(l);
		return jButton;
	}

	public void apply(String text, String unlessExpressionText) {
		if (applyCallback != null) {
			if (applyCallback.apply(caseSensitive.isSelected(), text, unlessExpressionText)) {
				expression.addCurrentTextToHistory();
				unlessExpression.addCurrentTextToHistory();
			}
		}

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

	@Override
	public void dispose() {
		originalConsole = null;
		apply.setEnabled(false);
		reload.setEnabled(false);
		source.setEnabled(false);
	}

	public void setApplyCallback(OpenGrepConsoleAction.ApplyCallback applyCallback) {
		this.applyCallback = applyCallback;
	}
}
