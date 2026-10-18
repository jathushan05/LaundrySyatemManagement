/* Decorative dust transition; registration still uses the original link. */
(() => {
  const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)');
  let active = false, frame = 0, timeout = 0, canvas = null;
  const animations = [];
  function reset() {
    active = false;
    cancelAnimationFrame(frame); clearTimeout(timeout);
    animations.splice(0).forEach(animation => animation.cancel());
    canvas?.remove(); canvas = null;
    document.body.removeAttribute('aria-busy');
  }
  window.addEventListener('pageshow', reset);
  document.addEventListener('click', event => {
    const link = event.target.closest('a[href]');
    if (!link || event.defaultPrevented || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey
        || link.hasAttribute('download') || (link.target && link.target !== '_self')) return;
    const target = new URL(link.href, location.href);
    if (target.origin !== location.origin || !/\/(signup|register)\/?$/.test(target.pathname)) return;
    if (reducedMotion.matches || typeof Element.prototype.animate !== 'function') return;
    event.preventDefault();
    if (active) return;
    active = true;
    const navigate = () => location.assign(target.href);
    // Navigation must still happen if rendering fails or the tab is throttled.
    timeout = setTimeout(navigate, 1350);
    try {
      canvas = document.createElement('canvas');
      canvas.setAttribute('aria-hidden', 'true');
      canvas.style.cssText = 'position:fixed;inset:0;width:100%;height:100%;pointer-events:all;z-index:2147483647;';
      const width = innerWidth, height = innerHeight, scale = Math.min(devicePixelRatio || 1, 2);
      canvas.width = width * scale; canvas.height = height * scale;
      const context = canvas.getContext('2d');
      if (!context) { reset(); navigate(); return; }
      context.scale(scale, scale);
      const colors = ['#1677ff', '#82b9ed', '#c6d8e9', '#eff5fc', '#75879b', '#294561'];
      const particles = Array.from({length:Math.min(1000, Math.round(width * height / 850))}, () => ({
        x:Math.random() * width, y:Math.random() * height, size:1 + Math.random() * 4,
        dx:60 + Math.random() * 200, dy:-30 - Math.random() * 180,
        delay:Math.random() * 0.3, color:colors[Math.floor(Math.random() * colors.length)]
      }));
      for (const child of document.body.children) {
        if (['SCRIPT','STYLE','LINK'].includes(child.tagName)) continue;
        animations.push(child.animate([
          {opacity:1, filter:'blur(0px)', transform:'translate(0,0)'},
          {opacity:0.65, filter:'blur(1px)', offset:0.3},
          {opacity:0, filter:'blur(5px)', transform:'translate(20px,-10px)'}
        ], {duration:1000, easing:'ease-in', fill:'forwards'}));
      }
      document.body.appendChild(canvas);
      document.body.setAttribute('aria-busy', 'true');
      const started = performance.now();
      function draw(now) {
        const progress = Math.min((now - started) / 1150, 1);
        context.clearRect(0, 0, width, height);
        for (const particle of particles) {
          const t = Math.max(0, (progress - particle.delay) / (1 - particle.delay));
          context.globalAlpha = Math.sin(Math.PI * t) * 0.85;
          context.fillStyle = particle.color;
          context.fillRect(particle.x + particle.dx * t, particle.y + particle.dy * t,
            particle.size * (1 - t * 0.7), particle.size * (1 - t * 0.7));
        }
        if (progress < 1) frame = requestAnimationFrame(draw);
        else { clearTimeout(timeout); navigate(); }
      }
      frame = requestAnimationFrame(draw);
    } catch (error) { reset(); navigate(); }
  });
})();
