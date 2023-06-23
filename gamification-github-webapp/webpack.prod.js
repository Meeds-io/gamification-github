const path = require('path');

const config = {
  context: path.resolve(__dirname, '.'),
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
          'eslint-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
          'eslint-loader',
        ]
      }
    ]
  },
  entry: {
    gitHubWebHookManagement: './src/main/webapp/vue-app/webhook-management/main.js',
    engagementCenterExtensions: './src/main/webapp/vue-app/engagementCenterExtensions/extensions.js',
    githubUserConnectorExtension: './src/main/webapp/vue-app/githubUserConnectorExtension/extension.js',
    githubAdminConnectorExtension: './src/main/webapp/vue-app/githubAdminConnectorExtension/extension.js'
  },
  output: {
    path: path.join(__dirname, 'target/gamification-github/'),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  externals: {
    vue: 'Vue',
    vuetify: 'Vuetify',
    jquery: '$',
  },
};

module.exports = config;
