package com.github.lancethomps.lava.common.logging;

import java.time.LocalDateTime;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;

/**
 * The Class LogLine.
 */
public class LogLine extends ExternalizableBean {

	/** The category. */
	private String category;

	/** The date. */
	private LocalDateTime date;

	/** The message. */
	private String message;

	/** The priority. */
	private String priority;

	/** The raw. */
	private String raw;

	/** The thread. */
	private String thread;

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return the date
	 */
	public LocalDateTime getDate() {
		return date;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * @return the raw
	 */
	public String getRaw() {
		return raw;
	}

	/**
	 * @return the thread
	 */
	public String getThread() {
		return thread;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	/**
	 * @param raw the raw to set
	 */
	public void setRaw(String raw) {
		this.raw = raw;
	}

	/**
	 * @param thread the thread to set
	 */
	public void setThread(String thread) {
		this.thread = thread;
	}
}
