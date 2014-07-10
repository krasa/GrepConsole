package krasa.grepconsole.gui;

import static krasa.grepconsole.Cloner.deepClone;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.*;

import krasa.grepconsole.gui.table.*;
import krasa.grepconsole.model.*;
import krasa.grepconsole.plugin.*;
import krasa.grepconsole.tail.TailIntegrationForm;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.*;
import com.intellij.ui.*;
import com.intellij.util.ui.ListTableModel;

public class SettingsDialog {
	private static final Logger log = Logger.getInstance(SettingsDialog.class);
	protected ListTableModel<GrepExpressionItem> model;
	protected Integer selectedRow;
	private JPanel rootComponent;
	private CheckboxTreeTable table;
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
	private JCheckBox showStatsInConsoleByDefault;
	private JCheckBox showStatsInStatusBarByDefault;
	private JButton fileTailSettings;
	private PluginState settings;

	public SettingsDialog(PluginState settings) {
		this.settings = settings;
		model = new ListTableModel<GrepExpressionItem>();// TODO
		DONATEButton.setBorder(BorderFactory.createEmptyBorder());
		DONATEButton.setContentAreaFilled(false);
		DONATEButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Grep%20Console%20%2d%20IntelliJ%20plugin&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest");
			}
		});
		addNewButton.addActionListener(new AddAction());
		resetToDefaultButton.addActionListener(new ResetToDefaultAction());
		copyButton.addActionListener(new CopyAction());
		deleteButton.addActionListener(new DeleteAction());
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
		table.addMouseListener(rightClickMenu());
		disableCopyDeleteButton();
		ansi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ansi.getModel().isSelected()) {
					hideAnsiCharacters.getModel().setSelected(true);
				}
			}
		});

		fileTailSettings.addActionListener(new FileTailSettings());
	}

	public MouseAdapter rightClickMenu() {
		return new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
				} else if (SwingUtilities.isRightMouseButton(e)) {
					if (getSelectedNode() == null) {
						return;
					}
					JPopupMenu popup = new JBPopupMenu();
					GrepExpressionItem selectedGrepExpressionItem = getSelectedGrepExpressionItem();
					if (selectedGrepExpressionItem != null) {
						popup.add(getConvertAction(selectedGrepExpressionItem));
					}
					popup.add(newMenuItem("Add", new AddAction()));
					popup.add(newMenuItem("Copy", new CopyAction()));
					popup.add(newMenuItem("Delete", new DeleteAction()));
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			private JMenuItem newMenuItem(String name, ActionListener l) {
				final JMenuItem item = new JMenuItem(name);
				item.addActionListener(l);
				return item;
			}

			private JMenuItem getConvertAction(final GrepExpressionItem item) {
				final boolean highlightOnlyMatchingText = item.isHighlightOnlyMatchingText();
				final JMenuItem convert = new JMenuItem(highlightOnlyMatchingText ? "Convert to whole line"
						: "Convert to words only");

				convert.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!highlightOnlyMatchingText) {
							getSelectedGrepExpressionItem().setHighlightOnlyMatchingText(true);
							getSelectedGrepExpressionItem().setContinueMatching(true);
							if (item.getGrepExpression().startsWith(".*")) {
								item.grepExpression(item.getGrepExpression().substring(2));
							}
							if (item.getGrepExpression().endsWith(".*")) {
								item.grepExpression(item.getGrepExpression().substring(0,
										item.getGrepExpression().length() - 2));
							}
						} else {
							getSelectedGrepExpressionItem().setHighlightOnlyMatchingText(false);
							getSelectedGrepExpressionItem().setContinueMatching(false);
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
		};
	}

	private GrepExpressionItem getSelectedGrepExpressionItem() {
		DefaultMutableTreeNode selectedNode = getSelectedNode();
		GrepExpressionItem item = null;
		if (selectedNode instanceof GrepExpressionItemTreeNode) {
			item = (GrepExpressionItem) selectedNode.getUserObject();
		}
		return item;
	}

	public DefaultMutableTreeNode getSelectedNode() {
		return (DefaultMutableTreeNode) table.getTree().getLastSelectedPathComponent();
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

	public Profile getProfile() {
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
		fileTailSettings = new JButton();
		NumberFormatter numberFormatter = new NumberFormatter();
		numberFormatter.setMinimum(0);
		maxLengthToMatch = new JFormattedTextField(numberFormatter);
		table = new SettingsTableBuilder(this).getTable();
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
		showStatsInConsoleByDefault.setSelected(data.isShowStatsInConsoleByDefault());
		showStatsInStatusBarByDefault.setSelected(data.isShowStatsInStatusBarByDefault());
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
		data.setShowStatsInConsoleByDefault(showStatsInConsoleByDefault.isSelected());
		data.setShowStatsInStatusBarByDefault(showStatsInStatusBarByDefault.isSelected());
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
		if (showStatsInConsoleByDefault.isSelected() != data.isShowStatsInConsoleByDefault())
			return true;
		if (showStatsInStatusBarByDefault.isSelected() != data.isShowStatsInStatusBarByDefault())
			return true;
		return false;
	}

	private GrepExpressionItem newItem(DefaultMutableTreeNode parent) {
		GrepExpressionGroup group = getGrepExpressionGroup(parent);
		GrepExpressionItem userObject = new GrepExpressionItem();
		userObject.setGrepExpression("foo");
		userObject.setEnabled(true);
		userObject.setContinueMatching(true);
		userObject.setHighlightOnlyMatchingText(true);
		userObject.getStyle().setBackgroundColor(new GrepColor(true, JBColor.CYAN));
		GrepExpressionItem newItem = userObject;
		group.add(newItem);
		return newItem;
	}

	private GrepExpressionGroup getGrepExpressionGroup(DefaultMutableTreeNode selectedNode) {
		return (GrepExpressionGroup) selectedNode.getUserObject();
	}

	private class AddAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selectedNode = getSelectedNode();

			CheckedTreeNode newChild;
			if (selectedNode == null) {
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
				newChild = new GroupTreeNode(new GrepExpressionGroup("new", new ArrayList<GrepExpressionItem>()));
				root.add(newChild);
			} else if (selectedNode.getUserObject() instanceof GrepExpressionGroup) {
				GrepExpressionItem newItem = newItem(selectedNode);
				newChild = new GrepExpressionItemTreeNode(newItem);
				selectedNode.add(newChild);
			} else {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				GrepExpressionItem newItem = newItem(parent);
				newChild = new GrepExpressionItemTreeNode(newItem);
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);
			}
			TableUtils.reloadTree(table);
			TableUtils.selectNode(newChild, table);
		}
	}

	private class ResetToDefaultAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsDialog.this.settings.setProfiles(DefaultState.createDefault());
			model.setItems(getProfile().getGrepExpressionItems());
			disableCopyDeleteButton();
			setData(getProfile());
		}
	}

	private class CopyAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selectedNode = getSelectedNode();
			if (selectedNode instanceof GrepExpressionItemTreeNode) {
				GrepExpressionItem expressionItem = copy(selectedNode);
				GrepExpressionItemTreeNode newChild = new GrepExpressionItemTreeNode(expressionItem);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);
				TableUtils.reloadTree(SettingsDialog.this.table);
				TableUtils.selectNode(newChild, SettingsDialog.this.table);
			} else if (selectedNode instanceof GroupTreeNode) {
				GrepExpressionGroup group = deepClone((GrepExpressionGroup) selectedNode.getUserObject());
				GroupTreeNode newChild = new GroupTreeNode(group);
				for (GrepExpressionItem grepExpressionItem : group.getGrepExpressionItems()) {
					grepExpressionItem.generateNewId();
					newChild.add(new GrepExpressionItemTreeNode(grepExpressionItem));
				}
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);
				TableUtils.reloadTree(SettingsDialog.this.table);
				TableUtils.selectNode(newChild, SettingsDialog.this.table);
			}
		}

		private GrepExpressionItem copy(DefaultMutableTreeNode selectedNode) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
			GrepExpressionItem expressionItem = deepClone((GrepExpressionItem) selectedNode.getUserObject());
			expressionItem.generateNewId();
			GrepExpressionGroup grepExpressionGroup = getGrepExpressionGroup(parent);
			grepExpressionGroup.add(expressionItem);
			return expressionItem;
		}
	}

	private class DeleteAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selectedNode = getSelectedNode();
			if (selectedNode != null) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.remove(selectedNode);
				TableUtils.reloadTree(SettingsDialog.this.table);
			}
		}
	}

	private class FileTailSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final TailIntegrationForm form = new TailIntegrationForm();
			form.setData(settings.getTailSettings());

			DialogBuilder builder = new DialogBuilder(SettingsDialog.this.getRootComponent());
			builder.setCenterPanel(form.getRoot());
			builder.setDimensionServiceKey("GrepConsoleTailFileDialog");
			builder.setTitle("Tail File settings");
			builder.removeAllActions();
			builder.addOkAction();
			builder.addCancelAction();

			boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
			if (isOk) {
				form.getData(settings.getTailSettings());
				GrepConsoleApplicationComponent.getInstance().getState().setTailSettings(settings.getTailSettings());
				form.rebind(settings.getTailSettings());
			}
		}
	}
}
