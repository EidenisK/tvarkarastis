function toggleAns(id) {
  var x = document.getElementsByClassName("ans");
  $(x[id]).slideToggle();
}

$('.navbar-nav>li>a').on('click', function(){
    $('.navbar-collapse').collapse('hide');
    var href = $(this).attr("href");
    $('html, body').animate({
      scrollTop: $(href).offset().top
    }, 500);
    return false;
});

var slideIndex = 1;
showDivs(slideIndex);

var timeoutVar;
timeoutVar = setTimeout(incrDivs, 4000);
resetTimeout();

function resetTimeout() {
  clearTimeout(timeoutVar);
  timeoutVar = setTimeout(incrDivs, 4000);
}
function incrDivs() {
  showDivs(slideIndex += 1);
  resetTimeout();
}
function plusDivs(n) {
  showDivs(slideIndex += n);
  resetTimeout();
}
function currentDiv(n) {
  showDivs(slideIndex = n);
  resetTimeout();
}

function showDivs(n) {
  var i;
  var x = document.getElementsByClassName("slide");
  if(n > x.length)
    slideIndex = 1;
  if(n < 1)
    slideIndex = x.length;
  for(i = 0; i < x.length; i++)
    x[i].style.display = "none";
  x[slideIndex-1].style.display = "block";

  $(x[slideIndex -2]).animate({opacity: '0'}, "slow");
  $(x[slideIndex -1]).animate({opacity: '1'}, "slow");  
}

function toggleOlder() {
  $("#senesni").slideToggle();
  var x = document.getElementById("toggleOlder");
  if(x.innerHTML === "SENESNI ↑")
    x.innerHTML = "SENESNI ↓";
  else x.innerHTML = "SENESNI ↑";
}

$(document).ready (function () {
    var total = 0;
    $.getJSON("https://api.github.com/repos/eidenisk/tvarkarastis/releases").done(function (data) {
      var x = document.getElementsByClassName("DC1");
      for(var i = 0; i < x.length; i++) {
        x[i].innerHTML = "Parsisiuntimų: " + data[i].assets[0].download_count;
        total += data[i].assets[0].download_count;
      }
      $.getJSON("https://api.github.com/repos/drflarre/tvarkarastis/releases").done(function (data) {
        var x = document.getElementsByClassName("DC");
        for(var i = 0; i < x.length; i++) {
          x[i].innerHTML = "Parsisiuntimų: " + data[i].assets[0].download_count;
          total += data[i].assets[0].download_count;
        }
        var tDL = document.getElementById("total-downloads");
        tDL.innerHTML = "PARSISIUNTIMAI <i>(viso: " + total + ")</i>";
      })
    })
});
