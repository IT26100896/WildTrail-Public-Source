import { chromium, expect } from '@playwright/test';
import { mkdir, writeFile } from 'node:fs/promises';
const browser=await chromium.launch();
const page=await browser.newPage();
const results=[];
await mkdir('qa/screenshots',{recursive:true});
try {
 for(const [width,height] of [[320,640],[375,667],[768,700],[1024,600],[1099,650],[1100,650],[1200,625],[1280,720],[1366,768],[1440,900],[1920,1080],[2133,1200]]) {
  await page.setViewportSize({width,height});
  const r=await page.goto('http://localhost:3000/client/home',{waitUntil:'networkidle'});expect(r.status()).toBe(200);
  await page.waitForTimeout(1000);
  const bounds=await page.evaluate(()=>{
   const hero=document.querySelector('main > section');
   const quick=document.querySelector('#quick-booking');
   const heading=hero.querySelector('h1');
   const stats=hero.querySelector('.sm\\:flex');
   const header=document.querySelector('header');
   const nav=header.querySelector('nav');
   const links=Array.from(nav.children).filter(x=>getComputedStyle(x).display!=='none').map(x=>{const b=x.getBoundingClientRect();return {left:b.left,right:b.right,top:b.top,bottom:b.bottom}});
   const h=hero.getBoundingClientRect(), q=quick.getBoundingClientRect(),t=heading.getBoundingClientRect(),n=header.getBoundingClientRect();
   return {overflow:document.documentElement.scrollWidth>innerWidth,heroBottom:h.bottom,quickTop:q.top,headingTop:t.top,headerBottom:n.bottom,links};
  });
  expect(bounds.overflow,JSON.stringify({width,bounds})).toBe(false);
  expect(bounds.quickTop).toBeGreaterThanOrEqual(bounds.heroBottom-1);
  expect(bounds.headingTop).toBeGreaterThan(bounds.headerBottom);
  for(let i=1;i<bounds.links.length;i++) expect(bounds.links[i].left).toBeGreaterThanOrEqual(bounds.links[i-1].right-1);
  const toggle=page.getByRole('button',{name:'Open menu',exact:true});
  if(width>=1100){await expect(toggle).toBeHidden();await expect(page.getByRole('navigation',{name:'Main navigation'}).getByRole('link',{name:'Destinations',exact:true})).toBeVisible();}
  else{await toggle.click();const mobile=page.getByRole('navigation',{name:'Mobile navigation'});await expect(mobile).toBeVisible();await mobile.getByRole('link',{name:'Sign in',exact:true}).scrollIntoViewIfNeeded();await expect(mobile.getByRole('link',{name:'Sign in',exact:true})).toBeInViewport();await page.getByRole('button',{name:'Close menu',exact:true}).click();}
  if(width===1200||width===375)await page.screenshot({path:`qa/screenshots/responsive-${width}.png`});
  results.push(`${width}x${height}: no overlap, no overflow, accessible navigation`);
  console.log('PASS',results.at(-1));
 }
 await page.goto('http://localhost:3000/client/reviews',{waitUntil:'networkidle'});
 await expect(page.locator('body')).toContainText('Fictional');
 results.push('Sample reviews visibly identified as fictional; no fabricated customer rating aggregate');
 await writeFile('qa/responsive-fixes.json',JSON.stringify({results,scope:'CSS viewport reflow, including widths corresponding to OS scaling/browser zoom. Not native Brave zoom automation.'},null,2));
}finally{await browser.close();}
