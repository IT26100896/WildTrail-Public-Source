import { chromium } from '@playwright/test';
const browser=await chromium.launch();const page=await browser.newPage({viewport:{width:1440,height:900}});
for(const width of [1440,390]){
 await page.setViewportSize({width,height:width===390?844:900});await page.goto('http://localhost:3000/client/home',{waitUntil:'networkidle'});
 const height=await page.evaluate(()=>document.documentElement.scrollHeight);
 for(let y=0;y<height;y+=600){await page.evaluate(y=>window.scrollTo({top:y,behavior:'instant'}),y);await page.waitForTimeout(100);}
 await page.evaluate(()=>window.scrollTo({top:0,behavior:'instant'}));await page.waitForTimeout(1000);
 console.log(JSON.stringify({width,unrevealed:await page.locator('.reveal:not(.in)').count(),brokenImages:await page.locator('img').evaluateAll(imgs=>imgs.filter(i=>i.complete&&!i.naturalWidth).map(i=>i.alt))}));
 await page.screenshot({path:`qa/screenshots/${width}-home.png`,fullPage:true});
 if(width===1440){await page.evaluate(()=>window.scrollTo({top:800,behavior:'instant'}));await page.screenshot({path:'qa/screenshots/home-content.png'});}
}await browser.close();
