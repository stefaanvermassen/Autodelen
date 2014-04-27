/*
 * =========================================
 * Datetimeinput v1
 * =========================================
 *
 * This script contains an extra plugin to JQuery named datetimeinput.
 * It links a datetimepicker object to an element, giving it several extra functionalities:
 * - Input validation
 * - Restriction of input tokens to numbers only
 *
 * An datetimeinput can be creating whilst handing it several specific options. Though these
 * options are not yet implemented, the functionality is already added to the datetimeinput object.
 *
 * Created by Benjamin on 27/03/2014.
 */
!function ($) {

    /**
     * Create the datetimeinput object.
     * @constructor
     */
    var Timeinput = function (element, options) {
        this.element = $(element);
        this.element.datetimeinput({
            formatString: 'hh:ii'
        });
        this.element.addClass('timeinput');
        this.formatString = "hh:mm";
        this.skipChars = ["-", " ", ":"];
        this.position = 0;
        this.popoverId = 'timeinput_popover';
        this.bodyId = 'popover_body';
        this.rowId = 'popover_body_row_';
        this.cellId = 'popover_body_hour';

        this.popoverFocused = false;

        this.selectedTime = null;

        this._attachEvents();
    };

    // Date time prototype
    Timeinput.prototype = {
        constructor: Timeinput,
        _events: [],

        /**
         * private function attaching several events to the element containing the
         * datettimeinput object
         * @private
         */
        _attachEvents: function () {
            this._detachEvents();
            this._events = [
                [this.element, {
                    focusin: $.proxy(this._focusin, this),
                    focusout: $.proxy(this._focusout, this)
                }]
            ];
            for (var i = 0, el, ev; i < this._events.length; i++) {
                el = this._events[i][0];
                ev = this._events[i][1];
                el.on(ev);
            }
        },

        /**
         * Detach events attached to the element containing this datetimeinput
         * @private
         */
        _detachEvents: function () {
            for (var i = 0, el, ev; i < this._events.length; i++) {
                el = this._events[i][0];
                ev = this._events[i][1];
                el.off(ev);
            }
            this._events = [];
        },

        _focusin: function() {
            if(this.popoverFocused)
                return;
            console.log(this.element);
            this.element.after($('<div>')
                .attr('id', this.popoverId)
                .attr('class', 'popover_input')
                .css('bottom', this.element.outerHeight())
                .css('width', this.element.outerWidth())
            );
            $('#' + this.popoverId).on({mouseenter: $.proxy(this._mouseenter, this),
                mouseleave: $.proxy(this._mouseleave, this)});
            this._showPopoverBody('Selecteer het uur:', 24);
        },

        _focusout: function() {
            if(!this.popoverFocused)
                $('#' + this.popoverId).remove();
            else
                this.element.focus();
        },

        _mouseenter: function() {
            this.popoverFocused = true;
        },

        _mouseleave: function() {
            this.popoverFocused = false;
        },

        setHourOfSelectedTime: function(evt) {
            this.selectedTime = evt.target.value + ':';
            this._showPopoverBody('Selecteer de minuten:', 60, 5, this.selectedTime);
        },

        setMinutesOfSelectedTime: function(evt) {
            this.selectedTime += evt.target.value;
            this.popoverFocused = false;
            this.element.datetimeinput('setValue', this.selectedTime);
            this._focusout();
        },

        _showPopoverBody: function(title, end, step, hour) {
            $('#' + this.popoverId).html('').append($('<b>')
                .text(title)
            )
            .append($('<table>')
                .append($('<tbody>')
                    .attr('id', this.bodyId)
                )
            );
            var step = step || 1;
            var hour = hour || null;
            var body = $('#' + this.bodyId);
            var row = 0;
            var postfix = (hour == null) ? ':00' : '';
            var prefix = (hour != null) ? hour : '';
            if(this.element.outerWidth()< 150)
                var jump = 2;
            else if(this.element.outerWidth() < 300)
                var jump = 3;
            else
                var jump = 4;
            for(var i = 0; i < end; i += step) {
                if(Math.floor(i/step) % jump == 0) {
                    row = Math.floor(i/step)/jump;
                    body.append($('<tr>')
                            .attr('id', this.rowId + row)
                    );
                }
                var value = (i < 10) ? '0' + i : i;
                $('#' + this.rowId + row).append($('<td>')
                        .attr('id', this.cellId + value)
                        .text(prefix + value + postfix)
                        .val(value)
                );
            };
            if(hour == null)
                $('td[id^=' + this.cellId + ']').on({click: $.proxy(this.setHourOfSelectedTime, this)});
            else
                $('td[id^=' + this.cellId + ']').on({click: $.proxy(this.setMinutesOfSelectedTime, this)});
        }
    };

    /**
     *  Add the datetimeinput to Jquery to be available.
     */
    $.fn.timeinput = function(option) {
        var args = Array.apply(null, arguments);
        args.shift();
        return this.each(function () {
            var $this = $(this),
                data = $this.data('timeinput'),
                options = typeof option == 'object' && option;
            // Initialise the timeinput if it not yet exists
            if (!data) {
                $this.data('timeinput', (data = new Timeinput(this, $.extend({}, options))));
            }
            // Execute a function call
            if (typeof option == 'string' && typeof data[option] == 'function') {
                data[option].apply(data, args);
            }
        });
    };

    $.fn.datetimeinput.Constructor = Timeinput;
}(window.jQuery);