/**
 * Created by Benjamin on 23/03/2014.
 */
// Javascript route
var route = myJsRoutes.controllers.InfoSessions.showSessionsPage;

$(document).ready(function() {
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
        startdate: ""
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
        startdate: ""
    });
    $("#input_from").datetimeinput();
    $("#input_until").datetimeinput();
});

$('#reset').on('click', function() {
    $("#input_from").datetimeinput("resetDatetimeinput");
    $("#input_until").datetimeinput("resetDatetimeinput");
    $('#searchButton').click();
});