(() => {
  const form = document.getElementById('signin-form');
  const password = document.getElementById('password');
  const toggle = document.getElementById('password-toggle');
  const submit = form.querySelector('[type="submit"]');
  const label = form.querySelector('.submit-label');
  const spinner = form.querySelector('.spinner');
  const progress = document.getElementById('signin-progress');
  let submitting = false;
  toggle.hidden = false;
  password.parentElement.classList.add('has-toggle');
  toggle.addEventListener('click', () => {
    const visible = password.type === 'password';
    password.type = visible ? 'text' : 'password';
    toggle.textContent = visible ? 'Hide' : 'Show';
    toggle.setAttribute('aria-label', visible ? 'Hide password' : 'Show password');
    toggle.setAttribute('aria-pressed', String(visible));
  });
  form.noValidate = true;
  const fields = [...form.querySelectorAll('input[required]')];
  function validate(input) {
    const invalid = !input.validity.valid;
    input.closest('.field').classList.toggle('has-error', invalid);
    input.setAttribute('aria-invalid', String(invalid));
    return !invalid;
  }
  fields.forEach(input => {
    input.addEventListener('blur', () => validate(input));
    input.addEventListener('input', () => {
      if (input.getAttribute('aria-invalid') === 'true') validate(input);
    });
  });
  form.addEventListener('submit', event => {
    if (submitting) { event.preventDefault(); return; }
    const valid = fields.map(validate).every(Boolean);
    if (!valid) {
      event.preventDefault(); fields.find(input => !input.validity.valid)?.focus(); return;
    }
    submitting = true; submit.disabled = true; spinner.hidden = false;
    label.textContent = 'Signing in...'; progress.textContent = 'Signing in. Please wait.';
    form.setAttribute('aria-busy', 'true');
  });
  window.addEventListener('pageshow', () => {
    submitting = false; submit.disabled = false; spinner.hidden = true;
    label.textContent = 'Sign in'; progress.textContent = ''; form.removeAttribute('aria-busy');
    password.type = 'password'; toggle.textContent = 'Show';
    toggle.setAttribute('aria-label', 'Show password'); toggle.setAttribute('aria-pressed', 'false');
  });
})();
