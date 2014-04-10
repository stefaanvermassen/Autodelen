/*
 * HOW TO USE
 *
 * In the main html-listpage include this javascript
 * and give a values to variable route, this route has to be added in conf/routes and
 * in controllers.Application.javascriptRoutes()
 * optional variables: previousBtnTxt, nextBtnTxt, firstBtnTxt, lastBtnTxt, buttonsAroundPage
 *
 * In the element with id="resultsTable", the table will be loaded
 *
 * In the partial list page give the th-elements class="sortable"
 * Give these th-elements a name that the route-function takes as an argument and stands for the column to sort on
 *
 * In the element with id="buttons" all the navigation buttons will come
 * This element has to have a name-attribute with in it the total amount of pages the list has
 *
 * To filter, you create input-textfields with class="searchTextField"
 * Give these input-elements a name that stands for the column to search in
 * The search-button has to have the id="searchButton"
 *
 * Example:
 *
 * In the main file between <script> -tags:
 * ...
 * var route = myJsRoutes.controllers.Cars.showCarsPage;
 * ...
 * <input class="searchTextField" name="name" value="Naam" type="text">
 * <input class="searchTextField" name="brand" value="Merk" type="text">
 * <button id="searchButton">Zoek!</button>
 *  ...
 * <div id="resultsTable">
 *      <!-- Here comes the loaded table-->
 * </div>
 *
 * In the partial file:
 *  ...
 *  <th name="name" class="sortable">Naam</th>
 *  <th name="brand" id= class="sortable">Merk</th>
 * ...
 *  <p id="buttons" name="@amountOfPages"></p>
 */

/* Variables we can overwrite after we included the script */
var previousBtnTxt = "Vorige";
var nextBtnTxt = "Volgende";
var firstBtnTxt = "<<";
var lastBtnTxt = ">>";
var loadingImage = 'assets/images/ajax-loader.gif';
var errorMessageFilter = "Zoekvelden mogen geen komma's (,) of gelijk-aan-tekens (=) bevatten!";

// For example: 2 means if we are at page 5, we will see: 3 4 5 6 7. If we are at page 1 we will see: 1 2 3 4 5
var buttonsAroundPage = 2;

if(typeof autoLoad == 'undefined') {
    autoLoad = 0;
}
if(typeof beginPage == 'undefined') {
    var beginPage = 1;
}
if(typeof beginAsc == 'undefined') {
    var beginAsc = 1;
}
if(typeof beginOrder == 'undefined') {
    var beginOrder = "";
}
if(typeof beginFilter == 'undefined') {
    var beginFilter = "";
}
var pageLoaded = false;

// Initially, we load the first page in ascending order, ordered by the default column, without filtering
$(document).ready(loadPage(beginPage, beginAsc, beginOrder, beginFilter));

/*
 * Filtering
 *
 * All input text fields that we want to search with have the class "searchTextField"
 * It also has to have a name attribute that will be used in the controller to know what to search on
 */
var searchButton = document.getElementById("searchButton");
if(searchButton != null) {
    searchButton.onclick = function () {
        var searchFields = document.getElementsByClassName("searchTextField");
        var values = new Array();
        var fields = new Array();
        for (var i = 0; i < searchFields.length; i++) {
            var searchField = searchFields[i];
            fields[i] = searchField.getAttribute('name');
            values[i] = searchField.value;
            if(fields[i].indexOf('=') != -1 || values[i].indexOf('=') != -1 ||
                fields[i].indexOf(',') != -1 || values[i].indexOf(',') != -1) {
                alert(errorMessageFilter);
                return;
            }
        }
        var searchString = createSearchString(fields, values);
        loadPage(1, 1, "", searchString);
    }
}

