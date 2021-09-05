package com.onetouch.testtask;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Dzmitry Kasakouski
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestTaskApplication.class})
@WebAppConfiguration
public class ControllerTest {

  private static final String REQUEST_BODY_VALID = "{\n" +
      "    \"request_uuid\": \"16d2dcfe-b89e-11e7-854a-58404eea6d16\",\n" +
      "    \"amount\": 15\n" +
      "}";

  private static final String SIGNATURE_VALID = "rbY5eeoQY6mKy6A1VRzwEnAnfwPU8jyWhEOwNbJL7YiDL1tTH58oF/sjgxU2tCL85+HB6Mcnw8Dr+BnZ7MmD4Ubnn0HR7tZmY21uahIg7/XaAt0nHRvUTycfehZd30tu1oaKZECgsYlh59ghoEsiPd3D5RmvlJiexZph7uqZnao=";


  private static final String REQUEST_BODY_BALANCE_OUTAGE = "{\n" +
      "    \"request_uuid\": \"16d2dcfe-b89e-11e7-854a-58404eea6d16\",\n" +
      "    \"amount\": 15000\n" +
      "}";

  private static final String SIGNATURE_BALANCE_OUTAGE = "nJ8OCkSSwMGiV1wAEPQnnfCI1U+WFB48j/FikF7BSPU1jh0ACa220oipXnQeDFwBTphGWi9KkU7Mrt1/y2a+gH9p+RSikJRum/kEnpOcEC6Ei4z0a2zsDenzJwtwkfqtPoXfFogwlovXdYXWhQfDxqf0R+dA8wNutbzgSrF0m7A=";

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  RestTemplate restTemplate;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
  }

  @Test
  public void testInitGame() throws Exception {
    final String testRedirectUrl = "testRedirectUrl";
    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(Collections.singletonMap("url", testRedirectUrl));
    mockMvc.perform(MockMvcRequestBuilders.post("/api/game"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl(testRedirectUrl));
  }

  @Test
  public void testMakeBet() throws Exception {
    final String testRedirectUrl = "testRedirectUrl";
    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(Collections.singletonMap("url", testRedirectUrl));
    mockMvc.perform(MockMvcRequestBuilders.post("/api/transaction/bet")
        .contentType(MediaType.APPLICATION_JSON)
        .content(REQUEST_BODY_VALID)
        .header("X-Signature", SIGNATURE_VALID))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.request_uuid").value("16d2dcfe-b89e-11e7-854a-58404eea6d16"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user").value("Jimm123"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(1485.15));
  }

  @Test
  public void testMakeBetInvalidSignature() throws Exception {
    final String testRedirectUrl = "testRedirectUrl";
    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(Collections.singletonMap("url", testRedirectUrl));
    mockMvc.perform(MockMvcRequestBuilders.post("/api/transaction/bet")
        .contentType(MediaType.APPLICATION_JSON)
        .content(REQUEST_BODY_VALID)
        .header("X-Signature", "invalid"))
        .andExpect(status().isForbidden())
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("invalid signature"));
  }

  @Test
  public void testMakeBetOutOfBalance() throws Exception {
    final String testRedirectUrl = "testRedirectUrl";
    when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(Collections.singletonMap("url", testRedirectUrl));
    mockMvc.perform(MockMvcRequestBuilders.post("/api/transaction/bet")
        .contentType(MediaType.APPLICATION_JSON)
        .content(REQUEST_BODY_BALANCE_OUTAGE)
        .header("X-Signature", SIGNATURE_BALANCE_OUTAGE))
        .andExpect(status().isPaymentRequired())
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Out of balance"));
  }
}
