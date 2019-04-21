package krasa.grepconsole.gui;

import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.model.StreamBufferSettings;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class StreamBufferSettingsForm {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(StreamBufferSettingsForm.class);

	private JTextField sleepTimeWhenIdle;
	private JTextField sleepTimeWhenWasActive;
	private JTextField currentlyPrintingDelta;
	private JTextField maxWaitTime;
	private JEditorPane about;
	private JPanel root;
	private JCheckBox useForCheckBox;

	public StreamBufferSettingsForm() {

		about.setText("<html><body>" +
				"Experimental and potentially dangerous workaround for <a href=\"https://youtrack.jetbrains.com/issue/IDEA-70016\">IDEA-70016</a>" +
				"<br/>Inspired by <a href=\"https://youtrack.jetbrains.com/issue/PY-32776\">PY-32776</a>" +
				"<br/>- Buffers all streams to minimize printing of incomplete lines" +
				"<br/>- Stderr is delayed until nothing more is being outputted, so that a whole stacktrace is printed together" +
				"<br/>- JUnit tests are not handled well, as they mix all different stream types when ComparisonFailure happens" +
				"<br/>- SYSTEM output type is currently not buffered" +

				"<br/>- Does not fix mixed order of stdout/stderr lines." +
				"<br/></body></html>");
		about.addHyperlinkListener(
				new BrowseHyperlinkListener()
		);
	}

	public JComponent getRoot() {
		return root;
	}

	public void setData(StreamBufferSettings data) {
		maxWaitTime.setText(data.getMaxWaitTime());
		sleepTimeWhenIdle.setText(data.getSleepTimeWhenIdle());
		currentlyPrintingDelta.setText(data.getCurrentlyPrintingDelta());
		sleepTimeWhenWasActive.setText(data.getSleepTimeWhenWasActive());
		useForCheckBox.setSelected(data.isUseForTests());
	}

	public void getData(StreamBufferSettings data) {
		data.setMaxWaitTime(maxWaitTime.getText());
		data.setSleepTimeWhenIdle(sleepTimeWhenIdle.getText());
		data.setCurrentlyPrintingDelta(currentlyPrintingDelta.getText());
		data.setSleepTimeWhenWasActive(sleepTimeWhenWasActive.getText());
		data.setUseForTests(useForCheckBox.isSelected());
	}

	public boolean isModified(StreamBufferSettings data) {
		if (maxWaitTime.getText() != null ? !maxWaitTime.getText().equals(data.getMaxWaitTime()) : data.getMaxWaitTime() != null)
			return true;
		if (sleepTimeWhenIdle.getText() != null ? !sleepTimeWhenIdle.getText().equals(data.getSleepTimeWhenIdle()) : data.getSleepTimeWhenIdle() != null)
			return true;
		if (currentlyPrintingDelta.getText() != null ? !currentlyPrintingDelta.getText().equals(data.getCurrentlyPrintingDelta()) : data.getCurrentlyPrintingDelta() != null)
			return true;
		if (sleepTimeWhenWasActive.getText() != null ? !sleepTimeWhenWasActive.getText().equals(data.getSleepTimeWhenWasActive()) : data.getSleepTimeWhenWasActive() != null)
			return true;
		if (useForCheckBox.isSelected() != data.isUseForTests()) return true;
		return false;
	}


	private class BrowseHyperlinkListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException e1) {
						LOG.warn("Exception browsing to " + e.getURL().toExternalForm() + " : " + e1);
					} catch (URISyntaxException e1) {
						LOG.warn("Incorrect URI syntax " + e.getURL().toExternalForm() + " : " + e1);
					}
				}
			}

		}
	}

}
