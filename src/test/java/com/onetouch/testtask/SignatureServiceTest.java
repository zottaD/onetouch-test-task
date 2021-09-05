package com.onetouch.testtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Dzmitry Kasakouski
 */
@ExtendWith(MockitoExtension.class)
public class SignatureServiceTest {

  private static String TEST_SIGNATURE = "OFI07YzRsqm8c3p/ET5Ov1R1MP/o9tr42Fcqh1mDKG02DhePYonxgRq85wq1c9BCEAvmGNq7YekfBKvkGpEdH54aWxs" +
      "/5HZRjxsZgemuBXUSYxF8qU8lP5rI84S7XdOZb8EUb1iGpGY820YVVBLiVIoEcce4+Iui0shIXWShj98=";

  private static String TEST_BODY = "body";

  @InjectMocks
  private SignatureService signatureService;

  @Test
  public void testGetSignature() throws IOException, NoSuchAlgorithmException {
    assertEquals(TEST_SIGNATURE, signatureService.getSignature(TEST_BODY));
  }

  @Test
  public void testVerifySignature() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
    assertTrue(signatureService.verifySignature(TEST_BODY, TEST_SIGNATURE));
  }

  @Test
  public void testVerifySignatureFailed() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
    assertFalse(signatureService.verifySignature("random", TEST_SIGNATURE));
  }
}
