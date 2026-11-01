# VeynaX Angular Frontend

This is the existing Angular 17 standalone frontend. It calls the separately maintained Spring Boot API; the frontend development roadmap does not make backend or database changes.

## Requirements

- Node.js 20 or newer
- Dependencies from the checked-in `package-lock.json`
- The API at `http://localhost:8080` for authenticated/live workflows

## Run locally

```powershell
cd D:\Sircopy\frontend
npm ci
npm start
```

Open `http://localhost:4200`. `proxy.conf.json` forwards `/api` to `http://localhost:8080`; both environment files use relative API paths.

## Verify

```powershell
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
npm run e2e
```

Unit specs use Karma/Jasmine and need a runnable ChromeHeadless browser. E2E runs with Playwright Chromium and serves the production build from `dist/veynax-frontend/browser`.

The API requires environment-provided database and JWT secrets. Bootstrap users and synthetic seed data are disabled by default in the backend; do not assume default login credentials exist. The frontend does not include or expose static demo credentials.

