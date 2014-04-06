/**
 * Created by Benjamin on 05/04/2014.
 */
function setAdjustEnvironment() {
    $('#fromadjust').removeClass('hidden');
    $('#fromdetails').addClass('hidden');
    $('#untiladjust').removeClass('hidden');
    $('#untildetails').addClass('hidden');
    $('#submit').removeClass('hidden');
    $('#adjust').text('Annuleren');
    $('#buttons').addClass('btn-group btn-group-xs');
}

function hideAdjustEnvironment() {
    $('#fromdetails').removeClass('hidden');
    $('#fromadjust').addClass('hidden');
    $('#untildetails').removeClass('hidden');
    $('#untiladjust').addClass('hidden');
    $('#submit').addClass('hidden');
    $('#adjust').text('Reservatie inkorten');
    $('#buttons').removeClass('btn-group btn-group-xs');
}

$(document).ready(function() {
    $('#adjust').on('click', function() {
        if(!($('#fromadjust').hasClass('hidden'))) {
            hideAdjustEnvironment();
        } else {
            setAdjustEnvironment();
        }
    });

    $('#reject').on('click', function() {
        $('#actions1').addClass('hidden');
        $('#actions2').removeClass('hidden');
    });
    $('#annulate').on('click', function() {
        $('#actions2').addClass('hidden');
        $('#actions1').removeClass('hidden');
    });

    $('#datetimepickerfrom').datetimepicker({
        weekStart: 1,
        language: 'nl',
        todayBtn: 0,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1,
        linkFormat: "yyyy-mm-dd HH:ii"
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
        linkFormat: "yyyy-mm-dd HH:ii"
    });
    $("#input_from").datetimeinput();
    $("#input_until").datetimeinput();
});
