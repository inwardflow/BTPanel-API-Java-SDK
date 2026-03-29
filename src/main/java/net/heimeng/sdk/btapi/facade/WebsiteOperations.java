package net.heimeng.sdk.btapi.facade;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

  public BtResult<List<WebsiteType>> listTypes() {
    return execute(new GetWebsiteTypesApi());
  }

  public BtResult<List<PhpVersion>> listPhpVersions() {
    return execute(new GetPhpVersionsApi());
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

  public BtResult<Boolean> addDomain(int siteId, WebsiteDomainBinding binding) {
    Objects.requireNonNull(binding, "binding cannot be null");
    return execute(
        new AddWebsiteDomainApi()
            .setId(siteId)
            .setWebname(binding.websiteName())
            .setDomain(binding.domain()));
  }

  public BtResult<Boolean> removeDomain(int siteId, WebsiteDomainRemoval removal) {
    Objects.requireNonNull(removal, "removal cannot be null");
    return execute(
        new DeleteWebsiteDomainApi()
            .setId(siteId)
            .setWebname(removal.websiteName())
            .setDomain(removal.domain())
            .setPort(removal.port()));
  }

  public BtResult<Boolean> delete(int siteId, String websiteName, WebsiteDeleteOptions options) {
    Objects.requireNonNull(options, "options cannot be null");
    return execute(
        new DeleteWebsiteApi(siteId, websiteName)
            .setDeleteFtp(options.deleteFtp())
            .setDeleteDatabase(options.deleteDatabase())
            .setDeletePath(options.deletePath()));
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

  public BtResult<String> getRootPath(int id) {
    return execute(new GetWebsiteRootPathApi().setId(id));
  }

  public BtResult<Boolean> updateRootPath(int siteId, String path) {
    return execute(new SetWebsiteRootPathApi().setId(siteId).setPath(path));
  }

  public BtResult<Boolean> updateRunPath(int siteId, String runPath) {
    return execute(new SetWebsiteRunPathApi().setId(siteId).setRunPath(runPath));
  }

  public BtResult<Boolean> toggleUserIni(String path) {
    return execute(new SetWebsiteUserIniApi().setPath(path));
  }

  public BtResult<String> getPhpVersion(int id) {
    return execute(new GetWebsitePhpVersionApi().setId(id));
  }

  public BtResult<Boolean> updatePhpVersion(int siteId, String phpVersion) {
    return execute(new SetWebsitePhpVersionApi().setId(siteId).setPhpVersion(phpVersion));
  }

  public BtResult<List<Map<String, Object>>> listPhpExtensions(int id) {
    return execute(new GetWebsitePhpExtensionsApi().setId(id));
  }

  public BtResult<Boolean> updatePhpExtension(int siteId, String moduleName, boolean enabled) {
    return execute(
        new SetWebsitePhpExtensionsApi()
            .setId(siteId)
            .setModuleName(moduleName)
            .setEnabled(enabled));
  }

  public BtResult<String> getRewriteRules(int id) {
    return execute(new GetWebsiteRewriteRulesApi().setId(id));
  }

  public BtResult<Boolean> updateRewriteRules(int siteId, WebsiteRewriteRulesOptions options) {
    Objects.requireNonNull(options, "options cannot be null");
    return execute(
        new SetWebsiteRewriteRulesApi()
            .setId(siteId)
            .setName(options.name())
            .setContent(options.content()));
  }

  public BtResult<String> getNginxConfig(Integer id, String domain) {
    return execute(new GetWebsiteNginxConfigApi().setId(id).setDomain(domain));
  }

  public BtResult<Boolean> updateNginxConfig(Integer siteId, WebsiteNginxConfigOptions options) {
    Objects.requireNonNull(options, "options cannot be null");
    return execute(
        new SetWebsiteNginxConfigApi()
            .setId(siteId)
            .setDomain(options.domain())
            .setContent(options.content()));
  }

  public BtResult<Boolean> enablePasswordProtection(
      int siteId, WebsitePasswordProtectionOptions options) {
    Objects.requireNonNull(options, "options cannot be null");
    return execute(
        new SetWebsitePasswordApi()
            .setId(siteId)
            .setUsername(options.username())
            .setPassword(options.password()));
  }

  public BtResult<Boolean> disablePasswordProtection(int siteId) {
    return execute(new CloseWebsitePasswordApi().setId(siteId));
  }

  public BtResult<Boolean> installSslCertificate(int siteId, WebsiteSslCertificateOptions options) {
    Objects.requireNonNull(options, "options cannot be null");
    return execute(
        new SetWebsiteSslApi()
            .setId(siteId)
            .setDomain(options.domain())
            .setCert(options.certificate())
            .setKey(options.privateKey())
            .setForceHttps(options.forceHttps()));
  }

  public BtResult<Boolean> disableSsl(int siteId) {
    return execute(new CloseWebsiteSslApi().setId(siteId));
  }

  public BtResult<List<Map<String, Object>>> listSslCertificates(int id) {
    return execute(new GetWebsiteSslListApi().setId(id));
  }

  public BtResult<Boolean> toggleLogs(int siteId) {
    return execute(new SetWebsiteLogsApi().setId(siteId));
  }

  public BtResult<Map<String, Object>> getLimitNet(int id) {
    return execute(new GetWebsiteLimitNetApi().setId(id));
  }

  public BtResult<Boolean> updateLimitNet(int siteId, WebsiteLimitNetOptions options) {
    Objects.requireNonNull(options, "options cannot be null");
    return execute(
        new SetWebsiteLimitNetApi()
            .setId(siteId)
            .setEnabled(options.enabled())
            .setPerserver(options.perServer())
            .setPerip(options.perIp())
            .setLimitRate(options.limitRate()));
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

  public BtResult<Boolean> createBackup(int id) {
    return execute(new CreateWebsiteBackupApi().setId(id));
  }

  public BtResult<Boolean> deleteBackup(int id) {
    return execute(new DeleteWebsiteBackupApi().setId(id));
  }
}
