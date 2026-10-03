package net.heimeng.sdk.btapi.model.ssl;

import java.util.Date;

import lombok.Data;

/** Summary of the certificate currently attached to a site. */
@Data
public class SslSiteCertificateDetail {

  private Integer id;
  private String issuer;
  private String subject;
  private Date validFrom;
  private Date validTo;
}
