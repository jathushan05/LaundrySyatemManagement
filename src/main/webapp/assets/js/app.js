/* AquaClean shared interactions. No business rule relies on JavaScript alone. */
document.addEventListener('DOMContentLoaded', () => {
  bindSidebar();
  bindValidation();
  bindPasswordToggles();
  bindPasswordStrength();
  bindConfirmations();
  bindLiveSearch();
  bindOrderForm();
  bindStarPicker();
  bindDateRules();
  bindCharacterCounters();
  bindProgress();
  bindStockForm();
});

function bindSidebar() {
  const sidebar = document.getElementById('sidebar');
  const backdrop = document.getElementById('sidebarBackdrop');
  const toggle = document.getElementById('sidebarToggle');
  if (!sidebar || !toggle) return;
  const close = () => { sidebar.classList.remove('open'); backdrop?.classList.remove('show'); };
  toggle.addEventListener('click', () => { sidebar.classList.toggle('open'); backdrop?.classList.toggle('show'); });
  backdrop?.addEventListener('click', close);
}

function bindValidation() {
  const password = document.getElementById('password');
  const confirmPassword = document.getElementById('confirmPassword');
  if (password && confirmPassword) {
    const validateMatch = () => {
      if (confirmPassword.value && password.value !== confirmPassword.value) {
        confirmPassword.setCustomValidity('Passwords must match.');
      } else {
        confirmPassword.setCustomValidity('');
      }
    };
    password.addEventListener('input', validateMatch);
    confirmPassword.addEventListener('input', validateMatch);
  }

  document.querySelectorAll('.needs-validation').forEach(form => {
    form.addEventListener('submit', event => {
      if (!form.checkValidity()) {
        event.preventDefault(); event.stopPropagation();
        form.querySelector(':invalid')?.focus();
      } else {
        const button = form.querySelector('button[type="submit"],button:not([type])');
        button?.querySelector('.button-label')?.classList.add('opacity-50');
        button?.querySelector('.spinner-border')?.classList.remove('d-none');
      }
      form.classList.add('was-validated');
    });
  });
}

function bindPasswordToggles() {
  document.querySelectorAll('.password-toggle').forEach(button => button.addEventListener('click', () => {
    const input = document.getElementById(button.dataset.target);
    if (!input) return;
    const willShow = input.type === 'password';
    input.type = willShow ? 'text' : 'password';
    button.querySelector('i')?.classList.toggle('bi-eye');
    button.querySelector('i')?.classList.toggle('bi-eye-slash');
    button.setAttribute('aria-pressed', String(willShow));
    button.setAttribute('aria-label', willShow ? 'Hide password' : 'Show password');
  }));
}

