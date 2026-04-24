document.addEventListener('DOMContentLoaded',function(){
 const path=location.pathname;
 const skip=['/','/home','/index','/login','/register','/verify','/forgot-password','/reset-password'];
 if(!path.startsWith('/admin') && !skip.includes(path) && !document.querySelector('.uf-back-button')){
   const a=document.createElement('a'); a.href='#'; a.className='uf-back-button'; a.innerHTML='← Retour';
   a.addEventListener('click',e=>{e.preventDefault(); if(history.length>1) history.back(); else location.href='/dashboard';});
   document.body.appendChild(a);
 }
 document.querySelectorAll('table').forEach(t=>{ if(!t.parentElement.classList.contains('uf-responsive-table')){ const w=document.createElement('div'); w.className='uf-responsive-table'; t.parentNode.insertBefore(w,t); w.appendChild(t); }});
});
