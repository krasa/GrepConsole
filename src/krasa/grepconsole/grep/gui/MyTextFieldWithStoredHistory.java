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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.text.StringUtil;
import krasa.grepconsole.grep.CopyListenerModel;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

/**
 * User: anna
 * Date: 16-Dec-2005
 */
public class MyTextFieldWithStoredHistory extends MyTextFieldWithHistory {
	private final String myPropertyName;
	private boolean initialized = false;
	
	public MyTextFieldWithStoredHistory(@NonNls final String propertyName) {
		myPropertyName = propertyName;
	}

	public void addCurrentTextToHistory(CopyListenerModel copyListenerModel) {
		if (!initialized) {
			throw new RuntimeException("not initialized");
		}
		super.addCurrentTextToHistory(copyListenerModel);
		PropertiesComponent.getInstance().setValue(myPropertyName, StringUtil.join(getHistory(), "\n"));
	}

	/**
	 * does not work when called from createUIComponents
	 */
	public List<GrepOptionsItem> reset() {
		initialized = true;
		final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
		final String history = propertiesComponent.getValue(myPropertyName);
		List<GrepOptionsItem> result = new ArrayList<>();
		if (history != null) {
			final String[] items = history.split("\n");
			for (String item : items) {
				if (item != null && item.length() > 0) {
					result.add(GrepOptionsItem.fromString(item));
				}
			}
			setHistory(result);
			if (!result.isEmpty()) {
				setSelectedItem(result.get(0));
			} else {
				setSelectedItem(new GrepOptionsItem().setExpression(""));
			}
		}
		return result;
	}

	public void clearHistory() {
		PropertiesComponent.getInstance().unsetValue(myPropertyName);
		setHistory(reset());
	}
}
