package org.exoplatform.gamification.connectors.github.listener;

import java.util.Map;

import org.exoplatform.gamification.connectors.github.services.GithubHooksManagement;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class GithubEventsListener extends Listener<Map<String, String>, String> {

  private static final Log      LOG = ExoLogger.getLogger(GithubEventsListener.class);

  private GithubHooksManagement githubHooksManagement;

  public GithubEventsListener(GithubHooksManagement githubHooksManagement) {
    this.githubHooksManagement = githubHooksManagement;
  }

  @Override
  public void onEvent(Event<Map<String, String>, String> event) throws Exception {
    String ruleTitle = event.getSource().get("ruleTitle");
    String senderId = event.getSource().get("senderId");
    String receiverId = event.getSource().get("receiverId");
    String object = event.getSource().get("object");
    githubHooksManagement.createGamificationHistory(ruleTitle, senderId, receiverId, object);
  }
}
