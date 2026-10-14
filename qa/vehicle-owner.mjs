import { chromium, expect } from '@playwright/test';
import { readFile, mkdir, writeFile } from 'node:fs/promises';
import crypto from 'node:crypto';

if (process.env.QA_ALLOW_MUTATIONS !== '1') throw Error('Use an isolated QA database for vehicle CRUD checks.');
const config = JSON.parse((await readFile(process.env.QA_CONFIG_PATH || '.local/config.json', 'utf8')).replace(/^\uFEFF/, ''));
const base = process.env.QA_BASE_URL || 'http://localhost:3000';
const browser = await chromium.launch();
const admin = await browser.newContext({ viewport: { width: 1366, height: 768 } });
const visitor = await browser.newContext();
const page = await admin.newPage();
const name = 'QA owner vehicle ' + crypto.randomBytes(4).toString('hex');
const plate = 'QA-' + crypto.randomBytes(4).toString('hex').toUpperCase();
const updatedPlate = plate + '-UP';
const checks = [], errors = [];
let vehicleId;
page.on('pageerror', error => errors.push(error.message));
page.on('dialog', dialog => dialog.accept());
async function check(label, task) { await task(); checks.push(label); console.log('PASS ' + label); }
async function go(path) { await page.goto(base + path, { waitUntil: 'domcontentloaded' }); }
try {
  await check('Create vehicle with owner details through administrator form', async () => {
    expect((await admin.request.post(base + '/api/auth/login', { data: { email: config.ADMIN_EMAIL, password: config.ADMIN_PASSWORD } })).status()).toBe(200);
    const original = (await (await admin.request.get(base + '/api/vehicles?all=1')).json())[0];
    await go('/admin/vehicles');
    await page.getByRole('button', { name: '+ New vehicle', exact: true }).click();
    await page.getByLabel('Vehicle name', { exact: true }).fill(name);
    await page.getByLabel('Vehicle number (registration plate)', { exact: true }).fill(plate);
    await page.getByLabel('Owner full name (admin only)', { exact: true }).fill('Sample Vehicle Owner');
    await page.getByLabel('Owner telephone number', { exact: true }).fill('+94 77 000 1234');
    await page.getByLabel('Owner ID / NIC', { exact: true }).fill('OWNER-QA-TEST-001');
    await page.getByLabel('Owner address', { exact: true }).fill('Sample address, QA Town');
    await page.getByLabel('Image URL', { exact: true }).fill(original.imageUrl);
    await page.getByRole('button', { name: 'Save', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'New vehicle', exact: true })).toHaveCount(0);
    const rows = await (await admin.request.get(base + '/api/vehicles?all=1')).json();
    const saved = rows.find(vehicle => vehicle.name === name);
    expect(saved.ownerIdNumber).toBe('OWNER-QA-TEST-001'); expect(saved.registrationNumber).toBe(plate); vehicleId = saved.id;
    const card = page.locator('div.overflow-hidden').filter({ has: page.getByText(name, { exact: true }) });
    await card.getByText('Owner details (admin only)', { exact: true }).click();
    await expect(card).toContainText('Sample Vehicle Owner');
    await expect(card).toContainText('Sample address, QA Town');
  });
  await check('Edit and reload preserve all owner fields on mobile without overflow', async () => {
    await page.setViewportSize({ width: 390, height: 844 });
    const card = page.locator('div.overflow-hidden').filter({ has: page.getByText(name, { exact: true }) });
    await card.getByRole('button', { name: 'Edit', exact: true }).click();
    await expect(page.getByLabel('Owner ID / NIC', { exact: true })).toHaveValue('OWNER-QA-TEST-001');
    await page.getByLabel('Vehicle number (registration plate)', { exact: true }).fill(updatedPlate);
    await page.getByLabel('Owner telephone number', { exact: true }).fill('0770009999');
    await expect(page.getByLabel('Owner telephone number', { exact: true })).toHaveValue('0770009999');
    await page.getByLabel('Owner address', { exact: true }).fill('Updated sample address, QA Town');
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1)).toBe(true);
    const [updated] = await Promise.all([
      page.waitForResponse(response => response.url().endsWith(`/api/vehicles/${vehicleId}`) && response.request().method() === 'PUT'),
      page.getByRole('button', { name: 'Save', exact: true }).click(),
    ]);
    expect(updated.status()).toBe(200);
    const saved = await updated.json();
    expect(saved.ownerPhone).toBe('0770009999'); expect(saved.registrationNumber).toBe(updatedPlate);
    await expect(page.getByRole('button', { name: 'Save', exact: true })).toHaveCount(0);
    await page.reload({ waitUntil: 'domcontentloaded' });
    await card.getByRole('button', { name: 'Edit', exact: true }).click();
    await expect(page.getByLabel('Owner telephone number', { exact: true })).toHaveValue('0770009999');
    await expect(page.getByLabel('Vehicle number (registration plate)', { exact: true })).toHaveValue(updatedPlate);
    await expect(page.getByLabel('Owner address', { exact: true })).toHaveValue('Updated sample address, QA Town');
    await expect(page.getByLabel('Owner full name (admin only)', { exact: true })).toHaveValue('Sample Vehicle Owner');
    await expect(page.getByLabel('Owner ID / NIC', { exact: true })).toHaveValue('OWNER-QA-TEST-001');
    await mkdir('qa/screenshots', { recursive: true });
    await page.locator('form').locator('..').evaluate(element => { element.scrollTop = 0; });
    await page.screenshot({ path: 'qa/screenshots/vehicle-owner-mobile.png', animations: 'disabled' });
    await page.getByRole('button', { name: 'Cancel', exact: true }).click();
  });
  await check('Public vehicle API and website omit private owner information', async () => {
    for (const path of ['/api/vehicles','/api/vehicles?all=1']) {
      const rows = await (await visitor.request.get(base + path)).json();
      const vehicle = rows.find(row => row.id === vehicleId);
      expect(vehicle).toBeDefined();
      expect(vehicle.registrationNumber).toBe(updatedPlate);
      for (const field of ['ownerName','ownerPhone','ownerAddress','ownerIdNumber']) expect(vehicle).not.toHaveProperty(field);
    }
    const publicPage = await visitor.newPage();
    await publicPage.goto(base + '/client/vehicles', { waitUntil: 'domcontentloaded' });
    await expect(publicPage.locator('body')).not.toContainText('OWNER-QA-TEST-001');
    await expect(publicPage.locator('body')).not.toContainText('Updated sample address');
  });
  await check('Delete temporary vehicle through the fleet UI', async () => {
    const card = page.locator('div.overflow-hidden').filter({ has: page.getByText(name, { exact: true }) });
    await card.getByRole('button', { name: 'Delete', exact: true }).click();
    await expect(page.getByText(name, { exact: true })).toHaveCount(0);
    vehicleId = undefined;
  });
  expect(errors).toEqual([]);
} finally {
  if (vehicleId) await admin.request.delete(base + `/api/vehicles/${vehicleId}`);
  await writeFile('qa/vehicle-owner-results.json', JSON.stringify({ checks, errors, complete: checks.length === 4 && errors.length === 0 }, null, 2));
  await browser.close();
}
