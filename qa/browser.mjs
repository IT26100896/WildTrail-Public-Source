import { chromium, expect } from '@playwright/test';
import { readFile, mkdir, writeFile } from 'node:fs/promises';
import crypto from 'node:crypto';

// Run against a disposable QA database; this suite creates campus sample records.
if (process.env.QA_ALLOW_MUTATIONS !== '1') throw new Error('Set QA_ALLOW_MUTATIONS=1 after starting the API with a disposable QA database.');
const config = JSON.parse((await readFile(process.env.QA_CONFIG_PATH || '.local/config.json', 'utf8')).replace(/^\uFEFF/, ''));
const base = process.env.QA_BASE_URL || 'http://localhost:3000';
const results = [], errors = [];
await mkdir('qa/screenshots', { recursive: true });
const browser = await chromium.launch();
const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
const customerPage = await context.newPage();
const adminContext = await browser.newContext({ viewport: { width: 1440, height: 900 } });
const adminPage = await adminContext.newPage();
let page = customerPage;
for (const target of [customerPage, adminPage]) {
target.on('pageerror', e => errors.push(e.message));
target.on('response', r => { if (r.url().startsWith(base) && r.status() >= 500) errors.push(`${r.status()} ${new URL(r.url()).pathname}`); });
target.on('dialog', dialog => dialog.accept());
}
async function check(name, task) { await task(); results.push(name); console.log(`PASS ${name}`); }
async function go(path) { page = path.startsWith('/admin') ? adminPage : customerPage; const response = await page.goto(base + path, { waitUntil: 'networkidle' }); expect(response?.status()).toBeLessThan(400); await expect(page.locator('body')).not.toContainText('Application error'); }
const unique = crypto.randomBytes(5).toString('hex');
const email = `browser-${unique}@example.test`;
const day = new Date(Date.now() + 20 * 864e5).toISOString().slice(0, 10);
try {
  await check('public routes and no published credentials', async () => {
    for (const route of ['/client/home','/client/destinations','/client/packages','/client/safaris','/client/vehicles','/client/about','/client/gallery','/client/reviews','/client/contact','/client/login','/client/register','/admin/login']) {
      await go(route);
      await expect(page.locator('body')).not.toContainText(/Admin@123|Customer@123|admin@wildtrail\.lk|nimal@example\.com|Demo credentials/i);
    }
  });
  await check('admin UI login and all protected panels', async () => {
    await go('/admin/login');
    await page.getByLabel('Email', { exact: true }).fill(config.ADMIN_EMAIL);
    await page.getByLabel('Password', { exact: true }).fill(config.ADMIN_PASSWORD);
    await page.getByRole('button', { name: 'Sign in', exact: true }).click();
    await page.waitForURL('**/admin/dashboard');
    for (const route of ['dashboard','bookings','payments','customers','packages','vehicles','guides','files','reviews','messages']) await go(`/admin/${route}`);
  });
  await check('customer UI registration in a separate browser context', async () => {
    await go('/client/register');
    await page.getByLabel('Full name', { exact: true }).fill('Browser QA Customer');
    await page.getByLabel('Phone', { exact: true }).fill('+94771234567');
    await page.getByLabel('Email', { exact: true }).fill(email);
    await page.getByLabel('Password', { exact: true }).fill('BrowserQA!12345');
    await page.getByRole('button', { name: 'Create account', exact: true }).click();
    await page.waitForURL('**/client/my-bookings');
    await go('/admin/dashboard');
  });
  await check('booking wizard and server price confirmation', async () => {
    await go(`/client/booking?package=1&date=${day}&guests=2`);
    await page.getByLabel('Safari date', { exact: true }).fill(day);
    await page.getByRole('button', { name: 'Continue' }).click();
    const jeep = page.getByRole('button').filter({ hasText: 'Standard Safari Jeep' }).first();
    await expect(jeep).toBeEnabled(); await jeep.click();
    await page.getByRole('button', { name: 'Continue' }).click();
    await page.getByLabel('Phone / WhatsApp *', { exact: true }).fill('+94771234567');
    await page.getByLabel('Pickup place *', { exact: true }).fill('QA park meeting point');
    await page.getByRole('button', { name: 'Continue' }).click();
    await expect(page.locator('body')).toContainText('Pay in cash');
    const confirm = page.getByRole('button', { name: /Confirm & book/ });
    await expect(confirm).toBeEnabled(); await confirm.click();
    await expect(page.locator('body')).toContainText('WT-', { timeout: 15000 });
    await page.screenshot({ path: 'qa/screenshots/booking-confirmation.png', fullPage: true });
    await go('/client/my-bookings');
    await expect(page.locator('article').first()).toContainText('PENDING');
  });
  await check('booking edit, statement, cash collection and refund in UI', async () => {
    await page.getByRole('button', { name: 'Change booking' }).first().click();
    await page.getByLabel('Guests', { exact: true }).fill('3');
    await page.getByRole('button', { name: 'Save changes' }).click();
    await expect(page.getByRole('button', { name: 'Save changes' })).toHaveCount(0);
    await page.getByRole('link', { name: 'View statement / receipt' }).first().click();
    await expect(page.locator('body')).toContainText('PENDING');
    await go('/admin/payments');
    await page.getByRole('button', { name: 'Mark paid', exact: true }).first().click();
    await expect(page.getByRole('button', { name: 'Refund', exact: true }).first()).toBeVisible();
    await go('/client/my-bookings'); await expect(page.locator('article').first()).toContainText('CONFIRMED');
    await go('/admin/payments'); await page.getByRole('button', { name: 'Refund', exact: true }).first().click();
    await expect(page.locator('tbody')).toContainText('REFUNDED');
    await go('/client/my-bookings'); await expect(page.locator('article').first()).toContainText('CANCELLED');
  });
  await check('profile update and server refresh', async () => {
    await go('/client/profile'); await page.getByLabel('Full name', { exact: true }).fill('Browser QA Updated');
    await page.getByRole('button', { name: 'Save profile', exact: true }).click();
    await expect(page.getByRole('status')).toContainText('Profile updated');
    await page.reload(); await expect(page.getByLabel('Full name', { exact: true })).toHaveValue('Browser QA Updated');
  });
  await check('review publishes immediately, admin replies, owner deletes', async () => {
    await go('/client/reviews'); await page.getByLabel('Your experience', { exact: true }).fill(`QA review ${unique}`);
    await page.getByRole('button', { name: 'Submit review', exact: true }).click();
    await expect(page.locator('body')).toContainText('Your review is now published');
    await go('/admin/reviews');
    const review=page.locator('article').filter({ hasText: `QA review ${unique}` });
    await expect(review).toContainText(email);
    await expect(page.getByRole('button', { name: 'Approve', exact: true })).toHaveCount(0);
    await review.getByLabel('Reply as administrator').fill('Thank you for your feedback');
    await review.getByRole('button', { name: 'Post reply', exact: true }).click();
    await expect(review).toContainText('Thank you for your feedback');
    await go('/client/my-reviews');
    await expect(page.getByRole('heading', { name: 'My reviews', exact: true })).toBeVisible();
    await expect(page.locator('body')).toContainText('Thank you for your feedback');
    await expect(page.getByRole('button', { name: 'Edit', exact: true })).toHaveCount(0);
    await page.getByRole('button', { name: 'Delete review', exact: true }).first().click();
    await expect(page.locator('body')).toContainText('No reviews submitted yet');
  });
  await check('contact form writes admin inbox', async () => {
    await go('/client/contact');
    await expect(page.getByLabel('Email', { exact: true })).toHaveValue(email); await page.getByLabel('Message', { exact: true }).fill(`Safari inquiry ${unique}`);
    await page.getByRole('button', { name: 'Send message', exact: true }).click();
    await expect(page.locator('body')).toContainText('message has been recorded');
    await go('/admin/messages'); await expect(page.locator('body')).toContainText(`Safari inquiry ${unique}`);
  });
  await check('guide file create, edit and delete UI', async () => {
    await go('/admin/guides'); await page.getByRole('button', { name: '+ New guide', exact: true }).click();
    await page.getByLabel('Guide name', { exact: true }).fill(`QA Guide ${unique}`);
    await page.getByLabel('Phone', { exact: true }).fill('+94771234567');
    await page.getByLabel('Licence number', { exact: true }).fill(`QA-${unique}`);
    await page.getByRole('button', { name: /Save/, exact: false }).click();
    const row = page.locator('table').first().locator('tr').filter({ hasText: `QA Guide ${unique}` }); await expect(row).toBeVisible();
    const create = await context.request.post(base + '/api/bookings', { data: { packageId: 1, vehicleId: 2, safariDate: day, timeSlot: '05:30', guests: 2, paymentMethod: 'CASH', fullName: 'Browser QA Updated', email, phone: '+94771234567' } });
    expect(create.status()).toBe(201); const scheduled = await create.json();
    const guideList = await (await adminContext.request.get(base + '/api/guides')).json();
    const guide = guideList.find(g => g.fullName === `QA Guide ${unique}`);
    await go('/admin/guides');
    await page.getByLabel('Active booking', { exact: true }).selectOption(String(scheduled.id));
    await page.getByLabel('Guide', { exact: true }).selectOption(String(guide.id));
    await page.getByRole('button', { name: 'Assign guide', exact: true }).click();
    await expect(page.locator('tr').filter({ hasText: scheduled.reference })).toContainText(`QA Guide ${unique}`);
    await page.screenshot({ path: 'qa/screenshots/guide-assignment.png', fullPage: true });
    expect((await adminContext.request.patch(base + `/api/bookings/${scheduled.id}`, { data: { status: 'CANCELLED' } })).status()).toBe(200);
    expect((await adminContext.request.delete(base + `/api/bookings/${scheduled.id}`)).status()).toBe(200);
    await go('/admin/guides');

    await row.getByRole('button', { name: 'Edit', exact: true }).click(); await page.getByLabel('Guide name', { exact: true }).fill(`QA Guide Edited ${unique}`);
    await page.getByRole('button', { name: /Save/, exact: false }).click();
    const edited = page.locator('table').first().locator('tr').filter({ hasText: `QA Guide Edited ${unique}` }); await expect(edited).toBeVisible();
    await edited.getByRole('button', { name: 'Delete', exact: true }).click(); await expect(edited).toHaveCount(0);
  });
  await check('six actual file snapshots save and read', async () => {
    await go('/admin/files');
    for (const name of ['users','packages','bookings','vehicles','payments','reviews']) {
      const box = page.locator('section').filter({ has: page.getByRole('heading', { name, exact: true }) });
      await box.getByRole('button', { name: 'Save snapshot', exact: true }).click();
      await expect(box.getByRole('button', { name: 'Read file', exact: true })).toBeEnabled();
      await box.getByRole('button', { name: 'Read file', exact: true }).click();
      await expect(page.locator('pre')).not.toContainText('password_hash');
      await expect(box.getByRole('button', { name: 'Save snapshot', exact: true })).toBeEnabled();
    }
  });
  await check('desktop and mobile pages fit viewport', async () => {
    for (const width of [1440,390]) {
      for (const target of [customerPage,adminPage]) await target.setViewportSize({ width, height: width === 390 ? 844 : 900 });
      for (const route of ['/client/home','/client/booking?package=1','/client/my-bookings','/client/contact','/admin/dashboard','/admin/guides','/admin/payments','/admin/messages']) {
        await go(route);
        const overflow = await page.evaluate(() => document.documentElement.scrollWidth > innerWidth + 1);
        expect(overflow, `${width}px overflow at ${route}`).toBe(false);
        if (['/client/home','/admin/dashboard','/admin/guides'].includes(route)) await page.screenshot({ path: `qa/screenshots/${width}-${route.split('/').pop()}.png`, fullPage: true });
      }
    }
  });
  expect(errors, 'uncaught frontend errors / HTTP 500').toEqual([]);
} finally {
  await writeFile('qa/browser-results.json', JSON.stringify({ at: new Date().toISOString(), browser: browser.version(), checks: results, errors, complete: results.length === 11 && errors.length === 0 }, null, 2));
  await browser.close();
}
