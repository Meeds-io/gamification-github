import gitHubWebHookManagementApp from './components/gitHubWebHookManagementApp.vue';
import './../css/main.less';
Vue.use(Vuetify);

const vuetify = new Vuetify({
  dark: true,
  iconfont: 'mdi',
});

$(document).ready(() => {
    new Vue({
      render: (h) => h(gitHubWebHookManagementApp),
      vuetify,
    }).$mount('#gitHubWebHookManagementApp');

});