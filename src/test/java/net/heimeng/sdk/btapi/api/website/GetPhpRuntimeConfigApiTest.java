package net.heimeng.sdk.btapi.api.website;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.model.BtResult;

@DisplayName("GetPhpRuntimeConfigApi tests")
class GetPhpRuntimeConfigApiTest {

  @Test
  @DisplayName("targets ajax?action=GetPHPConfig with the version the UI sends")
  void contract() throws Exception {
    GetPhpRuntimeConfigApi api = new GetPhpRuntimeConfigApi().setVersion("81");

    assertEquals("ajax?action=GetPHPConfig", api.getEndpoint());
    assertEquals("81", api.getParams().get("version"));
    assertTrue(validate(api));
    assertFalse(validate(new GetPhpRuntimeConfigApi().setVersion("8.1")));
    assertFalse(validate(new GetPhpRuntimeConfigApi()));
  }

  @Test
  @DisplayName("returns the unwrapped config object as a map")
  void parsesUnwrappedConfig() {
    BtResult<Map<String, Object>> result =
        new GetPhpRuntimeConfigApi()
            .setVersion("81")
            .parseResponse("{\"disable_functions\": \"exec,system\", \"libs\": []}");

    assertTrue(result.isSuccess());
    assertEquals("exec,system", result.getData().get("disable_functions"));
  }

  private static boolean validate(GetPhpRuntimeConfigApi api) throws Exception {
    Method method = api.getClass().getDeclaredMethod("validateParams");
    method.setAccessible(true);
    return (boolean) method.invoke(api);
  }
}
