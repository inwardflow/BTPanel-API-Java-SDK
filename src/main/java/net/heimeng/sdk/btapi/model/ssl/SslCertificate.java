package net.heimeng.sdk.btapi.model.ssl;

import java.util.Date;
import java.util.List;

import lombok.Data;

/** SSL certificate summary returned by the panel certificate list. */
@Data
public class SslCertificate {

  private int id;
  private String name;
  private String type;
  private List<String> domains;
  private String issuer;
  private Date validFrom;
  private Date validTo;
  private String status;
  private boolean autoRenew;

  /** Panel-side saved certificate hash used by {@code ssl?action=SetBatchCertToSite}. */
  private String hash;

  /**
   * Backward-compatible fingerprint field.
   *
   * <p>Some historical SDK code treated the panel hash as a certificate fingerprint, so the parser
   * still mirrors {@code hash} into this field when no dedicated fingerprint is present.
   */
  private String fingerprint;

  public boolean isValid() {
    return "valid".equalsIgnoreCase(status);
  }

  public boolean isExpired() {
    return "expired".equalsIgnoreCase(status);
  }

  public boolean isExpiringSoon() {
    return "expiring_soon".equalsIgnoreCase(status);
  }

  public String getFormattedValidityPeriod() {
    if (validFrom == null || validTo == null) {
      return "未知";
    }
    return String.format("%tF 至 %tF", validFrom, validTo);
  }
}
