package com.onetouch.testtask;

import java.io.IOException;
import java.security.AccessControlException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Dzmitry Kasakouski
 */
@RestController
@RequestMapping("/api")
public class Controller {

  private static final String MESSAGE_KEY = "message";

  GameService gameService;

  private ObjectMapper objectMapper;

  private SignatureService signatureService;

  private Controller(GameService gameService, ObjectMapper objectMapper, SignatureService signatureService) {
    this.gameService = gameService;
    this.objectMapper = objectMapper;
    this.signatureService = signatureService;
  }

  @PostMapping("/game")
  public void initGame(HttpServletResponse httpServletResponse) throws NoSuchAlgorithmException, IOException {

    String url = gameService.getGameUrl();
    httpServletResponse.setHeader(HttpHeaders.LOCATION, url);
    httpServletResponse.setStatus(HttpStatus.FOUND.value());
  }

  @PostMapping(value = "/transaction/bet")
  @ResponseStatus(HttpStatus.OK)
  public Map<Object, Object> makeBet(@RequestHeader("X-Signature") String signature, @RequestBody String body) throws
      NoSuchAlgorithmException, InvalidKeyException,
      IOException {
    if (!signatureService.verifySignature(body, signature)) {
      throw new AccessControlException("invalid signature");
    }
    final Map<String, Object> map = objectMapper.readValue(body, Map.class);
    return gameService.makeBet(String.valueOf(map.get("request_uuid")), Double.parseDouble(String.valueOf(map.get("amount"))));
  }

  @ExceptionHandler(ArithmeticException.class)
  @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
  public Map<Object, Object> handleOutOfBalance(ArithmeticException exception) {
    return Collections.singletonMap(MESSAGE_KEY, exception.getMessage());
  }

  @ExceptionHandler(AccessControlException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Map<Object, Object> handleInvalidSignature(AccessControlException exception) {
    return Collections.singletonMap(MESSAGE_KEY, exception.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<Object, Object> handleUnexpectedException(Exception exception) {
    return Collections.singletonMap(MESSAGE_KEY, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
  }
}
