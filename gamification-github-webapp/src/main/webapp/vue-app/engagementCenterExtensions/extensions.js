const githubUserActions = ['commentPullRequest', 'creatPullRequest', 'reviewPullRequest', 'pullRequestValidated', 'pushCode'];

export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'github',
    options: {
      rank: 60,
      icon: 'fab fa-github',
      iconColorClass: 'text-color',
      match: (actionLabel) => githubUserActions.includes(actionLabel),
      getLabel: () => ''
    },
  });
}