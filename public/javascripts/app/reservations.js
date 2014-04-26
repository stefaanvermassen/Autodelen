/**
 * Created by Benjamin on 23/03/2014.
 */
// Javascript route
var route = myJsRoutes.controllers.Reserve.showCarsPage;

function loadModal(carId) {
    myJsRoutes.controllers.Reserve.reserve(carId, $('#input_from_value' ).val(), $('#input_to_value' ).val()).ajax({
        success : function(html) {
            $("#resultModal").html(html);
            $('#detailsModal').modal('show');

        },
        error : function() {
            $("#resultModal").html("De reservatie kan niet worden uitgevoerd, probeer later opnieuw");
        }
    });
}

$(document).ready(function() {
    var format = "yyyy-mm-dd";
    var inputFrom = $('#input_from_date');
    var inputTo = $('#input_to_date');

    $("#test").calendar({
        startDateId: inputFrom,
        endDateId: inputTo,
        dateFormat: format
    }) ;

    inputFrom.datetimeinput({
        formatString: format
    });
    inputTo.datetimeinput({
        formatString: format
    });
    $('#input_from_time').timeinput();
    $('#input_to_time').timeinput();

    $('#extraButton').on('click', function() {
        if(!$('#extraFiltering').hasClass('in')) {
            $('#extraEnv').append($('#filterbuttons'));
            $('#extraButton').html('Minder filteropties');
        } else {
            $('#basicEnv').append($('#filterbuttons'));
            $('#extraButton').html('Meer filteropties');
        }
    });

    $('#extraFiltering').on('shown.bs.collapse', function (e) {
        $('html, body').animate({scrollTop: $(this).offset().top + -50}, 1000);
    });

    $('#searchButton').on('mousedown', function() {
        $('#input_from_value').val($('#input_from_date').val() + ' ' + $('#input_from_time').val());
        $('#input_to_value').val($('#input_to_date').val() + ' ' + $('#input_to_time').val());
        $('#input_fuel').val($('#selectFuel').find('option:selected').val());
    });

});

// Adjust the value of a checkbox to it's state (checked or unchecked)
$('input[type=checkbox]').each(function () {
    $(this).on('change', function() {
        this.value = this.checked ? 1 : 0;
    })
});