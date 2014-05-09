/*
 * =========================================
 * Calendar V1
 * =========================================
 *
 * Custom jquery plugin in order to display a calendar.
 * The calendar currently only supports the Dutch language, though
 * localisation can easily be added.
 *
 * The calendar is fully compatible with mobile devices:
 * - scrolling is prevented within the calendar in order to
 *      allow selecting
 *
 * Created by Benjamin on 27/04/2014.
 *
 */

!function ($) {
    function isLeapYear(year) {
        return (((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0));
    }

    function converToDateValue(day, month, year) {
        return (year * 10000) + (month * 100) + day;
    }

    function formatDateValue(dateValue, format) {
        if (dateValue == null)
            return null;
        var year = getYear(dateValue);
        var month = getMonth(dateValue);
        var day = getDay(dateValue);
        var success = true;
        var index = 0;
        var y = 0, m = 0, d = 0;
        var f = format.toLowerCase();
        var formattedString = '';
        while (index < f.length && success) {
            switch (f.charAt(index)) {
                case 'y':
                    var ystring = '';
                    while (index < f.length && f.charAt(index) === 'y') {
                        var value = year % 10;
                        year = Math.floor(year / 10);
                        ystring = value + ystring;
                        index++;
                    }
                    formattedString += ystring + f.charAt(index++);
                    break;
                case 'm':
                    index += 2;
                    formattedString += ((month < 10) ? '0' + month : month) + f.charAt(index++);
                    break;
                case 'd':
                    index += 2;
                    formattedString += ((day < 10) ? '0' + day : day) + f.charAt(index++);
                    break;
                default:
                    success = false;

            }
        }
        if(success)
            return formattedString;
        return year + '-' + ((month < 10) ? '0' + month : month) + '-' + ((day < 10) ? '0' + day : day);
    }

    function getYear(dateValue) {
        return Math.floor(dateValue/10000);
    }

    function getMonth(dateValue) {
        return Math.floor(dateValue/100) % 100 + 1;
    }

    function getDay(dateValue) {
        return dateValue % 100;
    }

    /**
     * Create the datetimeinput object.
     * @constructor
     */
    var Calendar = function (element, options) {
        this.options = options || {};
        this.element = $(element);
        this.language = this.language in dates ? this.language : 'nl';
        this.disablePast = options.disablePast || false;
        this.dateFormat = options.dateFormat || "yyyy-mm-dd";
        if(!this._checkDateFormat())
            this.dateFormat = "yyyy-mm-dd";
        this.startDateId = options.startDateId || null;
        if(!(this.startDateId instanceof jQuery) && this.startDateId != null)
            this.startDateId = $('#' + this.startDateId);
        this.endDateId = options.endDateId || null;
        if(!(this.endDateId instanceof jQuery) && this.endDateId != null)
            this.endDateId = $('#' + this.endDateId);

        var date = new Date();
        this.year = date.getFullYear();
        this.month = date.getMonth();
        this.day = date.getDate();
        this.dateValueToday = converToDateValue(this.day, this.month, this.year);
        this.monthDisplayed = this.month;
        this.yearDisplayed = this.year;

        this.calendarBody = 'calendar_body';
        this.prevButton = 'calendar_prev_button';
        this.nextButton = 'calendar_next_button';
        this.todayButton = 'calendar_today_button';
        this.title = 'calendar_title';
        this.titleLeft = 'calendar_title_left';
        this.titleRight = 'calendar_title_right';
        this.rowId = 'calendar_row_';
        this.cellId = 'calendar_cell_';

        this.firstSelectedValue = null;
        this.secondSelectedValue = null;
        this.mousedown = false;
        this.mousemove = false;

        this.initCalendar();
    };

    Calendar.prototype = {
        constructor: Calendar,
        _events: [],
        _bodyEvents: [],

        initCalendar: function() {
            this.renderCalender();
            this._attachEvents();
        },

        /**
         * Attach events attached to elements contained in this calendar
         * @private
         */
        _attachEvents: function () {
            this._detachEvents();
            this._events = [
                [this.prevButton, {
                    click: $.proxy(this._clickPrev, this)
                }],
                [this.nextButton, {
                    click: $.proxy(this._clickNext, this)
                }],
                [this.todayButton, {
                    click: $.proxy(this._clickToday, this)
                }]
            ];
            for(var i = 0, el, ev; i < this._events.length; i++) {
                el = $('#' + this._events[i][0]);
                ev = this._events[i][1];
                el.on(ev);
            }
            this._attachCalendarEvents();
        },

        _attachCalendarEvents: function() {
            var body = $('#' + this.calendarBody);
            body.off(this._bodyEvents);
            this._bodyEvents = {
                click: $.proxy(this._click, this),
                mousedown: $.proxy(this._mousedown, this),
                mouseup: $.proxy(this._mouseup, this),
                mousemove: $.proxy(this._mousemove, this),
                touchmove: $.proxy(this._touchmove, this)};
            body.on(this._bodyEvents);
        },

        /**
         * Detach events attached to elements contained in this calendar
         * @private
         */
        _detachEvents: function () {
            for(var i = 0, el, ev; i < this._events.length; i++) {
                el = $('#' + this._events[i][0]);
                ev = this._events[i][1];
                el.off(ev);
            }
            this._events = [];
        },

        _checkDateFormat: function() {
            var y;
            var m;
            var d;
            var f = this.dateFormat.toLocaleLowerCase();
            for(var i = 0; i < f.length; i++) {
                switch(f.charAt(i)) {
                    case('y'):
                        y = 0;
                        while(i < f.length && f.charAt(i) === 'y') {
                            y++;
                            i++;
                        }
                        break;
                    case('m'):
                        m = 0;
                        while(i < f.length && f.charAt(i) === 'm') {
                            m++;
                            i++;
                        }
                        break;
                    case('d'):
                        d = 0;
                        while(i < f.length && f.charAt(i) === 'd') {
                            d++;
                            i++;
                        }
                        break;
                    default:
                        return false;
                }
            }
            return (4 == y || 2 == y) && 2 == m && 2 == d;
        },

        _clickPrev: function() {
            if(this.monthDisplayed == this.month && this.yearDisplayed == this.year && this.disablePast)
                return;
            this.monthDisplayed = (this.monthDisplayed + 11) % 12;
            if(this.monthDisplayed == 11)
                this.yearDisplayed--;
            this.resetCalendar();
        },

        _clickNext: function() {
            this.monthDisplayed = (this.monthDisplayed + 1) % 12;
            if(this.monthDisplayed == 0)
                this.yearDisplayed++;
            this.resetCalendar();
        },

        _clickToday: function() {
            this.monthDisplayed = this.month;
            this.yearDisplayed = this.year;
            this.resetCalendar();
        },

        _clickCell: function(target, shiftpressed) {
            if(shiftpressed && this.firstSelectedValue != null) {
                if (this.firstSelectedValue >= target.value) {
                    this.setValueSecondSelected((this.secondSelectedValue != null) ? this.secondSelectedValue : this.firstSelectedValue);
                    this.setValueFirstSelected(target.value);
                } else {
                    this.setValueSecondSelected(target.value);
                }
            }
            else {
                this.setValueFirstSelected(target.value);
                this.setValueSecondSelected(this.firstSelectedValue);
            }
            this._colorSelectedDates();
        },

        _selectCell: function(target) {
            this._clickCell(target, true);
        },

        _click: function(evt) {
            if(!this.mousemove) {
                this._clickCell(evt.target, evt.shiftKey);
            }
            this.mousemove = false;
        },

        _mousedown: function(evt) {
            this.mousedown = true;
            this._clickCell(evt.target, $(evt.target).hasClass('selected'));
        },

        _mouseup: function() {
            if(this.firstSelectedValue < this.dateValueToday && this.secondSelectedValue < this.dateValueToday && this.disablePast) {
                this.setValueFirstSelected(null);
                this.setValueSecondSelected(null);
                this._colorSelectedDates();
            } else if(this.firstSelectedValue < this.dateValueToday && this.disablePast) {
                this.setValueFirstSelected(this.dateValueToday);
                this._colorSelectedDates();
            }
            this.mousedown = false;
        },

        _mousemove: function(evt) {
            if(this.mousedown) {
                this.mousemove = true;
                this._selectCell(evt.target);
            }
            this.element.css('cursor','default');
        },

        _touchmove: function(evt) {
            var touch = evt.originalEvent.touches[0] || evt.originalEvent.changedTouches[0];
            evt.preventDefault();
            this.mousemove = true;
            this._selectCell(document.elementFromPoint(touch.clientX, touch.clientY));
        },

        // Functions containing calendar logic
        setValueFirstSelected: function(value) {
            this.firstSelectedValue = value;
            if(this.startDateId != null)
                this.startDateId.val(formatDateValue(value, this.dateFormat));
        },

        setValueSecondSelected: function(value) {
            this.secondSelectedValue = value;
            if(this.endDateId != null)
                this.endDateId.val(formatDateValue(value, this.dateFormat));
        },

        resetCalendar: function() {
            this._fillCalendarBody();
            this.resetTitle();
        },

        firstWeekdayOfMonth: function() {
            var monthValues = [0, 3, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5];
            var monthLeapValues = [-1, 2, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5];
            var centryNumber = [6, 4, 2, 0];
            var y = this.yearDisplayed % 100;
            var c = Math.floor(this.yearDisplayed/100);
            var v = isLeapYear(this.yearDisplayed) ? monthLeapValues[this.monthDisplayed] : monthValues[this.monthDisplayed];
            return (1 + v + y + Math.floor(y/4) + centryNumber[c%4]) % 7;
        },

        numberOfDaysInMonth: function(month) {
            return [31, isLeapYear(this.yearDisplayed) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
        },

        // Rendering functions
        renderCalender: function() {
            this._renderTitle();
            this._renderTable();
        },

        _renderTitle: function() {
            this.element.append($('<table>')
                .attr('class', 'custom_title')
                .append($('<tbody>')
                    .append($('<tr>')
                        .append($('<td>')
                            .attr('id', this.titleLeft)
                            .attr('class', 'title_left')
                            .append($('<div>')
                                .attr('class', 'btn-group btn-group-sm')
                                .append($('<button>')
                                    .attr('id', this.prevButton)
                                    .attr('class', 'btn btn-default')
                                    .attr('type', 'button')
                                    .append($('<span>')
                                        .attr('class', 'glyphicon glyphicon-arrow-left')
                                    )
                                )
                                .append($('<button>')
                                    .attr('id', this.nextButton)
                                    .attr('class', 'btn btn-default')
                                    .attr('type', 'button')
                                    .append($('<span>')
                                        .attr('class', 'glyphicon glyphicon-arrow-right')
                                    )
                                )
                            )
                        )
                        .append($('<td>')
                            .attr('id', this.title)
                            .attr('class', 'title_center')
                        )
                        .append($('<td>')
                            .attr('id', this.titleRight)
                            .attr('class', 'title_right')
                            .append($('<div>')
                                .attr('class', 'btn-group btn-group-sm')
                                .append($('<button>')
                                    .attr('id', this.todayButton)
                                    .attr('class', 'btn btn-default')
                                    .attr('type', 'button')
                                    .append($('<b>')
                                        .text(dates[this.language].today)
                                    )
                                )
                            )
                        )
                    )
                )
            );
            this.resetTitle();
        },

        resetTitle: function() {
            $('#' + this.title).html('').append($('<h2>')
                    .text(dates[this.language].months[this.monthDisplayed] + ' ' + this.yearDisplayed)
            );
            this._resetTitleLeft();
        },

        _resetTitleLeft: function() {
            if(this.monthDisplayed > this.month || this.yearDisplayed > this.year || !this.disablePast)
                $('#' + this.prevButton).removeClass('disabled');
            else
                $('#' + this.prevButton).addClass('disabled');
        },

        _renderTable: function() {
            this.element.append($('<table>')
                .attr('class', 'custom_calendar')
                .append($('<thead>')
                    .attr('id', 'calendar_header')
                    .append($('<tr>')
                    )
                )
                .append($('<tbody>')
                    .attr('id', this.calendarBody)
                )
            );
            this._appendCalendarHeaders();
            this._fillCalendarBody();
        },

        _appendCalendarHeaders: function() {
            var headerRow = this.element.find('thead').find('tr');
            jQuery.each(dates[this.language].daysShort, function(_, val) {
                headerRow.append($('<th>').text(val));
            });
        },

        _colorSelectedDates: function() {
            if(this.firstSelectedValue != null) {
                var firstValue = this.firstSelectedValue;
                var secondValue = this.secondSelectedValue != null ? this.secondSelectedValue : this.firstSelectedValue;
                $('td[id^=' + this.cellId + ']').each(function () {
                    if (firstValue <= this.value && this.value <= secondValue)
                        $('#' + this.id).addClass('selected');
                    else
                        $('#' + this.id).removeClass('selected');
                });
            }
            else {
                $('td[id^=' + this.cellId + ']').each(function() {
                    $('#' + this.id).removeClass('selected');
                });
            }
        },

        _fillCalendarBody: function() {
            // Clear body
            var body = $('#' + this.calendarBody).html('');
            // Variables
            var day = 1;
            var startDay = this.firstWeekdayOfMonth();
            var maxDay = this.numberOfDaysInMonth(this.monthDisplayed);
            var prevMonth = (this.monthDisplayed + 11) % 12;
            var prevYear = this.monthDisplayed == 0 ?  this.yearDisplayed - 1 : this.yearDisplayed;
            var nextMonth = (this.monthDisplayed + 1) % 12;
            var nextYear = this.monthDisplayed == 11 ?  this.yearDisplayed + 1 : this.yearDisplayed;
            var maxPrev = this.numberOfDaysInMonth(prevMonth);
            var startVal = maxPrev - (startDay - 1);
            var row = 1;
            var cell = 0;
            // Append first row
            body.append($('<tr>')
                    .attr('id', this.rowId + row)
            );
            // Fill previous month visible dates
            while(startVal <= maxPrev) {
                this._appendCell(this.rowId + row, startVal++, '', prevMonth, prevYear);
                cell++;
            }
            // Fill values current month
            while(day <= maxDay) {
                var c = 'current_month';
                if(this.day == day && this.monthDisplayed == this.month && this.yearDisplayed == this.year)
                    c += ' today';
                this._appendCell(this.rowId + row, day, c);
                day++;
                cell++;
                if(cell == 7) {
                    cell = 0;
                    $('#calendar_body').append($('<tr>')
                            .attr('id', this.rowId + (++row))
                    );
                }
            }
            // Fill next month visible dates
            day = 1;
            var stop = cell <= 1 ? row : row + 1;
            while(row <= stop) {
                this._appendCell(this.rowId + row, day++, '', nextMonth, nextYear);
                cell++;
                if(cell == 7) {
                    cell = 0;
                    $('#calendar_body').append($('<tr>')
                            .attr('id', this.rowId + (++row))
                    );
                }
            }
            this._attachCalendarEvents();
            this._colorSelectedDates();
        },

        _appendCell: function(rowId, day, c, month, year) {
            var y = year || this.yearDisplayed;
            var m = month || this.monthDisplayed;
            m = month == 0 ? 0 : m;
            var value = converToDateValue(day, m, y);
            $('#' + rowId).append($('<td>')
                    .attr('id', this.cellId + value)
                    .attr('class', c + ((value < this.dateValueToday && this.disablePast) ? ' disabled ' : ''))
                    .text(day)
                    .val(value)

            );
        }
    };

    /**
     *  Add the calendar to Jquery to be available.
     */
    $.fn.calendar = function(option) {
        var args = Array.apply(null, arguments);
        args.shift();
        return this.each(function () {
            var $this = $(this),
                data = $this.data('calendar'),
                options = typeof option == 'object' && option;
            if (!data) {
                $this.data('calendar', (data = new Calendar(this, $.extend({}, options))));
            }
            if (typeof option == 'string' && typeof data[option] == 'function') {
                data[option].apply(data, args);
            }
        });
    };

    $.fn.calendar.Constructor = Calendar;

    var dates = $.fn.calendar.dates = {
        nl: {
            days:        ["Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"],
            daysShort:   ["Zon", "Maa", "Din", "Woe", "Don", "Vrij", "Zat"],
            months:      ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September",
                "October", "November", "December"],
            monthsShort: ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
            today:       "Vandaag"
        }
    };
}(window.jQuery);