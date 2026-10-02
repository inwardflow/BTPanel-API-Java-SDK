package net.heimeng.sdk.btapi.facade;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.heimeng.sdk.btapi.api.system.CheckPanelUpdateApi;
import net.heimeng.sdk.btapi.api.system.GetDiskInfoApi;
import net.heimeng.sdk.btapi.api.system.GetNetworkStatusApi;
import net.heimeng.sdk.btapi.api.system.GetSystemInfoApi;
import net.heimeng.sdk.btapi.api.system.GetTaskCountApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.system.DiskInfo;
import net.heimeng.sdk.btapi.model.system.NetworkStatus;
import net.heimeng.sdk.btapi.model.system.PanelUpdateInfo;
import net.heimeng.sdk.btapi.model.system.SystemInfo;

/**
 * 系统相关能力的门面入口。
 *
 * <p>用于聚合系统信息、磁盘状态、网络状态、任务数量和面板更新检查等常用能力。
 */
public final class SystemOperations extends AbstractOperations {

  public SystemOperations(BtClient client) {
    super(client);
  }

  public BtResult<SystemInfo> getSystemInfo() {
    return execute(new GetSystemInfoApi());
  }

  public CompletableFuture<BtResult<SystemInfo>> getSystemInfoAsync() {
    return executeAsync(new GetSystemInfoApi());
  }

  public BtResult<NetworkStatus> getNetworkStatus() {
    return execute(new GetNetworkStatusApi());
  }

  public BtResult<List<DiskInfo>> getDiskInfo() {
    return execute(new GetDiskInfoApi());
  }

  public BtResult<Integer> getTaskCount() {
    return execute(new GetTaskCountApi());
  }

  public BtResult<PanelUpdateInfo> checkPanelUpdate() {
    return execute(new CheckPanelUpdateApi());
  }

  public BtResult<PanelUpdateInfo> checkPanelUpdate(boolean forceCheck) {
    return execute(new CheckPanelUpdateApi(forceCheck));
  }
}
