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
    var Datetimeinput = function (element, options) {
        this.element = $(element);
        this.formatString = "YYYY-MM-DD hh:mm";
        this.skipChars = ["-", " ", ":"];
        this.position = 0;
        this.stepToken;
        this._attachEvents();

        this.resetDatetimeinput();
    };

    // Date time prototype
    Datetimeinput.prototype = {
        constructor: Datetimeinput,
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
                    click: $.proxy(this._click, this),
                    change: $.proxy(this.inputIsValidDate, this),
                    keydown: $.proxy(this._keydown, this),
                    keyup: $.proxy(this._keyup, this),
                    mouseover: $.proxy(this._mouseover, this)
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

        // EVENT HANDLERS

        /**
         * Function triggered after a click event.
         * The selected range is set to the beginning of a valid position (i.e. a token not
         * in skipChars).
         * @param evt the event
         * @private
         */
        _click: function (evt) {
            this.position = this.element[0].selectionStart - 1;
            if(this.position == -1)
                this.position = 0;
            else
                this.goToFirstPreviousValidPosition();
            this.element[0].setSelectionRange(this.position, this.position + 1);
        },

        /**
         * Function triggered after a keydown event.
         * The token corresponding to the key gets validated. If the token
         * is valid, the stepToken is set.
         * @param evt the event
         * @private
         */
        _keydown: function(evt) {
            var code = evt.keyCode || evt.which;
            if(this.validateToken(code)) {
                this.stepToken = true;
            } else {
                evt.preventDefault();
                this.stepToken = false;
            }
            this.element[0].setSelectionRange(this.position, this.position + 1);
        },

        /**
         * Function triggered after a keyup event.
         * If the stepToken is set, the selectionrange will shift right to the next
         * valid position.
         * @param evt the event
         * @private
         */
        _keyup: function(evt) {
            this.goToNextValidPosition();
            this.element[0].setSelectionRange(this.position, this.position + 1);
        },

        /**
         * Function triggered after a mouseover event.
         * Force the cursor to the default cursor.
         * @param evt the event
         * @private
         */
        _mouseover: function(evt) {
            this.element.css('cursor','default');
        },

        // EXTRA FUNCTIONS

        /**
         * Function telling whether a given position in the formatString is valid.
         * @returns {boolean} true if the token in the formatstring at position is not contained in skipChars
         */
        positionIsValid: function() {
            for(var i = 0; i < this.skipChars.length; i++) {
                if(this.skipChars[i] == this.formatString.charAt(this.position))
                    return false;
            }
            return true;
        },

        /**
         * Function shifting the selectionrange right to the next valid position inside the formatString if the
         * stepToken is set.
         */
        goToNextValidPosition: function() {
            while(this.position < this.formatString.length - 1 && this.stepToken) {
                this.position++;
                if(this.positionIsValid())
                    this.stepToken = false;
            }
        },

        /**
         * Function shifting the selectionrange left to the previous valid position inside the formatString if the
         * stepToken is set.
         */
        goToPreviousValidPosition: function() {
            var step = 1;
            while(this.position > 0 && step) {
                this.position--;
                if(this.positionIsValid())
                    step = false;
            }
        },

        /**
         * Function shifting the selection range to the left to the first valid position,
         * starting from the current position.
         */
        goToFirstPreviousValidPosition: function() {
            var stepBack = this.position != 0;
            while(this.position >= 0 && stepBack) {
                if(this.positionIsValid())
                    this.position--;
                else
                    stepBack = false;
            }
            // Correct position
            if(this.position != 0)
                this.position++;
        },

        /**
         * Function to reset the datetimeinput.
         */
        resetDatetimeinput: function() {
            this.element.val(this.formatString);
        },

        /**
         * Function validating the input.
         * If the input is no valid date, the input is removed
         */
        inputIsValidDate: function() {
            var date = this.inputToDate(this.element.val());
            if(isNaN(date.valueOf())) {
                this.element.val(this.formatString);
                return false;
            }
            return true;
        },

        /**
         * Function parsing the input to a date corresponding the formatString.
         * @param input the input to be parsed to a date
         * @returns {Date} the resulting date
         */
        inputToDate: function(input) {
            var index = 0;
            var input = this.element.val();
            var year = 0,
                month = 0,
                date = 0,
                hour = 0,
                min = 0,
                sec = 0;
            while(index < this.formatString.length) {
                switch(this.formatString.charAt(index)){
                    case('D'):
                        date = parseInt(input.substring(index, index + 2));
                        index += 3; // Skip "DD[skipchar]"
                        break;
                    case('M'):
                        month = parseInt(input.substring(index, index + 2));
                        index += 3; // Skip "DD[skipchar]"
                        break;
                    case('Y'):
                        year = parseInt(input.substring(index, index + 4));
                        index += 5; // Skip "YYYY[skipchar]"
                        break;
                    case('h'):
                        hour = parseInt(input.substring(index, index + 2));
                        index += 3; // Skip "hh[skipchar]";
                        break;
                    case('m'):
                        min = parseInt(input.substring(index, index + 2));
                        index += 3; // Skip "mm[skipchar]";
                        break;
                    case('s'):
                        sec = parseInt(input.substring(index, index + 2));
                        index += 3; // Skip "ss[skipchar]";
                        break;
                    default:
                        index++; // Avoid infinite loops
                        break;
                }
            }
            return new Date(year, month, date, hour, min, sec);
        },

        /**
         * Function validating a token.
         * The token is tested (corresponding to the current position) if it's either an valid date, month,
         * year, hours or minutes.
         * @param code the code indicating the token to be validated
         * @returns {boolean} true if the token is part of a valid month, year,...
         */
        validateToken: function(code) {
            var string = this.formatString;
            var pos = this.position;
            // Move left
            if(code == 37) {
                this.goToPreviousValidPosition()
                return false;
            }
            // Allow tab
            if(code == 9) {
                return true;
            }
            // Valid days: 0[1 - 9], 3[0 - 1], [1,2],
            if(string.charAt(pos) == 'D') {
                if(string.charAt(pos + 1) == 'D') {
                    return code >= 96 && code <= 99;
                }
                else {
                    if(this.element.val().charAt(pos - 1) == '0')
                        return code >= 97 && code <= 105;
                    else if(this.element.val().charAt(pos - 1) == '3')
                        return code >= 96 && code <= 97;
                    return code >= 96 && code <= 105;
                }
            }
            // Valid months: 0[1-9], 1[0-2]
            if(string.charAt(pos) == 'M') {
                if(string.charAt(pos + 1) == 'M') {
                    return code >= 96 && code <= 97;
                }
                else {
                    if (this.element.val().charAt(pos - 1) == '0')
                        return code >= 97 && code <= 105;
                    return code >= 96 && code <= 98;
                }
            }
            // Valid years
            if(string.charAt(pos) == 'Y')
                return code >= 96 && code <= 105;
            // Valid hours
            if(string.charAt(pos) == 'h') {
                if(string.charAt(pos + 1) == 'h') {
                    return code >= 96 && code <= 98;
                }
                else {
                    if (this.element.val().charAt(pos - 1) == '0')
                        return code >= 97 && code <= 105;
                    if (this.element.val().charAt(pos - 1) == '2')
                        return code >= 96 && code <= 99;
                    return code >= 96 && code <= 105;
                }
            }
            // Valid minutes
            if(string.charAt(pos) == 'm') {
                if(string.charAt(pos + 1) == 'm') {
                    return code >= 96 && code <= 101;
                }
                else {
                    return code >= 96 && code <= 105;
                }
            }
            return true;
        }
    };

    /**
     *  Add the datetimeinput to Jquery to be available.
     */
    $.fn.datetimeinput = function(option) {
        var args = Array.apply(null, arguments);
        args.shift();
        return this.each(function () {
            var $this = $(this),
                data = $this.data('datetimeinput'),
                options = typeof option == 'object' && option;
            // Initialise the datetimeinput if it not yet exists
            if (!data) {
                $this.data('datetimeinput', (data = new Datetimeinput(this, $.extend({}, options))));
            }
            // Execute a function call
            if (typeof option == 'string' && typeof data[option] == 'function') {
                data[option].apply(data, args);
            }
        });
    };

    $.fn.datetimeinput.Constructor = Datetimeinput;
}(window.jQuery);