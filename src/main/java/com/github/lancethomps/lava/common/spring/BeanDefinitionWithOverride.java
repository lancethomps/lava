package com.github.lancethomps.lava.common.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

public class BeanDefinitionWithOverride<T> implements FactoryBean<T>, BeanFactoryAware {

  private BeanFactory beanFactory;

  private String defaultBeanName;

  private T defaultValue;

  private String overrideBeanName;

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  public String getDefaultBeanName() {
    return defaultBeanName;
  }

  public void setDefaultBeanName(String defaultBeanName) {
    this.defaultBeanName = defaultBeanName;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T getObject() throws Exception {
    if ((overrideBeanName != null) && beanFactory.containsBean(overrideBeanName)) {
      return (T) beanFactory.getBean(overrideBeanName);
    }
    if ((defaultBeanName != null) && beanFactory.containsBean(defaultBeanName)) {
      return (T) beanFactory.getBean(defaultBeanName);
    }
    return defaultValue;
  }

  @Override
  public Class<?> getObjectType() {
    return Object.class;
  }

  public String getOverrideBeanName() {
    return overrideBeanName;
  }

  public void setOverrideBeanName(String overrideBeanName) {
    this.overrideBeanName = overrideBeanName;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
