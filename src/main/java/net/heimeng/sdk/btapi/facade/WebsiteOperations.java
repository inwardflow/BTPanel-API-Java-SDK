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

  public BtResult<Boolean> removeDomain(
      int siteId, String websiteName, String domain, Integer port) {
    return execute(
        new DeleteWebsiteDomainApi()
            .setId(siteId)
            .setWebname(websiteName)
            .setDomain(domain)
            .setPort(port));
  }

  /**
   * @deprecated use {@link #removeDomain(int, String, String, Integer)} instead.
   */
  @Deprecated
  public BtResult<Boolean> deleteDomain(int id, String webname, String domain, Integer port) {
    return removeDomain(id, webname, domain, port);
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

  public BtResult<Boolean> updateRemark(int siteId, String remark) {
    return execute(new SetWebsitePsApi().setId(siteId).setPs(remark));
  }

  /**
   * @deprecated use {@link #updateRemark(int, String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setRemark(int id, String ps) {
    return updateRemark(id, ps);
  }

  public BtResult<String> getRootPath(int id) {
    return execute(new GetWebsiteRootPathApi().setId(id));
  }

  public BtResult<Boolean> updateRootPath(int siteId, String path) {
    return execute(new SetWebsiteRootPathApi().setId(siteId).setPath(path));
  }

  /**
   * @deprecated use {@link #updateRootPath(int, String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setRootPath(int id, String path) {
    return updateRootPath(id, path);
  }

  public BtResult<Boolean> updateRunPath(int siteId, String runPath) {
    return execute(new SetWebsiteRunPathApi().setId(siteId).setRunPath(runPath));
  }

  /**
   * @deprecated use {@link #updateRunPath(int, String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setRunPath(int id, String runPath) {
    return updateRunPath(id, runPath);
  }

  public BtResult<Boolean> toggleUserIni(String path) {
    return execute(new SetWebsiteUserIniApi().setPath(path));
  }

  /**
   * @deprecated use {@link #toggleUserIni(String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setUserIni(String path) {
    return toggleUserIni(path);
  }

  public BtResult<String> getPhpVersion(int id) {
    return execute(new GetWebsitePhpVersionApi().setId(id));
  }

  public BtResult<Boolean> updatePhpVersion(int siteId, String phpVersion) {
    return execute(new SetWebsitePhpVersionApi().setId(siteId).setPhpVersion(phpVersion));
  }

  /**
   * @deprecated use {@link #updatePhpVersion(int, String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setPhpVersion(int id, String phpVersion) {
    return updatePhpVersion(id, phpVersion);
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

  public BtResult<Boolean> updatePhpExtension(int siteId, String moduleName, boolean enabled) {
    return execute(
        new SetWebsitePhpExtensionsApi()
            .setId(siteId)
            .setModuleName(moduleName)
            .setEnabled(enabled));
  }

  /**
   * @deprecated use {@link #updatePhpExtension(int, String, boolean)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setPhpExtension(int id, String moduleName, boolean enabled) {
    return updatePhpExtension(id, moduleName, enabled);
  }

  public BtResult<String> getRewriteRules(int id) {
    return execute(new GetWebsiteRewriteRulesApi().setId(id));
  }

  public BtResult<Boolean> updateRewriteRules(int siteId, String name, String content) {
    return execute(new SetWebsiteRewriteRulesApi().setId(siteId).setName(name).setContent(content));
  }

  /**
   * @deprecated use {@link #updateRewriteRules(int, String, String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setRewriteRules(int id, String name, String content) {
    return updateRewriteRules(id, name, content);
  }

  public BtResult<String> getNginxConfig(Integer id, String domain) {
    return execute(new GetWebsiteNginxConfigApi().setId(id).setDomain(domain));
  }

  public BtResult<Boolean> updateNginxConfig(Integer siteId, String domain, String content) {
    return execute(
        new SetWebsiteNginxConfigApi().setId(siteId).setDomain(domain).setContent(content));
  }

  /**
   * @deprecated use {@link #updateNginxConfig(Integer, String, String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setNginxConfig(Integer id, String domain, String content) {
    return updateNginxConfig(id, domain, content);
  }

  public BtResult<Boolean> enablePasswordProtection(int siteId, String username, String password) {
    return execute(
        new SetWebsitePasswordApi().setId(siteId).setUsername(username).setPassword(password));
  }

  /**
   * @deprecated use {@link #enablePasswordProtection(int, String, String)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setPassword(int id, String username, String password) {
    return enablePasswordProtection(id, username, password);
  }

  public BtResult<Boolean> disablePasswordProtection(int siteId) {
    return execute(new CloseWebsitePasswordApi().setId(siteId));
  }

  /**
   * @deprecated use {@link #disablePasswordProtection(int)} instead.
   */
  @Deprecated
  public BtResult<Boolean> closePassword(int id) {
    return disablePasswordProtection(id);
  }

  public BtResult<Boolean> installSslCertificate(
      int siteId, String domain, String cert, String key, Boolean forceHttps) {
    return execute(
        new SetWebsiteSslApi()
            .setId(siteId)
            .setDomain(domain)
            .setCert(cert)
            .setKey(key)
            .setForceHttps(forceHttps));
  }

  /**
   * @deprecated use {@link #installSslCertificate(int, String, String, String, Boolean)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setSsl(
      int id, String domain, String cert, String key, Boolean forceHttps) {
    return installSslCertificate(id, domain, cert, key, forceHttps);
  }

  public BtResult<Boolean> disableSsl(int siteId) {
    return execute(new CloseWebsiteSslApi().setId(siteId));
  }

  /**
   * @deprecated use {@link #disableSsl(int)} instead.
   */
  @Deprecated
  public BtResult<Boolean> closeSsl(int id) {
    return disableSsl(id);
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

  public BtResult<Boolean> toggleLogs(int siteId) {
    return execute(new SetWebsiteLogsApi().setId(siteId));
  }

  /**
   * @deprecated use {@link #toggleLogs(int)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setLogs(int id) {
    return toggleLogs(id);
  }

  public BtResult<Map<String, Object>> getLimitNet(int id) {
    return execute(new GetWebsiteLimitNetApi().setId(id));
  }

  public BtResult<Boolean> updateLimitNet(
      int siteId, Boolean enabled, Integer perServer, Integer perIp, Integer limitRate) {
    return execute(
        new SetWebsiteLimitNetApi()
            .setId(siteId)
            .setEnabled(enabled)
            .setPerserver(perServer)
            .setPerip(perIp)
            .setLimitRate(limitRate));
  }

  /**
   * @deprecated use {@link #updateLimitNet(int, Boolean, Integer, Integer, Integer)} instead.
   */
  @Deprecated
  public BtResult<Boolean> setLimitNet(
      int id, Boolean enabled, Integer perserver, Integer perip, Integer limitRate) {
    return updateLimitNet(id, enabled, perserver, perip, limitRate);
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
