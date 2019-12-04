package com.lancethomps.lava.common.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.format.Formatting;
import com.lancethomps.lava.common.merge.Merges;
import com.lancethomps.lava.common.ser.ExternalizableBean;
import com.lancethomps.lava.common.ser.Serializer;
import com.lancethomps.lava.common.time.TimerHandlingBean;
import com.lancethomps.lava.common.web.requests.MissingRequestParameter;

@SuppressWarnings("unchecked")
public abstract class AbstractApiResponse extends ExternalizableBean implements TimerHandlingBean {

  private Boolean allDataReturned;

  private Long calculationTime;

  private String debugMessage;

  private String failureReason;

  private List<String> messages;

  private List<MissingRequestParameter> missingParameters;

  private Boolean success;

  private Map<String, Long> timerLogs;

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

  public <T extends AbstractApiResponse> T addMessage(String message, Object... formatArgs) {
    if (message != null) {
      if (messages == null) {
        messages = new ArrayList<>();
      }
      messages.add(Formatting.getMessage(message, formatArgs));
    }
    return (T) this;
  }

  public <T extends AbstractApiResponse> T addMessages(String... messages) {
    if (messages != null) {
      if (this.messages == null) {
        this.messages = new ArrayList<>();
      }
      Stream.of(messages).filter(Checks::nonNull).forEach(this.messages::add);
    }
    return (T) this;
  }

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

  public <T extends AbstractApiResponse> T addMissingParameter(String name, Class<?> type) {
    if (type == null) {
      return (T) this;
    }
    return addMissingParameter(name, type.getSimpleName());
  }

  public <T extends AbstractApiResponse> T addMissingParameter(String name, String type) {
    if (name != null) {
      addMissingParameter(new MissingRequestParameter(name, type));
    }
    return (T) this;
  }

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

  public <T extends AbstractApiResponse> T copy() {
    return (T) Serializer.cloneViaJson(this, getClass());
  }

  public <T extends AbstractApiResponse> T copy(Class<T> type) {
    return Serializer.cloneViaJson(this, type);
  }

  public <T extends AbstractApiResponse> T createFailureReason(String failureReason, Object... formatArgs) {
    return setFailureReason(Formatting.getMessage(failureReason, formatArgs));
  }

  public Boolean getAllDataReturned() {
    return allDataReturned;
  }

  public Long getCalculationTime() {
    return calculationTime;
  }

  public <T extends AbstractApiResponse> T setCalculationTime(Long calculationTime) {
    this.calculationTime = calculationTime;
    return (T) this;
  }

  public String getDebugMessage() {
    return debugMessage;
  }

  public <T extends AbstractApiResponse> T setDebugMessage(String debugMessage) {
    this.debugMessage = debugMessage;
    return (T) this;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public <T extends AbstractApiResponse> T setFailureReason(String failureReason) {
    this.failureReason = failureReason;
    return (T) this;
  }

  public List<String> getMessages() {
    return messages;
  }

  public <T extends AbstractApiResponse> T setMessages(List<String> messages) {
    this.messages = messages;
    return (T) this;
  }

  public List<MissingRequestParameter> getMissingParameters() {
    return missingParameters;
  }

  public <T extends AbstractApiResponse> T setMissingParameters(List<MissingRequestParameter> missingParameters) {
    this.missingParameters = missingParameters;
    return (T) this;
  }

  public Boolean getSuccess() {
    return success;
  }

  @Override
  public Map<String, Long> getTimerLogs() {
    return timerLogs;
  }

  public boolean isAllDataReturned() {
    return (allDataReturned == null) || allDataReturned;
  }

  public <T extends AbstractApiResponse> T setAllDataReturned(Boolean allDataReturned) {
    this.allDataReturned = allDataReturned;
    return (T) this;
  }

  public boolean isSuccess() {
    return testSuccess();
  }

  public <T extends AbstractApiResponse> T setSuccess(Boolean success) {
    this.success = success;
    return (T) this;
  }

  @Override
  public <T extends TimerHandlingBean> T setTimerLogs(Map<String, Long> timerLogs) {
    this.timerLogs = timerLogs;
    return (T) this;
  }

  public boolean testAllDataReturned() {
    return (allDataReturned == null) || allDataReturned.booleanValue();
  }

  public boolean testSuccess() {
    return (success == null) || success.booleanValue();
  }

}
