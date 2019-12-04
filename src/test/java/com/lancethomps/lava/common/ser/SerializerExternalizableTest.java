package com.lancethomps.lava.common.ser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.jcs.io.ObjectInputStreamClassLoaderAware;
import org.junit.Test;

import com.lancethomps.lava.common.BaseTest;
import com.lancethomps.lava.common.FailOnLogErrorAppender;
import com.lancethomps.lava.common.Randoms;
import com.lancethomps.lava.common.TestingCommon;

public class SerializerExternalizableTest extends BaseTest {

  public <T> T deSerialize(byte[] data, ClassLoader loader)
    throws IOException,
           ClassNotFoundException {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    BufferedInputStream bis = new BufferedInputStream(bais);
    try (ObjectInputStream ois = new ObjectInputStreamClassLoaderAware(bis, loader)) {
      @SuppressWarnings("unchecked")
      T readObject = (T) ois.readObject();
      return readObject;
    }
  }

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

  public <T> byte[] serialize(T obj)
    throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }

    return baos.toByteArray();
  }

}
