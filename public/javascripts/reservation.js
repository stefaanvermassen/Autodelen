/**
 * Created by Benjamin on 23/03/2014.
 */

// Javascript route
var route = myJsRoutes.controllers.Reserve.showCarsPage;
// Date today
var today = new Date();

// Adjust the value of a checkbox to it's state (checked or unchecked)
$('input[type=checkbox]').each(function () {
    $(this).on('change', function() {
        this.value = this.checked ? 1 : 0;
    })
});

$('#reset').on('click', function() {
    $('#name').val('');
    $('#zipcode').val('');
    $('#seats').val('');
    $('input[type=time]').each(function () {
        $(this).val('');
    });
    $('input[type=date]').each(function () {
        $(this).val(dateString);
    });
    updateDates();
    $('input[type=checkbox]').each(function () {
        $(this).attr('checked', false);
        $(this).val(0);
    });
    $('#searchButton').click();
});

// Set the date up to today
var dateString = today.getFullYear() + '-' + ('0' + (today.getMonth()+1)).slice(-2) + '-' + ('0' + today.getDate()).slice(-2);
$('input[type=date]').each(function () {
    $(this).attr('min', dateString);
    $(this).val(dateString);
    updateDates();
});

// Create a string containing both the date and time
function updateDates() {
    $('#from').val($('#dateFrom').val() + ' ' + $('#timeFrom').val());
    $('#until').val($('#dateUntil').val() + ' ' + $('#timeUntil').val());
}

// Avoid corrupt information (from must always be before until)
$('#dateFrom').on('change', function() {
    if($('#dateFrom').val() == '')
        $('#timeFrom').val('');
    if($('#dateFrom').val() > $('#dateUntil').val())
        $('#dateUntil').val($('#dateFrom').val());
    else if($('#dateFrom').val() == $('#dateUntil').val() && $('#timeFrom').val() > $('#timeUntil').val())
        $('#timeUntil').val($('#timeFrom').val());
    updateDates();
});
$('#timeFrom').on('change', function() {
    if($('#dateFrom').val() == '')
        $('#dateFrom').val(dateString);
    if($('#dateFrom').val() == $('#dateUntil').val() && $('#timeFrom').val() > $('#timeUntil').val())
        $('#timeUntil').val($('#timeFrom').val());
    updateDates();
});
$('#dateUntil').on('change', function() {
    if($('#dateUntil').val() == '')
        $('#timeUntil').val('');
    if($('#dateUntil').val() < $('#dateFrom').val())
        $('#dateUntil').val($('#dateFrom').val());
    else if($('#dateUntil').val() == $('#dateFrom').val() && $('#timeUntil').val() < $('#timeFrom').val())
        $('#timeFrom').val($('#timeUntil').val());
    updateDates();
});
$('#timeUntil').on('change', function() {
    if($('#dateUntil').val() == '')
        $('#dateUntil').val(dateString);
    if($('#dateFrom').val() == $('#dateUntil').val() && $('#timeUntil').val() < $('#timeFrom').val())
        $('#timeFrom').val($('#timeUntil').val());
    updateDates();
});