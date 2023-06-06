import githubConnector from '../../js/githubConnector';

export function init() {
  extensionRegistry.registerExtension('gamification', 'connectors', githubConnector);
}