import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';
import { readFile } from 'node:fs/promises';
import { chromium, request } from '@playwright/test';

if (process.env.QA_ALLOW_MUTATIONS !== '1') {
  throw new Error('Set QA_ALLOW_MUTATIONS=1 to create and remove a temporary customer account.');
}
const saved = process.env.QA_BASE_URL
  ? null
  : JSON.parse((await readFile('.local/public-share.json', 'utf8')).replace(/^\uFEFF/, ''));
const origin = new URL(process.env.QA_BASE_URL ?? saved.url).origin;
const email = `public-qa-${randomUUID()}@example.test`;
const password = `Demo!${randomUUID()}`;
const api = await request.newContext({ baseURL: origin });
const browser = await chromium.launch();
const page = await browser.newPage();
let created = false;

try {
  // A foreign site must still be blocked; allowing the tunnel must not allow all origins.
  const foreign = await api.post('/api/auth/register', {
    headers: { Origin: 'https://unapproved.example.test' }, data: {},
  });
  assert.equal(foreign.status(), 403);

  // Submit the actual browser form so the request carries the public Origin header.
  await page.goto(`${origin}/client/register`, { waitUntil: 'networkidle' });
  await page.getByLabel('Full name', { exact: true }).fill('Public Registration QA');
  await page.getByLabel('Email', { exact: true }).fill(email);
  await page.getByLabel('Password', { exact: true }).fill(password);
  const responsePromise = page.waitForResponse(response =>
    new URL(response.url()).pathname === '/api/auth/register' && response.request().method() === 'POST');
  await page.getByRole('button', { name: 'Create account', exact: true }).click();
  const response = await responsePromise;
  created = response.status() === 201;
  assert.equal(response.status(), 201);
  assert.equal(response.headers()['access-control-allow-origin'], origin);
  await page.waitForURL(url => url.origin === origin && ['/client/my-bookings', '/client/home'].includes(url.pathname));
  const profile = await page.request.get(`${origin}/api/profile`);
  assert.equal(profile.status(), 200);
  assert.equal((await profile.json()).email, email);

  const logout = await page.request.post(`${origin}/api/auth/logout`, { headers: { Origin: origin } });
  assert.equal(logout.status(), 200);
  const signedOut = await page.request.get(`${origin}/api/profile`);
  assert.ok([401, 403].includes(signedOut.status()));
  const login = await page.request.post(`${origin}/api/auth/login`, {
    headers: { Origin: origin }, data: { email, password },
  });
  assert.equal(login.status(), 200);
  assert.equal((await page.request.get(`${origin}/api/profile`)).status(), 200);
  console.log('PASS public browser registration, session, logout/login, and foreign-origin rejection.');
} finally {
  try {
    if (created) {
      // Re-authenticate if an earlier assertion failed after logout, then remove only our account.
      await page.request.post(`${origin}/api/auth/login`, {
        headers: { Origin: origin }, data: { email, password },
      });
      const removed = await page.request.delete(`${origin}/api/profile`, { headers: { Origin: origin } });
      assert.equal(removed.status(), 200, 'Temporary customer cleanup failed');
      console.log('Temporary customer account removed.');
    }
  } finally {
    await api.dispose();
    await browser.close();
  }
}
