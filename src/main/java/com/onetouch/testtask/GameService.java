package com.onetouch.testtask;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author Dzmitry Kasakouski
 */
@Service
public class GameService {

  private static final String GAME_URL = "/api/operator/generic/v2/game/url";
  private String onetouchBaseUrl;

  RestTemplate restTemplate;

  private SignatureService signatureService;

  private GameService(@Value("${onetouch.base.url:https://test-platform.onetouch.io}") String onetouchBaseUrl, RestTemplate restTemplate,
      SignatureService signatureService) {
    this.onetouchBaseUrl = onetouchBaseUrl;
    this.restTemplate = restTemplate;
    this.signatureService = signatureService;
  }

  public String getGameUrl() throws NoSuchAlgorithmException, IOException {

    // TODO create DTO class
    String body = "{\n" +
        "  \"user\": \"john12345\",\n" +
        "  \"token\": \"f562a685-a160-4d17-876d-ab3363db331c\",\n" +
        "  \"sub_partner_id\": \"my-casino-id\",\n" +
        "  \"platform\": \"GPL_DESKTOP\",\n" +
        "  \"operator_id\": 1,\n" +
        "  \"lobby_url\": \"https://provider.com/casino\",\n" +
        "  \"lang\": \"ru\",\n" +
        "  \"ip\": \"142.245.172.168\",\n" +
        "  \"game_id\": 132,\n" +
        "  \"display_unit\": \"mBTC\",\n" +
        "  \"deposit_url\": \"https://provider.com/deposit\",\n" +
        "  \"currency\": \"BTC\",\n" +
        "  \"country\": \"EE\"\n" +
        "}";

    HttpEntity<String> httpEntity = new HttpEntity<>(body, getHeaders(body));
    //TODO doesn't work, cert issue: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
    return (String) restTemplate.postForObject(onetouchBaseUrl + GAME_URL, httpEntity, Map.class).get("url");
  }

  protected HttpHeaders getHeaders(String body) throws NoSuchAlgorithmException, IOException {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("X-Signature", signatureService.getSignature(body));

    return headers;
  }

  public Map<Object, Object> makeBet(String requestId, double amount) {
    final BigDecimal result = getUserBalance().subtract(BigDecimal.valueOf(amount));
    result.setScale(2, RoundingMode.DOWN);
    if (result.compareTo(BigDecimal.ONE) < 0) {
      throw new ArithmeticException("Out of balance");
    }
    // TODO call updateBalance method

    // TODO change to DTO object
    return new HashMap<Object, Object>() {{
      put("user", "Jimm123");
      put("request_uuid", requestId);
      put("currency", "USD");
      put("balance", result.setScale(2, RoundingMode.DOWN));
    }};
  }

  private BigDecimal getUserBalance() {
    // TODO get stored value
    return BigDecimal.valueOf(1500.15);
  }
}