// The function to load a new page
function loadPage(page, asc, orderBy, search) {
    pageLoaded = false;
    // Fill in loading image inside the table if the table is already rendered
    if(typeof $("#resultsTable").find('table').val() != 'undefined') {
        // Calculate the number of columns to create a td with the colspan set to that number
        var cols = $("#resultsTable").find('table').find('tr')[0].cells.length;
        $("#resultsTable").find('table').find('tbody').html($('<tr>')
                .append($('<td class="loading">')
                    .attr('colspan', cols)
                    .attr('style', 'text-align: center; background-color: #FFFFFF; padding: 8px;')
                    .append($('<img>')
                        .attr('alt', 'Loading...')
                        .attr('src', loadingImage)
                )
            )
        );
    // Fill in loading image if the table is not yet rendered
    } else {
        $("#resultsTable").append($('<div class="loading">')
            .attr('style', 'text-align: center;')
            .append($('<img>')
                .attr('alt', 'Loading...')
                .attr('src', loadingImage)
                )
            .append($('<p>')
                .text('Loading table'))
        );
    }

    route(page, asc, orderBy, search).ajax({
        success : function(html) {
            if(autoLoad != 1)
                $("#resultsTable").html(html);
            else {
                $(".loading").hide();
                $("#buttons").hide();
                $("#resultsTable").append(html);
            }
            // TODO: better way to pass amountOfResults and amountOfPages to javascript?
            var amountOfResultsAndPages = $('#buttons').attr('name').split(",");
            var amountOfResults = amountOfResultsAndPages[0];
            var amountOfPages = amountOfResultsAndPages[1];

            if(autoLoad != 1) {
                /*
                 * Navigation buttons
                 *
                 * These will come in the element with id="buttons"
                 */
                // Button to go to first page and to previous page
                var buttonString = "Aantal resultaten: " + amountOfResults + " (" + amountOfPages + " pagina's).<br>";
                if(amountOfPages > 1) {
                    if(page != 1) {
                        buttonString += "<button class='buttons btn' id='firstPage' name='1' type='button'>" + firstBtnTxt + "</button> " +
                            "<button class='buttons btn' id='previousPage' name='" + (page - 1)  + "' type='button'>" + previousBtnTxt + "</button> ";
                    }

                    // Calculate how many previous pages we create buttons to (standard 2, but less if we can't go back more, or more when we can't go further more -> max 4)
                    var previousPages = buttonsAroundPage;
                    var amountOfPreviousPages = 0;
                    if(amountOfPages - page < buttonsAroundPage) {
                        previousPages += buttonsAroundPage - (amountOfPages - page);
                    }
                    while(previousPages >= 1) {
                        if(page - previousPages >= 1) {
                            buttonString += "<button class='buttons btn' id='previousPage" +  previousPages + "' name='" + (page - previousPages) + "' type='button'>" + (page - previousPages) + "</button> ";
                            amountOfPreviousPages++;
                        }
                        previousPages--;
                    }
                    // Button for current page. Disabled ofcourse.
                    buttonString += "<button class='buttons btn' id='currentPage' name='" + page + "' type='button'>" + page + "</button> ";

                    // Calculate how many next pages we create buttons to (standard 2, but less if we can't go further more, or more when we can't go back more -> max 4)
                    var nextPages = 1;
                    while(page + nextPages <= amountOfPages && nextPages <= buttonsAroundPage + (buttonsAroundPage - amountOfPreviousPages)) {
                        buttonString += "<button class='buttons btn' id='nextPage" +  nextPages + "' name='" + (page + nextPages) + "' type='button'>" + (page + nextPages) + "</button> ";
                        nextPages++;
                    }

                    if(page != amountOfPages) {
                        // Button to go to last page and next page
                        buttonString += "<button class='buttons btn' id='nextPage' name='" + (page + 1)  + "' type='button'>" + nextBtnTxt + "</button> " +
                            "<button class='buttons btn' id='lastPage' name='" + amountOfPages + "' type='button'>" + lastBtnTxt + "</button>";
                    }
                }
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
                            loadPage(p, asc, orderBy, search);
                        }
                    }
                }
            }

            /*
             * Sorting
             *
             * All th-elements that we want to sort on have to have the class "sortable"
             * It also has to have a name attribute that will be used in the controller to know what to sort on
             */
            var sortables = document.getElementsByClassName('sortable');
            for (var i=0; i < sortables.length; i++) {
                var sortable = sortables[i];
                sortable.onclick = function() {
                    var orderByNew = this.getAttribute("name");
                    if(orderBy == orderByNew || orderBy == "") {
                        // Change order asc <-> desc if we click on the column that already is ordered
                        asc = (asc + 1) % 2;
                    } else { // Else do asc order
                        asc = 1;
                    }
                    page = 1;
                    loadPage(page, asc, orderByNew, search);
                };

                // Set class of sorted column to asc or desc (so we can style with css)
                if(sortable.getAttribute("name") == orderBy) {
                    sortable.setAttribute("class", sortable.getAttribute("class") + " " + (asc == 1 ? "asc" : "desc"));
                }
            }

            // For auto-loading on reaching bottom of document
            if(autoLoad == 1) {
                $(window).scroll(function() {
                    if($(window).scrollTop() == $(document).height() - $(window).height()) {
                        if(page < amountOfPages) {
                            page++;
                            loadPage(page, asc, orderBy, search);
                        }
                    }
                });
            }

            pageLoaded = true;
        },
        error : function() {
            // TODO: make clearer
            $("#resultsTable").html("Er ging iets mis...");
        }
    });
}

/*
 * Create a string "field1:value1,field2:value2" and so on
 */
function createSearchString(fields, values) {
    var searchString = "";
    for(var i = 0; i < fields.length; i++) {
        searchString += fields[i] + "=" + values[i];
        if(i != fields.length-1) {
            searchString += ",";
        }
    }
    return searchString;
}