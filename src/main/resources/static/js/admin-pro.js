document.addEventListener('DOMContentLoaded', function(){
  const body=document.body;
  const toggle=document.querySelector('[data-admin-menu-toggle]');
  const overlay=document.querySelector('[data-admin-overlay]');
  if(toggle){toggle.addEventListener('click',()=>body.classList.toggle('sidebar-open'));}
  if(overlay){overlay.addEventListener('click',()=>body.classList.remove('sidebar-open'));}
  document.querySelectorAll('.smart-back').forEach(btn=>{btn.addEventListener('click',function(e){e.preventDefault(); if(window.history.length>1){window.history.back();}else{window.location.href=this.getAttribute('data-fallback')||'/admin/dashboard';}})});
});
