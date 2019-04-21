package krasa.grepconsole.model;

import com.intellij.openapi.diagnostic.Logger;

public class StreamBufferSettings extends DomainObject {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(StreamBufferSettings.class);

	public static final String CURRENTLY_PRINTING_DELTA = "50";
	public static final String MAX_WAIT_TIME = "500";

	public static final String SLEEP_TIME_WHEN_WAS_ACTIVE = "10";
	public static final String SLEEP_TIME_WHEN_IDLE = "50";

	private String currentlyPrintingDelta = CURRENTLY_PRINTING_DELTA;
	private String maxWaitTime = MAX_WAIT_TIME;

	private String sleepTimeWhenWasActive = SLEEP_TIME_WHEN_WAS_ACTIVE;
	private String sleepTimeWhenIdle = SLEEP_TIME_WHEN_IDLE;

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
