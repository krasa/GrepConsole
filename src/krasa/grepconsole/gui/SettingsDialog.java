package krasa.grepconsole.gui;

import static krasa.grepconsole.Cloner.deepClone;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.DefaultState;
import krasa.grepconsole.plugin.PluginState;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

public class SettingsDialog {
	private JPanel rootComponent;
	private JTable table;
	private JButton addNewButton;
	private JButton resetToDefaultButton;
	private JCheckBox enableHighlightingCheckBox;
	private JFormattedTextField maxLengthToMatch;
	private JCheckBox enableMaxLength;
	private JButton copyButton;
	private JButton deleteButton;
	private JCheckBox enableFiltering;
	private JCheckBox ansi;
	private JCheckBox hideAnsiCharacters;
	private JCheckBox encodeText;
	private JCheckBox multilineOutput;
	private JButton DONATEButton;
	private PluginState settings;
	protected ListTableModel<GrepExpressionItem> model;
	protected Integer selectedRow;

	public SettingsDialog(final PluginState settings) {
		this.settings = settings;
		DONATEButton.setBorder(BorderFactory.createEmptyBorder());
		DONATEButton.setContentAreaFilled(false);
		DONATEButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Grep%20Console%20%2d%20IntelliJ%20plugin&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest");
			}
		});
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
				SettingsDialog.this.settings.setProfiles(DefaultState.createDefault());
				model.setItems(getProfile().getGrepExpressionItems());
				disableCopyDeleteButton();
				setData(getProfile());
			}
		});
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyRow();
			}
		});
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delete();

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
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
				} else if (SwingUtilities.isRightMouseButton(e)) {
					if (selectedRow == null) {
						return;
					}
					JPopupMenu popup = new JBPopupMenu();
					popup.add(getConvertAction());
					popup.add(getCopyAction());
					popup.add(getDeleteAction());
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			private JMenuItem getCopyAction() {
				final JMenuItem copy = new JMenuItem("Copy");
				copy.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						copyRow();
					}
				});
				return copy;
			}

			private JMenuItem getDeleteAction() {
				final JMenuItem delete = new JMenuItem("Delete");
				delete.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						delete();
					}
				});
				return delete;
			}

			private JMenuItem getConvertAction() {
				final GrepExpressionItem item = model.getItem(selectedRow);
				final boolean highlightOnlyMatchingText = item.isHighlightOnlyMatchingText();
				final JMenuItem convert = new JMenuItem(highlightOnlyMatchingText ? "Convert to whole line":"Convert to words only" );
			
				convert.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!highlightOnlyMatchingText) {
							model.getItem(selectedRow).setHighlightOnlyMatchingText(true);
							model.getItem(selectedRow).setContinueMatching(true);
							if (item.getGrepExpression().startsWith(".*")) {
								item.grepExpression(item.getGrepExpression().substring(2));
							}
							if (item.getGrepExpression().endsWith(".*")) {
								item.grepExpression(item.getGrepExpression().substring(0, item.getGrepExpression().length() - 2));
							}
						} else {
							model.getItem(selectedRow).setHighlightOnlyMatchingText(false);
							model.getItem(selectedRow).setContinueMatching(false);
							if (!item.getGrepExpression().startsWith(".*")) {
								item.grepExpression(".*" + item.getGrepExpression());
							}
							if (!item.getGrepExpression().endsWith(".*")) {
								item.grepExpression(item.getGrepExpression() + ".*");
							}
						}
						model.fireTableRowsUpdated(selectedRow, selectedRow);
					}
				});
				return convert;
			}
		});
		disableCopyDeleteButton();
		ansi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ansi.getModel().isSelected()) {
					hideAnsiCharacters.getModel().setSelected(true);
				}
			}
		});
	}

	private void delete() {
		if (selectedRow < model.getRowCount()) {
			model.removeRow(selectedRow);
			table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
			if (model.getRowCount() == 0) {
				disableCopyDeleteButton();
			}
		}
	}

	private void copyRow() {
		GrepExpressionItem expressionItem = deepClone(model.getItem(selectedRow));
		expressionItem.generateNewId();
		model.insertRow(selectedRow, expressionItem);
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
		List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Enabled", "enabled"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Filter out", "inputFilter"));
		columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Expression", "grepExpression").preferedStringValue("_____________________________________________"));
		columns.add(new JavaBeanColumnInfo<GrepExpressionItem, String>("Unless expression", "unlessGrepExpression").preferedStringValue("___________________________"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Case insensitive", "caseInsensitive"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Bold", "style.bold"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Italic", "style.italic"));
		columns.add(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Background", "style.backgroundColor"));
		columns.add(new ColorChooserJavaBeanColumnInfo<GrepExpressionItem>("Foreground", "style.foregroundColor"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Continue matching", "continueMatching"));
		columns.add(new CheckBoxJavaBeanColumnInfo<GrepExpressionItem, String>("Highlight only matching text",
				"highlightOnlyMatchingText"));
		columns.add(new SoundColumn("Sound", this));

		List<GrepExpressionItem> grepExpressionItems = getProfile().getGrepExpressionItems();
		model = new ListTableModel<GrepExpressionItem>(columns.toArray(new ColumnInfo[columns.size()]),
				grepExpressionItems, 0);
		table = new TableView<GrepExpressionItem>(model);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void setData(Profile data) {
		maxLengthToMatch.setText(data.getMaxLengthToMatch());
		enableMaxLength.setSelected(data.isEnableMaxLengthLimit());
		ansi.setSelected(data.isEnableAnsiColoring());
		hideAnsiCharacters.setSelected(data.isHideAnsiCommands());
		encodeText.setSelected(data.isEncodeText());
		enableFiltering.setSelected(data.isEnabledInputFiltering());
		enableHighlightingCheckBox.setSelected(data.isEnabledHighlighting());
		multilineOutput.setSelected(data.isMultiLineOutput());
	}

	public void getData(Profile data) {
		data.setMaxLengthToMatch(maxLengthToMatch.getText());
		data.setEnableMaxLengthLimit(enableMaxLength.isSelected());
		data.setEnableAnsiColoring(ansi.isSelected());
		data.setHideAnsiCommands(hideAnsiCharacters.isSelected());
		data.setEncodeText(encodeText.isSelected());
		data.setEnabledInputFiltering(enableFiltering.isSelected());
		data.setEnabledHighlighting(enableHighlightingCheckBox.isSelected());
		data.setMultiLineOutput(multilineOutput.isSelected());
	}

	public boolean isModified(Profile data) {
		if (maxLengthToMatch.getText() != null ? !maxLengthToMatch.getText().equals(data.getMaxLengthToMatch())
				: data.getMaxLengthToMatch() != null)
			return true;
		if (enableMaxLength.isSelected() != data.isEnableMaxLengthLimit())
			return true;
		if (ansi.isSelected() != data.isEnableAnsiColoring())
			return true;
		if (hideAnsiCharacters.isSelected() != data.isHideAnsiCommands())
			return true;
		if (encodeText.isSelected() != data.isEncodeText())
			return true;
		if (enableFiltering.isSelected() != data.isEnabledInputFiltering())
			return true;
		if (enableHighlightingCheckBox.isSelected() != data.isEnabledHighlighting())
			return true;
		if (multilineOutput.isSelected() != data.isMultiLineOutput())
			return true;
		return false;
	}
}
