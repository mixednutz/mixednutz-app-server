var keepAliveTimer = null;

$(document).ready(function(){
  console.log('ready keepAlive listener');
  
  var keepAliveFunction = function(){
    if (keepAliveTimer != null) {
      clearTimeout(keepAliveTimer);
    }
    keepAliveTimer = setTimeout(function(){
      $.ajax({
        url: '/internal/keepAlive',
        type: "GET",
        xhrFields: {withCredentials: true}
      });
    },3000);
  };
  
  $('body').bind('click keypress scroll', keepAliveFunction);
  $('textarea input').bind('click keypress', keepAliveFunction);
        
  
});
