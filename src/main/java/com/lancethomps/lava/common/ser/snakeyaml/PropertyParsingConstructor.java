package com.lancethomps.lava.common.ser.snakeyaml;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import com.lancethomps.lava.common.properties.PropertyParser;

public class PropertyParsingConstructor extends Constructor {

  public static final Tag CUSTOM_TAG = new Tag("!property_parser");

  public PropertyParsingConstructor() {
    super();
    yamlConstructors.put(CUSTOM_TAG, new ConstructAndParseProperties());
  }

  private class ConstructAndParseProperties extends AbstractConstruct {

    @Override
    public Object construct(Node node) {
      String val = constructScalar((ScalarNode) node);
      return PropertyParser.parseAndReplaceWithProps(val, null, PropertyParser.getDefaultPropertyParserHelper(), true);
    }

  }

}
