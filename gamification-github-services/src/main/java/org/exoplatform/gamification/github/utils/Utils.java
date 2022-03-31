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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class Utils {

  public static final String  HMAC_SHA1_ALGORITHM = "HmacSHA1";

  private static final char[] HEX                 =
                                  { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static final Log    LOG                 = ExoLogger.getLogger(Utils.class);

  private Utils() {
    // Private constructor for Utils class
  }

  public static boolean verifySignature(String payload, String signature, String secret) {
    boolean isValid = false;

    if (payload == null || signature == null || secret == null) {
      return false;
    }

    try {
      Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
      mac.init(signingKey);
      byte[] rawHmac = mac.doFinal(payload.getBytes());
      String expected = signature.substring(5);
      final int amount = rawHmac.length;
      char[] raw = new char[2 * amount];
      int j = 0;
      for (int i = 0; i < amount; i++) {
        raw[j++] = HEX[(0xF0 & rawHmac[i]) >>> 4];
        raw[j++] = HEX[(0x0F & rawHmac[i])];
      }
      String actual = new String(raw);

      isValid = expected.equals(actual);
    } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException ex) {
      LOG.error(ex);
    }
    return isValid;
  }

}
