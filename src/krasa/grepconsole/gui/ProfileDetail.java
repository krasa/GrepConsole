package krasa.grepconsole.gui;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.CopyAction;
import com.intellij.ide.actions.CutAction;
import com.intellij.ide.actions.PasteAction;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.tree.TreeUtil;
import krasa.grepconsole.gui.table.*;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.DefaultState;
import krasa.grepconsole.plugin.MyConfigurable;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import static krasa.grepconsole.Cloner.deepClone;

public class ProfileDetail {
	private static final String DIVIDER = "GrepConsole.ProfileDetail";

	private static final Logger log = Logger.getInstance(ProfileDetail.class);
	private JPanel rootComponent;
	private CheckboxTreeTable grepTable;
	private JButton addNewButton;
	private JButton resetToDefaultButton;
	private JCheckBox enableHighlightingCheckBox;
	private JFormattedTextField maxLengthToMatch;
	private JCheckBox enableMaxLength;
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

	private CheckboxTreeTable inputTable;
	private JButton installLivePlugin;
	private JButton addLivePluginScript;
	private JPanel highlightersPanel;
	private JPanel transfrormersPanel;
	private JPanel settings;
	private JSplitPane splitPane;
	private JButton resetHighlighters;
	private JCheckBox testHighlightersFirst;
	private JButton addNewInputFilterGroup;
	// private JCheckBox synchronous;
	public Profile profile;

