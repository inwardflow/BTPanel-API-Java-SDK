package net.heimeng.sdk.btapi.facade;

import java.util.List;
import java.util.Map;

import net.heimeng.sdk.btapi.api.website.AddWebsiteDomainApi;
import net.heimeng.sdk.btapi.api.website.CloseWebsitePasswordApi;
import net.heimeng.sdk.btapi.api.website.CloseWebsiteSslApi;
import net.heimeng.sdk.btapi.api.website.CreateWebsiteApi;
import net.heimeng.sdk.btapi.api.website.CreateWebsiteBackupApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteBackupApi;
import net.heimeng.sdk.btapi.api.website.DeleteWebsiteDomainApi;
import net.heimeng.sdk.btapi.api.website.GetPhpVersionsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteBackupsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteConfigApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteDetailApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteDomainsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteLimitNetApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteListApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteNginxConfigApi;
import net.heimeng.sdk.btapi.api.website.GetWebsitePhpExtensionsApi;
import net.heimeng.sdk.btapi.api.website.GetWebsitePhpVersionApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteRewriteRulesApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteRootPathApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteSslListApi;
import net.heimeng.sdk.btapi.api.website.GetWebsiteTypesApi;
import net.heimeng.sdk.btapi.api.website.GetWebsitesApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteLimitNetApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteLogsApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteNginxConfigApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePasswordApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePhpExtensionsApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePhpVersionApi;
import net.heimeng.sdk.btapi.api.website.SetWebsitePsApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteRewriteRulesApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteRootPathApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteRunPathApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteSslApi;
import net.heimeng.sdk.btapi.api.website.SetWebsiteUserIniApi;
import net.heimeng.sdk.btapi.api.website.StartWebsiteApi;
import net.heimeng.sdk.btapi.api.website.StopWebsiteApi;
import net.heimeng.sdk.btapi.client.BtClient;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.website.CreateWebsiteResult;
import net.heimeng.sdk.btapi.model.website.PhpVersion;
import net.heimeng.sdk.btapi.model.website.WebsiteInfo;
import net.heimeng.sdk.btapi.model.website.WebsiteType;

/**
 * 网站相关能力的门面入口。
 *
 * <p>该门面覆盖网站生命周期管理、域名与配置维护、PHP 设置、限流和备份等常用能力， 适合作为上层业务访问网站接口的统一入口。
 */
public final class WebsiteOperations extends AbstractOperations {

  public WebsiteOperations(BtClient client) {
    super(client);
  }

  public BtResult<List<WebsiteInfo>> list() {
    return execute(new GetWebsitesApi());
  }

  public BtResult<List<WebsiteInfo>> list(int page, int limit) {
    return execute(new GetWebsitesApi(page, limit));
  }

  public BtResult<List<Map<String, Object>>> listRaw(
      Integer page, Integer limit, Integer type, String order, String search) {
    return execute(
        new GetWebsiteListApi()
            .setPage(page)
            .setLimit(limit)
            .setType(type)
            .setOrder(order)
            .setSearch(search));
  }

  /**
   * @deprecated use {@link #listRaw(Integer, Integer, Integer, String, String)} instead.
   */
  @Deprecated
  public BtResult<List<Map<String, Object>>> rawList(
      Integer page, Integer limit, Integer type, String order, String search) {
    return listRaw(page, limit, type, order, search);
  }

  public BtResult<List<WebsiteType>> listTypes() {
    return execute(new GetWebsiteTypesApi());
  }

  /**
   * @deprecated use {@link #listTypes()} instead.
   */
  @Deprecated
  public BtResult<List<WebsiteType>> getTypes() {
    return listTypes();
  }

  public BtResult<List<PhpVersion>> listPhpVersions() {
    return execute(new GetPhpVersionsApi());
  }

  /**
   * @deprecated use {@link #listPhpVersions()} instead.
   */
  @Deprecated
  public BtResult<List<PhpVersion>> getPhpVersions() {
    return listPhpVersions();
  }

  public BtResult<CreateWebsiteResult> create(CreateWebsiteApi api) {
    return execute(api);
  }

  public BtResult<CreateWebsiteResult> create(
      String domain, String path, int typeId, String phpVersion, int port, String ps) {
    return execute(new CreateWebsiteApi(domain, path, typeId, phpVersion, port, ps));
  }

  public BtResult<Map<String, Object>> getDetail(int id) {
    return execute(new GetWebsiteDetailApi().setId(id));
  }

  public BtResult<Map<String, Object>> getConfig(Integer id, String path) {
    return execute(new GetWebsiteConfigApi().setId(id).setPath(path));
  }

  public BtResult<List<Map<String, Object>>> listDomains(int siteId) {
    return execute(new GetWebsiteDomainsApi().setSiteId(siteId));
  }

  /**
   * @deprecated use {@link #listDomains(int)} instead.
   */
  @Deprecated
  public BtResult<List<Map<String, Object>>> getDomains(int siteId) {
    return listDomains(siteId);
  }

  public BtResult<Boolean> addDomain(int id, String webname, String domain) {
    return execute(new AddWebsiteDomainApi().setId(id).setWebname(webname).setDomain(domain));
  }

  public BtResult<Boolean> deleteDomain(int id, String webname, String domain, Integer port) {
    return execute(
        new DeleteWebsiteDomainApi().setId(id).setWebname(webname).setDomain(domain).setPort(port));
  }

