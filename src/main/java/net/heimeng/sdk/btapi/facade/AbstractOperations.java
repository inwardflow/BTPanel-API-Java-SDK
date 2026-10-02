package net.heimeng.sdk.btapi.facade;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.heimeng.sdk.btapi.api.BtApi;
import net.heimeng.sdk.btapi.client.BtClient;

/**
 * 领域门面的公共基类。
 *
 * <p>该基类负责封装底层客户端的同步与异步执行入口，避免每个门面重复维护同样的委托逻辑。
 */
abstract class AbstractOperations {

  private final BtClient client;

  AbstractOperations(BtClient client) {
    this.client = Objects.requireNonNull(client, "client cannot be null");
  }

  protected final <T> T execute(BtApi<T> api) {
    return client.execute(api);
  }

  protected final <T> CompletableFuture<T> executeAsync(BtApi<T> api) {
    return client.executeAsync(api);
  }
}
