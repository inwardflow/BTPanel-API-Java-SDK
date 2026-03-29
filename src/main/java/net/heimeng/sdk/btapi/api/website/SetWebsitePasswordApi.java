package net.heimeng.sdk.btapi.api.website;

/** 设置站点访问密码保护的 API。 */
public class SetWebsitePasswordApi extends AbstractWebsiteBooleanApi {

  private static final String ENDPOINT = "site?action=SetHasPwd";

  public SetWebsitePasswordApi() {
    super(ENDPOINT, "密码访问设置成功", "密码访问设置失败");
  }

  public SetWebsitePasswordApi setId(Integer id) {
    requirePositiveInteger(id, "id");
    addParam("id", id);
    return this;
  }

  public SetWebsitePasswordApi setUsername(String username) {
    requireNonBlank(username, "username");
    addParam("username", username);
    return this;
  }

  public SetWebsitePasswordApi setPassword(String password) {
    requireNonBlank(password, "password");
    addParam("password", password);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasPositiveIntegerParam("id") && hasRequiredParams("username", "password");
  }
}
