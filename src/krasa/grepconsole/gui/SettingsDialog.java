package krasa.grepconsole.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

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
	private PluginState settings;
	protected ListTableModel<GrepExpressionItem> model;

	public SettingsDialog(PluginState settings) {
		this.settings = settings;
		addNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.addRow(new GrepExpressionItem());
			}
		});
		resetToDefaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsDialog.this.settings.setProfiles(PluginState.createDefault());
				model.setItems(getProfile().getGrepExpressionItems());
			}
		});
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

	private void createUIComponents() {
		List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
		columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Expression", "grepExpression"));
		columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Unless expression", "unlessGrepExpression"));
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
		});
		columns.add(new ButtonColumnInfo<GrepExpressionItem>("Down") {
			@Override
			void onButtonClicked(GrepExpressionItem grepExpressionItem) {
				int i = model.indexOf(grepExpressionItem);
				if (i < model.getRowCount() - 1) {
					model.exchangeRows(i + 1, i);
					table.setRowSelectionInterval(i + 1, i + 1);
				}
			}
		});
		columns.add(new ButtonColumnInfo<GrepExpressionItem>("Copy") {
			@Override
			void onButtonClicked(GrepExpressionItem grepExpressionItem) {
				model.addRow((GrepExpressionItem) new Cloner().deepClone(grepExpressionItem).generateNewId());
			}
		});
		columns.add(new ButtonColumnInfo<GrepExpressionItem>("Delete") {
			@Override
			void onButtonClicked(GrepExpressionItem grepExpressionItem) {
				model.removeRow(model.indexOf(grepExpressionItem));
			}
		});

		List<GrepExpressionItem> grepExpressionItems = getProfile().getGrepExpressionItems();
		model = new ListTableModel<GrepExpressionItem>(columns.toArray(new ColumnInfo[columns.size()]),
				grepExpressionItems, 0);
		table = new TableView<GrepExpressionItem>(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public boolean isSettingsModified(PluginState data) {
		getData(getProfile());
		return !this.settings.equals(data);

	}

	public void setData(Profile data) {
		enableCheckBox.setSelected(data.isEnabled());
	}

	public void getData(Profile data) {
		data.setEnabled(enableCheckBox.isSelected());
	}

	public boolean isModified(Profile data) {
		if (enableCheckBox.isSelected() != data.isEnabled())
			return true;
		return false;
	}
}
