// Metro configured for the pnpm monorepo: watch the workspace root and resolve
// the @cyan/* packages from the root node_modules.
const { getDefaultConfig } = require("expo/metro-config");
const path = require("path");

const projectRoot = __dirname;
const workspaceRoot = path.resolve(projectRoot, "../..");

const config = getDefaultConfig(projectRoot);

config.watchFolders = [workspaceRoot];
config.resolver.nodeModulesPaths = [
  path.resolve(projectRoot, "node_modules"),
  path.resolve(workspaceRoot, "node_modules"),
];
config.resolver.disableHierarchicalLookup = true;

// Native-only modules that have no web implementation. On the web preview they
// are stubbed to an empty module so Metro never tries to bundle their native
// (codegen) internals; the ad/billing adapters detect the empty stub and fall
// back to their dev behaviour. Device builds are unaffected.
// expo-sqlite is imported (transitively) by the store but never used on web —
// the store swaps in a localStorage-backed KV there. Stubbing it stops the
// native "ExpoSQLite" module from being required in the browser.
const NATIVE_ONLY_ON_WEB = ["react-native-google-mobile-ads", "react-native-iap", "expo-sqlite"];
config.resolver.resolveRequest = (context, moduleName, platform) => {
  if (platform === "web" && NATIVE_ONLY_ON_WEB.some((m) => moduleName === m || moduleName.startsWith(m + "/"))) {
    return { type: "empty" };
  }
  return context.resolveRequest(context, moduleName, platform);
};

module.exports = config;
