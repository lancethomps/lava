package com.github.lancethomps.lava.common.ser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.jcs.io.ObjectInputStreamClassLoaderAware;
import org.junit.Test;

import com.github.lancethomps.lava.common.BaseTest;
import com.github.lancethomps.lava.common.FailOnLogErrorAppender;
import com.github.lancethomps.lava.common.Randoms;
import com.github.lancethomps.lava.common.TestingCommon;

/**
 * The Class SerializerExternalizableTest.
 */
public class SerializerExternalizableTest extends BaseTest {

	/**
	 * De serialize.
	 *
	 * @param <T> the generic type
	 * @param data the data
	 * @param loader the loader
	 * @return the t
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public <T> T deSerialize(byte[] data, ClassLoader loader)
		throws IOException,
		ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		BufferedInputStream bis = new BufferedInputStream(bais);
		try (ObjectInputStream ois = new ObjectInputStreamClassLoaderAware(bis, loader)) {
			@SuppressWarnings("unchecked") // Need to cast from Object
			T readObject = (T) ois.readObject();
			return readObject;
		}
	}

	/**
	 * Ensure type property is not included after deserialization.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void ensureTypePropertyIsNotIncludedAfterDeserialization() throws Exception {
		FailOnLogErrorAppender.attach();
		try {
			Object inner = Randoms.createRandomValue(String.class);
			ExternalizableBeanTest bean = new ExternalizableBeanTest(inner);
			byte[] ser = serialize(bean);
			ExternalizableBeanTest deser = deSerialize(ser, null);
			TestingCommon.assertEqualsViaJsonDiff("Externalized bean does not match original.", bean, deser);
		} finally {
			FailOnLogErrorAppender.detach();
		}
	}

	/**
	 * Serialize.
	 *
	 * @param <T> the generic type
	 * @param obj the obj
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public <T> byte[] serialize(T obj)
		throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(obj);
		}

		return baos.toByteArray();
	}

}
