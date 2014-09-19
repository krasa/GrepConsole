package krasa.grepconsole.gui.table.column;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class ButtonRenderer extends JButton implements TableCellRenderer {

	public ButtonRenderer() {
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(UIManager.getColor("Button.background"));
		}
		setStyle(value);
		return this;
	}

	protected void setStyle(Object value) {
		setText(getText(value));
	}

	protected String getText(Object value) {
		return (value == null) ? "" : value.toString();
	}
}
