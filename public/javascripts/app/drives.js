/**
 * Created by Benjamin on 05/04/2014.
 */

$(document).ready(function() {



    $('li[id^="tab"]').on('click', function() {
        $('li[id^="tab"]').each(function () {
            $(this).find('input').removeClass('searchTextField');
        });
        $(this).find('input').addClass('searchTextField');
        $('#searchButton').click();
    });
});