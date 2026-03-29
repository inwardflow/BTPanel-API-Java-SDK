package net.heimeng.sdk.btapi.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.heimeng.sdk.btapi.api.ftp.ChangeFtpPasswordApi;
import net.heimeng.sdk.btapi.api.ftp.CreateFtpAccountApi;
import net.heimeng.sdk.btapi.api.ftp.DeleteFtpAccountApi;
import net.heimeng.sdk.btapi.api.ftp.GetFtpAccountsApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ftp.FtpAccount;

@ExtendWith(MockitoExtension.class)
@DisplayName("FtpOperations facade tests")
class FtpOperationsTest {

  @Mock private BtClient client;

  @Test
  @DisplayName("list should delegate to GetFtpAccountsApi")
  void listDelegatesToClient() {
    FtpOperations operations = new FtpOperations(client);
    when(client.execute(any(GetFtpAccountsApi.class))).thenReturn(successListResponse());

    BtResult<List<FtpAccount>> result = operations.list();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    verify(client).execute(any(GetFtpAccountsApi.class));
  }

  @Test
  @DisplayName("create should delegate to CreateFtpAccountApi")
  void createDelegatesToClient() {
    FtpOperations operations = new FtpOperations(client);
    when(client.execute(any(CreateFtpAccountApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.create(new FtpCreateRequest("demo", "secret", "/www/wwwroot/demo", "Demo FTP"));

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                api ->
                    api.getEndpoint().equals("ftp?action=AddUser")
                        && "demo".equals(api.getParams().get("ftp_username"))
                        && "secret".equals(api.getParams().get("ftp_password"))
                        && "/www/wwwroot/demo".equals(api.getParams().get("path"))
                        && "Demo FTP".equals(api.getParams().get("ps"))));
  }

  @Test
  @DisplayName("delete should delegate to DeleteFtpAccountApi")
  void deleteDelegatesToClient() {
    FtpOperations operations = new FtpOperations(client);
    when(client.execute(any(DeleteFtpAccountApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result = operations.delete(new FtpDeleteRequest(1, "demo"));

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                api ->
                    api.getEndpoint().equals("ftp?action=DeleteUser")
                        && Integer.valueOf(1).equals(api.getParams().get("id"))
                        && "demo".equals(api.getParams().get("username"))));
  }

  @Test
  @DisplayName("updatePassword should delegate to ChangeFtpPasswordApi")
  void updatePasswordDelegatesToClient() {
    FtpOperations operations = new FtpOperations(client);
    when(client.execute(any(ChangeFtpPasswordApi.class))).thenReturn(successBoolean());

    BtResult<Boolean> result =
        operations.updatePassword(
            new FtpPasswordUpdateRequest(1, "demo", "/www/wwwroot/demo", "new-secret"));

    assertTrue(result.isSuccess());
    verify(client)
        .execute(
            argThat(
                api ->
                    api.getEndpoint().equals("ftp?action=SetUser")
                        && Integer.valueOf(1).equals(api.getParams().get("id"))
                        && "demo".equals(api.getParams().get("ftp_username"))
                        && "new-secret".equals(api.getParams().get("new_password"))
                        && "/www/wwwroot/demo".equals(api.getParams().get("path"))));
  }

  private static BtResult<Boolean> successBoolean() {
    BtResult<Boolean> response = new BtResult<>();
    response.setStatus(true);
    response.setData(true);
    return response;
  }

  private static BtResult<List<FtpAccount>> successListResponse() {
    FtpAccount ftpAccount = new FtpAccount();
    ftpAccount.setId(1);
    ftpAccount.setUsername("demo");
    ftpAccount.setPath("/www/wwwroot/demo");

    BtResult<List<FtpAccount>> response = new BtResult<>();
    response.setStatus(true);
    response.setData(List.of(ftpAccount));
    return response;
  }
}
