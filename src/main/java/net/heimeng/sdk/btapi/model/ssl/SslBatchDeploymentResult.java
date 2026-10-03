package net.heimeng.sdk.btapi.model.ssl;

import java.util.List;

import lombok.Data;

/** Batch deployment summary returned by {@code ssl?action=SetBatchCertToSite}. */
@Data
public class SslBatchDeploymentResult {

  private int total;
  private int successCount;
  private int failedCount;
  private List<SslBatchDeploymentItem> successList = List.of();
  private List<SslBatchDeploymentItem> failedList = List.of();

  public boolean isFullySuccessful() {
    return total > 0 && failedCount == 0 && successCount == total;
  }
}
