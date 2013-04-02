package krasa.grepconsole.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ButtonEditor<Item> extends DefaultCellEditor {
	protected JButton button;

	private String label;
	private Item item;

	private boolean isPushed;

	public ButtonEditor(JCheckBox checkBox) {
		super(checkBox);
		button = new JButton();
		button.setOpaque(true);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		item = (Item) value;
		if (isSelected) {
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
		} else {
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		setStyle(item);
		isPushed = true;
		return button;
	}

	protected void setStyle(Item item) {
		label = getLabel(item);
		button.setText(label);
	}

	protected String getLabel(Object value) {
		return (value == null) ? "" : value.toString();
	}

	public Object getCellEditorValue() {
		if (isPushed) {

		}
		isPushed = false;
		return label;
	}

	protected void onButtonClicked(Item item) {
		JOptionPane.showMessageDialog(button, label + ": Ouch!");
	}

	public boolean stopCellEditing() {
		isPushed = false;
		return super.stopCellEditing();
	}

	protected void fireEditingStopped() {
		super.fireEditingStopped();
		onButtonClicked(item);
	}
}
