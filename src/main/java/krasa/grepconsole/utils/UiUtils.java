package krasa.grepconsole.utils;

import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class UiUtils {
	/**
	 * https://stackoverflow.com/a/27190162/685796
	 * <p>
	 * Installs a listener to receive notification when the jTextComponent of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the jTextComponent component's {@link Document},
	 * and a {@link PropertyChangeListener
	 * } on the jTextComponent component to detect
	 * if the {@code Document} itself is replaced.
	 *
	 * @param jTextComponent any jTextComponent component, such as a {@link JTextField}
	 *                       or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *                       when the jTextComponent is changed; the source object for the events
	 *                       will be the jTextComponent component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent jTextComponent, ChangeListener changeListener) {
		Objects.requireNonNull(jTextComponent);
		Objects.requireNonNull(changeListener);
		DocumentListener dl = new DocumentListener() {
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				lastChange++;
				ApplicationManager.getApplication().invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;

						changeListener.stateChanged(new ChangeEvent(jTextComponent));
					}
				});
			}
		};
		jTextComponent.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document) e.getOldValue();
			Document d2 = (Document) e.getNewValue();
			if (d1 != null) d1.removeDocumentListener(dl);
			if (d2 != null) d2.addDocumentListener(dl);
			dl.changedUpdate(null);
		});
		Document d = jTextComponent.getDocument();
		if (d != null) d.addDocumentListener(dl);
	}
}
