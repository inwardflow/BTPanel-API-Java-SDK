package net.heimeng.sdk.btapi.facade;

/**
 * Website deletion behavior options.
 *
 * @param deleteFtp whether the related FTP account should also be deleted
 * @param deleteDatabase whether the related database should also be deleted
 * @param deletePath whether the website directory should also be deleted
 */
public record WebsiteDeleteOptions(boolean deleteFtp, boolean deleteDatabase, boolean deletePath) {

  public static WebsiteDeleteOptions none() {
    return new WebsiteDeleteOptions(false, false, false);
  }

  public static WebsiteDeleteOptions deleteAll() {
    return new WebsiteDeleteOptions(true, true, true);
  }
}
