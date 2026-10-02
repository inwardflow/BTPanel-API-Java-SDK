package net.heimeng.sdk.btapi.model.ssl;

import java.util.List;

import lombok.Data;

/** Site-level SSL status returned by {@code site?action=GetSSL}. */
@Data
public class SslSiteStatus {

  private boolean enabled;
  private Integer orderId;
  private List<String> domains = List.of();
  private String privateKeyPem;
  private String certificatePem;
  private int type = -1;
  private boolean httpToHttps;
  private SslSiteCertificateDetail certificateDetail;
  private String contactEmail;

  public boolean hasCertificate() {
    return certificateDetail != null
        || (privateKeyPem != null && !privateKeyPem.isBlank())
        || (certificatePem != null && !certificatePem.isBlank());
  }
}
