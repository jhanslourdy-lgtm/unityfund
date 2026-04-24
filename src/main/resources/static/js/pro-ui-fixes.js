(function(){
  function ready(fn){document.readyState !== 'loading' ? fn() : document.addEventListener('DOMContentLoaded', fn)}
  ready(function(){
    document.querySelectorAll('[data-admin-menu]').forEach(function(btn){
      btn.addEventListener('click', function(){
        var shell=document.querySelector('.uf-admin-shell'); if(shell) shell.classList.toggle('sidebar-open');
      });
    });
    document.querySelectorAll('[data-admin-overlay]').forEach(function(ov){
      ov.addEventListener('click', function(){var shell=document.querySelector('.uf-admin-shell'); if(shell) shell.classList.remove('sidebar-open');});
    });
    document.addEventListener('keydown', function(e){if(e.key==='Escape'){var shell=document.querySelector('.uf-admin-shell'); if(shell) shell.classList.remove('sidebar-open');}});
  });
})();
