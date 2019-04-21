package krasa.grepconsole.model;

import com.intellij.openapi.diagnostic.Logger;

public class StreamBufferSettings extends DomainObject {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(StreamBufferSettings.class);

	private String currentlyPrintingDelta = "50";
	private String maxWaitTime = "500";

	private String sleepTimeWhenWasActive = "1";
	private String sleepTimeWhenIdle = "5";

	private boolean useForTests = false;

	public String getCurrentlyPrintingDelta() {
		return currentlyPrintingDelta;
	}

	public void setCurrentlyPrintingDelta(String currentlyPrintingDelta) {
		this.currentlyPrintingDelta = currentlyPrintingDelta;
	}

	public String getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(String maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public String getSleepTimeWhenWasActive() {
		return sleepTimeWhenWasActive;
	}

	public void setSleepTimeWhenWasActive(String sleepTimeWhenWasActive) {
		this.sleepTimeWhenWasActive = sleepTimeWhenWasActive;
	}

	public String getSleepTimeWhenIdle() {
		return sleepTimeWhenIdle;
	}

	public void setSleepTimeWhenIdle(String sleepTimeWhenIdle) {
		this.sleepTimeWhenIdle = sleepTimeWhenIdle;
	}


	public boolean isUseForTests() {
		return useForTests;
	}

	public void setUseForTests(final boolean useForTests) {
		this.useForTests = useForTests;
	}
}
