package net.heimeng.sdk.btapi.api.website;

/**
 * 获取指定 PHP 版本运行配置的 API。
 *
 * <p>对应面板 9.0 软件商店“PHP 设置 &gt; 安装扩展”等页签加载时的请求：{@code ajax?action=GetPHPConfig}，参数为 PHP 版本号 {@code
 * version}（例如 {@code 81}）。返回该 PHP 版本的配置对象，包含禁用函数 {@code disable_functions}、扩展列表等字段，SDK 以原始 {@code
 * Map} 形式返回。
 *
 * <p>PHP 扩展属于某个 PHP 版本而不是某个站点，面板没有按站点查询或切换扩展的接口。
 */
public class GetPhpRuntimeConfigApi extends AbstractWebsiteMapQueryApi {

  private static final String ENDPOINT = "ajax?action=GetPHPConfig";

  public GetPhpRuntimeConfigApi() {
    super(ENDPOINT, "PHP config", "Success", "Failed to fetch PHP config", true);
  }

  /**
   * 设置 PHP 版本号。
   *
   * @param version 不带点的版本号，例如 {@code 81} 表示 PHP 8.1
   * @return 当前 API 实例
   */
  public GetPhpRuntimeConfigApi setVersion(String version) {
    addParam("version", version);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return params.get("version") instanceof String version && version.matches("\\d{2}");
  }
}
