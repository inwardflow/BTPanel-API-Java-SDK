package net.heimeng.sdk.btapi.client;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.exception.BtApiException;
import net.heimeng.sdk.btapi.facade.DatabaseOperations;
import net.heimeng.sdk.btapi.facade.FileOperations;
import net.heimeng.sdk.btapi.facade.FtpOperations;
import net.heimeng.sdk.btapi.facade.SslOperations;
import net.heimeng.sdk.btapi.facade.SystemOperations;
import net.heimeng.sdk.btapi.facade.WebsiteOperations;

/**
 * 基于 {@link BtClient} 构建的高层 SDK 入口。
 *
 * <p>该类保留底层通用执行能力，同时为系统、网站、数据库、文件、FTP 和 SSL 等常用领域 提供语义化门面，适合作为业务代码的默认入口。
 */
public final class BtApiManager implements AutoCloseable {

  private static final long DEFAULT_TIMEOUT_MILLIS = 60_000L;

  private final BtClient client;
  private final SystemOperations systemOperations;
  private final WebsiteOperations websiteOperations;
  private final DatabaseOperations databaseOperations;
  private final FileOperations fileOperations;
  private final FtpOperations ftpOperations;
  private final SslOperations sslOperations;

  public BtApiManager(BtClient client) {
    this.client = Objects.requireNonNull(client, "client cannot be null");
    this.systemOperations = new SystemOperations(client);
    this.websiteOperations = new WebsiteOperations(client);
    this.databaseOperations = new DatabaseOperations(client);
    this.fileOperations = new FileOperations(client);
    this.ftpOperations = new FtpOperations(client);
    this.sslOperations = new SslOperations(client);
  }

  /** 同步执行底层 API 请求。 */
  public <T> T execute(BtApi<T> api) {
    return client.execute(api);
  }

  /** 异步执行底层 API 请求。 */
  public <T> CompletableFuture<T> executeAsync(BtApi<T> api) {
    return client.executeAsync(api);
  }

  /** 异步执行 API 请求，并在给定超时时间内等待结果。 */
  public <T> T executeAsyncWithTimeout(BtApi<T> api, long timeout, TimeUnit unit) {
    try {
      return executeAsync(api).get(timeout, unit);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new BtApiException("API execution interrupted", exception);
    } catch (ExecutionException exception) {
      Throwable cause = exception.getCause();
      if (cause instanceof BtApiException) {
        throw (BtApiException) cause;
      }
      throw new BtApiException("API execution failed", cause);
    } catch (TimeoutException exception) {
      throw new BtApiException(
          "API execution timed out after " + timeout + " " + unit.name().toLowerCase(), exception);
    }
  }

  /** 使用默认超时时间异步执行 API 请求。 */
  public <T> T executeAsyncWithTimeout(BtApi<T> api) {
    return executeAsyncWithTimeout(api, DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
  }

  /** 使用 {@link Duration} 指定超时时长来异步执行 API 请求。 */
  public <T> T executeAsyncWithTimeout(BtApi<T> api, Duration timeout) {
    return executeAsyncWithTimeout(api, timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  /** 返回一个带超时策略的异步执行结果。 */
  public <T> CompletableFuture<T> executeAsyncWithTimeoutFuture(BtApi<T> api, Duration timeout) {
    return executeAsync(api).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  /** 获取系统相关操作入口。 */
  public SystemOperations system() {
    return systemOperations;
  }

  /** 获取网站相关操作入口。 */
  public WebsiteOperations website() {
    return websiteOperations;
  }

  /** 获取数据库相关操作入口。 */
  public DatabaseOperations database() {
    return databaseOperations;
  }

  /** 获取文件相关操作入口。 */
  public FileOperations file() {
    return fileOperations;
  }

  /** 获取 FTP 相关操作入口。 */
  public FtpOperations ftp() {
    return ftpOperations;
  }

  /** 获取 SSL 相关操作入口。 */
  public SslOperations ssl() {
    return sslOperations;
  }

  /** 获取底层客户端实例。 */
  public BtClient getClient() {
    return client;
  }

  @Override
  public void close() {
    client.close();
  }
}
