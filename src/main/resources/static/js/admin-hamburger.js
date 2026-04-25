document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelector('.admin-mobile-toggle')) return;

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'admin-mobile-toggle';
    btn.setAttribute('aria-label', 'Ouvrir le menu admin');
    btn.innerHTML = '☰';

    const backdrop = document.createElement('div');
    backdrop.className = 'admin-backdrop';

    btn.addEventListener('click', function () {
        document.body.classList.toggle('admin-menu-open');
    });

    backdrop.addEventListener('click', function () {
        document.body.classList.remove('admin-menu-open');
    });

    document.body.appendChild(btn);
    document.body.appendChild(backdrop);
});
