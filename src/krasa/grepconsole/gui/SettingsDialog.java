package krasa.grepconsole.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.PluginState;

import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.rits.cloning.Cloner;

public class SettingsDialog {
	private JPanel rootComponent;
	private JTable table;
	private JButton addNewButton;
	private JButton resetToDefaultButton;
	private JCheckBox enableCheckBox;
	private JFormattedTextField maxLengthToMatch;
	private JCheckBox enableMaxLength;
	private JButton copyButton;
	private JButton deleteButton;
	private PluginState settings;
	protected ListTableModel<GrepExpressionItem> model;
	protected Integer selectedRow;

	public SettingsDialog(PluginState settings) {
		this.settings = settings;
		addNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selectNewRow = model.getRowCount() == 0;
				model.addRow(new GrepExpressionItem());
				if (selectNewRow) {
					table.getSelectionModel().setSelectionInterval(0, 0);
				}
			}
		});
		resetToDefaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsDialog.this.settings.setProfiles(PluginState.createDefault());
				model.setItems(getProfile().getGrepExpressionItems());
				disableCopyDeleteButton();
			}
		});
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GrepExpressionItem expressionItem = (GrepExpressionItem) new Cloner().deepClone(model.getItem(selectedRow));
				expressionItem.generateNewId();
				model.addRow(expressionItem);
			}
		});
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedRow < model.getRowCount()) {
					model.removeRow(selectedRow);
					table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
					if (model.getRowCount() == 0) {
						disableCopyDeleteButton();
					}
				}

			}
		});
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object source = e.getSource();
					if (source instanceof DefaultListSelectionModel) {
						setSelectedRow(((DefaultListSelectionModel) source).getLeadSelectionIndex());
					}

				}
			}
		});
		disableCopyDeleteButton();
	}

	private void disableCopyDeleteButton() {
		deleteButton.setEnabled(false);
		copyButton.setEnabled(false);
	}

	private void setSelectedRow(Integer selectedRow) {
		deleteButton.setEnabled(selectedRow != null);
		copyButton.setEnabled(selectedRow != null);
		this.selectedRow = selectedRow;
	}

	public JPanel getRootComponent() {
		return rootComponent;
	}

	public PluginState getSettings() {
		getData(getProfile());
		return settings;
	}

	private Profile getProfile() {
		return settings.getDefaultProfile();
	}

	public void importFrom(PluginState settings) {
		this.settings = settings;
		model.setItems(getProfile().getGrepExpressionItems());
		setData(settings.getDefaultProfile());
	}

	public boolean isSettingsModified(PluginState data) {
		getData(getProfile());
		return !this.settings.equals(data);
	}

	private void createUIComponents() {
		NumberFormatter numberFormatter = new NumberFormatter();
		numberFormatter.setMinimum(0);
		maxLengthToMatch = new JFormattedTextField(numberFormatter);
		System.err.println("");
		List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
		columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Expression", "grepExpression").preferedStringValue("_____________________________________________"));
		columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Unless expression", "unlessGrepExpression").preferedStringValue("___________________________"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Case insensitive", "caseInsensitive"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Bold", "style.bold"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Italic", "style.italic"));
		columns.add(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Background", "style.backgroundColor"));
		columns.add(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Foreground", "style.foregroundColor"));
		columns.add(new ButtonColumnInfo<GrepExpressionItem>("Up") {
			@Override
			void onButtonClicked(GrepExpressionItem grepExpressionItem) {
				int i = model.indexOf(grepExpressionItem);
				if (i > 0) {
					model.exchangeRows(i - 1, i);
					table.setRowSelectionInterval(i - 1, i - 1);
				}
			}
		}.width(50));
		columns.add(new ButtonColumnInfo<GrepExpressionItem>("Down") {
			@Override
			void onButtonClicked(GrepExpressionItem grepExpressionItem) {
				int i = model.indexOf(grepExpressionItem);
				if (i < model.getRowCount() - 1) {
					model.exchangeRows(i + 1, i);
					table.setRowSelectionInterval(i + 1, i + 1);
				}
			}
		}.width(60));

		List<GrepExpressionItem> grepExpressionItems = getProfile().getGrepExpressionItems();
		model = new ListTableModel<GrepExpressionItem>(columns.toArray(new ColumnInfo[columns.size()]),
				grepExpressionItems, 0);
		table = new TableView<GrepExpressionItem>(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void setData(Profile data) {
		enableCheckBox.setSelected(data.isEnabled());
		enableMaxLength.setSelected(data.isEnableMaxLengthLimit());
		maxLengthToMatch.setValue(data.getMaxLengthToMatch());
	}

	public void getData(Profile data) {
		data.setEnabled(enableCheckBox.isSelected());
		data.setEnableMaxLengthLimit(enableMaxLength.isSelected());
		try {
			data.setMaxLengthToMatch(Integer.valueOf(maxLengthToMatch.getText()));
		} catch (NumberFormatException e) {
		}
	}

	public boolean isModified(Profile data) {
		if (enableCheckBox.isSelected() != data.isEnabled())
			return true;
		if (enableMaxLength.isSelected() != data.isEnableMaxLengthLimit())
			return true;
		if (maxLengthToMatch.getText() != null ? !maxLengthToMatch.getText().equals(data.getMaxLengthToMatch())
				: data.getMaxLengthToMatch() != null)
			return true;
		return false;
	}
}
