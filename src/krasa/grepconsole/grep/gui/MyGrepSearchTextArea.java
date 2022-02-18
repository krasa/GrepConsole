// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.grepconsole.grep.gui;

import com.intellij.find.FindBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.ex.TooltipDescriptionProvider;
import com.intellij.openapi.actionSystem.ex.TooltipLinkProvider;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.Producer;
import krasa.grepconsole.grep.GrepCompositeModel;
import krasa.grepconsole.grep.GrepModel;
import krasa.grepconsole.utils.UiUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyGrepSearchTextArea extends MySearchTextArea {
	public static final Icon EXCLUDE = IconLoader.getIcon("/krasa/grepconsole/icons/exclMark.svg", MyGrepSearchTextArea.class);
	public static final Icon EXCLUDE_HOVER = IconLoader.getIcon("/krasa/grepconsole/icons/exclMark_hover.svg", MyGrepSearchTextArea.class);
	public static final Icon EXCLUDE_SELECTED = IconLoader.getIcon("/krasa/grepconsole/icons/exclMark_selected.svg", MyGrepSearchTextArea.class);

	private final AtomicBoolean caseSensitive;
	private final AtomicBoolean wholeWords;
	private final AtomicBoolean regex;
	private final AtomicBoolean wholeLine;
	private final AtomicBoolean exclude;
	private GrepPanel grepPanel;

	public MyGrepSearchTextArea(GrepPanel grepPanel, GrepModel model) {
		super(createJbTextArea(), true);
		this.grepPanel = grepPanel;
		getTextArea().addKeyListener(myEnterRedispatcher);

		UiUtils.addChangeListener(getTextArea(), e -> grepPanel.textExpressionChanged());

		caseSensitive = new AtomicBoolean();
		MySwitchStateToggleAction myCaseSensitiveAction =
				new MySwitchStateToggleAction(
						AllIcons.Actions.MatchCase, AllIcons.Actions.MatchCaseHovered, AllIcons.Actions.MatchCaseSelected,
						caseSensitive, () -> Boolean.TRUE, FindBundle.message("find.popup.case.sensitive"));
		wholeWords = new AtomicBoolean();
		MySwitchStateToggleAction myWholeWordsAction =
				new MySwitchStateToggleAction(
						AllIcons.Actions.Words, AllIcons.Actions.WordsHovered, AllIcons.Actions.WordsSelected,
						wholeWords, () -> Boolean.TRUE, FindBundle.message("find.whole.words"));
		regex = new AtomicBoolean();
		MySwitchStateToggleAction myRegexAction =
				new MySwitchStateToggleAction(
						AllIcons.Actions.Regex, AllIcons.Actions.RegexHovered, AllIcons.Actions.RegexSelected,
						regex, () -> Boolean.TRUE, FindBundle.message("find.regex"));
		wholeLine = new AtomicBoolean();
//		MySwitchStateToggleAction myWholeLineAction =
//				new MySwitchStateToggleAction(
//						LINE, LINE_HOVER, LINE_SELECTED,
//						wholeLine, () -> !wholeWords.get(), "Whole &line");

		exclude = new AtomicBoolean();
		MySwitchStateToggleAction myExcludeAction =
				new MySwitchStateToggleAction(
						EXCLUDE, EXCLUDE_HOVER, EXCLUDE_SELECTED,
						exclude, () -> Boolean.TRUE, "&Exclude");

		setExtraActions(myCaseSensitiveAction, myWholeWordsAction, myRegexAction, myExcludeAction);
		load(model);
	}

	private final KeyAdapter myEnterRedispatcher = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER && grepPanel != null) {
				grepPanel.dispatchEvent(e);
			}
		}
	};

	private static JBTextArea createJbTextArea() {
		JBTextArea innerTextComponent = new JBTextArea();
		innerTextComponent.setRows(1);
		innerTextComponent.setColumns(12);
		innerTextComponent.setMinimumSize(new Dimension(100, 0));
		return innerTextComponent;
	}

	public GrepModel grepModel() {
		return new GrepModel(caseSensitive.get(), wholeWords.get(), regex.get(), getTextArea().getText(), exclude.get());
	}

	public void load(GrepModel grepModel) {
		caseSensitive.set(grepModel.isCaseSensitive());
		regex.set(grepModel.isRegex());
		wholeWords.set(grepModel.isCaseSensitive());
		exclude.set(grepModel.isExclude());
		getTextArea().setText(grepModel.getExpression());
	}

	//