	public ProfileDetail(MyConfigurable myConfigurable, SettingsContext settingsContext) {
		String value = PropertiesComponent.getInstance().getValue(DIVIDER);
		if (value != null) {
			try {
				splitPane.setDividerLocation(Integer.parseInt(value));
			} catch (NumberFormatException e) {
			}
		}
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent pce) {
						Object newValue = pce.getNewValue();
						PropertiesComponent.getInstance().setValue(DIVIDER, String.valueOf(newValue));
					}
				}
		);


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
		resetToDefaultButton.addActionListener(new ResetAllToDefaultAction());

		addNewButton.addActionListener(new AddNewItemAction(grepTable, false));
		addNewGroup.addActionListener(new AddNewGroupAction(grepTable));
		addNewInputFilterGroup.addActionListener(new AddNewGroupAction(inputTable));
		grepTable.addMouseListener(rightClickMenu(grepTable, false));
		grepTable.addKeyListener(new DeleteListener(grepTable));

		inputTable.addMouseListener(rightClickMenu(inputTable, true));
		inputTable.addKeyListener(new DeleteListener(inputTable));

		if (settingsContext == SettingsContext.CONSOLE) {
			contextSpecificText.setText("Select items for which statistics should be displayed ('"
					+ GrepTableBuilder.CONSOLE_COUNT + "' column)");
		} else if (settingsContext == SettingsContext.STATUS_BAR) {
			contextSpecificText.setText("Select items for which statistics should be displayed ('"
					+ GrepTableBuilder.STATUS_BAR_COUNT + "' column)");
		} else {
			contextSpecificText.setVisible(false);
		}
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Messages.showInfoMessage(rootComponent,
						"You can copy/paste table rows to/from plaintext.\n\n" +
								"You can manipulate output text or execute any custom actions (e.g. notifications) by making your own extension plugin or by scripting via LivePlugin - https://github.com/dkandalov/live-plugin\n\n" +
								"Whole line - Matches a whole line, otherwise finds a matching substrings - 'Unless expression' works only for whole lines.\n" +
								"Continue matching - Matches a line against the next configured items.\n"
						,
						"Input filtering");
			}
		});

		boolean livePlugin = PluginManager.isPluginInstalled(PluginId.getId("LivePlugin"));
		installLivePlugin.setEnabled(!livePlugin);
		addLivePluginScript.setEnabled(livePlugin);
		installLivePlugin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HashSet<String> pluginIds = new HashSet<>();
				pluginIds.add("LivePlugin");
				PluginsAdvertiser.installAndEnablePlugins(pluginIds, new Runnable() {
					@Override
					public void run() {
						installLivePlugin.setEnabled(false);
					}
				});
			}
		});
		addLivePluginScript.addActionListener(new LivePluginExampleAction(addLivePluginScript));
		resetHighlighters.addActionListener(new ResetHighlightersToDefaultAction());

	}

	public MouseAdapter rightClickMenu(CheckboxTreeTable table, boolean input) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
				} else if (SwingUtilities.isRightMouseButton(e)) {
					boolean somethingSelected = getSelectedNode(table) != null;

					JPopupMenu popup = new JBPopupMenu();
					GrepExpressionItem selectedGrepExpressionItem = getSelectedGrepExpressionItem(table);
					if (selectedGrepExpressionItem != null) {
						popup.add(getConvertAction(selectedGrepExpressionItem, table));
						popup.add(new JPopupMenu.Separator());
					}
					popup.add(newMenuItem("Add New Item", new AddNewItemAction(table, input)));
					if (somethingSelected) {
						popup.add(newMenuItem("Duplicate", new DuplicateAction(table)));
					}
					popup.add(new JPopupMenu.Separator());

					CopyAction copyAction = (CopyAction) ActionManager.getInstance().getAction("$Copy");
					if (somethingSelected) {
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


					}
					PasteAction pasteAction = (PasteAction) ActionManager.getInstance().getAction("$Paste");
					popup.add(newMenuItem("Paste (" + KeymapUtil.getFirstKeyboardShortcutText(pasteAction) + ")", new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							pasteAction.actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(table),
									ActionPlaces.UNKNOWN, new Presentation(""), ActionManager.getInstance(), 0));
						}
					}));
					if (somethingSelected) {
						popup.add(newMenuItem("Delete (Del)", new DeleteAction(table)));
					}

					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			private JBMenuItem newMenuItem(String name, ActionListener l) {
				final JBMenuItem item = new JBMenuItem(name);
				item.addActionListener(l);
				return item;
			}

			private JBMenuItem getConvertAction(final GrepExpressionItem item, CheckboxTreeTable table) {
				final boolean highlightOnlyMatchingText = item.isHighlightOnlyMatchingText();
				final JBMenuItem convert = new JBMenuItem(highlightOnlyMatchingText ? "Convert to whole line"
						: "Convert to words only");

				try {
					convert.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (!highlightOnlyMatchingText) {
								getSelectedGrepExpressionItem(table).setHighlightOnlyMatchingText(true);
								getSelectedGrepExpressionItem(table).setContinueMatching(true);
								if (item.getGrepExpression().startsWith(".*")) {
									item.grepExpression(item.getGrepExpression().substring(2));
								}
								if (item.getGrepExpression().endsWith(".*")) {
									item.grepExpression(item.getGrepExpression().substring(0,
											item.getGrepExpression().length() - 2));
								}
							} else {
								getSelectedGrepExpressionItem(table).setHighlightOnlyMatchingText(false);
								getSelectedGrepExpressionItem(table).setContinueMatching(false);
								if (!item.getGrepExpression().startsWith(".*")) {
									item.grepExpression(".*" + item.getGrepExpression());
								}
								if (!item.getGrepExpression().endsWith(".*")) {
									item.grepExpression(item.getGrepExpression() + ".*");
								}
							}
							reloadNode(ProfileDetail.this.getSelectedNode(table), table);
						}

					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				return convert;
			}
		};
	}

	private void reloadNode(final DefaultMutableTreeNode selectedNode, CheckboxTreeTable grepTable) {
		DefaultTreeModel model = (DefaultTreeModel) grepTable.getTree().getModel();
		model.nodeChanged(selectedNode);
	}

	private GrepExpressionItem getSelectedGrepExpressionItem(CheckboxTreeTable grepTable) {
		DefaultMutableTreeNode selectedNode = getSelectedNode(grepTable);
		GrepExpressionItem item = null;
		if (selectedNode instanceof GrepExpressionItemTreeNode) {
			item = (GrepExpressionItem) selectedNode.getUserObject();
		}
		return item;
	}

	public DefaultMutableTreeNode getSelectedNode(CheckboxTreeTable table) {
		return (DefaultMutableTreeNode) table.getTree().getLastSelectedPathComponent();
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
		resetTreeModel(profile.isDefaultProfile());
	}

	public void resetTreeModel(boolean foldingsEnabled) {
		((GrepTableBuilder.MyCheckboxTreeTable) grepTable).foldingsEnabled(foldingsEnabled);
		resetTable(grepTable, this.profile.getGrepExpressionGroups());

		enableFoldings.setEnabled(foldingsEnabled);

		resetTable(inputTable, this.profile.getInputFilterGroups());
	}

	protected void resetTable(CheckboxTreeTable grepTable, List<GrepExpressionGroup> grepExpressionGroups) {
		CheckedTreeNode root = (CheckedTreeNode) grepTable.getTree().getModel().getRoot();
		root.removeAllChildren();
		for (GrepExpressionGroup group : grepExpressionGroups) {
			GrepExpressionGroupTreeNode newChild = new GrepExpressionGroupTreeNode(group);
			root.add(newChild);
		}
		TableUtils.reloadTree(grepTable);
		TreeUtil.expandAll(grepTable.getTree());
	}

	private void createUIComponents() {
		NumberFormatter numberFormatter = new NumberFormatter();
		numberFormatter.setMinimum(0);
		maxLengthToMatch = new JFormattedTextField(numberFormatter);
		maxLengthToGrep = new JFormattedTextField(numberFormatter);
		maxProcessingTime = new JFormattedTextField(numberFormatter);
		grepTable = new GrepTableBuilder(this).getTable();
		inputTable = new TransformerTableBuilder(this).getTable();
	}


	public void rebuildProfile() {
		List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		grepExpressionGroups.clear();
		fillProfileFromTable(grepExpressionGroups, grepTable);


		List<GrepExpressionGroup> inputFilterGroups = profile.getInputFilterGroups();
		inputFilterGroups.clear();
		fillProfileFromTable(inputFilterGroups, inputTable);
	}

	protected void fillProfileFromTable(List<GrepExpressionGroup> grepExpressionGroups, CheckboxTreeTable table) {
		DefaultMutableTreeNode model = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
		Enumeration children = model.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode o = (DefaultMutableTreeNode) children.nextElement();
			if (o instanceof GrepExpressionGroupTreeNode) {
				GrepExpressionGroup grepExpressionGroup = ((GrepExpressionGroupTreeNode) o).getObject();
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
		multilineInputFilter.setSelected(data.isMultilineInputFilter());
		testHighlightersFirst.setSelected(data.isTestHighlightersInInputFilter());
		multilineOutput.setSelected(data.isMultiLineOutput());
		enableMaxLength.setSelected(data.isEnableMaxLengthLimit());
		maxProcessingTime.setText(data.getMaxProcessingTime());
		enableMaxLengthGrep.setSelected(data.isEnableMaxLengthGrepLimit());
		maxLengthToMatch.setText(data.getMaxLengthToMatch());
		maxLengthToGrep.setText(data.getMaxLengthToGrep());
		enableHighlightingCheckBox.setSelected(data.isEnabledHighlighting());
		enableFiltering.setSelected(data.isEnabledInputFiltering());
		enableFoldings.setSelected(data.isEnableFoldings());
		filterOutBeforeGreppingToASubConsole.setSelected(data.isFilterOutBeforeGrep());
		showStatsInStatusBar.setSelected(data.isShowStatsInStatusBarByDefault());
		showStatsInConsole.setSelected(data.isShowStatsInConsoleByDefault());
		alwaysPinGrepConsoles.setSelected(data.isAlwaysPinGrepConsoles());
	}

	public void getData(Profile data) {
		data.setMultilineInputFilter(multilineInputFilter.isSelected());
		data.setTestHighlightersInInputFilter(testHighlightersFirst.isSelected());
		data.setMultiLineOutput(multilineOutput.isSelected());
		data.setEnableMaxLengthLimit(enableMaxLength.isSelected());
		data.setMaxProcessingTime(maxProcessingTime.getText());
		data.setEnableMaxLengthGrepLimit(enableMaxLengthGrep.isSelected());
		data.setMaxLengthToMatch(maxLengthToMatch.getText());
		data.setMaxLengthToGrep(maxLengthToGrep.getText());
		data.setEnabledHighlighting(enableHighlightingCheckBox.isSelected());
		data.setEnabledInputFiltering(enableFiltering.isSelected());
		data.setEnableFoldings(enableFoldings.isSelected());
		data.setFilterOutBeforeGrep(filterOutBeforeGreppingToASubConsole.isSelected());
		data.setShowStatsInStatusBarByDefault(showStatsInStatusBar.isSelected());
		data.setShowStatsInConsoleByDefault(showStatsInConsole.isSelected());
		data.setAlwaysPinGrepConsoles(alwaysPinGrepConsoles.isSelected());
	}

	public boolean isModified(Profile data) {
		if (multilineInputFilter.isSelected() != data.isMultilineInputFilter()) return true;
		if (testHighlightersFirst.isSelected() != data.isTestHighlightersInInputFilter()) return true;
		if (multilineOutput.isSelected() != data.isMultiLineOutput()) return true;
		if (enableMaxLength.isSelected() != data.isEnableMaxLengthLimit()) return true;
		if (maxProcessingTime.getText() != null ? !maxProcessingTime.getText().equals(data.getMaxProcessingTime()) : data.getMaxProcessingTime() != null)
			return true;
		if (enableMaxLengthGrep.isSelected() != data.isEnableMaxLengthGrepLimit()) return true;
		if (maxLengthToMatch.getText() != null ? !maxLengthToMatch.getText().equals(data.getMaxLengthToMatch()) : data.getMaxLengthToMatch() != null)
			return true;
		if (maxLengthToGrep.getText() != null ? !maxLengthToGrep.getText().equals(data.getMaxLengthToGrep()) : data.getMaxLengthToGrep() != null)
			return true;
		if (enableHighlightingCheckBox.isSelected() != data.isEnabledHighlighting()) return true;
		if (enableFiltering.isSelected() != data.isEnabledInputFiltering()) return true;
		if (enableFoldings.isSelected() != data.isEnableFoldings()) return true;
		if (filterOutBeforeGreppingToASubConsole.isSelected() != data.isFilterOutBeforeGrep()) return true;
		if (showStatsInStatusBar.isSelected() != data.isShowStatsInStatusBarByDefault()) return true;
		if (showStatsInConsole.isSelected() != data.isShowStatsInConsoleByDefault()) return true;
		if (alwaysPinGrepConsoles.isSelected() != data.isAlwaysPinGrepConsoles()) return true;
		return false;
	}


	private class DeleteListener extends KeyAdapter {
		private CheckboxTreeTable myTable;

		public DeleteListener(CheckboxTreeTable table) {
			myTable = table;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			final int keyCode = e.getKeyCode();
			if (keyCode == KeyEvent.VK_DELETE) {
				delete(myTable);
			}
		}
	}

	private void delete(CheckboxTreeTable table) {
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
		TableUtils.reloadTree(table);
		TableUtils.selectNode((DefaultMutableTreeNode) selectNode, table);
	}

	private class AddNewItemAction implements ActionListener {
		private CheckboxTreeTable myTable;
		private final boolean input;

		public AddNewItemAction(CheckboxTreeTable table, boolean input) {
			myTable = table;
			this.input = input;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selectedNode = getSelectedNode(myTable);
			GrepExpressionItem userObject;
			if (input) {
				GrepExpressionItem item = new GrepExpressionItem();
				item.setInputFilter(true);
				item.action(GrepExpressionItem.ACTION_REMOVE);
				item.setGrepExpression(".*unwanted line.*");
				item.setEnabled(true);
				userObject = item;
			} else {
				GrepExpressionItem item = new GrepExpressionItem();
				item.setGrepExpression("foo");
				item.setEnabled(true);
				item.setContinueMatching(true);
				item.setHighlightOnlyMatchingText(true);
				item.getStyle().setBackgroundColor(new GrepColor(true, JBColor.CYAN));
				userObject = item;
			}
			final CheckedTreeNode newChild = new GrepExpressionItemTreeNode(userObject);
			if (selectedNode == null) {
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) myTable.getTree().getModel().getRoot();
				DefaultMutableTreeNode lastChild = getLastChild(root);
				if (lastChild == null) {
					GrepExpressionGroupTreeNode aNew = new GrepExpressionGroupTreeNode(new GrepExpressionGroup("new"));
					aNew.add(newChild);
					root.add(aNew);
				} else {
					lastChild.add(newChild);
				}
			} else if (selectedNode.getUserObject() instanceof GrepExpressionGroup) {
				selectedNode.add(newChild);
			} else {
				GrepExpressionGroupTreeNode parent = (GrepExpressionGroupTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);
			}
			rebuildProfile();
			TableUtils.reloadTree(myTable);
			TableUtils.selectNode(newChild, myTable);
			myTable.requestFocus();
		}

	}


	protected DefaultMutableTreeNode getLastChild(DefaultMutableTreeNode root) {
		try {
			return (DefaultMutableTreeNode) root.getLastChild();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private class AddNewGroupAction implements ActionListener {
		private CheckboxTreeTable myTable;

		public AddNewGroupAction(CheckboxTreeTable table) {
			this.myTable = table;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) myTable.getTree().getModel().getRoot();
			GrepExpressionGroupTreeNode aNew = new GrepExpressionGroupTreeNode(new GrepExpressionGroup("new"));
			root.add(aNew);
			rebuildProfile();
			TableUtils.reloadTree(myTable);
			TableUtils.selectNode(aNew, myTable);
			myTable.requestFocus();
		}
	}

	private class ResetAllToDefaultAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Profile profile = ProfileDetail.this.profile;
			ProfileDetail.this.profile = null;

			profile.resetToDefault();
			importFrom(profile);
		}
	}

	private class ResetHighlightersToDefaultAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Profile profile = ProfileDetail.this.profile;

			List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
			grepExpressionGroups.clear();
			grepExpressionGroups.add(new GrepExpressionGroup("default", DefaultState.createDefaultItems()));

			importFrom(profile);
		}
	}

	private class DuplicateAction implements ActionListener {
		private CheckboxTreeTable myTable;

		public DuplicateAction(CheckboxTreeTable table) {
			this.myTable = table;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			CheckboxTreeTable table = this.myTable;
			DefaultMutableTreeNode selectedNode = getSelectedNode(this.myTable);
			if (selectedNode instanceof GrepExpressionItemTreeNode) {
				GrepExpressionItemTreeNode newChild = new GrepExpressionItemTreeNode(deepClone((GrepExpressionItem) selectedNode.getUserObject()));
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);

				TableUtils.reloadTree(table);
				TableUtils.selectNode(newChild, table);
			} else if (selectedNode instanceof GrepExpressionGroupTreeNode) {
				GrepExpressionGroup group = deepClone((GrepExpressionGroup) selectedNode.getUserObject());
				GrepExpressionGroupTreeNode newChild = new GrepExpressionGroupTreeNode(group);
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
		private CheckboxTreeTable myTable;

		public DeleteAction(CheckboxTreeTable grepTable) {
			myTable = grepTable;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			delete(myTable);
		}
	}

}
