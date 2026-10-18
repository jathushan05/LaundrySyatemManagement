// Static JSP presentation preview. Does not replace Tomcat integration testing.
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const os = require('node:os');
const assert = require('node:assert/strict');
const { chromium } = require('playwright');
const root = path.resolve(__dirname, '../src/main/webapp');
const screenshots = fs.mkdtempSync(path.join(os.tmpdir(), 'aquaclean-home-'));
let html = fs.readFileSync(path.join(root, 'WEB-INF/views/home.jsp'), 'utf8');
html = html.replace(/<%@[^%]*%>/g, '').replace(/<c:if\b[\s\S]*?<\/c:if>/g, '');
html = html.replace(/<c:choose>[\s\S]*?<c:otherwise>([\s\S]*?)<\/c:otherwise><\/c:choose>/g, '$1');
html = html.replaceAll('${pageContext.request.contextPath}', '');
const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://localhost');
  if (url.pathname === '/' || url.pathname === '/home') { res.setHeader('Content-Type', 'text/html'); res.end(html); return; }
  const target = path.resolve(root, '.' + url.pathname);
  if (!target.startsWith(root + path.sep) || !fs.existsSync(target) || !fs.statSync(target).isFile()) { res.writeHead(404); res.end(); return; }
  const ext = path.extname(target);
  res.setHeader('Content-Type', ({'.css':'text/css','.js':'text/javascript','.png':'image/png'})[ext] || 'text/plain');
  fs.createReadStream(target).pipe(res);
});
(async () => {
  let browser;
  try {
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
    browser = await chromium.launch({headless:true});
    const page = await browser.newPage({viewport:{width:1440,height:1000}});
    const errors = []; page.on('pageerror', e => errors.push(e.message));
    const address = `http://127.0.0.1:${server.address().port}`;
    await page.goto(address); await page.locator('.service-photo').first().waitFor();
    await page.locator('#slide-next').click();
    assert.equal(await page.locator('.hero-slide:not([hidden])').getAttribute('aria-label'), '2 of 3: Dry cleaning');
    await page.locator('#slide-prev').click();
    await page.locator('summary').first().click();
    assert.equal(await page.locator('details').first().getAttribute('open'), '');
    await page.locator('summary').first().click();
    await page.evaluate(() => window.scrollTo(0,0));
    await page.screenshot({path:path.join(screenshots, 'desktop.png'),fullPage:true});
    for (const width of [390,768]) {
      await page.setViewportSize({width,height:844});
      assert(await page.locator('#menu-toggle').isVisible());
      await page.locator('#menu-toggle').click();
      assert.equal(await page.locator('#menu-toggle').getAttribute('aria-expanded'), 'true');
      await page.locator('#site-nav a[href="#services"]').click();
      assert.equal(await page.locator('#menu-toggle').getAttribute('aria-expanded'), 'false');
      assert(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), `Overflow at ${width}`);
      if (width === 390) await page.screenshot({path:path.join(screenshots, 'mobile.png'),fullPage:true});
    }
    await page.emulateMedia({reducedMotion:'reduce'});
    assert.equal(await page.locator('#slide-pause').textContent(), 'Play');
    assert.deepEqual(errors, []);
    console.log('PASS: desktop/mobile layout, carousel next/previous, service expansion, menu, reduced motion, no JS errors.');
    console.log('Screenshots:', screenshots);
  } finally { if (browser) await browser.close(); server.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
