document.addEventListener('DOMContentLoaded', function () {
  if (!document.querySelector('aside, .sidebar, .admin-sidebar')) return;

  let header = document.querySelector('.admin-header, header, main, .admin-main, .content, .admin-content') || document.body;
  let button = document.querySelector('.admin-menu-toggle');
  if (!button) {
    button = document.createElement('button');
    button.type = 'button';
    button.className = 'admin-menu-toggle';
    button.innerHTML = '☰ Menu';
    header.prepend(button);
  }

  let backdrop = document.querySelector('.admin-backdrop');
  if (!backdrop) {
    backdrop = document.createElement('div');
    backdrop.className = 'admin-backdrop';
    document.body.appendChild(backdrop);
  }

  function closeMenu() { document.body.classList.remove('admin-sidebar-open'); }
  function toggleMenu() { document.body.classList.toggle('admin-sidebar-open'); }

  button.addEventListener('click', toggleMenu);
  backdrop.addEventListener('click', closeMenu);
  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') closeMenu();
  });
});
