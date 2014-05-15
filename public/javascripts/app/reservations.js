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
    var now = new Date();
    var later = new Date();
    later.setHours(later.getHours() + 1);
    var format = "yyyy-mm-dd";
    var inputFrom = $('#input_from_date');
    var inputTo = $('#input_to_date');
    var tommorow = new Date();
    tommorow.setDate(tommorow.getDate() + 1);
    var tommorowLater = new Date();
    tommorowLater.setDate(tommorowLater.getDate() + 1);
    tommorowLater.setHours(tommorowLater.getHours() + 1);
    var yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 2);

    $("#calendar").calendar({
        startDateId: inputFrom,
        endDateId: inputTo,
        dateFormat: format,
        events: [new CalendarEvent("Test", new Date(), tommorow),
                new CalendarEvent("Auto2", yesterday, new Date()),
                new CalendarEvent("Auto2", new Date(), new Date()),
                new CalendarEvent("Auto2", new Date(), new Date()),
                new CalendarEvent("Auto2", new Date(), new Date()),
                new CalendarEvent("Auto2", new Date(), new Date()),
                new CalendarEvent("Auto2", new Date, tommorowLater),
            new CalendarEvent("Een andere auto", new Date(), new Date()),
            new CalendarEvent("Een andere auto", new Date(), new Date()),
            new CalendarEvent("Een andere auto", new Date(), new Date())]
    }) ;

    inputFrom.datetimeinput({
        formatString: format
    });
    inputTo.datetimeinput({
        formatString: format
    });
    $('#input_from_time').timeinput();
    $('#input_to_time').timeinput();

    inputFrom.val(now.getFullYear() + '-'
        + (now.getMonth() < 10 ? '0' : '') + now.getMonth() + '-'
        + (now.getDate() < 10 ? '0' : '') + now.getDate());
    inputTo.val(later.getFullYear() + '-'
        + (later.getMonth() < 10 ? '0' : '') + later.getMonth() + '-'
        + (later.getDate() < 10 ? '0' : '') + later.getDate());
    $('#input_from_time').val(
            (now.getHours() < 10 ? '0' : '') + now.getHours() + ':' +
            (now.getMinutes() < 10 ? '0' : '') + now.getMinutes());
    $('#input_to_time').val(
            (later.getHours() < 10 ? '0' : '') + later.getHours() + ':' +
            (later.getMinutes() < 10 ? '0' : '') + later.getMinutes());

    $('#extraButton').on('click', function() {
        if(!$('#extraFiltering').hasClass('in')) {
            $('#extraEnv').append($('#filterbuttons'));
            $('#extraButton').html('minder filteropties');
        } else {
            $('#basicEnv').append($('#filterbuttons'));
            $('#extraButton').html('meer filteropties');
        }
    });

    $('#extraFiltering').on('shown.bs.collapse', function (e) {
        // $('html, body').animate({scrollTop: $(this).offset().top - 50}, 1000);
    });

    var search = $('#searchButton');

    search.on('mousedown', function() {
        $('#input_from_value').val($('#input_from_date').val() + ' ' + $('#input_from_time').val());
        $('#input_to_value').val($('#input_to_date').val() + ' ' + $('#input_to_time').val());
        $('#input_fuel').val($('#selectFuel').find('option:selected').val());
    });

    search.on('mouseup', function() {
        // $('html, body').animate({scrollTop: $('#resultsTable').offset().top - 50}, 1000);
    });

});

// Adjust the value of a checkbox to it's state (checked or unchecked)
$('input[type=checkbox]').each(function () {
    $(this).on('change', function() {
        this.value = this.checked ? 1 : 0;
    })
});