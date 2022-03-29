import gitHubWebHookManagementApp from './components/gitHubWebHookManagementApp.vue';

Vue.use(Vuetify);
const vueInstance = new Vue({
  el: '#gitHubWebHookManagementApp',
  render: (h) => h(gitHubWebHookManagementApp),
});
