package krasa.grepconsole.gui;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.CopyAction;
import com.intellij.ide.actions.CutAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.tree.TreeUtil;
import krasa.grepconsole.gui.table.CheckboxTreeTable;
import krasa.grepconsole.gui.table.GrepExpressionGroupTreeNode;
import krasa.grepconsole.gui.table.GrepExpressionItemTreeNode;
import krasa.grepconsole.gui.table.TableUtils;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.MyConfigurable;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static krasa.grepconsole.Cloner.deepClone;

public class ProfileDetail {
	private static final Logger log = Logger.getInstance(ProfileDetail.class);
	private JPanel rootComponent;
	private CheckboxTreeTable table;
	private JButton addNewButton;
	private JButton resetToDefaultButton;
	private JCheckBox enableHighlightingCheckBox;
	private JFormattedTextField maxLengthToMatch;
	private JCheckBox enableMaxLength;
	private JButton duplicateButton;
	private JCheckBox enableFiltering;
	private JCheckBox multilineOutput;
	private JButton DONATEButton;
	private JCheckBox showStatsInConsole;
	private JCheckBox showStatsInStatusBar;
	private JButton addNewGroup;
	private JLabel contextSpecificText;
	private JCheckBox enableFoldings;
	private JFormattedTextField maxProcessingTime;
	private JCheckBox filterOutBeforeGreppingToASubConsole;
	private JButton web;
	private JCheckBox alwaysPinGrepConsoles;
	private JFormattedTextField maxLengthToGrep;
	private JCheckBox enableMaxLengthGrep;
	private JButton rehighlightAll;
	private JButton help;
	private JCheckBox multilineInputFilter;
	// private JCheckBox synchronous;
	public Profile profile;

