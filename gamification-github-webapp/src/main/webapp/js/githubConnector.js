/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
 */
export default {
  name: 'github',
  title: 'Github Profile',
  description: 'Connect your Github account so your actions from there can be gamified and recognized',
  icon: 'mdi-github',
  initialized: true,
  isSignedIn: true,
  identifier: '',
  user: '',
  rank: 10,
  REDIRECT_URL: `${window.location.protocol}//${window.location.hostname}${window.location.port && ':' || ''}${window.location.port || ''}${eXo.env.portal.context}/githubAuth/callback`,
  PROFILE_BASER_URL: 'https://github.com',
  openOauthPopup(connector) {
    const width = 500;
    const height = 600;
    const left = window.innerWidth / 2 - width / 2;
    const top = window.innerHeight / 2 - height / 2;
    const authUrl = `https://github.com/login/oauth/authorize?client_id=98f2cb1bad3d5ab6ebb4&redirect_uri=${connector.REDIRECT_URL}`;
    return window.open(authUrl, 'Github OAuth', `width=${width}, height=${height}, left=${left}, top=${top}`);
  },
};
