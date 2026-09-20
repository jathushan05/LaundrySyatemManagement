/* Homepage-only interactions; private records stay behind authentication. */
document.documentElement.classList.add('js');
const menu = document.getElementById('site-nav');
const toggle = document.getElementById('menu-toggle');
toggle.addEventListener('click', () => {
  const open = toggle.getAttribute('aria-expanded') !== 'true';
  toggle.setAttribute('aria-expanded', String(open));
  menu.classList.toggle('is-open', open);
});
menu.addEventListener('click', event => {
  if (event.target.closest('a')) { menu.classList.remove('is-open'); toggle.setAttribute('aria-expanded', 'false'); }
});
document.addEventListener('keydown', event => {
  if (event.key === 'Escape' && menu.classList.contains('is-open')) {
    menu.classList.remove('is-open'); toggle.setAttribute('aria-expanded', 'false'); toggle.focus();
  }
});
const hero = document.querySelector('.hero');
const slides = [...document.querySelectorAll('.hero-slide')];
const tabs = [...document.querySelectorAll('[data-slide]')];
const pause = document.getElementById('slide-pause');
const motion = window.matchMedia('(prefers-reduced-motion: reduce)');
let current = 0, paused = motion.matches, timer;
function showSlide(index) {
  current = (index + slides.length) % slides.length;
  slides.forEach((slide, i) => { slide.hidden = i !== current; slide.classList.toggle('is-active', i === current); });
  tabs.forEach((tab, i) => tab.setAttribute('aria-pressed', String(i === current)));
}
function stop() { clearInterval(timer); }
function start() {
  stop();
  if (!paused && !document.hidden && !hero.matches(':hover') && !hero.contains(document.activeElement)) {
    timer = setInterval(() => showSlide(current + 1), 6000);
  }
}
function labelPause() { pause.textContent = paused ? 'Play' : 'Pause'; pause.setAttribute('aria-label', paused ? 'Play slideshow' : 'Pause slideshow'); }
tabs.forEach(tab => tab.addEventListener('click', () => { showSlide(Number(tab.dataset.slide)); start(); }));
document.getElementById('slide-prev').addEventListener('click', () => { showSlide(current - 1); start(); });
document.getElementById('slide-next').addEventListener('click', () => { showSlide(current + 1); start(); });
pause.addEventListener('click', () => { paused = !paused; labelPause(); start(); });
hero.addEventListener('mouseenter', stop); hero.addEventListener('mouseleave', start);
hero.addEventListener('focusin', stop); hero.addEventListener('focusout', () => setTimeout(start, 0));
document.addEventListener('visibilitychange', start);
motion.addEventListener('change', () => { paused = motion.matches; labelPause(); start(); });
labelPause(); showSlide(0); start();