	public ProfileDetail(MyConfigurable myConfigurable, SettingsContext settingsContext) {
		// int version = Integer.parseInt(ApplicationInfo.getInstance().getMajorVersion());
		// if (version < 163) {
		// synchronous.setVisible(false);
		// }
		DONATEButton.setBorder(null);
		DONATEButton.setContentAreaFilled(false);
		DONATEButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Grep%20Console%20%2d%20IntelliJ%20plugin&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest");
			}
		});
		web.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BareBonesBrowserLaunch.openURL("https://plugins.jetbrains.com/plugin/7125-grep-console");
			}
		});
		rehighlightAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myConfigurable.apply(null);
				ServiceManager.getInstance().rehighlight();
			}
		});
		addNewButton.addActionListener(new AddNewItemAction());
		addNewGroup.addActionListener(new AddNewGroupAction());
		resetToDefaultButton.addActionListener(new ResetToDefaultAction());
		duplicateButton.addActionListener(new DuplicateAction());
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
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
		table.addKeyListener(new DeleteListener());
		disableCopyDeleteButton();


		if (settingsContext == SettingsContext.CONSOLE) {
			contextSpecificText.setText("Select items for which statistics should be displayed ('"
					+ SettingsTableBuilder.CONSOLE_COUNT + "' column)");
		} else if (settingsContext == SettingsContext.STATUS_BAR) {
			contextSpecificText.setText("Select items for which statistics should be displayed ('"
					+ SettingsTableBuilder.STATUS_BAR_COUNT + "' column)");
		} else {
			contextSpecificText.setVisible(false);
		}
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Messages.showInfoMessage(rootComponent,
						"Filter out - A line will not be filtered out if any previous expression matches first. \n" +
								" - Sometimes you may want to see only lines that are highlighted. To do this, add a \".*\" as the last item and set to \"Whole line\" and \"Filter out\".\n" +
								"Whole line - Matches a whole line, otherwise finds a matching substrings - 'Unless expression' works only for whole lines.\n" +
								"Continue matching - Matches a line against the next configured items to apply multiple highlights.\n" +
								"Clear Console - Will not work if any previous non-filtering expression is matched first.\n"
						,
						"Columns caveats");
			}
		});
	}

	public MouseAdapter rightClickMenu() {
		return new MouseAdapter() {
			@Override
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
						popup.add(new JPopupMenu.Separator());
					}
					popup.add(newMenuItem("Add New Item", new AddNewItemAction()));
					popup.add(newMenuItem("Duplicate", new DuplicateAction()));
					popup.add(new JPopupMenu.Separator());

					CopyAction copyAction = (CopyAction) ActionManager.getInstance().getAction("$Copy");
					popup.add(newMenuItem("Copy (" + KeymapUtil.getFirstKeyboardShortcutText(copyAction) + ")", new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							copyAction.actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(table),
									ActionPlaces.UNKNOWN, new Presentation(""), ActionManager.getInstance(), 0));
						}
					}));
					CutAction cutAction = (CutAction) ActionManager.getInstance().getAction("$Cut");
					popup.add(newMenuItem("Cut (" + KeymapUtil.getFirstKeyboardShortcutText(cutAction) + ")", new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cutAction.actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(table),
									ActionPlaces.UNKNOWN, new Presentation(""), ActionManager.getInstance(), 0));
						}
					}));
					popup.add(newMenuItem("Delete (Del)", new DeleteAction()));
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			private JBMenuItem newMenuItem(String name, ActionListener l) {
				final JBMenuItem item = new JBMenuItem(name);
				item.addActionListener(l);
				return item;
			}

			private JBMenuItem getConvertAction(final GrepExpressionItem item) {
				final boolean highlightOnlyMatchingText = item.isHighlightOnlyMatchingText();
				final JBMenuItem convert = new JBMenuItem(highlightOnlyMatchingText ? "Convert to whole line"
						: "Convert to words only");

				try {
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
							reloadNode(ProfileDetail.this.getSelectedNode());
						}

					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				return convert;
			}
		};
	}

	private void reloadNode(final DefaultMutableTreeNode selectedNode) {
		DefaultTreeModel model = (DefaultTreeModel) table.getTree().getModel();
		model.nodeChanged(selectedNode);
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
		duplicateButton.setEnabled(false);
	}

	private void setSelectedRow(Integer selectedRow) {
		duplicateButton.setEnabled(selectedRow != null && selectedRow >= 0);
	}

	public JPanel getRootComponent() {
		return rootComponent;
	}

	public Profile getSettings() {
		getData(profile);
		return profile;
	}

	public void importFrom(@NotNull Profile profile) {
		if (this.profile != null) {//keep changes when switching to another profile
			getData(this.profile);
		}
		this.profile = profile;
		setData(profile);
		foldingsEnabled(profile.isDefaultProfile());
	}

	public void foldingsEnabled(boolean defaultProfile) {
		resetTreeModel(defaultProfile);
		enableFoldings.setEnabled(defaultProfile);
	}

	private void resetTreeModel(boolean defaultProfile) {
		table.foldingsEnabled(defaultProfile);
		CheckedTreeNode root = (CheckedTreeNode) table.getTree().getModel().getRoot();
		root.removeAllChildren();
		for (GrepExpressionGroup group : this.profile.getGrepExpressionGroups()) {
			GrepExpressionGroupTreeNode newChild = new GrepExpressionGroupTreeNode(group);
			for (GrepExpressionItem grepExpressionItem : group.getGrepExpressionItems()) {
				newChild.add(new GrepExpressionItemTreeNode(grepExpressionItem));
			}
			root.add(newChild);
		}
		TableUtils.reloadTree(table);
		TreeUtil.expandAll(table.getTree());
	}

	private void createUIComponents() {
		NumberFormatter numberFormatter = new NumberFormatter();
		numberFormatter.setMinimum(0);
		maxLengthToMatch = new JFormattedTextField(numberFormatter);
		maxLengthToGrep = new JFormattedTextField(numberFormatter);
		maxProcessingTime = new JFormattedTextField(numberFormatter);
		table = new SettingsTableBuilder(this).getTable();
	}

	private GrepExpressionItem newItem() {
		GrepExpressionItem item = new GrepExpressionItem();
		item.setGrepExpression("foo");
		item.setEnabled(true);
		item.setContinueMatching(true);
		item.setHighlightOnlyMatchingText(true);
		item.getStyle().setBackgroundColor(new GrepColor(true, JBColor.CYAN));
		return item;
	}

	private GrepExpressionGroup getGrepExpressionGroup(DefaultMutableTreeNode selectedNode) {
		return (GrepExpressionGroup) selectedNode.getUserObject();
	}

	private GrepExpressionItem getSelectedGrepExpressionItem(DefaultMutableTreeNode selectedNode) {
		return (GrepExpressionItem) selectedNode.getUserObject();
	}

	public void rebuildProfile() {
		List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		grepExpressionGroups.clear();

		DefaultMutableTreeNode model = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
		Enumeration children = model.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode o = (DefaultMutableTreeNode) children.nextElement();
			if (o instanceof GrepExpressionGroupTreeNode) {
				GrepExpressionGroup grepExpressionGroup = ((GrepExpressionGroupTreeNode) o).getGrepExpressionGroup();
				grepExpressionGroup.getGrepExpressionItems().clear();
				Enumeration children1 = o.children();
				while (children1.hasMoreElements()) {
					Object o1 = children1.nextElement();
					if (o1 instanceof GrepExpressionItemTreeNode) {
						GrepExpressionItem grepExpressionItem = ((GrepExpressionItemTreeNode) o1).getGrepExpressionItem();
						grepExpressionGroup.add(grepExpressionItem);
					} else {
						throw new IllegalStateException("unexpected tree node" + o1);
					}
				}
				grepExpressionGroups.add(grepExpressionGroup);
			} else {
				throw new IllegalStateException("unexpected tree node" + o);
			}
		}
	}

	public void setData(Profile data) {
		enableMaxLength.setSelected(data.isEnableMaxLengthLimit());
		maxProcessingTime.setText(data.getMaxProcessingTime());
		enableMaxLengthGrep.setSelected(data.isEnableMaxLengthGrepLimit());
		maxLengthToMatch.setText(data.getMaxLengthToMatch());
		maxLengthToGrep.setText(data.getMaxLengthToGrep());
		alwaysPinGrepConsoles.setSelected(data.isAlwaysPinGrepConsoles());
		enableHighlightingCheckBox.setSelected(data.isEnabledHighlighting());
		enableFiltering.setSelected(data.isEnabledInputFiltering());
		enableFoldings.setSelected(data.isEnableFoldings());
		filterOutBeforeGreppingToASubConsole.setSelected(data.isFilterOutBeforeGrep());
		showStatsInStatusBar.setSelected(data.isShowStatsInStatusBarByDefault());
		showStatsInConsole.setSelected(data.isShowStatsInConsoleByDefault());
		multilineOutput.setSelected(data.isMultiLineOutput());
		multilineInputFilter.setSelected(data.isMultilineInputFilter());
	}

	public void getData(Profile data) {
		data.setEnableMaxLengthLimit(enableMaxLength.isSelected());
		data.setMaxProcessingTime(maxProcessingTime.getText());
		data.setEnableMaxLengthGrepLimit(enableMaxLengthGrep.isSelected());
		data.setMaxLengthToMatch(maxLengthToMatch.getText());
		data.setMaxLengthToGrep(maxLengthToGrep.getText());
		data.setAlwaysPinGrepConsoles(alwaysPinGrepConsoles.isSelected());
		data.setEnabledHighlighting(enableHighlightingCheckBox.isSelected());
		data.setEnabledInputFiltering(enableFiltering.isSelected());
		data.setEnableFoldings(enableFoldings.isSelected());
		data.setFilterOutBeforeGrep(filterOutBeforeGreppingToASubConsole.isSelected());
		data.setShowStatsInStatusBarByDefault(showStatsInStatusBar.isSelected());
		data.setShowStatsInConsoleByDefault(showStatsInConsole.isSelected());
		data.setMultiLineOutput(multilineOutput.isSelected());
		data.setMultilineInputFilter(multilineInputFilter.isSelected());
	}

	public boolean isModified(Profile data) {
		if (enableMaxLength.isSelected() != data.isEnableMaxLengthLimit()) return true;
		if (maxProcessingTime.getText() != null ? !maxProcessingTime.getText().equals(data.getMaxProcessingTime()) : data.getMaxProcessingTime() != null)
			return true;
		if (enableMaxLengthGrep.isSelected() != data.isEnableMaxLengthGrepLimit()) return true;
		if (maxLengthToMatch.getText() != null ? !maxLengthToMatch.getText().equals(data.getMaxLengthToMatch()) : data.getMaxLengthToMatch() != null)
			return true;
		if (maxLengthToGrep.getText() != null ? !maxLengthToGrep.getText().equals(data.getMaxLengthToGrep()) : data.getMaxLengthToGrep() != null)
			return true;
		if (alwaysPinGrepConsoles.isSelected() != data.isAlwaysPinGrepConsoles()) return true;
		if (enableHighlightingCheckBox.isSelected() != data.isEnabledHighlighting()) return true;
		if (enableFiltering.isSelected() != data.isEnabledInputFiltering()) return true;
		if (enableFoldings.isSelected() != data.isEnableFoldings()) return true;
		if (filterOutBeforeGreppingToASubConsole.isSelected() != data.isFilterOutBeforeGrep()) return true;
		if (showStatsInStatusBar.isSelected() != data.isShowStatsInStatusBarByDefault()) return true;
		if (showStatsInConsole.isSelected() != data.isShowStatsInConsoleByDefault()) return true;
		if (multilineOutput.isSelected() != data.isMultiLineOutput()) return true;
		if (multilineInputFilter.isSelected() != data.isMultilineInputFilter()) return true;
		return false;
	}

	private class DeleteListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			final int keyCode = e.getKeyCode();
			if (keyCode == KeyEvent.VK_DELETE) {
				delete();
			}
		}
	}

	private void delete() {
		TreeNode selectNode = null;
		TreeTableTree tree = table.getTree();
		int[] selectionRows = tree.getSelectionRows();
		if (selectionRows == null) {
			return;
		}
		Arrays.sort(selectionRows);
		selectionRows = ArrayUtil.reverseArray(selectionRows);
		for (int selectionRow : selectionRows) {
			TreePath treePath = tree.getPathForRow(selectionRow);
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();

			if (selectNode == null || selectNode == selectedNode || selectNode.getParent() == selectedNode) {
				int index = parent.getIndex(selectedNode);
				if (index + 1 < parent.getChildCount()) {
					selectNode = parent.getChildAt(index + 1);
				} else if (index > 0) {
					selectNode = parent.getChildAt(index - 1);
				} else {
					selectNode = parent;
				}
			}
			parent.remove(selectedNode);
		}
		rebuildProfile();
		TableUtils.reloadTree(this.table);
		TableUtils.selectNode((DefaultMutableTreeNode) selectNode, table);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
		if (root.getChildCount() == 0) {
			disableCopyDeleteButton();
		}
	}

	private class AddNewItemAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selectedNode = getSelectedNode();
			CheckedTreeNode newChild;
			if (selectedNode == null) {
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
				GrepExpressionGroupTreeNode aNew = new GrepExpressionGroupTreeNode(new GrepExpressionGroup("new"));
				newChild = new GrepExpressionItemTreeNode(newItem());
				aNew.add(newChild);
				root.add(aNew);
			} else if (selectedNode.getUserObject() instanceof GrepExpressionGroup) {
				newChild = new GrepExpressionItemTreeNode(newItem());
				selectedNode.add(newChild);
			} else {
				GrepExpressionGroupTreeNode parent = (GrepExpressionGroupTreeNode) selectedNode.getParent();
				newChild = new GrepExpressionItemTreeNode(newItem());
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);
			}
			rebuildProfile();
			TableUtils.reloadTree(table);
			TableUtils.selectNode(newChild, table);
			table.requestFocus();
		}
	}

	private class AddNewGroupAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
			GrepExpressionGroupTreeNode aNew = new GrepExpressionGroupTreeNode(new GrepExpressionGroup("new"));
			root.add(aNew);
			rebuildProfile();
			TableUtils.reloadTree(table);
			TableUtils.selectNode(aNew, table);
			table.requestFocus();
		}
	}

	private class ResetToDefaultAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			disableCopyDeleteButton();
			profile.resetToDefault();
			importFrom(profile);
		}
	}

	private class DuplicateAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			CheckboxTreeTable table = ProfileDetail.this.table;
			DefaultMutableTreeNode selectedNode = getSelectedNode();
			if (selectedNode instanceof GrepExpressionItemTreeNode) {
				GrepExpressionItemTreeNode newChild = new GrepExpressionItemTreeNode(deepClone((GrepExpressionItem) selectedNode.getUserObject()));
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);

				TableUtils.reloadTree(table);
				TableUtils.selectNode(newChild, table);
			} else if (selectedNode instanceof GrepExpressionGroupTreeNode) {
				GrepExpressionGroup group = deepClone((GrepExpressionGroup) selectedNode.getUserObject());
				GrepExpressionGroupTreeNode newChild = new GrepExpressionGroupTreeNode(group);
				for (GrepExpressionItem grepExpressionItem : group.getGrepExpressionItems()) {
					newChild.add(new GrepExpressionItemTreeNode(grepExpressionItem));
				}
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);

				TableUtils.reloadTree(table);
				TableUtils.expand(newChild, table);
				TableUtils.selectNode(newChild, table);
			}
			rebuildProfile();
		}

	}

	private class DeleteAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			delete();
		}
	}

}
