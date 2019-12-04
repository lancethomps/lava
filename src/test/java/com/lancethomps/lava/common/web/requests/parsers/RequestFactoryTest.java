package com.lancethomps.lava.common.web.requests.parsers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.lancethomps.lava.common.BaseTest;
import com.lancethomps.lava.common.TestingCommon;
import com.lancethomps.lava.common.collections.MapUtil;
import com.lancethomps.lava.common.ser.Serializer;

// TODO: add test for fields with prefix
public class RequestFactoryTest extends BaseTest {

  private static final Logger LOG = Logger.getLogger(RequestFactoryTest.class);

  @Test
  public void createFromPathKeyParamsTest() throws Exception {
    final Map<String, Object> manualBean = Serializer.readJsonAsMap(getClass().getResourceAsStream("/request-factory/path-key-test-data.json"));
    final Map<String, Object> requestBean = RequestFactory.createFromPathKeyParams(
      MapUtil
        .createFromQueryString("data.name=test&data.type=PREFERENCE&data.children[0].name=testChild&data.children[0].type=PREFERENCE_CHILD")
        .entrySet(),
      Serializer.MAP_TYPE,
      "data."
    );
    requestBean.remove("@type");
    TestingCommon.assertEqualsViaJsonDiff("HTTP Request Path Key Object Creation", manualBean, requestBean);
  }

  @Test
  public void createFromRequestsParameter() throws Exception {
    String json =
      IOUtils.toString(RequestFactoryTest.class.getResourceAsStream("/request-factory/requests-parameter-test-data.json"), StandardCharsets.UTF_8);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter(
      "requests",
      json
    );
    List<MockRequestWithAllowedJsonParam> parsedFromRequest =
      RequestFactory.createBeansFromRequest(null, MockRequestWithAllowedJsonParam.class, request, true, true);
    TestingCommon.assertEqualsViaJsonDiff(
      "Requests parsed from disallowed HTTP 'requests' parameter were not empty.",
      Arrays.asList(new MockRequestWithAllowedJsonParam()),
      parsedFromRequest
    );

    RequestFactory.addAllowJsonParamTypes(MockRequestWithAllowedJsonParam.class);
    parsedFromRequest = RequestFactory.createBeansFromRequest(null, MockRequestWithAllowedJsonParam.class, request, true, true);
    List<MockRequestWithAllowedJsonParam> parsedFromJson = Serializer.readJsonAsList(json, MockRequestWithAllowedJsonParam.class);
    TestingCommon.assertEqualsViaJsonDiff(
      "Requests parsed from allowed HTTP 'requests' parameter did not match requests deserialized from JSON.",
      parsedFromJson,
      parsedFromRequest
    );
  }

}
