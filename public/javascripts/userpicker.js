$(document).ready(initUserPicker());

function initUserPicker() {
    $(".userpicker > input[type=text]").on("input", function() {
        var userpicker = $(this).parent();

        $.get(userpicker.data("url") + "?search=" + userpicker.find("input[type=text]").val(), function(data) {
            userpicker.find(".dropdown-menu").html(data);
            userpicker.find("input[type=text]").dropdown("toggle");
        });

        userpicker.find("input[type=hidden]").val("");
        userpicker.find("div").html("");
    });

    $(".userpicker > input[type=text]").on("focus", function() {
        if (!$(this).parent().hasClass("open")) {
            $(this).dropdown("toggle");
        }
    });

    $(".userpicker").on("show.bs.dropdown", function() {
        if ($(this).find(".dropdown-menu").html() == "") {
            return false;
        }
    });

    $(".userpicker").on("hide.bs.dropdown", function() {
        if ($(this).find("input[type=text]").is(":focus") && $(this).find(".dropdown-menu").html() != "") {
            return false;
        }
    });

    $(".userpicker > .dropdown-menu").on("keypress", "li", function() {
        $(this).parent().parent().find("input[type=text]").trigger("focus");
    });

    $(".userpicker > .dropdown-menu").on("keydown", "li", function(e) {
        if (e.which == 9) {
            $(this).parent().parent().find("input[type=text]").dropdown("toggle");
        }
    });

    $(".userpicker > .dropdown-menu").on("click", "li", function() {
        var userpicker = $(this).parent().parent();
        userpicker.find("input[type=text]").val($(this).find("span").text());
        userpicker.find("input[type=hidden]").val($(this).data("uid"));
        userpicker.find("div").html($(this).data("uid"));
        userpicker.find(".dropdown-menu").html("<li data-uid=\"" + $(this).data("uid") + "\"><a href=\"javascript:void(0)\"><span><strong>" + $(this).find("span").text() + "</strong></span> (" + $(this).data("uid") + ")</a></li>");
        $(":input:tabbable").eq($(":input:tabbable").index(userpicker.find("input[type=text]")) + 1).focus();
    });

    $(".userpicker > .dropdown-menu").on("keydown", "li:first", function(e) {
        if (e.which == 38) {
            e.preventDefault();
            e.stopPropagation();
            $(this).parent().parent().find("input[type=text]").trigger("focus");
        }
    });

    $(".userpicker > .dropdown-menu").on("keydown", "li:last", function(e) {
        if (e.which == 40) {
            e.preventDefault();
            e.stopPropagation();
            $(this).parent().parent().find("input[type=text]").trigger("focus");
        }
    });

    $(".userpicker > input[type=text]").on("keydown", function(e) {
        if (e.which == 9) {
            e.preventDefault();
            e.stopPropagation();
            $(this).blur();
            $(this).dropdown("toggle");
            if (e.shiftKey) {
                $(":input:tabbable").eq($(":input:tabbable").index($(this)) - 1).focus();
            } else {
                $(":input:tabbable").eq($(":input:tabbable").index($(this)) + 1).focus();
            }
        } else if (e.which == 38) {
            e.preventDefault();
            e.stopPropagation();
            $(this).parent().find(".dropdown-menu > li:last a").trigger("focus");
        }
    });
}