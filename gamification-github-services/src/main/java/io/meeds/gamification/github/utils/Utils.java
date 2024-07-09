/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2022 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.gamification.github.utils;

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

  public static final String   CONNECTOR_NAME                             = "github";

  public static final String   HMAC_SHA1_ALGORITHM                        = "HmacSHA1";

  public static final String   ORGANIZATION                               = "organization";

  public static final String   ORGANIZATION_ID                            = "organizationId";

  public static final String   REPOSITORY_ID                              = "repositoryId";

  public static final String   REPOSITORY_IDS                             = "repositoryIds";

  public static final String   REPOSITORY                                 = "repository";

  public static final String   ID                                         = "id";

  public static final String   LOGIN                                      = "login";

  public static final String   NAME                                       = "name";

  public static final String   EVENTS                                     = "events";

  public static final String   PULL_REQUEST                               = "pull_request";

  public static final String   ISSUE                                      = "issue";

  public static final String   URL                                        = "url";

  public static final String   HTML                                       = "html";

  public static final String   HTML_URL                                   = "html_url";

  public static final String   LINKS                                      = "_links";

  public static final String   HREF                                       = "href";

  public static final String   HEAD_COMMIT                                = "head_commit";

  public static final String   COMMENT                                    = "comment";

  public static final String   USER                                       = "user";

  public static final String   SENDER                                     = "sender";

  public static final String   PUSHER                                     = "pusher";

  public static final String   ACTION                                     = "action";

  public static final String   STATE                                      = "state";

  public static final String   NOT_PLANNED                                = "not_planned";

  public static final String   STATE_REASON                               = "state_reason";

  public static final String   LABEL                                      = "label";

  public static final String   PULL_REQUEST_REVIEW                        = "review";

  public static final String   PULL_REQUEST_COMMENTED                     = "commented";

  public static final String   PULL_REQUEST_VALIDATED                     = "approved";

  public static final String   OPENED                                     = "opened";

  public static final String   CREATED                                    = "created";

  public static final String   CLOSED                                     = "closed";

  public static final String   DELETED                                    = "deleted";

  public static final String   LABELED                                    = "labeled";

  public static final String   UNLABELED                                  = "unlabeled";

  public static final String   MERGED                                     = "merged";

  public static final String   REVIEW_REQUESTED                           = "review_requested";

  public static final String   REVIEW_REQUEST_REMOVED                     = "review_request_removed";

  public static final String   REQUESTED_REVIEWER                         = "requested_reviewer";

  public static final String   REVIEW_PULL_REQUEST_EVENT_NAME             = "reviewPullRequest";

  public static final String   PULL_REQUEST_VALIDATED_EVENT_NAME          = "pullRequestValidated";

  public static final String   VALIDATE_PULL_REQUEST_EVENT_NAME           = "validatePullRequest";

  public static final String   CREATE_PULL_REQUEST_EVENT_NAME             = "creatPullRequest";

  public static final String   PULL_REQUEST_REVIEW_COMMENT_EVENT_NAME     = "pullRequestReviewComment";

  public static final String   COMMENT_PULL_REQUEST_EVENT_NAME            = "commentPullRequest";

  public static final String   DELETE_PULL_REQUEST_COMMENT_EVENT_NAME     = "deletePullRequestComment";

  public static final String   CREATE_ISSUE_EVENT_NAME                    = "createIssue";

  public static final String   ADD_ISSUE_LABEL_EVENT_NAME                 = "addIssueLabel";

  public static final String   REQUEST_REVIEW_FOR_PULL_REQUEST_EVENT_NAME = "requestReviewForPullRequest";

  public static final String   COMMENT_ISSUE_EVENT_NAME                   = "commentIssue";

  public static final String   DELETE_ISSUE_COMMENT_EVENT_NAME            = "deleteIssueComment";

  public static final String   CLOSE_PULL_REQUEST_EVENT_NAME              = "closePullRequest";

  public static final String   REVIEW_REQUEST_REMOVED_EVENT_NAME          = "reviewRequestRemoved";

  public static final String   CLOSE_ISSUE_EVENT_NAME                     = "closeIssue";

  public static final String   DELETE_ISSUE_LABEL_EVENT_NAME              = "deleteIssueLabel";

  public static final String   PUSH_CODE_EVENT_NAME                       = "pushCode";

  public static final String   GITHUB_ACTION_EVENT                        = "github.action.event";

  public static final String   GITHUB_CANCEL_ACTION_EVENT                 = "github.cancel.action.event";

  public static final String   ISSUE_TYPE                                 = "githubIssue";

  public static final String   PR_TYPE                                    = "githubPR";

  public static final String   REVIEW_COMMENT_TYPE                        = "githubReviewComment";

  public static final String   COMMENT_PR_TYPE                            = "githubCommentPR";

  public static final String   COMMENT_ISSUE_TYPE                         = "githubCommentIssue";

  public static final String   GITHUB_API_URL                             = "https://api.github.com";

  public static final String   ORGANIZATIONS                              = "/orgs/";

  public static final String   AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS         = "The user is not authorized to access gitHub Hooks";

  public static final String   TOKEN                                      = "token ";

  public static final String   AUTHORIZATION                              = "Authorization";

  public static final String   GITHUB_CONNECTION_ERROR                    = "github.connectionError";

  public static final String   DESCRIPTION                                = "description";

  public static final String   AVATAR_URL                                 = "avatar_url";

  public static final String[] GITHUB_TRIGGERS                            = new String[] { "pull_request", "issue_comment",           // NOSONAR
      "pull_request_review_comment", "pull_request_review", "issues", "push" };

  private static final char[]  HEX                                        =
                                   { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static final Log     LOG                                        = ExoLogger.getLogger(Utils.class);

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

  @SuppressWarnings("unchecked")
  public static Map<String, Object> fromJsonStringToMap(String jsonString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(jsonString, Map.class);
    } catch (IOException e) {
      throw new IllegalStateException("Error converting JSON string to map: " + jsonString, e);
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object>[] fromJsonStringToMapCollection(String jsonString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(jsonString, Map[].class);
    } catch (IOException e) {
      throw new IllegalStateException("Error converting JSON string to map: " + jsonString, e);
    }
  }

  @SuppressWarnings("unchecked")
  public static String extractSubItem(Map<String, Object> map, String... keys) {
    Object currentObject = map;
    for (String key : keys) {
      if (currentObject instanceof Map) {
        currentObject = ((Map<String, Object>) currentObject).get(key);
      } else {
        return null;
      }
    }
    return currentObject != null ? currentObject.toString() : null;
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
