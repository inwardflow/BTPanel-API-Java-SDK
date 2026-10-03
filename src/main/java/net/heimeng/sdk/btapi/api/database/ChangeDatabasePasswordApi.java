package net.heimeng.sdk.btapi.api.database;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import net.heimeng.sdk.btapi.api.BaseBtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.model.BtResult;

/**
 * 修改数据库密码API实现
 *
 * <p>对应面板 9.0 数据库列表“改密”操作（MySQL）：{@code database?action=ResDatabasePassword}，参数为数据库 {@code id}、
 * 数据库用户名 {@code name}、新密码 {@code password} 和数据库名 {@code data_name}。旧版本 SDK 使用的 {@code
 * database?action=ChangeDBPassword} 不在 9.0 UI 中。
 *
 * @author InwardFlow
 * @since 2.0.0
 */
public class ChangeDatabasePasswordApi extends BaseBtApi<BtResult<Boolean>> {

  /** API端点路径 */
  private static final String ENDPOINT = "database?action=ResDatabasePassword";

  /**
   * 构造函数，按面板 UI 的参数创建修改数据库密码请求。
   *
   * @param databaseId 数据库 ID（数据库列表中的 {@code id}）
   * @param databaseName 数据库名称
   * @param username 数据库用户名
   * @param newPassword 新密码
   */
  public ChangeDatabasePasswordApi(
      int databaseId, String databaseName, String username, String newPassword) {
    this(databaseName, username, newPassword);
    setDatabaseId(databaseId);
  }

  /**
   * 构造函数，创建不带数据库 ID 的请求。
   *
   * @param databaseName 数据库名称
   * @param username 数据库用户名
   * @param newPassword 新密码
   * @deprecated 面板 UI 改密时会发送数据库 {@code id}，面板据此定位数据库。请改用 {@link #ChangeDatabasePasswordApi(int,
   *     String, String, String)}，或调用 {@link #setDatabaseId(int)}。
   */
  @Deprecated(since = "0.2.0", forRemoval = true)
  public ChangeDatabasePasswordApi(String databaseName, String username, String newPassword) {
    super(ENDPOINT, HttpMethod.POST);

    // 设置必需参数
    setDatabaseName(databaseName);
    setUsername(username);
    setNewPassword(newPassword);
  }

  /**
   * 设置数据库 ID。
   *
   * @param databaseId 数据库 ID，必须为正数
   * @return 当前API实例，支持链式调用
   */
  public ChangeDatabasePasswordApi setDatabaseId(int databaseId) {
    if (databaseId <= 0) {
      throw new IllegalArgumentException("Database id must be positive");
    }
    addParam("id", databaseId);
    return this;
  }

  /**
   * 设置数据库名称，对应请求参数 {@code data_name}。
   *
   * @param databaseName 数据库名称
   * @return 当前API实例，支持链式调用
   */
  public ChangeDatabasePasswordApi setDatabaseName(String databaseName) {
    if (StrUtil.isEmpty(databaseName)) {
      throw new IllegalArgumentException("Database name cannot be empty");
    }
    addParam("data_name", databaseName);
    return this;
  }

  /**
   * 设置数据库用户名，对应请求参数 {@code name}。
   *
   * @param username 数据库用户名
   * @return 当前API实例，支持链式调用
   */
  public ChangeDatabasePasswordApi setUsername(String username) {
    if (StrUtil.isEmpty(username)) {
      throw new IllegalArgumentException("Username cannot be empty");
    }
    addParam("name", username);
    return this;
  }

  /**
   * 设置数据库新密码
   *
   * @param newPassword 新密码
   * @return 当前API实例，支持链式调用
   */
  public ChangeDatabasePasswordApi setNewPassword(String newPassword) {
    if (StrUtil.isEmpty(newPassword)) {
      throw new IllegalArgumentException("New password cannot be empty");
    }
    addParam("password", newPassword);
    return this;
  }

  /**
   * 解析API响应字符串为{@code BtResult<Boolean>}对象
   *
   * @param response API响应字符串
   * @return {@code BtResult<Boolean>}对象，data为true表示修改密码成功
   * @throws BtApiException 当解析失败时抛出
   */
  @Override
  public BtResult<Boolean> parseResponse(String response) {
    if (response == null || response.isEmpty()) {
      throw new BtApiException("Empty response received");
    }

    try {
      if (!JSONUtil.isTypeJSON(response)) {
        throw new BtApiException("Invalid JSON response: " + response);
      }

      JSONObject json = JSONUtil.parseObj(response);
      BtResult<Boolean> result = new BtResult<>();

      // 检查响应状态
      boolean success = json.getBool("status", false);
      result.setStatus(success);
      result.setMsg(
          json.getStr(
              "msg",
              success
                  ? "Database password changed successfully"
                  : "Failed to change database password"));
      result.setData(success);

      return result;

    } catch (JSONException e) {
      throw new BtApiException("Invalid JSON response: " + response);
    } catch (Exception e) {
      throw new BtApiException(
          "Failed to parse change database password response: " + e.getMessage(), e);
    }
  }
}
