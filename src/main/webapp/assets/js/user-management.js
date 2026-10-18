document.querySelectorAll('form').forEach(form => {
    const password = form.querySelector('[name="password"]');
    const confirm = form.querySelector('[name="confirmPassword"]');
    if (!password || !confirm) return;
    const validate = () => {
        password.setCustomValidity(new TextEncoder().encode(password.value).length > 72 ? 'Password must not exceed 72 UTF-8 bytes.' : '');
        confirm.setCustomValidity(password.value === confirm.value ? '' : 'Passwords do not match.');
    };
    password.addEventListener('input', validate);
    confirm.addEventListener('input', validate);
});
