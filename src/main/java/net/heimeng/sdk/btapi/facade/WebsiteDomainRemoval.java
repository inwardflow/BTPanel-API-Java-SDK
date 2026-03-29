package net.heimeng.sdk.btapi.facade;

/**
 * Website domain removal request.
 *
 * @param websiteName website display or primary domain name expected by BT Panel
 * @param domain domain to remove
 * @param port bound port that should be removed
 */
public record WebsiteDomainRemoval(String websiteName, String domain, int port) {

  public WebsiteDomainRemoval {
    new WebsiteDomainBinding(websiteName, domain);
    if (port <= 0) {
      throw new IllegalArgumentException("port must be positive");
    }
  }
}
