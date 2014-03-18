var page = 1;
var asc = 1; // true
var orderBy = 0;
$(document).ready(loadPage(1));

function loadPage(nr) {
    route(nr, asc, orderBy).ajax({
        success : function(html) {
            $("#carsTable").html(html);

            // Navigation buttons
            var previousButton = document.getElementById("previousPage");
            if(page == 1) {
                previousButton.setAttribute("disabled", "disabled");
            } else {
                previousButton.onclick = function(){
                    page--;
                    loadPage(page, asc, orderBy);
                };
            }

            var nextButton = document.getElementById("nextPage");
            if(page == amountOfPages) {
                nextButton.setAttribute("disabled", "disabled");
            }
            nextButton.onclick = function(){
                page++;
                loadPage(page, asc, orderBy);
            };

            // Sorting
            var orderBys = new Array();
            for (var i=1; i <= amountOfSortables; i++) {
                var sortable = document.getElementById("sortable" + i);
                sortable.onclick = function() {
                    orderBy = this.getAttribute("name");
                    alert(orderBy);
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