import { chromium, expect, request } from '@playwright/test';
import { readFile } from 'node:fs/promises';
const base=JSON.parse((await readFile('.local/public-share.json','utf8')).replace(/^\uFEFF/,'')).url;
if(process.env.QA_ALLOW_MUTATIONS !== '1') throw new Error('Enable only for a temporary customer account that is removed after this check.');
const config=JSON.parse((await readFile('.local/config.json','utf8')).replace(/^\uFEFF/,''));
const email=`campus-ui-${Date.now()}@example.test`;
const admin=await request.newContext({baseURL:base});
expect((await admin.post('/api/auth/admin-login',{data:{email:config.ADMIN_EMAIL,password:config.ADMIN_PASSWORD}})).status()).toBe(200);
const browser=await chromium.launch();const page=await browser.newPage({viewport:{width:1200,height:700}});
try {
 const home=await page.goto(base+'/client/home',{waitUntil:'networkidle'});expect(home.status()).toBe(200);
 await expect(page.getByRole('navigation',{name:'Main navigation'}).getByRole('link',{name:'Destinations',exact:true})).toBeVisible();
 expect((await page.request.post(base+'/api/auth/register',{data:{fullName:'Campus UI Test',email,password:'CampusUITest!12345',phone:'+94771234567'}})).status()).toBe(201);
 const date=new Date(Date.now()+60*864e5).toISOString().slice(0,10);
 await page.goto(base+`/client/booking?package=1&date=${date}&guests=2`,{waitUntil:'networkidle'});
 await page.getByRole('button',{name:'Continue',exact:false}).click();
 await page.getByRole('button').filter({hasText:'Standard Safari Jeep'}).first().click();
 await page.getByRole('button',{name:'Continue',exact:false}).click();
 await page.getByLabel('Full name *',{exact:true}).fill('Campus UI Test');await page.getByLabel('Phone / WhatsApp *',{exact:true}).fill('+94771234567');
 await page.getByRole('button',{name:'Continue',exact:false}).click();
 await page.getByRole('button',{name:/Card payment · demo/}).click();
 await expect(page.locator('body')).toContainText('No charge is made');
 await expect(page.getByRole('button',{name:/Confirm & book demo/})).toBeEnabled();
 await expect(page.locator('input[autocomplete="cc-number"]')).toHaveCount(0);
 await page.screenshot({path:'qa/screenshots/card-demo-live.png',fullPage:true});
 console.log('PASS live home desktop navigation and card demo selection; no booking submitted and no card data requested');
 const api=await page.request.get(base+'/api/packages');expect(api.status()).toBe(200);console.log('PASS live API HTTP200');
}finally{const response=await admin.get('/api/customers');expect(response.status()).toBe(200);const customers=await response.json();const temporary=customers.find(c=>c.email===email);if(temporary)expect((await admin.delete('/api/customers/'+temporary.id)).ok()).toBe(true);await admin.dispose();await browser.close();console.log('Temporary customer account removed.');}
