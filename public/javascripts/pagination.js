var page = 1;
$(document).ready(loadPage(1));

function loadPage(nr) {
    myJsRoutes.controllers.Cars.showCarsPage(nr).ajax({
        success : function(html) {
            $("#carsTable").html(html);

            // Navigation buttons
            var previousButton = document.getElementById("previousPage");
            if(page == 1) {
                previousButton.setAttribute("disabled", "disabled");
            } else {
                previousButton.onclick = function(){
                    page--;
                    loadPage(page);
                };
            }

            var nextButton = document.getElementById("nextPage");
            if(page == amountOfPages) {
                nextButton.setAttribute("disabled", "disabled");
            }
            nextButton.onclick = function(){
                page++;
                loadPage(page);
            };

        }
    });
}