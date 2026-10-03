package net.heimeng.sdk.btapi.client;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import net.heimeng.sdk.btapi.config.CertificatePins;

/**
 * 按公钥指纹校验服务端证书的信任管理器。
 *
 * <p>只比对叶子证书：服务端在 TLS 握手中必须证明持有叶子证书的私钥，而证书链中的其他证书未经签名校验，
 * 不能作为信任依据。公钥本身已被固定，因此不再额外校验主机名；宝塔自签证书通常只包含面板 IP。
 */
final class PinnedPublicKeyTrustManager extends X509ExtendedTrustManager {

  private final List<String> pins;

  PinnedPublicKeyTrustManager(List<String> pins) {
    if (pins == null || pins.isEmpty()) {
      throw new IllegalArgumentException("At least one pin is required");
    }
    this.pins = List.copyOf(pins);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    verify(chain);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    verify(chain);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException {
    verify(chain);
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    throw new CertificateException("Client certificates are not supported");
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    throw new CertificateException("Client certificates are not supported");
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException {
    throw new CertificateException("Client certificates are not supported");
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }

  private void verify(X509Certificate[] chain) throws CertificateException {
    if (chain == null || chain.length == 0) {
      throw new CertificateException("Server did not present a certificate");
    }

    X509Certificate leaf = chain[0];
    leaf.checkValidity();

    String peerPin = CertificatePins.sha256(leaf);
    if (!pins.contains(peerPin)) {
      throw new CertificateException(
          "Certificate pinning failure: server public key "
              + peerPin
              + " does not match any configured pin "
              + pins
              + ". If the panel certificate was replaced, verify the new fingerprint on the"
              + " server itself before updating the pin.");
    }
  }
}
