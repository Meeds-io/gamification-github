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

export function saveGithubWebHook(organizationName, accessToken) {
  const formData = new FormData();
  formData.append('organizationName', organizationName);
  formData.append('accessToken', accessToken);
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

export function updateWebHookAccessToken(webHookId, accessToken) {
  const formData = new FormData();
  formData.append('webHookId', webHookId);
  formData.append('accessToken', accessToken);
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks`, {
    method: 'PATCH',
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

export function getWebHookRepos(organizationId, offset, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks/${organizationId}/repos?offset=${offset || 0}&limit=${limit|| 10}&returnSize=true`, {
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

export function saveRepositoryStatus(repositoryId, organizationId, enabled) {
  const formData = new FormData();
  formData.append('repositoryId', repositoryId);
  formData.append('organizationId', organizationId);
  formData.append('enabled', enabled);

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks/repo/status`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function enableDisableWatchScope(organizationId, enabled) {
  const formData = new FormData();
  formData.append('organizationId', organizationId);
  formData.append('enabled', enabled);
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks/watchScope/status`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function saveEventStatus(organizationId, event, enabled) {
  const formData = new FormData();
  formData.append('organizationId', organizationId);
  formData.append('event', event);
  formData.append('enabled', enabled);

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/gamification/connectors/github/hooks/event/status`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}