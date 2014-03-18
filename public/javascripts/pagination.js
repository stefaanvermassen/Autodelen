/*
 * HOW TO USE
 *
 * In the main listpage include this javascript
 * and give values to variables amountOfPages, route, amountOfSortables
 *  TODO: put amountOfPages not here yet but in the partial list page
 *
 * In the partial list page give the th-elements id="sortableX" where X = 1 to amountOfSortables to the columns you want to sort on
 * Give these th-elements a name that the route-function takes as an argument and stands for the column to sort on
 * This script will take care of the rest
 *
 * Example:
 *
 * var amountOfPages = @amountOfPages;
 * var route = myJsRoutes.controllers.Cars.showCarsPage;
 * var amountOfSortables = 2;
 *
 * In the partial file:
 *  ...
 *  <th name="name" id="sortable1" class="sortable">Naam</th>
 *  <th name="brand" id="sortable2" class="sortable">Merk</th>
 * ...
 */

/* Variables we can overwrite after we included the script */
var previousBtnTxt = "Vorige";
var nextBtnTxt = "Volgende";
var firstBtnTxt = "<<";
var lastBtnTxt = ">>";

// For example: 2 means if we are at page 5, we will see: 3 4 5 6 7. If we are at page 1 we will see: 1 2 3 4 5
var buttonsAroundPage = 2;

$(document).ready(loadPage(1, 1, 0));

function loadPage(page, asc, orderBy) {
    route(page, asc, orderBy).ajax({
        success : function(html) {
            $("#carsTable").html(html);

            /*
             * Navigation buttons
             *
             * These will come in the element with id="buttons"
             */
            // Button to go to first page and to previous page
            var buttonString = "<button class='buttons' id='firstPage' name='1' type='button'>" + firstBtnTxt + "</button> " +
                "<button class='buttons' id='previousPage' name='" + (page - 1)  + "' type='button'>" + previousBtnTxt + "</button> ";

            // Calculate how many previous pages we create buttons to (standard 2, but less if we can't go back more, or more when we can't go further more -> max 4)
            var previousPages = buttonsAroundPage;
            var amountOfPreviousPages = 0;
            if(amountOfPages - page < buttonsAroundPage) {
                previousPages += buttonsAroundPage - (amountOfPages - page);
            }
            while(previousPages >= 1) {
                if(page - previousPages >= 1) {
                    buttonString += "<button class='buttons' id='previousPage" +  previousPages + "' name='" + (page - previousPages) + "' type='button'>" + (page - previousPages) + "</button> ";
                    amountOfPreviousPages++;
                }
                previousPages--;
            }
            // Button for current page. Disabled ofcourse.
            buttonString += "<button class='buttons' id='currentPage' name='" + page + "' type='button'>" + page + "</button> ";

            // Calculate how many next pages we create buttons to (standard 2, but less if we can't go further more, or more when we can't go back more -> max 4)
            var nextPages = 1;
            while(page + nextPages <= amountOfPages && nextPages <= buttonsAroundPage + (buttonsAroundPage - amountOfPreviousPages)) {
                buttonString += "<button class='buttons' id='nextPage" +  nextPages + "' name='" + (page + nextPages) + "' type='button'>" + (page + nextPages) + "</button> ";
                nextPages++;
            }

            // Button to go to last page and next page
            buttonString += "<button class='buttons' id='nextPage' name='" + (page + 1)  + "' type='button'>" + nextBtnTxt + "</button> " +
                "<button class='buttons' id='lastPage' name='" + amountOfPages + "' type='button'>" + lastBtnTxt + "</button>";

            // Add the buttons to the html-file
            $("#buttons").html(buttonString);

            // Now let's add the appropriote onclick-functions to the buttons and disable them if needed
            var buttons = document.getElementsByClassName('buttons');
            for(var i = 0; i < buttons.length; i++) {
                var p = parseInt(buttons[i].getAttribute("name"));
                if(p < 1 || p > amountOfPages || p == page) {
                    buttons[i].setAttribute("disabled", "disabled");
                } else {
                    buttons[i].onclick = function() {
                        var p = parseInt(this.getAttribute("name"));
                        loadPage(p, asc, orderBy);
                    }
                }
            }

            /*
             * Sorting
             *
             * All th-elements that we want to sort on have to have an id = "sortableX"
             * Where X is a number between 1 and amountOfSortables
             * It also has to have a name attribute that will be used in the controller to know what to sort on
             */
            for (var i=1; i <= amountOfSortables; i++) {
                var sortable = document.getElementById("sortable" + i);
                sortable.onclick = function() {
                    var orderByNew = this.getAttribute("name");
                    if(orderBy == orderByNew) {
                        // Change order asc <-> desc if we click on the column that already is ordered
                        asc = (asc + 1) % 2;
                    } else { // Else do asc order
                        asc = 1;
                    }
                    page = 1;
                    loadPage(page, asc, orderByNew);
                };

                // Set class of sorted column to asc or desc (so we can style with css)
                if(sortable.getAttribute("name") == orderBy) {
                    sortable.setAttribute("class", sortable.getAttribute("class" + " " + (asc == 1 ? "asc" : "desc")));
                }
            }

            /*
             * TODO: Filtering
             */
        }
    });
}