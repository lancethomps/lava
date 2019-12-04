package com.lancethomps.lava.common.metrics;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lancethomps.lava.common.DynamicDataHandler;

public class HealthCheckOwner implements DynamicDataHandler {

  @JsonIgnore
  private Map<String, Object> data;

  private String deptCode;

  private String email;

  private String firstName;

  private String lastName;

  private String middleName;

  private String userId;

  @Override
  @JsonAnyGetter
  public Map<String, Object> getData() {
    return data;
  }

  public String getDeptCode() {
    return deptCode;
  }

  public HealthCheckOwner setDeptCode(String deptCode) {
    this.deptCode = deptCode;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public HealthCheckOwner setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public HealthCheckOwner setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public HealthCheckOwner setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getMiddleName() {
    return middleName;
  }

  public HealthCheckOwner setMiddleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public String getUserId() {
    return userId;
  }

  public HealthCheckOwner setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public <T extends DynamicDataHandler> T setData(Map<String, Object> data) {
    this.data = data;
    return (T) this;
  }

}