//	class ItemChangeListener implements ItemListener {
//		@Override
//		public void itemStateChanged(ItemEvent event) {
//			if (event.getStateChange() == ItemEvent.SELECTED) {
//				GrepOptionsItem item = (GrepOptionsItem) event.getItem();
//				updateGrepOptions(item);
//			}
//		}
//	}
//
//	protected void updateGrepOptions(GrepOptionsItem selectedItem) {
//		if (selectedItem != null) {
////			wholeLine.setSelected(selectedItem.isWholeLine());
////			regex.setSelected(selectedItem.isRegex());
////			matchCase.setSelected(selectedItem.isCaseSensitive());
//		}
//	}
	private final class MySwitchStateToggleAction extends DumbAwareToggleAction implements TooltipLinkProvider, TooltipDescriptionProvider {
		private final AtomicBoolean myState;
		private final Producer<Boolean> myEnableStateProvider;
		private final TooltipLink myTooltipLink;

		private MySwitchStateToggleAction(@NotNull Icon icon, @NotNull Icon hoveredIcon, @NotNull Icon selectedIcon,
										  @NotNull AtomicBoolean state,
										  @NotNull Producer<Boolean> enableStateProvider, @Nls @NotNull String message1) {
			this(icon, hoveredIcon, selectedIcon, state, enableStateProvider, null, message1);
		}

		private MySwitchStateToggleAction(@NotNull Icon icon, @NotNull Icon hoveredIcon, @NotNull Icon selectedIcon,
										  @NotNull AtomicBoolean state,
										  @NotNull Producer<Boolean> enableStateProvider,
										  @Nullable TooltipLink tooltipLink, @Nls @NotNull String message1) {
			super(message1, null, icon);
			myState = state;
			myEnableStateProvider = enableStateProvider;
			myTooltipLink = tooltipLink;
			getTemplatePresentation().setHoveredIcon(hoveredIcon);
			getTemplatePresentation().setSelectedIcon(selectedIcon);
			ShortcutSet shortcut = ActionUtil.getMnemonicAsShortcut(this);
			if (shortcut != null) {
				setShortcutSet(shortcut);
				registerCustomShortcutSet(shortcut, MyGrepSearchTextArea.this);
			}
		}

		@Override
		public @Nullable
		TooltipLink getTooltipLink(@Nullable JComponent owner) {
			return myTooltipLink;
		}

		@Override
		public boolean isSelected(@NotNull AnActionEvent e) {
			return myState.get();
		}

		@Override
		public void update(@NotNull AnActionEvent e) {
			e.getPresentation().setEnabled(myEnableStateProvider.produce());
			Toggleable.setSelected(e.getPresentation(), myState.get());
		}

		@Override
		public void setSelected(@NotNull AnActionEvent e, boolean selected) {
			myState.set(selected);
//			if (myState == myRegexState) {
//				mySuggestRegexHintForEmptyResults = false;
//				if (selected) myWholeWordsState.set(false);
//			}
			updateControls();
			apply();
		}
	}

	@Override
	protected void apply() {
		grepPanel.reload();
	}

	@Override
	protected void reload(GrepCompositeModel selectedValue) {
		if (selectedValue.getModels().size() > 1) {
			grepPanel.initModel(null, selectedValue);
			grepPanel.reload();
		} else if (selectedValue.getModels().size() == 1) {
			load(selectedValue.getModels().get(0));
			grepPanel.reload();
		}
	}

	@Override
	protected void reload() {
		grepPanel.reload();
	}


}
