const githubUserActions = ['commentPullRequest', 'creatPullRequest', 'reviewPullRequest', 'pullRequestValidated', 'pushCode'];

export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'github',
    options: {
      rank: 60,
      icon: 'mdi-github',
      match: (actionLabel) => githubUserActions.includes(actionLabel),
      getLabel: () => ''
    },
  });
}