/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

export function getGithubWebHooks(paramsObj) {
  const formData = new FormData();
  if (paramsObj) {
    Object.keys(paramsObj).forEach(key => {
      const value = paramsObj[key];
      if (window.Array && Array.isArray && Array.isArray(value)) {
        value.forEach(val => formData.append(key, val));
      } else {
        formData.append(key, value);
      }
    });
  }
  const params = new URLSearchParams(formData).toString();
  return fetch(`/gamification-github/rest/hooks?${params}`, {
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

export function getGithubWebHookById(hookId) {
  return fetch(`/gamification-github/rest/hooks/${hookId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting github webhook');
    }
  });
}

export function saveGithubWebHook(organizationName, accessToken) {
  const formData = new FormData();
  formData.append('organizationName', organizationName);
  formData.append('accessToken', accessToken);
  return fetch('/gamification-github/rest/hooks', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      if (resp.status === 404 || resp.status === 401) {
        return resp.text().then((text) => {
          throw new Error(text);
        });
      } else {
        throw new Error('Error when saving github webhook');
      }
    }
  });
}

export function updateWebHookAccessToken(webHookId, accessToken) {
  const formData = new FormData();
  formData.append('accessToken', accessToken);
  return fetch(`/gamification-github/rest/hooks/${webHookId}`, {
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
  return fetch(`/gamification-github/rest/hooks/${organizationId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when deleting github webhook');
    }
  });
}

export function getWebHookRepos(organizationId, page, perPage, keyword) {
  return fetch(`/gamification-github/rest/hooks/${organizationId}/repos?page=${page || 0}&perPage=${perPage|| 10}&keyword=${keyword || ''}`, {
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

  return fetch('/gamification-github/rest/hooks/repo/status', {
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

export function forceUpdateWebhooks() {
  return fetch('/gamification-github/rest/hooks/forceUpdate', {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when updating github webhooks');
    }
  });
}