/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package io.meeds.gamification.github.mock;

import io.meeds.gamification.model.RuleDTO;
import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;

import java.util.List;

public class RuleServiceMock implements RuleService {
  @Override
  public RuleDTO findRuleById(long id) {
    return null;
  }

  @Override
  public RuleDTO findRuleById(long id, String username) {
    return null;
  }

  @Override
  public RuleDTO findRuleByTitle(String ruleTitle) {
    return null;
  }

  @Override
  public List<RuleDTO> getRules(RuleFilter ruleFilter, String username, int offset, int limit) {
    return null;
  }

  @Override
  public List<RuleDTO> getRules(RuleFilter ruleFilter, int offset, int limit) {
    return null;
  }

  @Override
  public int countRules(RuleFilter ruleFilter, String username) {
    return 0;
  }

  @Override
  public int countRules(RuleFilter ruleFilter) {
    return 0;
  }

  @Override
  public int countActiveRules(long programId) {
    return 0;
  }

  @Override
  public RuleDTO deleteRuleById(long ruleId, String username) {
    return null;
  }

  @Override
  public RuleDTO deleteRuleById(long ruleId) {
    return null;
  }

  @Override
  public RuleDTO createRule(RuleDTO ruleDTO, String username) {
    return null;
  }

  @Override
  public RuleDTO createRule(RuleDTO ruleDTO) {
    return null;
  }

  @Override
  public RuleDTO updateRule(RuleDTO ruleDTO, String username) {
    return null;
  }

  @Override
  public List<RuleDTO> getPrerequisiteRules(long ruleId) {
    return null;
  }
}
