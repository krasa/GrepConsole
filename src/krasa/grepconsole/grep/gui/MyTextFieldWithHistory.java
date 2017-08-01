/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package krasa.grepconsole.grep.gui;

import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.ui.ComboBox;
import krasa.grepconsole.grep.GrepModel;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class MyTextFieldWithHistory extends ComboBox {
	private int myHistorySize = 5;
	private final MyModel myModel;

	public MyTextFieldWithHistory() {
		myModel = new MyModel();
		setModel(myModel);
		setEditable(true);
	}

	// API compatibility with 7.0.1
	@SuppressWarnings({"UnusedDeclaration"})
	public MyTextFieldWithHistory(boolean cropList) {
		this();
	}

	public void addDocumentListener(DocumentListener listener) {
		getTextEditor().getDocument().addDocumentListener(listener);
	}

	public void removeDocumentListener(DocumentListener listener) {
		getTextEditor().getDocument().removeDocumentListener(listener);
	}

	public void addKeyboardListener(final KeyListener listener) {
		getTextEditor().addKeyListener(listener);
	}

	/**
	 * @param aHistorySize -1 means unbounded
	 */
	public void setHistorySize(int aHistorySize) {
		myHistorySize = aHistorySize;
	}

	public void setHistory(List<GrepOptionsItem> aHistory) {
		myModel.setItems(aHistory);
	}

	public List<String> getHistory() {
		final int itemsCount = myModel.getSize();
		List<String> history = new ArrayList<>(itemsCount);
		for (int i = 0; i < itemsCount; i++) {
			GrepOptionsItem elementAt = (GrepOptionsItem) myModel.getElementAt(i);
			history.add(elementAt.asString());
		}
		return history;
	}

	public void setText(String aText) {
		getTextEditor().setText(aText);
	}

	public String getText() {
		return getTextEditor().getText();
	}

	public void removeNotify() {
		super.removeNotify();
		hidePopup();
	}

	public void addCurrentTextToHistory(GrepModel grepModel) {
		myModel.addElement(GrepOptionsItem.from(grepModel));
	}

	public void selectText() {
		getTextEditor().selectAll();
	}

	public JTextField getTextEditor() {
		return (JTextField) getEditor().getEditorComponent();
	}

	@Override
	public void setPopupVisible(boolean v) {
		if (v) {
			final FileTextField fileTextField = (FileTextField) getTextEditor().getClientProperty(FileTextField.KEY);
			// don't allow showing combobox popup when file completion popup is displayed (IDEA-68711)
			if (fileTextField != null && fileTextField.isPopupDisplayed()) {
				return;
			}
		}
		super.setPopupVisible(v);
	}

	public class MyModel extends AbstractListModel implements ComboBoxModel {
		private List<GrepOptionsItem> myFullList = new ArrayList<>();

		private GrepOptionsItem mySelectedItem;

		public Object getElementAt(int index) {
			return myFullList.get(index);
		}

		public int getSize() {
			return Math.min(myHistorySize == -1 ? Integer.MAX_VALUE : myHistorySize, myFullList.size());
		}

		public void addElement(GrepOptionsItem obj) {
			if (0 == obj.expression.trim().length()) {
				return;
			}
			myFullList.remove(obj);
			mySelectedItem = obj;
			insertElementAt(obj, 0);
		}

		public void insertElementAt(GrepOptionsItem obj, int index) {
			myFullList.add(index, obj);
			fireIntervalAdded(this, index, index);
		}

		public GrepOptionsItem getSelectedItem() {
			return mySelectedItem;
		}

		public void setSelectedItem(Object anItem) {
			if (anItem instanceof String) {
				String newExpression = (String) anItem;
				if (!newExpression.isEmpty()) {
					for (GrepOptionsItem grepOptionsItem : myFullList) {
						if (grepOptionsItem.expression.equals(anItem)) {
							mySelectedItem = grepOptionsItem;
							break;
						}
					}
				}
				mySelectedItem = GrepOptionsItem.from(mySelectedItem).setExpression(newExpression);
			} else {
				mySelectedItem = (GrepOptionsItem) anItem;
			}
			fireContentsChanged();
		}

		public void fireContentsChanged() {
			fireContentsChanged(this, -1, -1);
		}

		public void setItems(List<GrepOptionsItem> aList) {
			myFullList = new ArrayList<>(aList);
			fireContentsChanged();
		}
	}

	protected static class TextFieldWithProcessing extends JTextField {
		public void processKeyEvent(KeyEvent e) {
			super.processKeyEvent(e);
		}
	}
}