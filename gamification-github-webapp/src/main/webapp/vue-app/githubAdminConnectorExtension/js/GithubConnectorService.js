/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association
 * contact@meeds.io
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
 */

export function getGithubWebHooks(offset, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks?offset=${offset || 0}&limit=${limit|| 10}&returnSize=true`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting github webhooks');
    }
  });
}

export function saveGithubWebHook(organizationName, accessToken , hookSecret) {
  const formData = new FormData();
  formData.append('organizationName', organizationName);
  formData.append('accessToken', accessToken);
  formData.append('hookSecret', hookSecret);

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when saving github webhook');
    }
  });
}

export function deleteGithubWebHook(organizationId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks/${organizationId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when deleting github webhook');
    }
  });
}