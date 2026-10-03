package net.heimeng.sdk.btapi.model.ssl;

import java.util.List;

import lombok.Data;

/** Candidate site lists returned by {@code ssl?action=GetSiteDomain}. */
@Data
public class SslDeployableSites {

  private List<String> allSites = List.of();
  private List<String> matchedSites = List.of();

  public boolean hasMatchedSites() {
    return matchedSites != null && !matchedSites.isEmpty();
  }
}
