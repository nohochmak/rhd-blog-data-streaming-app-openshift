export const environment = {
  production: false,
  topNSize: window["env"]["topNSize"] || 50,
  queueSize: window["env"]["queueSize"] || 15,
  apiHost: window["env"]["apiHost"] || "localhost",
  apiPort: window["env"]["apiPort"] || 8080
};
