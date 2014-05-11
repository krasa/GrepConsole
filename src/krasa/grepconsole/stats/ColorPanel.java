package krasa.grepconsole.stats;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

public class ColorPanel extends JComponent {
	private static final Dimension SIZE = new Dimension(15, 15);
	protected final Dimension size;

	private boolean isFiringEvent = false;
	private boolean isEditable = true;
	private final List<ActionListener> myListeners = new CopyOnWriteArrayList<ActionListener>();
	@Nullable
	private Color myColor = null;
	private Color borderColor = null;
	private String tooltip;

	public ColorPanel(String tooltip) {
		this.tooltip = tooltip;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				onMousePressed(e);
			}

		});
		// addMouseListener(new MouseAdapter() {
		// @Override
		// public void mousePressed(MouseEvent e) {
		// if (!isEnabled() || !isEditable)
		// return;
		// Color color = ColorChooser.chooseColor(ColorPanel.this,
		// UIBundle.message("color.panel.select.color.dialog.description"), myColor);
		// if (color != null) {
		// setSelectedColor(color);
		// fireActionEvent();
		// }
		// }
		// });
		size = SIZE;
	}

	public ColorPanel(String tooltip, final Dimension dimension) {
		this.tooltip = tooltip;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				onMousePressed(e);
			}

		});
		// addMouseListener(new MouseAdapter() {
		// @Override
		// public void mousePressed(MouseEvent e) {
		// if (!isEnabled() || !isEditable)
		// return;
		// Color color = ColorChooser.chooseColor(ColorPanel.this,
		// UIBundle.message("color.panel.select.color.dialog.description"), myColor);
		// if (color != null) {
		// setSelectedColor(color);
		// fireActionEvent();
		// }
		// }
		// });
		size = dimension;
	}

	protected void onMousePressed(MouseEvent e) {
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		try {
			if (myColor != null && isEnabled()) {
				g2d.setColor(myColor);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				g2d.draw(new Rectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1));
			}
			g2d.setColor(borderColor);
			g2d.draw(new Rectangle2D.Double(1.5, 1.5, getWidth() - 3, getHeight() - 3));
		} finally {
			g2d.dispose();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return size;
	}

	@Override
	public Dimension getMaximumSize() {
		return size;
	}

	@Override
	public Dimension getMinimumSize() {
		return size;
	}

	@Override
	public String getToolTipText() {
		// if (myColor == null || !isEnabled()) {
		// return null;
		// }
		// StringBuilder buffer = new StringBuilder("0x").append(ColorUtil.toHex(myColor).toUpperCase());
		// if (isEnabled() && isEditable) {
		// buffer.append(" (Click to customize)");
		// }
		return tooltip;
	}

	private void fireActionEvent() {
		if (!isEditable)
			return;
		if (!isFiringEvent) {
			try {
				isFiringEvent = true;
				ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "colorPanelChanged");
				for (ActionListener listener : myListeners) {
					listener.actionPerformed(event);
				}
			} finally {
				isFiringEvent = false;
			}
		}
	}

	public void removeActionListener(ActionListener actionlistener) {
		myListeners.remove(actionlistener);
	}

	public void addActionListener(ActionListener actionlistener) {
		myListeners.add(actionlistener);
	}

	@Nullable
	public Color getSelectedColor() {
		return myColor;
	}

	public void setSelectedColor(@Nullable Color color) {
		myColor = color;
		repaint();
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}
}
