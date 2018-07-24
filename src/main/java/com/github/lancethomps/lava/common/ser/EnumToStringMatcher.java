package com.github.lancethomps.lava.common.ser;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

/**
 * Uses the toString() method of an Enum when reading/writing instead of the value.
 */
public class EnumToStringMatcher implements Matcher {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.simpleframework.xml.transform.Matcher#match(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Transform match(Class type) throws Exception {
		if (type.isEnum()) {
			return new EnumToStringTransform(type);
		}

		return null;
	}

	/**
	 * The Class EnumToStringTransform.
	 */
	@SuppressWarnings("rawtypes")
	public class EnumToStringTransform implements Transform<Enum> {

		/** The type. */
		private final Class type;

		/**
		 * Instantiates a new enum to string transform.
		 * 
		 * @param type the type
		 */
		public EnumToStringTransform(Class type) {
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.simpleframework.xml.transform.Transform#read(java.lang.String)
		 */
		@Override
		public Enum read(String value) throws Exception {
			for (Object o : type.getEnumConstants()) {
				if (o.toString().equals(value)) {
					return (Enum) o;
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.simpleframework.xml.transform.Transform#write(java.lang.Object)
		 */
		@Override
		public String write(Enum value) throws Exception {
			return value.toString();
		}
	}

}
