# Quickstart

```java
import java.util.List;

import net.heimeng.sdk.btapi.client.BtApiManager;
import net.heimeng.sdk.btapi.client.BtClientFactory;
import net.heimeng.sdk.btapi.model.BtResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.system.SystemInfo;

try (BtApiManager apiManager =
    BtClientFactory.createApiManager("https://your-panel-host:port", "your-api-key")) {

  BtResult<SystemInfo> systemInfo = apiManager.system().getSystemInfo();
  System.out.println(systemInfo.getData().getOs());

  apiManager.website().list().getData().forEach(site -> System.out.println(site.getName()));

  BtResult<List<SslCertificate>> certificates = apiManager.ssl().list();
  certificates.getData().forEach(
      cert ->
          System.out.println(
              cert.getName() + " -> " + cert.getDomains() + " (hash=" + cert.getHash() + ")"));
}
```

For BTPanel 9.0.0 developer-signature usage, the verified site SSL flow is the saved-certificate
deployment chain, not the historical `SetSSL` PEM write route:

```java
import net.heimeng.sdk.btapi.model.ssl.SslBatchDeploymentResult;
import net.heimeng.sdk.btapi.model.ssl.SslCertificate;
import net.heimeng.sdk.btapi.model.ssl.SslDeployableSites;
import net.heimeng.sdk.btapi.model.ssl.SslSiteStatus;

String siteName = "example.com";

SslCertificate cert =
    apiManager.ssl().list().getData().stream()
        .filter(item -> "integration-test.example.com".equals(item.getName()))
        .findFirst()
        .orElseThrow();

SslSiteStatus beforeDeploy = apiManager.ssl().getWebsiteStatus(siteName).getData();
System.out.println(beforeDeploy.isEnabled());

SslDeployableSites deployableSites =
    apiManager.ssl().getDeployableSites(List.of(cert.getName())).getData();
System.out.println(deployableSites.getAllSites());

SslBatchDeploymentResult deployment =
    apiManager
        .ssl()
        .deploySavedCertificate(cert.getHash(), siteName, cert.getName())
        .getData();
System.out.println(deployment.isFullySuccessful());
```

Notes:
- `SslCertificate.hash` is the panel-side saved certificate identifier used by
  `SetBatchCertToSite`.
- `SslCertificate.domains` usually comes from the certificate `CN / SAN`, not from the panel site
  name.
- `apiManager.ssl().install(...)` is still kept for backward compatibility, but it is not the
  recommended path for the currently validated BTPanel 9.0.0 developer API flow.

For lower-level access, you can still execute raw `BtApi<T>` requests:

```java
import net.heimeng.sdk.btapi.api.website.GetWebsitePhpVersionApi;

String phpVersion = apiManager.execute(new GetWebsitePhpVersionApi().setId(1)).getData();
```
