import './initComponents.js';

const githubUserActions = ['commentPullRequest', 'creatPullRequest', 'reviewPullRequest', 'pullRequestValidated', 'pushCode'];

export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'github',
    options: {
      rank: 60,
      vueComponent: Vue.options.components['github-action-value'],
      match: (actionLabel) => githubUserActions.includes(actionLabel),
    },
  });
}