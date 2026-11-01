import { defineConfig, devices } from '@playwright/test';

const chromiumExecutable = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH;

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:4201',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'], launchOptions: chromiumExecutable ? { executablePath: chromiumExecutable } : undefined } },
  ],
  webServer: {
    command: `"${process.execPath}" node_modules/serve/build/main.js -s dist/veynax-frontend/browser -l 4201`,
    url: 'http://localhost:4201',
    reuseExistingServer: false,
    timeout: 120000,
  },
});
