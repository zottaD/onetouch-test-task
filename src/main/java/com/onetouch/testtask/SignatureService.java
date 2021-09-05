package com.onetouch.testtask;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 * @author Dzmitry Kasakouski
 */
@Service
public class SignatureService {

  public String getSignature(String body) throws IOException, NoSuchAlgorithmException {
    final RSAPrivateKey rsaPrivateKey = readPrivateKey(ResourceUtils.getFile("classpath:private_test_task.pem"));
    Signature privateSignature = Signature.getInstance("SHA256withRSA");
    try {
      privateSignature.initSign(rsaPrivateKey);
      privateSignature.update(body.getBytes(UTF_8));
      byte[] signature = privateSignature.sign();

      return Base64.getEncoder().encodeToString(signature);
    } catch (SignatureException | InvalidKeyException e) {
      throw new SecurityException(e);
    }
  }

  private RSAPrivateKey readPrivateKey(File file) throws IOException {

    try (FileReader keyReader = new FileReader(file)) {

      PEMParser pemParser = new PEMParser(keyReader);
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
      PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();

      return (RSAPrivateKey) converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
    }
  }

  private RSAPublicKey readPublicKey(File file) throws IOException {
    try (FileReader keyReader = new FileReader(file)) {

      PEMParser pemParser = new PEMParser(keyReader);
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

      return (RSAPublicKey) converter.getPublicKey((SubjectPublicKeyInfo) pemParser.readObject());
    }
  }

  public boolean verifySignature(String body, String signatureHeader)
      throws IOException, NoSuchAlgorithmException, InvalidKeyException {
    final RSAPublicKey rsaPublicKey = readPublicKey(ResourceUtils.getFile("classpath:public_test_task.pem"));

    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(rsaPublicKey);

    try {
      signature.update(body.getBytes(UTF_8));
      return signature.verify(Base64.getDecoder().decode(signatureHeader));
    } catch (SignatureException e) {
      return false;
    }
  }
}
