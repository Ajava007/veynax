import { test, expect, type Page } from '@playwright/test';

async function seedTestSession(page: Page, role: string) {
  await page.addInitScript((userRole) => {
    localStorage.setItem('veynax_user', JSON.stringify({
      token: 'ui-test-token', tokenType: 'Bearer', username: 'ui-test', fullName: 'UI Test', role: userRole
    }));
    localStorage.setItem('veynax_token', 'ui-test-token');
  }, role);
}

function apiResponse(data: unknown) {
  return JSON.stringify({ success: true, message: 'OK', data, timestamp: '2026-10-30T00:00:00Z' });
}

type PartFixture = {
  id: number; partCode: string; name: string; category: string; uom: string;
  standardCost: number; revision?: string; status: string; description?: string;
};

test.describe('VeynaX public entry and route guard', () => {
  test('public landing links to the sign-in page', async ({ page }) => {
    await page.goto('/');

    await expect(page).toHaveURL('/');
    await expect(page.getByRole('heading', { name: /the future takes shape on the line/i })).toBeVisible();
    await page.getByRole('link', { name: 'Sign in' }).click();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('protected factory pages redirect unauthenticated visitors to sign-in', async ({ page }) => {
    await page.goto('/production');

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('heading', { name: 'Welcome back' })).toBeVisible();
  });

  test('sign-in form requires a username and password', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByLabel('Username')).toBeVisible();
    await expect(page.getByLabel('Password')).toBeVisible();
    await page.getByRole('button', { name: /sign in to workspace/i }).click();

    await expect(page.getByRole('alert').first()).toContainText('This field is required.');
    await expect(page).toHaveURL(/\/login$/);
  });

  test('sign-in panel remains usable at a phone viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/login');

    await expect(page.getByRole('heading', { name: 'Welcome back' })).toBeVisible();
    await expect(page.getByRole('button', { name: /sign in to workspace/i })).toBeVisible();
  });

  test('unsupported approval and audit pages explain why records are missing', async ({ page }) => {
    await seedTestSession(page, 'ROLE_ADMIN');

    await page.goto('/approval-queues');
    await expect(page.getByText('Approval requests are unavailable.')).toBeVisible();
    await expect(page.getByText('No requests or decisions are being shown or saved here.')).toBeVisible();

    await page.goto('/audit-log');
    await expect(page.getByText('Audit records are unavailable.')).toBeVisible();
    await expect(page.getByText('Search and filters will be added when real audit records are available.')).toBeVisible();
  });

  test('dashboard does not show made-up production numbers when APIs are unavailable', async ({ page }) => {
    await seedTestSession(page, 'ROLE_ADMIN');

    await page.goto('/dashboard');

    await expect(page.getByRole('heading', { name: /Welcome back,/i })).toBeVisible();
    await expect(page.locator('.metric-card--primary .metric-value')).toContainText('—');
    await expect(page.getByText('Waiting for API', { exact: false })).toBeVisible();
  });

  test('factory admins can start a vehicle model form and see required field errors', async ({ page }) => {
    await seedTestSession(page, 'ROLE_FACTORY_ADMIN');
    await page.goto('/master-data/vehicle-models');

    await expect(page.getByRole('heading', { name: 'Vehicle models' })).toBeVisible();
    await page.getByRole('button', { name: 'New model' }).click();
    await page.getByRole('button', { name: 'Add model' }).click();
    await expect(page.getByRole('alert').first()).toContainText('This field is required.');
  });

  test('view-only users cannot see vehicle model actions', async ({ page }) => {
    await seedTestSession(page, 'ROLE_VIEWER');
    await page.goto('/master-data/vehicle-models');

    await expect(page.getByRole('heading', { name: 'Vehicle models' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'New model' })).toHaveCount(0);
  });

  test('engineering loads parts and updates BOM and routing for the selected model', async ({ page }) => {
    await seedTestSession(page, 'ROLE_FACTORY_ADMIN');
    let createdPart: unknown;
    let updatedPart: unknown;
    let createdBom: unknown;
    let updatedBom: unknown;
    let createdOperation: unknown;
    let updatedOperation: unknown;
    const partRows: PartFixture[] = [
      { id: 1, partCode: 'ENG-2', name: 'Electric motor', category: 'Electrical', uom: 'EA', standardCost: 1200, status: 'ACTIVE' },
      { id: 2, partCode: 'ENG-3', name: 'Steel bracket', category: 'Assembly', uom: 'EA', standardCost: 25, revision: 'B', status: 'ACTIVE' }
    ];
    await page.route('**/api/v1/engineering/parts**', async route => {
      if (route.request().method() === 'POST') {
        const body = await route.request().postDataJSON() as Omit<PartFixture, 'id'>;
        createdPart = body;
        const record = { id: 3, ...body } as PartFixture;
        partRows.push(record);
        return route.fulfill({ status: 200, contentType: 'application/json', body: apiResponse(record) });
      }
      if (route.request().method() === 'PUT') {
        const body = await route.request().postDataJSON() as Partial<PartFixture>;
        updatedPart = body;
        const id = Number(new URL(route.request().url()).pathname.split('/').pop());
        const index = partRows.findIndex(part => part.id === id);
        if (index >= 0) partRows[index] = { ...partRows[index], ...body };
        return route.fulfill({ status: 200, contentType: 'application/json', body: apiResponse(index >= 0 ? partRows[index] : body) });
      }
      return route.fulfill({ status: 200, contentType: 'application/json', body: apiResponse({ content: partRows }) });
    });
    await page.route('**/api/v1/master-data/vehicle-models**', route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: apiResponse({ content: [
        { id: 1, modelCode: 'AUTO-1', modelName: 'First model', brand: 'VeynaX', status: 'ACTIVE' },
        { id: 2, modelCode: 'AUTO-2', modelName: 'Second model', brand: 'VeynaX', status: 'ACTIVE' }
      ] })
    }));
    await page.route('**/api/v1/engineering/bom**', async route => {
      if (route.request().method() === 'POST') {
        createdBom = await route.request().postDataJSON();
        return route.fulfill({ status: 200, contentType: 'application/json', body: apiResponse({ id: 3, ...createdBom as object }) });
      }
      if (route.request().method() === 'PUT') {
        updatedBom = await route.request().postDataJSON();
        return route.fulfill({ status: 200, contentType: 'application/json', body: apiResponse({ id: 2, ...updatedBom as object }) });
      }
      const parent = new URL(route.request().url()).searchParams.get('parentPartCode');
      return route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: apiResponse(parent === 'ENG-2'
          ? [{ id: 4, parentPartCode: parent, componentPartCode: 'ENG-3', quantity: 1, scrapFactor: 0, optional: false, level: 2 }]
          : parent === 'ENG-3' ? []
          : [{ id: 2, parentPartCode: parent, componentPartCode: 'ENG-2', quantity: 2, scrapFactor: 0, optional: false, level: 1 }])
      });
    });
    await page.route('**/api/v1/engineering/routing**', async route => {
      if (route.request().method() === 'POST') {
        createdOperation = await route.request().postDataJSON();
        return route.fulfill({ status: 200, contentType: 'application/json', body: apiResponse({ id: 3, ...createdOperation as object }) });
      }
      if (route.request().method() === 'PUT') {
        updatedOperation = await route.request().postDataJSON();
        return route.fulfill({ status: 200, contentType: 'application/json', body: apiResponse({ id: 1, ...updatedOperation as object }) });
      }
      const modelCode = new URL(route.request().url()).searchParams.get('modelCode') ?? '';
      return route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: apiResponse([{ id: 1, modelCode, operationNumber: 10, operationName: 'Final assembly', workCenter: 'LINE-A', setupTimeSec: 60, cycleTimeSec: 90 }])
      });
    });

    await page.goto('/engineering');
    await expect(page.getByText('Electric motor').first()).toBeVisible();
    await page.getByRole('button', { name: 'Add part' }).first().click();
    await page.getByRole('button', { name: 'Add part' }).last().click();
    await expect(page.getByRole('alert').first()).toContainText('This field is required.');
    await page.getByRole('button', { name: 'Cancel' }).click();
    await page.getByRole('button', { name: 'Add part' }).click();
    await page.getByLabel('Part code').fill('PART-3');
    await page.getByLabel('Name', { exact: true }).fill('Test spacer');
    await page.getByLabel('Unit').fill('EA');
    await page.getByRole('button', { name: 'Add part' }).last().click();
    await expect(page.getByText('Part added.')).toBeVisible();
    expect(createdPart).toEqual(expect.objectContaining({ partCode: 'PART-3', name: 'Test spacer', uom: 'EA' }));
    await page.getByRole('button', { name: 'Edit Test spacer' }).click();
    await page.getByLabel('Name', { exact: true }).fill('Updated spacer');
    await page.getByRole('button', { name: 'Save changes' }).click();
    await expect(page.getByText('Part updated.')).toBeVisible();
    expect(updatedPart).toEqual(expect.objectContaining({ partCode: 'PART-3', name: 'Updated spacer' }));

    await page.getByRole('button', { name: 'BOM (multi-level)' }).click();
    const modelSelect = page.getByRole('combobox', { name: 'Vehicle model for bill of materials' });
    await expect(modelSelect).toHaveValue('AUTO-1');
    await modelSelect.selectOption('AUTO-2');
    await expect(page.getByText('Second model', { exact: true })).toBeVisible();
    await expect(page.getByText('Electric motor').first()).toBeVisible();
    await expect(page.getByText('Steel bracket', { exact: true })).toBeVisible();
    await expect(page.getByText('2', { exact: true }).first()).toBeVisible();
    await page.getByRole('button', { name: 'Add component' }).click();
    await page.getByLabel('Component part').selectOption('ENG-2');
    await page.getByLabel('Quantity').fill('3');
    await page.getByRole('button', { name: 'Add component' }).last().click();
    await expect(page.getByText('BOM row added.')).toBeVisible();
    expect(createdBom).toEqual(expect.objectContaining({ parentPartCode: 'AUTO-2', componentPartCode: 'ENG-2', quantity: 3 }));
    await expect(page.getByRole('button', { name: 'Delete BOM row ENG-2' })).toHaveCount(0);
    await page.getByRole('button', { name: 'Edit BOM row ENG-2' }).click();
    await page.getByLabel('Quantity').fill('4');
    await page.getByRole('button', { name: 'Save changes' }).click();
    await expect(page.getByText('BOM row updated.')).toBeVisible();
    expect(updatedBom).toEqual(expect.objectContaining({ parentPartCode: 'AUTO-2', componentPartCode: 'ENG-2', quantity: 4 }));

    await page.getByRole('button', { name: 'Manufacturing Routing' }).click();
    await expect(page.getByText('Final assembly')).toBeVisible();
    await expect(page.getByText('1.50', { exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Delete operation 10' })).toHaveCount(0);
    await page.getByRole('button', { name: 'Edit operation 10' }).click();
    await page.getByLabel('Cycle (seconds)').fill('100');
    await page.getByRole('button', { name: 'Save changes' }).click();
    await expect(page.getByText('Work step updated.')).toBeVisible();
    expect(updatedOperation).toEqual(expect.objectContaining({ modelCode: 'AUTO-2', operationNumber: 10, cycleTimeSec: 100 }));
    await page.getByRole('button', { name: 'Add work step' }).click();
    await page.getByLabel('Step name').fill('Final inspection');
    await page.getByLabel('Work center').fill('LINE-B');
    await page.getByLabel('Cycle (seconds)').fill('120');
    await page.getByRole('button', { name: 'Add work step' }).last().click();
    await expect(page.getByText('Work step added.')).toBeVisible();
    expect(createdOperation).toEqual(expect.objectContaining({ modelCode: 'AUTO-2', operationName: 'Final inspection', workCenter: 'LINE-B', cycleTimeSec: 120 }));
  });
});
