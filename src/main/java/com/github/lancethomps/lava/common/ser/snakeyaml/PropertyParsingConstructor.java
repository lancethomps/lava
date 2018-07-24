package com.github.lancethomps.lava.common.ser.snakeyaml;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import com.github.lancethomps.lava.common.properties.PropertyParser;

/**
 * The Class PropertyParsingConstructor.
 */
public class PropertyParsingConstructor extends Constructor {

	/** The Constant CUSTOM_TAG. */
	public static final Tag CUSTOM_TAG = new Tag("!property_parser");

	/**
	 * Instantiates a new property parsing constructor.
	 */
	public PropertyParsingConstructor() {
		super();
		yamlConstructors.put(CUSTOM_TAG, new ConstructAndParseProperties());
	}

	/**
	 * The Class ConstructAndParseProperties.
	 */
	private class ConstructAndParseProperties extends AbstractConstruct {

		/*
		 * (non-Javadoc)
		 * @see org.yaml.snakeyaml.constructor.Construct#construct(org.yaml.snakeyaml.nodes.Node)
		 */
		@Override
		public Object construct(Node node) {
			String val = (String) constructScalar((ScalarNode) node);
			return PropertyParser.parseAndReplaceWithProps(val, null, PropertyParser.getDefaultPropertyParserHelper(), true);
		}
	}

}
