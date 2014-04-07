/**
 * Created by Benjamin on 23/03/2014.
 */
// Javascript route
var route = myJsRoutes.controllers.InfoSessions.showSessionsPage;

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

$('#reset').on('click', function() {
    $("#input_from").datetimeinput("resetDatetimeinput");
    $("#input_until").datetimeinput("resetDatetimeinput");
    $('#searchButton').click();
});