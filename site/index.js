function toggleAns(id) {
  var x = document.getElementsByClassName("ans");
  if(x[id].style.display == "none" || x[id].style.display == "")
    x[id].style.display = "block";
  else x[id].style.display = "none";
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
  var dots = document.getElementsByClassName("demo");
  if(n > x.length)
    slideIndex = 1;
  if(n < 1)
    slideIndex = x.length;
  for(i = 0; i < x.length; i++)
    x[i].style.display = "none";
  x[slideIndex-1].style.display = "block";
}

function toggleOlder() {
  var x = document.getElementById("senesni");
  var y = document.getElementById("toggleOlder");
  if(x.style.display === "none" || x.style.display === "") {
    x.style.display = "block";
    y.innerHTML = "SENESNI ↑";
  } else {
    x.style.display = "none";
    y.innerHTML = "SENESNI ↓";
  }

}

$(document).ready (function () {
    $.getJSON("https://api.github.com/repos/drflarre/tvarkarastis/releases").done(function (data) {
      var x = document.getElementsByClassName("DC");
      for(var i = 0; i < x.length; i++)
        x[i].innerHTML = "Parsisiuntimų: " + data[i].assets[0].download_count;
    })
});
