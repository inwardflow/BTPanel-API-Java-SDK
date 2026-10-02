package net.heimeng.sdk.btapi.model.ssl;

import lombok.Data;

/** Per-site deployment item from {@code ssl?action=SetBatchCertToSite}. */
@Data
public class SslBatchDeploymentItem {

  private boolean status;
  private String certName;
  private String siteName;
  private String message;
}
