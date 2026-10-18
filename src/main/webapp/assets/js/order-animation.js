/* Cart + check animations.
   1) Dashboard "New order" button: cart rolls in, check pops, then the order form opens.
   2) After an order is created: the same animation plays over the order page and fades away. */
(() => {
  const CART_SVG = `
<svg class="cart-anim-svg" viewBox="0 0 120 120" width="150" height="150" aria-hidden="true" focusable="false">
  <g class="cart-body">
    <g fill="none" stroke="#111" stroke-width="8" stroke-linecap="round" stroke-linejoin="round">
      <path d="M10 22H22Q28 22 30 30L46 80H96"/>
      <path d="M35 40H70"/>
      <path d="M40 62H92"/>
    </g>
    <g stroke="#111" stroke-width="6" stroke-linecap="round"><path d="M50 46V56"/><path d="M62 46V56"/></g>
    <g class="cart-wheel"><circle cx="50" cy="101" r="9" fill="#111"/><circle cx="50" cy="96.5" r="2.2" fill="#fff"/></g>
    <g class="cart-wheel"><circle cx="82" cy="101" r="9" fill="#111"/><circle cx="82" cy="96.5" r="2.2" fill="#fff"/></g>
  </g>
  <circle class="cart-ring" cx="94" cy="30" r="22" fill="none" stroke="#4cae32" stroke-width="3"/>
  <g class="cart-badge">
    <circle cx="94" cy="30" r="22" fill="#4cae32"/>
    <path class="cart-check" d="M83 31L91 39L105 23" pathLength="1" fill="none" stroke="#fff" stroke-width="6.5" stroke-linecap="round" stroke-linejoin="round"/>
  </g>
</svg>`;

  const reduceMotion = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  let busy = false;

  function showOverlay(title, subtitle) {
    const overlay = document.createElement('div');
    overlay.className = 'order-anim';
    overlay.setAttribute('role', 'status');
    overlay.setAttribute('aria-live', 'polite');
    overlay.innerHTML = CART_SVG;
    const heading = document.createElement('p');
    heading.textContent = title;
    overlay.appendChild(heading);
    if (subtitle) {
      const sub = document.createElement('small');
      sub.textContent = subtitle;
      overlay.appendChild(sub);
    }
    document.body.appendChild(overlay);
    requestAnimationFrame(() => overlay.classList.add('play'));
    return overlay;
  }

  function dismiss(overlay) {
    if (!overlay.isConnected) return;
    overlay.classList.add('leaving');
    setTimeout(() => overlay.remove(), 320);
  }

  function bindNewOrderButton() {
    const trigger = document.querySelector('[data-order-animation]');
    if (!trigger) return;
    trigger.addEventListener('click', event => {
      // Let new-tab / modified clicks behave normally.
      if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
      if (busy) { event.preventDefault(); return; }
      if (reduceMotion()) return;
      event.preventDefault();
      busy = true;
      showOverlay('Starting a new order…');
      setTimeout(() => window.location.assign(trigger.href), 1250);
    });
  }

  function playOrderCreated() {
    const marker = document.querySelector('[data-order-created]');
    if (!marker || reduceMotion()) return;
    const number = marker.dataset.orderNumber;
    const overlay = showOverlay('Order created!', number || '');
    const close = () => dismiss(overlay);
    overlay.addEventListener('click', close);
    document.addEventListener('keydown', event => { if (event.key === 'Escape') close(); }, { once: true });
    setTimeout(close, 2000);
  }

  document.addEventListener('DOMContentLoaded', () => {
    bindNewOrderButton();
    playOrderCreated();
  });

  // Back button restores the page from cache: clear any leftover overlay.
  window.addEventListener('pageshow', event => {
    if (!event.persisted) return;
    busy = false;
    document.querySelectorAll('.order-anim').forEach(el => el.remove());
  });
})();
