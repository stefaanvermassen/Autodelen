!function ($) {

    var months = ['Januari', 'Februari', 'Maart', 'April', 'Mei', 'Juni', 'Juli', 'Augustus', 'September', 'Oktober', 'November', 'December'];
    var monthDays = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    var monthValues = [0, 3, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5];
    var monthLeapValues = [-1, 2, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5];
    var centryNumber = [6, 4, 2, 0];
    var tableHeaders = ['Zon', 'Maa', 'Din', 'Woe', 'Don', 'Vrij', 'Zat'];

    function converToDateValue(day, month, year) {
        return (year * 10000) + (month * 100) + day;
    }

    function getYear(dateValue) {
        return Math.floor(dateValue/10000);
    }

    function getMonth(dateValue) {
        return Math.floor(dateValue/100) % 100;
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
        var date = new Date();
        this.year = date.getFullYear();
        this.month = date.getMonth();
        this.day = date.getDate();

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
        this.selectionStartId = options.selectionStartId || null;
        this.selectionEndId = 'selection_end';
        this.selectionStartLabel = 'Selectie van:';
        this.selectionEndLabel = 'Selectie tot:';

        this.firstSelectedValue = -1;
        this.secondSelectedValue = -1;

        this.mousedown = false;

        this.initCalendar();
    };

    Calendar.prototype = {
        constructor: Calendar,
        _events: [],
        _cellEvents: [],

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
            var cells = $('td[id^=' + this.cellId + ']');
            cells.off(this._cellEvents);
            this._cellEvents = {
                click: $.proxy(this._clickCell, this)
            };
            cells.on(this._cellEvents);
            $('#' + this.calendarBody).on({mousedown: $.proxy(this._mousedown, this), mouseup:$.proxy(this._mouseup, this),
                mousemove: $.proxy(this._mousemove, this)})
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

        _clickPrev: function() {
            if(this.monthDisplayed == this.month && this.yearDisplayed == this.year)
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

        _clickCell: function(evt, ignoreshift) {
            var check = ignoreshift || false;
            if((evt.shiftKey || check) && this.firstSelectedValue != -1) {
                if (this.firstSelectedValue >= evt.target.value) {
                    this.secondSelectedValue = (this.secondSelectedValue != -1) ? this.secondSelectedValue : this.firstSelectedValue;
                    this.setValueFirstSelected(evt.target.value);
                } else {
                    this.secondSelectedValue = evt.target.value;
                }
            }
            else {
                this.secondSelectedValue = -1;
                this.setValueFirstSelected(evt.target.value);
            }
            this._colorSelectedDates();
        },

        _mousedown: function(evt) {
            this.mousedown = true;
            this._clickCell(evt);
        },

        _mouseup: function() {
            this.mousedown = false;
        },

        _mousemove: function(evt) {
            if(this.mousedown)
                this._clickCell(evt, true);
            this.element.css('cursor','default');
        },

        // Functions containing calendar logic
        setValueFirstSelected: function(value) {
            this.firstSelectedValue = value;
            if(this.selectionStartId != null)
                this.selectionStartId.val(value);
        },

        resetCalendar: function() {
            this._fillCalendarBody();
            this.resetTitle();
        },

        firstWeekdayOfMonth: function() {
            var y = this.yearDisplayed % 100;
            var c = Math.floor(this.yearDisplayed/100);
            var v = this.isLeapYear() ? monthLeapValues[this.monthDisplayed] : monthValues[this.monthDisplayed];
            return (1 + v + y + Math.floor(y/4) + centryNumber[c%4]) % 7;
        },

        numberOfDaysInMonth: function(month) {
            if(month != 1)
                return monthDays[month];
            return this.isLeapYear() ? 29 : 28;
        },

        isLeapYear: function(year) {
            var y = year || this.yearDisplayed;
            var lastDigits = this.yearDisplayed % 100;
            return (lastDigits != 0 && y % 4 == 0) || (lastDigits == 0 && y % 400 == 0);
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
                                    .text('<<')
                                )
                                .append($('<button>')
                                    .attr('id', this.nextButton)
                                    .attr('class', 'btn btn-default')
                                    .attr('type', 'button')
                                    .text('>>')
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
                                    .text('Vandaag')
                                )
                            )
                        )
                    )
                )
            );
            this.resetTitle();
        },

        resetTitle: function() {
            $('#' + this.title).html("").append($('<h2>')
                    .text(months[this.monthDisplayed] + ' ' + this.yearDisplayed)
            );
            this._resetTitleLeft();
        },

        _resetTitleLeft: function() {
            if(this.monthDisplayed > this.month || this.yearDisplayed > this.year)
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
            jQuery.each(tableHeaders, function(_, val) {
                headerRow.append($('<th>').text(val));
            });
        },

        _colorSelectedDates: function() {
            if(this.firstSelectedValue != -1) {
                var firstValue = this.firstSelectedValue;
                var secondValue = this.secondSelectedValue != -1 ? this.secondSelectedValue : this.firstSelectedValue;
                $('td[id^=' + this.cellId + ']').each(function() {
                    if(firstValue <= this.value && this.value <= secondValue)
                        $('#' + this.id).addClass('selected');
                    else
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
            var prevYear = this.monthDisplayed == 11 ?  this.yearDisplayed - 1 : this.yearDisplayed;
            var nextMonth = (this.monthDisplayed + 1) % 12;
            var nextYear = this.monthDisplayed == 0 ?  this.yearDisplayed + 1 : this.yearDisplayed;
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
            var value = converToDateValue(day, m, y);
            $('#' + rowId).append($('<td>')
                    .attr('id', this.cellId + value)
                    .attr('class', c)
                    .text(day)
                    .val(value)

            );
        }
    };

    /**
     *  Add the datetimeinput to Jquery to be available.
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
}(window.jQuery);