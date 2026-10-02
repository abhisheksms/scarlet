module.exports = function (api) {
  api.cache(true);
  return {
    // babel-preset-expo adds the react-native-worklets plugin (Reanimated 4)
    // on its own when the package is installed — listing it here as well
    // would apply it twice.
    presets: ["babel-preset-expo"],
  };
};
