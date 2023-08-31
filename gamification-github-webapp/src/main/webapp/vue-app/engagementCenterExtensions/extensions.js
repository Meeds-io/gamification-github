export function init() {
  extensionRegistry.registerExtension('engagementCenterActions', 'user-actions', {
    type: 'github',
    options: {
      rank: 60,
      icon: 'fab fa-github',
      iconColorClass: 'text-color',
      match: (actionLabel) => [
        'commentPullRequest',
        'creatPullRequest',
        'reviewPullRequest',
        'pullRequestValidated',
        'pushCode',
        'addIssueLabel',
        'validatePullRequest',
        'commentIssue',
        'createIssue',
        'requestReviewForPullRequest'
      ].includes(actionLabel),
      getLabel: () => ''
    },
  });
}