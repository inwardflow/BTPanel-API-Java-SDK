package net.heimeng.sdk.btapi.api.file;

/**
 * 解压文件的 API。
 *
 * <p>对应面板 9.0 文件管理的“解压”：{@code files?action=UnZip}，参数为压缩包路径 {@code sfile}、解压目录 {@code dfile}、
 * {@code type}、文件名编码 {@code coding}、解压密码 {@code password} 和解压后的目录权限 {@code power}。默认值与 UI 一致：
 * {@code type=zip}（UI 对 {@code .tar.gz} 同样发送 {@code zip}）、{@code coding=UTF-8}、空密码、{@code
 * power=755}。
 *
 * <p>旧版本 SDK 使用的 {@code files?action=UnCompress} 不在 9.0 UI 中。
 */
public class UncompressFileApi extends AbstractFileBooleanApi {

  private static final String ENDPOINT = "files?action=UnZip";

  public UncompressFileApi() {
    super(ENDPOINT, "解压成功", "解压失败");
    addParam("type", "zip");
    addParam("coding", "UTF-8");
    addParam("password", "");
    addParam("power", "755");
  }

  /**
   * 设置压缩包的完整路径，对应请求参数 {@code sfile}。
   *
   * @param path 压缩包路径，例如 {@code /www/backup/site.tar.gz}
   * @return 当前 API 实例
   */
  public UncompressFileApi setPath(String path) {
    addParam("sfile", path);
    return this;
  }

  /**
   * 设置解压目录，对应请求参数 {@code dfile}。
   *
   * @param target 解压目录，例如 {@code /www/wwwroot/site}
   * @return 当前 API 实例
   */
  public UncompressFileApi setTarget(String target) {
    addParam("dfile", target);
    return this;
  }

  /**
   * 设置解压密码，仅加密的压缩包需要。
   *
   * @param password 解压密码，无密码时传空字符串
   * @return 当前 API 实例
   */
  public UncompressFileApi setPassword(String password) {
    addParam("password", password == null ? "" : password);
    return this;
  }

  /**
   * 设置压缩包内文件名的编码。
   *
   * @param coding 编码，例如 {@code UTF-8} 或 {@code GBK}
   * @return 当前 API 实例
   */
  public UncompressFileApi setCoding(String coding) {
    addParam("coding", coding);
    return this;
  }

  /**
   * 设置解压后目录的权限。
   *
   * @param power 八进制权限，例如 {@code 755}
   * @return 当前 API 实例
   */
  public UncompressFileApi setPower(String power) {
    addParam("power", power);
    return this;
  }

  @Override
  protected boolean validateParams() {
    return hasRequiredParams("sfile", "dfile", "type", "coding", "power");
  }
}
