const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader')

const config = {
  context: path.resolve(__dirname, '.'),
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
        ]
      }
    ]
  },
  entry: {
    engagementCenterExtensions: './src/main/webapp/vue-app/engagementCenterExtensions/extensions.js',
    connectorExtensions: './src/main/webapp/vue-app/connectorExtensions/extensions.js',
    connectorEventExtensions: './src/main/webapp/vue-app/connectorEventExtensions/extensions.js',
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
  plugins: [
    new ESLintPlugin({
      files: [
        './src/main/webapp/vue-app/*.js',
        './src/main/webapp/vue-app/*.vue',
        './src/main/webapp/vue-app/**/*.js',
        './src/main/webapp/vue-app/**/*.vue',
      ],
    }),
    new VueLoaderPlugin()
  ],
};

module.exports = config;
