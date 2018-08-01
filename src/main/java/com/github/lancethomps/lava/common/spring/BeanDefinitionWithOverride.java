package com.github.lancethomps.lava.common.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * The Class BeanDefinitionWithOverride.
 *
 * @author lancethomps
 * @param <T> the generic type
 */
public class BeanDefinitionWithOverride<T> implements FactoryBean<T>, BeanFactoryAware {

	/** The bean factory. */
	private BeanFactory beanFactory;

	/** The default bean name. */
	private String defaultBeanName;

	/** The default value. */
	private T defaultValue;

	/** The override bean name. */
	private String overrideBeanName;

	/**
	 * @return the beanFactory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * @return the defaultBeanName
	 */
	public String getDefaultBeanName() {
		return defaultBeanName;
	}

	/**
	 * @return the defaultValue
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return Object.class;
	}

	/**
	 * @return the overrideBeanName
	 */
	public String getOverrideBeanName() {
		return overrideBeanName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets the default bean name.
	 *
	 * @param defaultBeanName the new default bean name
	 */
	public void setDefaultBeanName(String defaultBeanName) {
		this.defaultBeanName = defaultBeanName;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Sets the override bean name.
	 *
	 * @param overrideBeanName the new override bean name
	 */
	public void setOverrideBeanName(String overrideBeanName) {
		this.overrideBeanName = overrideBeanName;
	}
}
