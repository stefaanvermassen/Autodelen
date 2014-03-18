var previousBtnTxt = "Vorige";
var nextBtnTxt = "Volgende";
var firstBtnTxt = "<<";
var lastBtnTxt = ">>";

var page = 1;
var asc = 1; // true
var orderBy = 0;
$(document).ready(loadPage(1));

function loadPage(nr) {
    route(nr, asc, orderBy).ajax({
        success : function(html) {
            $("#carsTable").html(html);


            // Navigation buttons

            $("#buttons").html("<button id='firstPage' type='button'>" + firstBtnTxt + "</button> " +
                "<button id='previousPage' type='button'>" + previousBtnTxt + "</button> " +
                "<button id='currentPage' type='button' disabled='disabled'>" + page + "</button> " +
                "<button id='nextPage' type='button'>" + nextBtnTxt + "</button> " +
                "<button id='lastPage' type='button'>" + lastBtnTxt + "</button>");

            var firstButton = document.getElementById("firstPage");
            var previousButton = document.getElementById("previousPage");
            if(page == 1) {
                firstButton.setAttribute("disabled", "disabled");
                previousButton.setAttribute("disabled", "disabled");
            } else {
                firstButton.onclick = function(){
                    page = 1;
                    loadPage(page, asc, orderBy);
                };
                previousButton.onclick = function(){
                    page--;
                    loadPage(page, asc, orderBy);
                };
            }

            var nextButton = document.getElementById("nextPage");
            var lastButton = document.getElementById("lastPage");
            if(page == amountOfPages) {
                nextButton.setAttribute("disabled", "disabled");
                lastButton.setAttribute("disabled", "disabled");
            } else {
                nextButton.onclick = function(){
                    page++;
                    loadPage(page, asc, orderBy);
                };
                lastButton.onclick = function(){
                    page = amountOfPages;
                    loadPage(page, asc, orderBy);
                };
            }

            // Sorting
            var orderBys = new Array();
            for (var i=1; i <= amountOfSortables; i++) {
                var sortable = document.getElementById("sortable" + i);
                sortable.onclick = function() {
                    orderBy = this.getAttribute("name");
                    asc = (asc + 1) % 2;
                    page = 1;
                    loadPage(page, asc, orderBy);
                };
            }

            if(orderBy == 0) {
                orderBy = $('#sortable1').attr('name');
            }

        }
    });
}