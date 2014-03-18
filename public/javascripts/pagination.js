var previousBtnTxt = "Vorige";
var nextBtnTxt = "Volgende";
var firstBtnTxt = "<<";
var lastBtnTxt = ">>";

var buttonsAroundPage = 2;

var page = 1;
var asc = 1; // true
var orderBy = 0;
$(document).ready(loadPage());

function loadPage() {
    route(page, asc, orderBy).ajax({
        success : function(html) {
            $("#carsTable").html(html);

            /* Navigation buttons */
            // Button to go to first page and to previous page
            var buttonString = "<button id='firstPage' type='button'>" + firstBtnTxt + "</button> " +
                "<button id='previousPage' type='button'>" + previousBtnTxt + "</button> ";

            // Calculate how many previous pages we create buttons to (standard 2, but less if we can't go back more, or more when we can't go further more -> max 4)
            var previousPages = buttonsAroundPage;
            var amountOfPreviousPages = 0;
            if(amountOfPages - page < buttonsAroundPage) {
                previousPages += buttonsAroundPage - (amountOfPages - page);
            }
            while(previousPages >= 1) {
                if(page - previousPages >= 1) {
                    buttonString += "<button id='previousPage" +  previousPages + "' name='" + previousPages + "' type='button'>" + (page - previousPages) + "</button> ";
                    amountOfPreviousPages++;
                }
                previousPages--;
            }
            // Button for current page. Disabled ofcourse.
            buttonString += "<button id='currentPage' type='button' disabled='disabled'>" + page + "</button> ";

            // Calculate how many next pages we create buttons to (standard 2, but less if we can't go further more, or more when we can't go back more -> max 4)
            var nextPages = 1;
            while(page + nextPages <= amountOfPages && nextPages <= buttonsAroundPage + (buttonsAroundPage - amountOfPreviousPages)) {
                buttonString += "<button id='nextPage" +  nextPages + "' name='" + nextPages + "' type='button'>" + (page + nextPages) + "</button> ";
                nextPages++;
            }

            // Button to go to last page and next page
            buttonString += "<button id='nextPage' type='button'>" + nextBtnTxt + "</button> " +
                "<button id='lastPage' type='button'>" + lastBtnTxt + "</button>"

            // Add the buttons to the html-file
            $("#buttons").html(buttonString);


            // Now let's add the appropriote onclick-functions to the buttons
            var firstButton = document.getElementById("firstPage");
            var previousButton = document.getElementById("previousPage");
            if(page == 1) {
                firstButton.setAttribute("disabled", "disabled");
                previousButton.setAttribute("disabled", "disabled");
            } else {
                firstButton.onclick = function(){
                    page = 1;
                    loadPage();
                };
                previousButton.onclick = function(){
                    page--;
                    loadPage();
                };
            }

            var previousPages = buttonsAroundPage;
            var amountOfPreviousPages = 0;
            if(amountOfPages - page < buttonsAroundPage) {
                previousPages += buttonsAroundPage - (amountOfPages - page);
            }
            while(previousPages >= 1) {
                if(page - previousPages >= 1) {
                    var previousButton = document.getElementById("previousPage" + previousPages);
                    previousButton.onclick = function() {
                        var minus = parseInt(this.getAttribute("name"));
                        page = page - minus;
                        loadPage();
                    };
                    amountOfPreviousPages++;
                }
                previousPages--;
            }

            var nextPages = 1;
            while(page + nextPages <= amountOfPages && nextPages <= buttonsAroundPage + (buttonsAroundPage - amountOfPreviousPages)) {
                var nextButton = document.getElementById("nextPage" + nextPages);
                nextButton.onclick = function() {
                    var plus = parseInt(this.getAttribute("name"));
                    page = page + plus;
                    loadPage();
                };
                nextPages++;
            }

            var nextButton = document.getElementById("nextPage");
            var lastButton = document.getElementById("lastPage");
            if(page == amountOfPages) {
                nextButton.setAttribute("disabled", "disabled");
                lastButton.setAttribute("disabled", "disabled");
            } else {
                nextButton.onclick = function(){
                    page++;
                    loadPage();
                };
                lastButton.onclick = function(){
                    page = amountOfPages;
                    loadPage();
                };
            }

            /* Sorting */
            var orderBys = new Array();
            for (var i=1; i <= amountOfSortables; i++) {
                var sortable = document.getElementById("sortable" + i);
                sortable.onclick = function() {
                    orderBy = this.getAttribute("name");
                    asc = (asc + 1) % 2;
                    page = 1;
                    loadPage();
                };
            }

            // By default we orderBy the first sortable column
            if(orderBy == 0) {
                orderBy = $('#sortable1').attr('name');
            }

        }
    });
}