  public BtResult<Boolean> delete(
      int id, String webname, boolean deleteFtp, boolean deleteDatabase, boolean deletePath) {
    return execute(
        new DeleteWebsiteApi(id, webname)
            .setDeleteFtp(deleteFtp)
            .setDeleteDatabase(deleteDatabase)
            .setDeletePath(deletePath));
  }

  public BtResult<Boolean> start(int id) {
    return execute(new StartWebsiteApi().setId(id));
  }

  public BtResult<Boolean> stop(int id) {
    return execute(new StopWebsiteApi().setId(id));
  }

  public BtResult<Boolean> setRemark(int id, String ps) {
    return execute(new SetWebsitePsApi().setId(id).setPs(ps));
  }

  public BtResult<String> getRootPath(int id) {
    return execute(new GetWebsiteRootPathApi().setId(id));
  }

  public BtResult<Boolean> setRootPath(int id, String path) {
    return execute(new SetWebsiteRootPathApi().setId(id).setPath(path));
  }

  public BtResult<Boolean> setRunPath(int id, String runPath) {
    return execute(new SetWebsiteRunPathApi().setId(id).setRunPath(runPath));
  }

  public BtResult<Boolean> setUserIni(String path) {
    return execute(new SetWebsiteUserIniApi().setPath(path));
  }

  public BtResult<String> getPhpVersion(int id) {
    return execute(new GetWebsitePhpVersionApi().setId(id));
  }

  public BtResult<Boolean> setPhpVersion(int id, String phpVersion) {
    return execute(new SetWebsitePhpVersionApi().setId(id).setPhpVersion(phpVersion));
  }

  public BtResult<List<Map<String, Object>>> listPhpExtensions(int id) {
    return execute(new GetWebsitePhpExtensionsApi().setId(id));
  }

  /**
   * @deprecated use {@link #listPhpExtensions(int)} instead.
   */
  @Deprecated
  public BtResult<List<Map<String, Object>>> getPhpExtensions(int id) {
    return listPhpExtensions(id);
  }

  public BtResult<Boolean> setPhpExtension(int id, String moduleName, boolean enabled) {
    return execute(
        new SetWebsitePhpExtensionsApi().setId(id).setModuleName(moduleName).setEnabled(enabled));
  }

  public BtResult<String> getRewriteRules(int id) {
    return execute(new GetWebsiteRewriteRulesApi().setId(id));
  }

  public BtResult<Boolean> setRewriteRules(int id, String name, String content) {
    return execute(new SetWebsiteRewriteRulesApi().setId(id).setName(name).setContent(content));
  }

  public BtResult<String> getNginxConfig(Integer id, String domain) {
    return execute(new GetWebsiteNginxConfigApi().setId(id).setDomain(domain));
  }

  public BtResult<Boolean> setNginxConfig(Integer id, String domain, String content) {
    return execute(new SetWebsiteNginxConfigApi().setId(id).setDomain(domain).setContent(content));
  }

  public BtResult<Boolean> setPassword(int id, String username, String password) {
    return execute(
        new SetWebsitePasswordApi().setId(id).setUsername(username).setPassword(password));
  }

  public BtResult<Boolean> closePassword(int id) {
    return execute(new CloseWebsitePasswordApi().setId(id));
  }

  public BtResult<Boolean> setSsl(
      int id, String domain, String cert, String key, Boolean forceHttps) {
    return execute(
        new SetWebsiteSslApi()
            .setId(id)
            .setDomain(domain)
            .setCert(cert)
            .setKey(key)
            .setForceHttps(forceHttps));
  }

  public BtResult<Boolean> closeSsl(int id) {
    return execute(new CloseWebsiteSslApi().setId(id));
  }

  public BtResult<List<Map<String, Object>>> listSslCertificates(int id) {
    return execute(new GetWebsiteSslListApi().setId(id));
  }

  /**
   * @deprecated use {@link #listSslCertificates(int)} instead.
   */
  @Deprecated
  public BtResult<List<Map<String, Object>>> getSslList(int id) {
    return listSslCertificates(id);
  }

  public BtResult<Boolean> setLogs(int id) {
    return execute(new SetWebsiteLogsApi().setId(id));
  }

  public BtResult<Map<String, Object>> getLimitNet(int id) {
    return execute(new GetWebsiteLimitNetApi().setId(id));
  }

  public BtResult<Boolean> setLimitNet(
      int id, Boolean enabled, Integer perserver, Integer perip, Integer limitRate) {
    return execute(
        new SetWebsiteLimitNetApi()
            .setId(id)
            .setEnabled(enabled)
            .setPerserver(perserver)
            .setPerip(perip)
            .setLimitRate(limitRate));
  }

  public BtResult<List<Map<String, Object>>> listBackups(
      Integer siteId, Integer page, Integer limit, String callback) {
    return execute(
        new GetWebsiteBackupsApi()
            .setPage(page)
            .setLimit(limit)
            .setSiteId(siteId)
            .setCallback(callback));
  }

  /**
   * @deprecated use {@link #listBackups(Integer, Integer, Integer, String)} instead.
   */
  @Deprecated
  public BtResult<List<Map<String, Object>>> getBackups(
      Integer page, Integer limit, Integer siteId, String callback) {
    return listBackups(siteId, page, limit, callback);
  }

  public BtResult<Boolean> createBackup(int id) {
    return execute(new CreateWebsiteBackupApi().setId(id));
  }

  public BtResult<Boolean> deleteBackup(int id) {
    return execute(new DeleteWebsiteBackupApi().setId(id));
  }
}
