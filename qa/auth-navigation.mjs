import { chromium, expect } from '@playwright/test';
import { readFile, writeFile } from 'node:fs/promises';
import crypto from 'node:crypto';
if(process.env.QA_ALLOW_MUTATIONS!=='1')throw Error('Disposable QA database required');
const config=JSON.parse((await readFile('.local/config.json','utf8')).replace(/^\uFEFF/,''));
const browser=await chromium.launch();const context=await browser.newContext({viewport:{width:1366,height:600}});const page=await context.newPage();const results=[];const errors=[];
page.on('pageerror',e=>errors.push(e.message));
const base='http://localhost:3000';
async function go(path){await page.goto(base+path,{waitUntil:'networkidle'});}
async function login(email,password){await go('/client/login');await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();}
async function check(name,fn){await fn();results.push(name);console.log('PASS '+name);}
try{
await check('Responsive navigation at effective zoom widths',async()=>{
 for(const width of [1920,1518,1440,1366,1280,1200,1093,1024,911,800,683,390,320]){
  await page.setViewportSize({width,height:600});await go('/client/home');
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
  const header=page.locator('header').first();
  if(width>=1100){await expect(page.getByRole('button',{name:'Open menu',exact:true})).toBeHidden();const rectangles=await header.locator('a:visible,button:visible').evaluateAll(nodes=>nodes.map(n=>{const r=n.getBoundingClientRect();return {left:r.left,right:r.right,top:r.top,bottom:r.bottom};}));for(let i=1;i<rectangles.length;i++)expect(rectangles[i].left).toBeGreaterThanOrEqual(rectangles[i-1].right-1);}
  else{await page.getByRole('button',{name:'Open menu',exact:true}).click();const menu=page.getByRole('navigation',{name:'Mobile navigation'});expect(await menu.evaluate(n=>n.getBoundingClientRect().height)).toBe(600);await menu.getByRole('link',{name:'Sign in',exact:true}).scrollIntoViewIfNeeded();await expect(menu.getByRole('link',{name:'Sign in',exact:true})).toBeInViewport();await page.keyboard.press('Escape');expect(await page.evaluate(()=>document.body.style.overflow)).not.toBe('hidden');}
 }
 await page.setViewportSize({width:1366,height:768});await go('/client/home');await page.getByRole('button',{name:'More',exact:false}).click();await expect(page.locator('#more-links').getByRole('link',{name:'Contact'})).toBeVisible();await page.keyboard.press('Escape');await expect(page.locator('#more-links')).toHaveCount(0);await expect(page.locator('footer')).not.toContainText('Staff sign in');await page.screenshot({path:'qa/screenshots/navigation-1366.png'});
});
await check('Unified staff login and reachable logout on short screens',async()=>{
 await page.setViewportSize({width:1366,height:600});await login(config.ADMIN_EMAIL,config.ADMIN_PASSWORD);await page.waitForURL('**/admin/dashboard');await expect(page.getByRole('button',{name:'Sign out',exact:true})).toBeInViewport();
 await page.screenshot({path:'qa/screenshots/admin-short-screen.png'});await page.getByRole('button',{name:'Sign out',exact:true}).click();await page.waitForURL('**/client/login');expect((await context.cookies()).filter(c=>c.name.startsWith('wt_'))).toHaveLength(0);await go('/admin/dashboard');expect(page.url()).toContain('/client/login');
});
const staffEmail=`staff-ui-${crypto.randomBytes(5).toString('hex')}@example.test`;const staffPassword='StaffBrowserQA!12345';
await check('Create administrator through protected UI then sign in',async()=>{
 await login(config.ADMIN_EMAIL,config.ADMIN_PASSWORD);await page.waitForURL('**/admin/dashboard');await page.getByRole('link',{name:/Administrators/}).click();
 await page.getByLabel('Full name',{exact:true}).fill('Browser QA Administrator');await page.getByLabel('Email',{exact:true}).fill(staffEmail);await page.getByLabel('Staff title',{exact:true}).fill('Operations');await page.getByLabel('New administrator password',{exact:true}).fill(staffPassword);await page.getByLabel('Your current password',{exact:true}).fill(config.ADMIN_PASSWORD);await page.getByRole('button',{name:'Create administrator',exact:true}).click();await expect(page.getByRole('status')).toContainText('created');await expect(page.getByLabel('New administrator password',{exact:true})).toHaveValue('');await page.getByRole('button',{name:'Sign out',exact:true}).click();await page.waitForURL('**/client/login');await login(staffEmail,staffPassword);await page.waitForURL('**/admin/dashboard');await page.getByRole('button',{name:'Sign out',exact:true}).click();await page.waitForURL('**/client/login');
});
await check('Customer common login cannot access staff provisioning',async()=>{
 const email=`customer-ui-${crypto.randomBytes(5).toString('hex')}@example.test`;
 const register=await context.request.post(base+'/api/auth/register',{data:{email,password:'CustomerBrowserQA!123',fullName:'Customer QA',phone:'+94771234567'}});expect(register.status()).toBe(201);await context.request.post(base+'/api/auth/logout');await login(email,'CustomerBrowserQA!123');await page.waitForURL('**/client/my-bookings');expect((await context.request.get(base+'/api/admin/accounts')).status()).toBe(403);
 await page.setViewportSize({width:1366,height:600});await page.getByRole('button',{name:'My account',exact:false}).click();await page.locator('#account-links').getByRole('button',{name:'Sign out',exact:true}).click();await page.waitForURL('**/client/login');expect((await context.cookies()).filter(c=>c.name.startsWith('wt_'))).toHaveLength(0);
});
expect(errors).toEqual([]);
}finally{await writeFile('qa/auth-navigation-results.json',JSON.stringify({results,errors,passed:results.length,layoutWidths:[1920,1518,1440,1366,1280,1200,1093,1024,911,800,683,390,320],note:'CSS viewport coverage for zoom and screen-size reflow; native browser zoom controls not exercised'},null,2));await browser.close();}