function bindPasswordStrength() {
  const password = document.getElementById('password');
  const assist = document.getElementById('passwordAssist');
  const label = document.getElementById('passwordStrengthLabel');
  if (!password || !assist || !label) return;

  const update = () => {
    const value = password.value;
    if (!value) {
      delete assist.dataset.strength;
      label.textContent = 'Password strength: not entered';
      return;
    }

    let score = 0;
    if (value.length >= 8) score++;
    if (value.length >= 12) score++;
    if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score++;
    if (/\d/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;

    const strength = score <= 1 ? 'weak' : score <= 2 ? 'fair' : score <= 3 ? 'good' : 'strong';
    assist.dataset.strength = strength;
    label.textContent = `Password strength: ${strength}`;
  };

  password.addEventListener('input', update);
  update();
}

function bindConfirmations() {
  const modalElement = document.getElementById('confirmModal');
  if (!modalElement || typeof bootstrap === 'undefined') return;
  const modal = new bootstrap.Modal(modalElement);
  const message = document.getElementById('confirmMessage');
  const confirmButton = document.getElementById('confirmAction');
  let pendingForm = null;
  document.querySelectorAll('form[data-confirm]').forEach(form => form.addEventListener('submit', event => {
    event.preventDefault(); pendingForm = form; message.textContent = form.dataset.confirm; modal.show();
  }));
  confirmButton?.addEventListener('click', () => { if (pendingForm) pendingForm.submit(); });
}

function bindLiveSearch() {
  document.querySelectorAll('.filter-bar .search-field input').forEach(input => input.addEventListener('input', () => {
    const panel = input.closest('.panel');
    const rows = panel?.querySelectorAll('table tbody tr');
    if (!rows?.length) return;
    const term = input.value.trim().toLowerCase();
    rows.forEach(row => row.hidden = term.length > 1 && !row.textContent.toLowerCase().includes(term));
  }));
}

function bindOrderForm() {
  const container = document.getElementById('orderItems');
  if (!container) return;
  const template = document.getElementById('orderItemTemplate');
  const calculate = () => {
    let total = 0;
    container.querySelectorAll('.order-item-row').forEach(row => {
      const quantity = Number(row.querySelector('.quantity-input')?.value || 0);
      const price = Number(row.querySelector('.price-input')?.value || 0);
      const subtotal = Math.max(0, quantity * price);
      row.querySelector('.line-total span').textContent = subtotal.toFixed(2);
      total += subtotal;
    });
    const output = document.getElementById('orderGrandTotal');
    if (output) output.textContent = total.toFixed(2);
  };
  const bindRow = row => {
    row.querySelector('.service-select')?.addEventListener('change', event => {
      const price = event.target.selectedOptions[0]?.dataset.price;
      if (price !== undefined) row.querySelector('.price-input').value = Number(price).toFixed(2);
      calculate();
    });
    row.querySelectorAll('.quantity-input,.price-input').forEach(input => input.addEventListener('input', calculate));
    row.querySelector('.remove-order-item')?.addEventListener('click', () => {
      if (container.querySelectorAll('.order-item-row').length === 1) {
        showInlineNotice('An order needs at least one item.'); return;
      }
      row.remove(); calculate();
    });
  };
  container.querySelectorAll('.order-item-row').forEach(bindRow);
  document.getElementById('addOrderItem')?.addEventListener('click', () => {
    const fragment = template.content.cloneNode(true);
    const row = fragment.querySelector('.order-item-row');
    container.appendChild(fragment); bindRow(row); row.querySelector('input')?.focus(); calculate();
  });
  calculate();
}

function bindStarPicker() {
  document.querySelectorAll('.star-picker').forEach(picker => {
    const input = picker.querySelector('input[name="rating"]');
    const buttons = [...picker.querySelectorAll('button[data-value]')];
    const paint = value => buttons.forEach(button => button.classList.toggle('selected', Number(button.dataset.value) <= value));
    paint(Number(picker.dataset.rating || 0));
    buttons.forEach(button => {
      button.addEventListener('mouseenter', () => paint(Number(button.dataset.value)));
      button.addEventListener('focus', () => paint(Number(button.dataset.value)));
      button.addEventListener('click', () => { input.value = button.dataset.value; picker.dataset.rating = button.dataset.value; paint(Number(button.dataset.value)); });
    });
    picker.addEventListener('mouseleave', () => paint(Number(picker.dataset.rating || 0)));
  });
}

function bindDateRules() {
  const today = new Date(); today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
  document.querySelectorAll('[data-min-today]').forEach(input => input.min = today.toISOString().slice(0, 10));
  document.querySelectorAll('[data-after]').forEach(input => {
    const before = document.querySelector(`[name="${input.dataset.after}"]`);
    const validate = () => {
      input.setCustomValidity(before?.value && input.value && input.value < before.value ? 'The date must be after the completion date.' : '');
    };
    before?.addEventListener('change', validate); input.addEventListener('change', validate); validate();
  });
  document.querySelectorAll('[data-after-datetime]').forEach(input => {
    const before = document.querySelector(`[name="${input.dataset.afterDatetime}"]`);
    const validate = () => input.setCustomValidity(before?.value && input.value && input.value < before.value ? 'Delivery must be after pickup.' : '');
    before?.addEventListener('change', validate); input.addEventListener('change', validate); validate();
  });
}

function bindCharacterCounters() {
  document.querySelectorAll('textarea[maxlength]').forEach(area => {
    const output = area.parentElement.querySelector('[data-char-count]');
    if (!output) return;
    const update = () => output.textContent = area.value.length;
    area.addEventListener('input', update); update();
  });
}

function bindProgress() {
  document.querySelectorAll('.order-progress').forEach(progress => {
    if (progress.dataset.currentStatus === 'CANCELLED') { progress.classList.add('cancelled'); return; }
    const steps = [...progress.querySelectorAll('.progress-step')];
    const current = steps.findIndex(step => step.dataset.step === progress.dataset.currentStatus);
    steps.forEach((step, index) => step.classList.toggle('complete', index <= current));
  });
}

function bindStockForm() {
  const type = document.getElementById('stockType');
  const order = document.getElementById('stockOrder');
  if (!type || !order) return;
  const update = () => { order.required = type.value === 'USAGE'; order.closest('div').classList.toggle('required-field', order.required); };
  type.addEventListener('change', update); update();
}

function showInlineNotice(message) {
  const notice = document.createElement('div');
  notice.className = 'toast-notice'; notice.textContent = message; document.body.appendChild(notice);
  requestAnimationFrame(() => notice.classList.add('show'));
  setTimeout(() => { notice.classList.remove('show'); setTimeout(() => notice.remove(), 250); }, 2600);
}

const chartColors = ['#1677ff','#35b6ff','#6b5cff','#15b87a','#ffb020','#ef5b72','#7c8aa5'];
function chartReady(id) { return typeof Chart !== 'undefined' && document.getElementById(id); }
function createLineChart(id, labels, values, label) {
  if (!chartReady(id)) return;
  new Chart(document.getElementById(id), {type:'line',data:{labels,datasets:[{label,data:values,borderColor:'#1677ff',backgroundColor:'rgba(22,119,255,.12)',fill:true,tension:.35,pointRadius:4,pointBackgroundColor:'#fff',pointBorderWidth:2}]},options:chartOptions()});
}
function createDoughnutChart(id, labels, values) {
  if (!chartReady(id)) return;
  new Chart(document.getElementById(id), {type:'doughnut',data:{labels,datasets:[{data:values,backgroundColor:chartColors,borderWidth:0,hoverOffset:5}]},options:{responsive:true,maintainAspectRatio:false,cutout:'68%',plugins:{legend:{position:'bottom',labels:{usePointStyle:true,padding:14}}}}});
}
function createBarChart(id, labels, values, label) {
  if (!chartReady(id)) return;
  new Chart(document.getElementById(id), {type:'bar',data:{labels,datasets:[{label,data:values,backgroundColor:'rgba(22,119,255,.78)',borderRadius:7,maxBarThickness:44}]},options:chartOptions()});
}
function chartOptions() {
  return {responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false}},scales:{x:{grid:{display:false},ticks:{color:'#72809a'}},y:{beginAtZero:true,grid:{color:'#edf1f7'},ticks:{color:'#72809a'}}}};
}
