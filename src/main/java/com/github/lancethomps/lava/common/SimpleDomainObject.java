package com.github.lancethomps.lava.common;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.lancethomps.lava.common.ser.PostConstructor;
import com.github.lancethomps.lava.common.ser.Serializer;

/**
 * The Class SimpleDomainObject.
 *
 * @author lathomps
 */
@SuppressWarnings("serial")
public class SimpleDomainObject extends PostConstructor implements Serializable {

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return Serializer.toLogString(this);
	}

}
