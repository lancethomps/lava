package com.github.lancethomps.lava.common.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.lancethomps.lava.common.Checks;
import com.github.lancethomps.lava.common.format.Formatting;
import com.github.lancethomps.lava.common.merge.Merges;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.github.lancethomps.lava.common.ser.Serializer;
import com.github.lancethomps.lava.common.time.TimerHandlingBean;
import com.github.lancethomps.lava.common.web.requests.MissingRequestParameter;

/**
 * The Class AbstractApiResponse.
 *
 * @author lathomps
 */
@SuppressWarnings("unchecked")
public abstract class AbstractApiResponse extends ExternalizableBean implements TimerHandlingBean {

	/** The all data returned. */
	private Boolean allDataReturned;

	/** The calculation time. */
	private Long calculationTime;

	/** The debug message. */
	private String debugMessage;

	/** The failure reason. */
	private String failureReason;

	/** The messages. */
	private List<String> messages;

	/** The missing parameters. */
	private List<MissingRequestParameter> missingParameters;

	/** The success. */
	private Boolean success;

	/** The timer logs. */
	private Map<String, Long> timerLogs;

	/**
	 * Adds the debug message.
	 *
	 * @param <T> the generic type
	 * @param debugMessage the debug message
	 * @param formatArgs the format args
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T addDebugMessage(String debugMessage, Object... formatArgs) {
		if (debugMessage != null) {
			debugMessage = (formatArgs == null) || (formatArgs.length == 0) ? debugMessage : String.format(debugMessage, formatArgs);
			if (this.debugMessage != null) {
				debugMessage = this.debugMessage + System.lineSeparator() + debugMessage;
			}
			return setDebugMessage(debugMessage);
		}
		return (T) this;
	}

	/**
	 * Adds the message.
	 *
	 * @param <T> the generic type
	 * @param message the message
	 * @param formatArgs the format args
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T addMessage(String message, Object... formatArgs) {
		if (message != null) {
			if (messages == null) {
				messages = new ArrayList<>();
			}
			messages.add(Formatting.getMessage(message, formatArgs));
		}
		return (T) this;
	}

	/**
	 * Adds the messages.
	 *
	 * @param <T> the generic type
	 * @param messages the messages
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T addMessages(String... messages) {
		if (messages != null) {
			if (this.messages == null) {
				this.messages = new ArrayList<>();
			}
			Stream.of(messages).filter(Checks::nonNull).forEach(this.messages::add);
		}
		return (T) this;
	}

	/**
	 * Adds the missing parameter.
	 *
	 * @param <T> the generic type
	 * @param param the param
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T addMissingParameter(MissingRequestParameter param) {
		if (param != null) {
			if (getMissingParameters() == null) {
				setMissingParameters(new ArrayList<>());
			}
			getMissingParameters().add(param);
			String message = param.getMessage();
			addMessage(message);
			setSuccess(false);
			if (getFailureReason() == null) {
				setFailureReason(message);
			}
		}
		return (T) this;
	}

	/**
	 * Adds the missing parameter.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param type the type
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T addMissingParameter(String name, Class<?> type) {
		if (type == null) {
			return (T) this;
		}
		return addMissingParameter(name, type.getSimpleName());
	}

	/**
	 * Adds the missing parameter.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @param type the type
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T addMissingParameter(String name, String type) {
		if (name != null) {
			addMissingParameter(new MissingRequestParameter(name, type));
		}
		return (T) this;
	}

	/**
	 * Adds the other.
	 *
	 * @param <T> the generic type
	 * @param other the other
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T addOther(@Nonnull AbstractApiResponse other) {
		setAllDataReturned(Merges.combineBooleans(other, this, AbstractApiResponse::getAllDataReturned, false));
		setCalculationTime(Merges.combineNumericalValues(other, this, AbstractApiResponse::getCalculationTime));
		setDebugMessage(Merges.combineStrings(other, this, AbstractApiResponse::getDebugMessage));
		setFailureReason(Merges.combineStrings(other, this, AbstractApiResponse::getFailureReason));
		setMessages(Merges.combineCollections(other, this, AbstractApiResponse::getMessages));
		setMissingParameters(Merges.combineCollections(other, this, AbstractApiResponse::getMissingParameters));
		setSuccess(Merges.combineBooleans(other, this, AbstractApiResponse::getSuccess, false));
		addTimersFromOther(other);
		return (T) this;
	}

	/**
	 * Copy.
	 *
	 * @param <T> the generic type
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T copy() {
		return (T) Serializer.cloneViaJson(this, getClass());
	}

	/**
	 * Copy.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T copy(Class<T> type) {
		return Serializer.cloneViaJson(this, type);
	}

	/**
	 * Creates the failure reason.
	 *
	 * @param <T> the generic type
	 * @param failureReason the failure reason
	 * @param formatArgs the format args
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T createFailureReason(String failureReason, Object... formatArgs) {
		return setFailureReason(Formatting.getMessage(failureReason, formatArgs));
	}

	/**
	 * Gets the all data returned.
	 *
	 * @return the allDataReturned
	 */
	public Boolean getAllDataReturned() {
		return allDataReturned;
	}

