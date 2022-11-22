import GithubActionValue from './components/GithubActionValue.vue';
const components = {
  'github-action-value': GithubActionValue,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

