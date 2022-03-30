import GitHubWebHookManagementApp from './components/GitHubWebHookManagementApp.vue';

const components = {
  'github-webhook-management-app': GitHubWebHookManagementApp,
};

for (const key in components) {
  Vue.component(key, components[key]);
}