	/**
	 * Gets the calculation time.
	 *
	 * @return the calculationTime
	 */
	public Long getCalculationTime() {
		return calculationTime;
	}

	/**
	 * Gets the debug message.
	 *
	 * @return the debugMessage
	 */
	public String getDebugMessage() {
		return debugMessage;
	}

	/**
	 * Gets the failure reason.
	 *
	 * @return the failureReason
	 */
	public String getFailureReason() {
		return failureReason;
	}

	/**
	 * Gets the messages.
	 *
	 * @return the messages
	 */
	public List<String> getMessages() {
		return messages;
	}

	/**
	 * Gets the missing parameters.
	 *
	 * @return the missingParameters
	 */
	public List<MissingRequestParameter> getMissingParameters() {
		return missingParameters;
	}

	/**
	 * Gets the success.
	 *
	 * @return the success
	 */
	public Boolean getSuccess() {
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerHandlingBean#getTimerLogs()
	 */
	@Override
	public Map<String, Long> getTimerLogs() {
		return timerLogs;
	}

	/**
	 * Checks if is all data returned.
	 *
	 * @return true, if is all data returned
	 */
	public boolean isAllDataReturned() {
		return (allDataReturned == null) || allDataReturned;
	}

	/**
	 * Checks if is success.
	 *
	 * @return true, if is success
	 */
	public boolean isSuccess() {
		return testSuccess();
	}

	/**
	 * Sets the all data returned.
	 *
	 * @param <T> the generic type
	 * @param allDataReturned the allDataReturned to set
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T setAllDataReturned(Boolean allDataReturned) {
		this.allDataReturned = allDataReturned;
		return (T) this;
	}

	/**
	 * Sets the calculation time.
	 *
	 * @param <T> the generic type
	 * @param calculationTime the calculationTime to set
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T setCalculationTime(Long calculationTime) {
		this.calculationTime = calculationTime;
		return (T) this;
	}

	/**
	 * Sets the debug message.
	 *
	 * @param <T> the generic type
	 * @param debugMessage the debugMessage to set
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T setDebugMessage(String debugMessage) {
		this.debugMessage = debugMessage;
		return (T) this;
	}

	/**
	 * Sets the failure reason.
	 *
	 * @param <T> the generic type
	 * @param failureReason the failureReason to set
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T setFailureReason(String failureReason) {
		this.failureReason = failureReason;
		return (T) this;
	}

	/**
	 * Sets the messages.
	 *
	 * @param <T> the generic type
	 * @param messages the messages to set
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T setMessages(List<String> messages) {
		this.messages = messages;
		return (T) this;
	}

	/**
	 * Sets the missing parameters.
	 *
	 * @param <T> the generic type
	 * @param missingParameters the missingParameters to set
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T setMissingParameters(List<MissingRequestParameter> missingParameters) {
		this.missingParameters = missingParameters;
		return (T) this;
	}

	/**
	 * Sets the success.
	 *
	 * @param <T> the generic type
	 * @param success the success to set
	 * @return the t
	 */
	public <T extends AbstractApiResponse> T setSuccess(Boolean success) {
		this.success = success;
		return (T) this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.github.lancethomps.lava.common.time.TimerHandlingBean#setTimerLogs(java.util.Map)
	 */
	@Override
	public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
		this.timerLogs = timerLogs;
		return (T) this;
	}

	/**
	 * Test all data returned.
	 *
	 * @return true, if successful
	 */
	public boolean testAllDataReturned() {
		return (allDataReturned == null) || allDataReturned.booleanValue();
	}

	/**
	 * Test success.
	 *
	 * @return true, if successful
	 */
	public boolean testSuccess() {
		return (success == null) || success.booleanValue();
	}

}
