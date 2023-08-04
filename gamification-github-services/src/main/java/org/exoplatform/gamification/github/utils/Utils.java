/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.gamification.github.utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

public class Utils {

  public static final String  HMAC_SHA1_ALGORITHM = "HmacSHA1";

  private static final char[] HEX                 =
                                  { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static final Log    LOG                 = ExoLogger.getLogger(Utils.class);

  private Utils() {
    // Private constructor for Utils class
  }

  public static boolean verifySignature(String webhookSecret, String payload, String signature) {
    boolean isValid;
    if (signature == null || webhookSecret == null) {
      return false;
    }
    try {
      Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      SecretKeySpec signingKey = new SecretKeySpec(webhookSecret.getBytes(), HMAC_SHA1_ALGORITHM);
      mac.init(signingKey);
      byte[] rawHmac = mac.doFinal(payload.getBytes());
      String expected = signature.substring(5);
      final int amount = rawHmac.length;
      char[] raw = new char[2 * amount];
      int j = 0;
      for (byte b : rawHmac) {
        raw[j++] = HEX[(0xF0 & b) >>> 4];
        raw[j++] = HEX[(0x0F & b)];
      }
      String actual = new String(raw);

      isValid = expected.equals(actual);
    } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
      throw new IllegalStateException("Error verifying signature", e);
    }
    return isValid;
  }

  public static Map<String, Object> fromJsonStringToMap(String jsonString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(jsonString, Map.class);
    } catch (IOException e) {
      throw new IllegalStateException("Error converting JSON string to map: " + jsonString, e);
    }
  }

  public static String generateRandomSecret(int length) {
    SecureRandom secureRandom = new SecureRandom();
    StringBuilder word = new StringBuilder();

    for (int i = 0; i < length; i++) {
      char randomChar;
      if (secureRandom.nextBoolean()) {
        randomChar = (char) (secureRandom.nextInt(26) + 'A');
      } else {
        randomChar = (char) (secureRandom.nextInt(26) + 'a');
      }
      word.append(randomChar);
    }
    return word.toString();
  }

  public static String encode(String token) {
    try {
      CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
      return codecInitializer.getCodec().encode(token);
    } catch (TokenServiceInitializationException e) {
      LOG.warn("Error when encoding token", e);
      return null;
    }
  }

  public static String decode(String encryptedToken) {
    try {
      CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
      return codecInitializer.getCodec().decode(encryptedToken);
    } catch (TokenServiceInitializationException e) {
      LOG.warn("Error when decoding token", e);
      return null;
    }
  }
}
