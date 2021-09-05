package com.onetouch.testtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

/**
 * @author Dzmitry Kasakouski
 */

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

  @InjectMocks
  private GameService gameService;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private SignatureService signatureService;

  @Test
  public void testGetGameUrl() throws IOException, NoSuchAlgorithmException {
    final String testUrl = "testUrl";
    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(Collections.singletonMap("url", testUrl));
    assertEquals(testUrl, gameService.getGameUrl());
  }

  @Test
  public void testGetHeaders() throws IOException, NoSuchAlgorithmException {
    final String testSignature = "testSignature";
    final String body = "body";
    when(signatureService.getSignature(body)).thenReturn(testSignature);
    final HttpHeaders headers = gameService.getHeaders(body);
    assertEquals(testSignature, headers.get("X-Signature").get(0));
  }

  @Test
  public void testMakeBet() {
    final String testRequestId = "testRequestId";
    final double testAmount = 15;
    final Map<Object, Object> result = gameService.makeBet(testRequestId, testAmount);
    assertEquals("Jimm123", result.get("user"));
    assertEquals(testRequestId, result.get("request_uuid"));
    assertEquals("USD", result.get("currency"));
    assertEquals(BigDecimal.valueOf(1485.15), result.get("balance"));
  }

  @Test
  public void testMakeBetOutOfBalance() {
    assertThrows(ArithmeticException.class, () -> {
      final String testRequestId = "testRequestId";
      final double testAmount = 15000;
      gameService.makeBet(testRequestId, testAmount);
    });
  }
}
