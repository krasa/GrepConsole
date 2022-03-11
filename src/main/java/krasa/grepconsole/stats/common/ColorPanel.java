package krasa.grepconsole.stats.common;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

public class ColorPanel extends JComponent {
	private static final Dimension SIZE = new Dimension(15, 15);
	protected final Dimension size;

	@Nullable
	private Color myColor = null;
	private Color borderColor = null;
	private String tooltip;

	public ColorPanel(String tooltip) {
		this(tooltip, SIZE);
	}

	public ColorPanel(String tooltip, final Dimension dimension) {
		this.tooltip = tooltip;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				onMousePressed(e);
			}

		});
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
		return tooltip;
	}

	public void setSelectedColor(@Nullable Color color) {
		myColor = color;
		repaint();
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

}
