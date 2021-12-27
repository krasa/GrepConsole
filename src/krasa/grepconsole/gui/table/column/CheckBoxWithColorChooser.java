/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package krasa.grepconsole.gui.table.column;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.ColorChooser;
import krasa.grepconsole.model.GrepColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Konstantin Bulenkov
 */
public class CheckBoxWithColorChooser extends JPanel {
	private final JCheckBox myCheckbox;
	protected MyColorButton myColorButton;
	private Color myColor;
	private GrepColor originalGrepColor;

	public CheckBoxWithColorChooser(String text, GrepColor originalGrepColor) {
		this.originalGrepColor = originalGrepColor;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		Color colorAsAWT = originalGrepColor.getColorAsAWT();
		if (colorAsAWT == null) {
			colorAsAWT = Color.BLACK;
		}
		myColor = colorAsAWT;
		myCheckbox = new JCheckBox(text, originalGrepColor.isEnabled());
		add(myCheckbox);
		myColorButton = new MyColorButton();
		add(myColorButton);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				myColorButton.mouseAdapter.mousePressed(e);
			}
		});
	}

	public GrepColor getOriginalGrepColor() {
		return originalGrepColor;
	}

	public void setMnemonic(char c) {
		myCheckbox.setMnemonic(c);
	}

	public Color getColor() {
		return myColor;
	}

	public void setColor(Color color) {
		myColor = color;
	}

	public boolean isSelected() {
		return myCheckbox.isSelected();
	}

	public void setSelected(boolean selected) {
		myCheckbox.setSelected(selected);
	}

	public void onColorChanged() {

	}

	private class MyColorButton extends JButton {
		protected MouseAdapter mouseAdapter;

		MyColorButton() {
			setMargin(new Insets(0, 0, 0, 0));
			setFocusable(false);
			setDefaultCapable(false);
			setFocusable(false);
			if (SystemInfo.isMac) {
				putClientProperty("JButton.buttonType", "square");
			}

			// final ClickListener clickListener = new ClickListener() {
			// @Override
			// public boolean onClick(@NotNull MouseEvent e, int clickCount) {
			// final Color color = ColorChooser.chooseColor(myCheckbox, "Chose color",
			// CheckBoxWithColorChooser.this.myColor);
			// if (color != null) {
			// if (!myCheckbox.isSelected()) {
			// myCheckbox.setSelected(true);
			// }
			// myColor = color;
			// }
			// return true;
			// }
			// };
			// clickListener.installOn(this);
			mouseAdapter = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					final Color color = ColorChooser.chooseColor(myCheckbox, "Chose color",
							CheckBoxWithColorChooser.this.myColor);
					if (color != null) {
						if (!myCheckbox.isSelected()) {
							myCheckbox.setSelected(true);
						}
						myColor = color;
						onColorChanged();
					}
				}
			};
			addMouseListener(mouseAdapter);
			;
		}

		@Override
		public void paint(Graphics g) {
			final Color color = g.getColor();
			g.setColor(myColor);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(color);
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(12, 12);
		}
	}
}
