/**
 * Created by HannesM on 18/04/14.
 */
$(document).ready(function() {
    $('li[id^="tab"]').on('click', function() {
        var name = $(this).attr("name");
        $('#hidden_input').attr("value", name);

        $('#searchButton').click();
    });
});