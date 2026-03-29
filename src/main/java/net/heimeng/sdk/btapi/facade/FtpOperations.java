package net.heimeng.sdk.btapi.facade;

import java.util.List;
import java.util.Objects;

import net.heimeng.sdk.btapi.api.ftp.ChangeFtpPasswordApi;
import net.heimeng.sdk.btapi.api.ftp.CreateFtpAccountApi;
import net.heimeng.sdk.btapi.api.ftp.DeleteFtpAccountApi;
import net.heimeng.sdk.btapi.api.ftp.GetFtpAccountsApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ftp.FtpAccount;

/**
 * FTP 相关能力的门面入口。
 *
 * <p>封装 FTP 账号的列表查询、创建、删除和密码修改等常用操作。
 */
public final class FtpOperations extends AbstractOperations {

  public FtpOperations(BtClient client) {
    super(client);
  }

  public BtResult<List<FtpAccount>> list() {
    return execute(new GetFtpAccountsApi());
  }

  public BtResult<Boolean> create(FtpCreateRequest request) {
    Objects.requireNonNull(request, "request cannot be null");
    return execute(
        new CreateFtpAccountApi()
            .setUsername(request.username())
            .setPassword(request.password())
            .setPath(request.path())
            .setRemark(request.remark()));
  }

  public BtResult<Boolean> delete(FtpDeleteRequest request) {
    Objects.requireNonNull(request, "request cannot be null");
    return execute(new DeleteFtpAccountApi().setId(request.accountId()).setUsername(request.username()));
  }

  public BtResult<Boolean> updatePassword(FtpPasswordUpdateRequest request) {
    Objects.requireNonNull(request, "request cannot be null");
    return execute(
        new ChangeFtpPasswordApi()
            .setId(request.accountId())
            .setUsername(request.username())
            .setNewPassword(request.newPassword())
            .setPath(request.path()));
  }
}
