package krasa.grepconsole.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.intellij.ui.table.TableView;
import com.rits.cloning.Cloner;
import krasa.grepconsole.plugin.PluginSettings;
import krasa.grepconsole.model.GrepExpressionItem;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

public class SettingsDialog {
	private JPanel rootComponent;
	private JTable table;
	private JButton addNewButton;
	private PluginSettings settings;
    protected ListTableModel<GrepExpressionItem> model;

    public SettingsDialog(PluginSettings settings) {
		this.settings = settings;
		addNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getGrepExpressionItems().add(new GrepExpressionItem());
				model.fireTableDataChanged();
			}
		});
	}

	public JPanel getRootComponent() {
		return rootComponent;
	}

	public boolean isModified(PluginSettings settings) {
		return !this.settings.equals(settings);
	}

    public PluginSettings getSettings() {
        return settings;
    }

    public void importFrom(PluginSettings settings) {
        this.settings= settings;
        model.setItems(settings.getDefaultProfile().getGrepExpressionItems());
    }

	private void createUIComponents() {
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Expression", "grepExpression"));
//        columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Unless expression", "unlessGrepExpression"));
        columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Case insensitive", "caseInsensitive"));
        columns.add(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Background", "style.backgroundColor"));
        columns.add(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Foreground", "style.foregroundColor"));
        columns.add(new ButtonColumnInfo<GrepExpressionItem>("Delete") {
			@Override
			void onButtonClicked(GrepExpressionItem grepExpressionItem) {
				getGrepExpressionItems().remove(grepExpressionItem);
				model.fireTableDataChanged();
			}
		});        columns.add(new ButtonColumnInfo<GrepExpressionItem>("Copy") {
			@Override
			void onButtonClicked(GrepExpressionItem grepExpressionItem) {
				GrepExpressionItem e = (GrepExpressionItem) new Cloner().deepClone(grepExpressionItem).generateNewId();
				getGrepExpressionItems().add(e);
				model.fireTableDataChanged();
			}
		});
        List<GrepExpressionItem> grepExpressionItems = getGrepExpressionItems();
        model = new ListTableModel<GrepExpressionItem>(columns.toArray(new ColumnInfo[columns.size()]), grepExpressionItems, 0);
        table = new TableView<GrepExpressionItem>(model);
    }

	private List<GrepExpressionItem> getGrepExpressionItems() {
		return settings.getDefaultProfile().getGrepExpressionItems();
	}
}
