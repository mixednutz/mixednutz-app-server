var keepAliveTimer = null;

$(document).ready(function(){
  console.log('ready');
  
  var keepAliveFunction = function(){
    console.log('keepAlive');
    if (keepAliveTimer != null) {
      clearTimeout(keepAliveTimer);
    }
    keepAliveTimer = setTimeout(function(){
      $.ajax({
        url: '/keepAlive',
        type: "GET",
        xhrFields: {withCredentials: true},
        success: function(){console.log('success');}
      }, 3000);
    });
  
    $('body').bind('click keypress scroll', keepAliveFunction);
    $('textarea input').bind('click keypress', keepAliveFunction);
        
  };
});
