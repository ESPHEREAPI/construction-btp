export const environment = {
  production: true,
  // Relative and root-absolute: nginx proxies /api/* to the backend on the
  // same origin, regardless of which path the frontend itself is served
  // under (e.g. /construction-btp/).
  apiUrl: '/api',
  appName: 'GMC-cloud',
  version: '1.0.0'
};
