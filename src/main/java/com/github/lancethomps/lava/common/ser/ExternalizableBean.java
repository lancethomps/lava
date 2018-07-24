package com.github.lancethomps.lava.common.ser;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The Class SerializableBean.
 */
// @JsonDeserialize(converter = ExternalizableBeanConverter.class, using = CustomDelegatingDeserializer.class)
// @JsonDeserialize(converter = ExternalizableBeanConverter.class)
public abstract class ExternalizableBean extends PostConstructor implements Externalizable, Serializable {

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Serializer.readExternal(in, this);
	}

	@Override
	public String toString() {
		return Serializer.toLogString(this);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		Serializer.writeExternal(out, this);
	}

}
