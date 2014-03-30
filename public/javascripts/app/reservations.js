/**
 * Created by Benjamin on 23/03/2014.
 */
// Javascript route
var route = myJsRoutes.controllers.Reserve.showCarsPage;

$(document).ready(function() {
    // Specify dates today and later
    var now = new Date();
    var later = new Date();
    later.setMinutes(later.getMinutes() + 5);
    // First datetimepicker
    $('#datetimepickerfrom').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn: 0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii",
        startDate: now // current date and time
    });
    $('#datetimepickeruntil').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn: 0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii",
        startDate: later // date and time to five minutes later than now
    });
    $("#input_from").datetimeinput();
    $("#input_until").datetimeinput();
});


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
    $("#input_from").datetimeinput("resetDatetimeinput");
    $("#input_until").datetimeinput("resetDatetimeinput");
    $('input[type=checkbox]').each(function () {
       $(this).attr('checked', false);
       $(this).val(0);
    });
    $('#searchButton').click();
});