import { chromium, expect } from '@playwright/test';
import { readFile, mkdir, writeFile } from 'node:fs/promises';
import crypto from 'node:crypto';

if (process.env.QA_ALLOW_MUTATIONS !== '1') throw Error('A disposable QA database is required.');
const config = JSON.parse((await readFile(process.env.QA_CONFIG_PATH || '.local/config.json', 'utf8')).replace(/^\uFEFF/, ''));
const base = process.env.QA_BASE_URL || 'http://localhost:3000';
const browser = await chromium.launch();
const admin = await browser.newContext({ viewport: { width: 1366, height: 768 } });
const visitor = await browser.newContext();
const page = await admin.newPage();
const publicPage = await visitor.newPage();
const checks = [], errors = [];
const date = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Colombo', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(Date.now() + 40 * 864e5));
const unique = crypto.randomBytes(5).toString('hex');
let reviewId, bookingId;
await mkdir('qa/screenshots', { recursive: true });
for (const target of [page, publicPage]) {
  target.on('pageerror', error => errors.push(error.message));
  target.on('dialog', dialog => dialog.accept());
}
async function go(path) { await page.goto(base + path, { waitUntil: 'domcontentloaded' }); }
async function check(name, task) { await task(); checks.push(name); console.log('PASS ' + name); }
try {
  await check('Administrator can visit website through logo and return to dashboard', async () => {
    await go('/client/login');
    await page.getByLabel('Email', { exact: true }).fill(config.ADMIN_EMAIL);
    await page.getByLabel('Password', { exact: true }).fill(config.ADMIN_PASSWORD);
    await page.getByRole('button', { name: 'Sign in', exact: true }).click();
    await page.waitForURL('**/admin/dashboard');
    await page.getByRole('link', { name: 'WildTrail home', exact: true }).click();
    await page.waitForURL('**/client/home');
    await page.getByRole('button', { name: 'Admin account', exact: false }).click();
    await expect(page.locator('#account-links')).toContainText(config.ADMIN_EMAIL);
    await page.locator('#account-links').getByRole('link', { name: 'Admin dashboard', exact: true }).click();
    await page.waitForURL('**/admin/dashboard');
    await go('/client/login');
    await page.waitForURL('**/client/home');
    await expect(page.getByRole('button', { name: 'Admin account', exact: false })).toBeVisible();
  });
  await check('Administrator books a safari using account identity and pickup place', async () => {
    await go(`/client/booking?package=1&date=${date}&guests=2`);
    await page.getByRole('button', { name: 'Continue', exact: false }).click();
    await page.getByRole('button').filter({ hasText: 'Standard Safari Jeep' }).first().click();
    await page.getByRole('button', { name: 'Continue', exact: false }).click();
    await expect(page.getByLabel('Email *', { exact: true })).toHaveValue(config.ADMIN_EMAIL);
    await page.getByLabel('Phone / WhatsApp *', { exact: true }).fill('+94770001234');
    await page.getByLabel('Pickup place *', { exact: true }).fill('QA safari meeting point');
    await page.getByRole('button', { name: 'Continue', exact: false }).click();
    await page.getByRole('button', { name: /Confirm & book/ }).click();
    await expect(page.getByRole('heading', { name: 'Your safari is booked!' })).toBeVisible();
    await expect(page.locator('body')).toContainText('QA safari meeting point');
    await expect(page.locator('body')).toContainText('Payment summary');
    await expect(page.locator('body')).toContainText('Not paid yet');
    const rows = await (await admin.request.get(base + '/api/bookings/mine')).json();
    bookingId = rows.find(row => row.pickupPlace === 'QA safari meeting point').id;
    expect(rows.find(row => row.id === bookingId).customerRole).toBe('ROLE_ADMIN');
    await page.screenshot({ path: 'qa/screenshots/admin-personal-confirmation.png', fullPage: true, animations: 'disabled' });
  });
  await check('Personal payment history and receipt show details without admin redirect', async () => {
    await go('/client/payments');
    await expect(page.locator('body')).toContainText('QA safari meeting point');
    await expect(page.locator('body')).toContainText('Payment date');
    await go(`/client/receipts/${bookingId}`);
    await expect(page.locator('body')).toContainText('Vehicle number');
    await expect(page.locator('body')).toContainText('Payment details');
    await go('/admin/bookings');
    await expect(page.locator('tbody')).toContainText(config.ADMIN_EMAIL);
    await expect(page.locator('tbody')).toContainText('Administrator');
  });
  await check('Recorded payment date appears after collection', async () => {
    const payments = await (await admin.request.get(base + '/api/payments')).json();
    const payment = payments.find(row => row.bookingId === bookingId);
    expect((await admin.request.patch(base + `/api/payments/${payment.id}`, { data: { status: 'PAID' } })).status()).toBe(200);
    await go(`/client/receipts/${bookingId}`);
    await expect(page.locator('body')).toContainText('Payment receipt');
    await expect(page.locator('body')).not.toContainText('Not paid yet');
    await expect(page.locator('body')).toContainText('PAID');
  });
  await check('Administrator reviews publish immediately with authoritative name and email', async () => {
    await go('/client/reviews');
    await page.getByLabel('Your experience', { exact: true }).fill(`Admin review ${unique}`);
    await page.getByRole('button', { name: 'Submit review', exact: true }).click();
    await expect(page.locator('body')).toContainText('Your review is now published');
    const rows = await (await admin.request.get(base + '/api/reviews/mine')).json();
    const review = rows.find(row => row.comment === `Admin review ${unique}`);
    reviewId = review.id;
    expect(review.authorEmail).toBe(config.ADMIN_EMAIL);
    expect(review.authorRole).toBe('ROLE_ADMIN');
    await publicPage.goto(base + '/client/reviews', { waitUntil: 'domcontentloaded' });
    const published = publicPage.locator('article').filter({ hasText: `Admin review ${unique}` });
    await expect(published).toContainText('Administrator');
    expect(JSON.stringify(await (await visitor.request.get(base + '/api/reviews')).json())).not.toContain(config.ADMIN_EMAIL);
  });
  await check('Administrator replies from website and inbox shows true identity', async () => {
    const review = page.locator('article.reveal').filter({ hasText: `Admin review ${unique}` });
    await review.getByLabel('Reply as administrator').fill('Thanks from the safari team');
    await review.getByRole('button', { name: 'Post reply', exact: true }).click();
    await expect(review).toContainText('Thanks from the safari team');
    await go('/admin/reviews');
    const managed = page.locator('article').filter({ hasText: `Admin review ${unique}` });
    await expect(managed).toContainText(config.ADMIN_EMAIL);
    await expect(managed).toContainText('Thanks from the safari team');
    await expect(page.getByRole('button', { name: /^(Edit|Approve|Hide)$/ })).toHaveCount(0);
    await publicPage.reload({ waitUntil: 'domcontentloaded' });
    await expect(publicPage.locator('article').filter({ hasText: `Admin review ${unique}` })).toContainText('Thanks from the safari team');
    await go('/client/contact');
    await expect(page.getByLabel('Email', { exact: true })).toHaveValue(config.ADMIN_EMAIL);
    await page.getByLabel('Message', { exact: true }).fill(`Admin request ${unique}`);
    await page.getByRole('button', { name: 'Send message', exact: true }).click();
    await expect(page.locator('body')).toContainText('Your message has been recorded');
    await go('/admin/messages');
    const message = page.locator('article').filter({ hasText: `Admin request ${unique}` });
    await expect(message).toContainText(config.ADMIN_EMAIL);
    await expect(message).toContainText('Administrator');
  });
  await check('Samples are deleted from database and public website', async () => {
    await go('/admin/reviews');
    await page.getByRole('button', { name: /^samples$/i }).click();
    const samples = await (await admin.request.get(base + '/api/reviews?all=1')).json();
    const sampleName = samples.find(review => review.sample).authorName;
    const sample = page.locator('article').filter({ hasText: sampleName });
    await expect(sample).toBeVisible();
    await sample.getByRole('button', { name: 'Delete review', exact: true }).click();
    await expect(sample).toHaveCount(0);
    await publicPage.reload({ waitUntil: 'domcontentloaded' });
    await expect(publicPage.locator('body')).not.toContainText(sampleName);
  });
  await check('Administrator profile and responsive website remain usable', async () => {
    await go('/client/profile');
    await expect(page.getByLabel('Email', { exact: true })).toHaveValue(config.ADMIN_EMAIL);
    await expect(page.getByRole('button', { name: 'Delete account', exact: true })).toHaveCount(0);
    for (const width of [1920,1440,1366,1200,1100,1093,390,320]) {
      await page.setViewportSize({ width, height: 768 });
      for (const route of ['/client/home','/client/my-bookings','/client/payments','/client/reviews','/admin/reviews']) {
        await go(route);
        expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), `${width}: ${route}`).toBe(true);
      }
    }
    await page.setViewportSize({ width: 1366, height: 768 });
    await go('/admin/reviews');
    await page.screenshot({ path: 'qa/screenshots/review-replies-admin.png', fullPage: true });
  });
  await check('Own review delete removes replies and logout clears both areas', async () => {
    await go('/client/my-reviews');
    const own = page.locator('article').filter({ hasText: `Admin review ${unique}` });
    await own.getByRole('button', { name: 'Delete review', exact: true }).click();
    await expect(own).toHaveCount(0);
    await publicPage.reload({ waitUntil: 'domcontentloaded' });
    await expect(publicPage.locator('body')).not.toContainText(`Admin review ${unique}`);
    await page.getByRole('button', { name: 'Admin account', exact: false }).click();
    await page.locator('#account-links').getByRole('button', { name: 'Sign out', exact: true }).click();
    await page.waitForURL('**/client/login');
    expect((await admin.request.get(base + '/api/profile')).status()).toBe(401);
    expect((await admin.request.get(base + '/api/admin/dashboard')).status()).toBe(401);
  });
  expect(errors).toEqual([]);
} finally {
  // Remove only records created by this run; the database must still be disposable QA.
  await admin.request.post(base + '/api/auth/login', { data: { email: config.ADMIN_EMAIL, password: config.ADMIN_PASSWORD } });
  if (reviewId) await admin.request.delete(base + `/api/reviews/${reviewId}`);
  if (bookingId) {
    const payments = await (await admin.request.get(base + '/api/payments')).json();
    const payment = payments.find(row => row.bookingId === bookingId);
    if (payment?.status === 'PAID') await admin.request.patch(base + `/api/payments/${payment.id}`, { data: { status: 'REFUNDED' } });
    else await admin.request.patch(base + `/api/bookings/${bookingId}`, { data: { status: 'CANCELLED' } });
    await admin.request.delete(base + `/api/bookings/${bookingId}`);
  }
  await writeFile('qa/account-booking-reviews-results.json', JSON.stringify({ checks, errors, complete: checks.length === 9 && errors.length === 0 }, null, 2));
  await browser.close();
}
