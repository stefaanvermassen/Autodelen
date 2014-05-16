/*
 * =========================================
 * Calendar V2
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
    /**
     * Create the datetimeinput object.
     * @constructor
     */
    var Calendar = function (element, options) {
        this.options = options || {};
        this.element = $(element);
        this.language = this.language in locales ? this.language : 'nl';
        this.disablePast = options.disablePast || false;
        this.dateFormat = options.dateFormat || "yyyy-mm-dd";
        this.events = options.events || null;
        this._checkEvents();
        if(!this._checkDateFormat())
            this.dateFormat = "yyyy-mm-dd";
        this.startDateId = options.startDateId || null;
        if(!(this.startDateId instanceof jQuery) && this.startDateId != null)
            this.startDateId = $('#' + this.startDateId);
        this.endDateId = options.endDateId || null;
        if(!(this.endDateId instanceof jQuery) && this.endDateId != null)
            this.endDateId = $('#' + this.endDateId);
        this.selectable = this.startDateId != null && this.endDateId != null;

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
        this.legend = 'calendar_legend';
        this.rowId = 'calendar_row_';
        this.cellId = 'calendar_cell_';

        this.firstSelectedValue = null;
        this.secondSelectedValue = null;
        this.mousedown = false;
        this.mousemove = false;

        this.environments = [new EventEnvironment('fill1'), new EventEnvironment('fill2'), new EventEnvironment('fill3'),
            new EventEnvironment('fill4'), new EventEnvironment('fill5')];
        this.colorAssociator = new ColorAssociator();

        this.initCalendar();
    };

    Calendar.prototype = {
        constructor: Calendar,
        _events: [],
        _bodyEvents: [],
        _windowEvents: [],

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
        },

        _attachCalendarEvents: function() {
            if(!this.selectable)
                return;
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
            if(typeof target == 'undefined')
                return;
            if(shiftpressed && this.firstSelectedValue != null) {
                if (this.firstSelectedValue >= target.val()) {
                    this.setValueSecondSelected((this.secondSelectedValue != null) ? this.secondSelectedValue : this.firstSelectedValue);
                    this.setValueFirstSelected(target.val());
                } else {
                    this.setValueSecondSelected(target.val());
                }
            }
            else {
                this.setValueFirstSelected(target.val());
                this.setValueSecondSelected(this.firstSelectedValue);
            }
            this._colorSelectedDates();
        },

        _selectCell: function(target) {
            this._clickCell(target, true);
        },

        _click: function(evt) {
            if(!this.mousemove) {
                var target = this._findTableCell($(evt.target));
                this._clickCell(target, evt.shiftKey);
            }
            this.mousemove = false;
        },

        _mousedown: function() {
            this.mousedown = true;
        },

        _mouseup: function() {
            if(this.disablePast) {
                if(this.firstSelectedValue < this.dateValueToday && this.secondSelectedValue < this.dateValueToday) {
                    this.setValueFirstSelected(null);
                    this.setValueSecondSelected(null);
                    this._colorSelectedDates();
                } else if(this.firstSelectedValue < this.dateValueToday) {
                    this.setValueFirstSelected(this.dateValueToday);
                    this._colorSelectedDates();
                }
            }
            this.mousedown = false;
        },

        _mousemove: function(evt) {
            if(this.mousedown) {
                this.mousemove = true;
                var target = this._findTableCell($(evt.target));
                this._selectCell(target);
            }
            this.element.css('cursor','default');
        },

        _touchmove: function(evt) {
            evt.preventDefault();
            var touch = evt.originalEvent.touches[0] || evt.originalEvent.changedTouches[0];
            this.mousemove = true;
            var target = this._findTableCell($(document.elementFromPoint(touch.clientX, touch.clientY)));
            this._selectCell(target);
        },

        _findTableCell: function(target) {
            while(typeof target[0] != 'undefined' && !target.is("td"))
                target = $(target[0].parentElement);
            return target;
        },

        // Functions containing calendar logic
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

        _checkEvents: function() {
            if(this.events == null)
                return true;
            for(var i = 0; i < this.events.length; i++) {
                if(this.events[i] == null || this.events[i].constructor != CalendarEvent || !this.events[i].validEvent)
                    this.events.splice(i, 1);
            }
            this.events.sort(compareEvents);
        },

        setValueFirstSelected: function(value) {
            this.firstSelectedValue = value;
            if(this.startDateId != null) {
                var formatDate =  CalGlobal.formatDateValue(value, this.dateFormat);
                this.startDateId.text(formatDate);
                this.startDateId.val(formatDate).trigger('change');
            }
        },

        setValueSecondSelected: function(value) {
            this.secondSelectedValue = value;
            if(this.endDateId != null) {
                var formatDate =  CalGlobal.formatDateValue(value, this.dateFormat);
                this.endDateId.text(formatDate);
                this.endDateId.val(formatDate).trigger('change');
            }
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
            var v = CalGlobal.isLeapYear(this.yearDisplayed) ? monthLeapValues[this.monthDisplayed] : monthValues[this.monthDisplayed];
            return (1 + v + y + Math.floor(y/4) + centryNumber[c%4]) % 7;
        },

        numberOfDaysInMonth: function(month) {
            return [31, CalGlobal.isLeapYear(this.yearDisplayed) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
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
                                        .text(locales[this.language].today)
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
                    .text(locales[this.language].months[this.monthDisplayed] + ' ' + this.yearDisplayed)
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
            jQuery.each(locales[this.language].daysShort, function(_, val) {
                headerRow.append($('<th>').text(val));
            });
        },

        _visibleEvents: function() {
            if(this.events != null) {
                var evts = this.events;
                var func = this._getEventEnvironment;
                var environments = this.environments;
                var colorAssociator = this.colorAssociator;
                colorAssociator.reset();
                $('td[id^=' + this.cellId + ']').each(function () {
                    var i = 0;
                    while(i < evts.length && evts[i].startDate <= this.value) {
                        if(evts[i].startDate <= this.value && this.value <= evts[i].endDate) {
                            var env = func(this.value, evts[i], environments);
                            if(env != null) {
                                var shour = Math.floor(evts[i].startTime / 100);
                                var smin = evts[i].startTime % 100;
                                var ehour = Math.floor(evts[i].endTime / 100);
                                var emin = evts[i].endTime % 100;
                                $(this).find('div.' + env.name)
                                    .append($('<div>')
                                        .css('background-color', colorAssociator.getColor(evts[i].name))
                                        .attr('class', 'event' + (evts[i].startDate == this.value ? ' start' : '')
                                            + (evts[i].endDate == this.value ? ' end' : ''))
                                        .popover({title: '<b>' + evts[i].name + '</b>',
                                            content: 'Van ' +
                                                (shour < 10 ? '0' : '') + shour + ':' + (smin < 10 ? '0' : '') + smin +
                                                ' tot ' +
                                                (ehour < 10 ? '0' : '') + ehour + ':' + (emin < 10 ? '0' : '') + emin +
                                                (evts[i].link != null ? ' (<a href="' + evts[i].link + '">' +
                                                    (evts[i].linktext != null ? evts[i].linktext : 'link') + '</a>)' : ''),
                                            placement: 'top',
                                            html: true})
                                    );
                            }
                        }
                        i++;
                    }
                    for(i = 0; i < environments.length; i++) {
                        var amount = $(this).find('div.' + environments[i].name).children().size();
                        if(amount != 0){
                            var width = Math.floor(100/amount);
                            var last = Math.floor(100/amount) + 100 % amount;
                            $(this).find('div.' + environments[i].name).find('div.event').each(function(index) {
                                $(this).css('width', (index == amount - 1 ? last : width) + '%').css('float', 'left');
                            });
                        }
                    }
                });
                this._renderLegend();
            }
        },

        _renderLegend: function() {
            var legend = $('div[id^=' + this.legend + ']');
            if(this.colorAssociator.colors[0].eventName == null){
                this.element.removeClass('col-lg-10 col-md-9 col-sm-12');
                legend.remove();
                return;
            }
            if(legend.length == 0) {
                this.element.addClass('col-lg-10 col-md-9 col-sm-12').after($('<div>')
                    .attr('id', this.legend + '_large')
                    .attr('class', 'visible-lg visible-md col-lg-2 col-md-3 legend')
                )
                .before($('<div>')
                        .attr('id', this.legend + '_small')
                        .attr('class', 'visible-sm visible-xs col-sm-12 legend')
                );
                legend = $('div[id^=' + this.legend + ']');
            }
            legend.html('').append($('<h5>')
                .append($('<b>')
                    .text(locales[this.language].legend)
                )
            )
            .append($('<hr />'));
            var index = 1;
            var association = this.colorAssociator.colors[0];
            while (index < this.colorAssociator.colors.length && association.eventName != null) {
                legend.append($('<div>')
                        .attr('class', 'col-md-12 col-sm-4 col-xs-6')
                        .append($('<div>')
                            .attr('class', 'col-xs-1 item_color')
                            .css('background-color', association.color)
                    ).append($('<div>')
                            .attr('class', 'col-xs-11 item_text')
                            .text(association.eventName)
                    )
                );
                association = this.colorAssociator.colors[index++];
            }
        },

        _getEventEnvironment: function(cellValue, event, environments) {
            var env = null;
            for(var i = 0; i < environments.length; i++) {
                if(environments[i].event != null && environments[i].event.name == event.name) {
                    env = environments[i];
                    break;
                }
            }
            if(env == null) {
                for(i = 0; i < environments.length; i++) {
                    if (environments[i].event == null) {
                        env = environments[i];
                        break;
                    }
                }
            }
            if(env == null) {
                for(i = 0; i < environments.length; i++) {
                    if (!environments[i].claimed) {
                        env = environments[i];
                        break;
                    }
                }
            }
            if(env != null)
                env.claimEnvironment(event, cellValue);
            return env;
        },

        _colorSelectedDates: function() {
            if(this.firstSelectedValue != null) {
                var firstValue = this.firstSelectedValue;
                var secondValue = this.secondSelectedValue != null ? this.secondSelectedValue : this.firstSelectedValue;
                $('td[id^=' + this.cellId + ']').each(function () {
                    if (firstValue <= this.value && this.value <= secondValue)
                        $(this).addClass('selected');
                    else
                        $(this).removeClass('selected');
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
            if(this.selectable)
                body.attr('data-toggle', 'tooltip').attr('data-placement', 'right').attr('title', 'Klik en sleep met de cursor over ' +
                    'de datums\nOF\nSelecteer de begindatum door te klikken en selecteer daarna\n' +
                    'de einddatum door te klikken met shift ingedrukt.');
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
            this._visibleEvents();
        },

        _appendCell: function(rowId, day, c, month, year) {
            var y = year || this.yearDisplayed;
            var m = month || this.monthDisplayed;
            m = month == 0 ? 0 : m;
            var value = converToDateValue(day, m, y);
            $('#' + rowId).append($('<td>')
                    .attr('id', this.cellId + value)
                    .attr('class', c + ((value < this.dateValueToday && this.disablePast) ? ' disabled ' : ''))
                    .val(value)
                    .append($('<p>')
                        .attr('class', 'day')
                        .text(day)
                    )
            );
            for(var i = this.environments.length - 1; i >= 0; i--)
                $('#' + this.cellId + value).append($('<div>')
                    .attr('class', 'fill ' + this.environments[i].name)
                )
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

    var locales = $.fn.calendar.locales = {
        nl: {
            days:        ["Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"],
            daysShort:   ["Zon", "Maa", "Din", "Woe", "Don", "Vrij", "Zat"],
            months:      ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September",
                "October", "November", "December"],
            monthsShort: ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
            today:       "Vandaag",
            legend:      "Legende"
        }
    };

    var ColorAssociation = function(color) {
        this.eventName = null;
        this.color = color;
    };

    var ColorAssociator = function() {
        this.reset();
    };

    ColorAssociator.prototype = {
        constructor: ColorAssociator,

        getColor: function(eventName) {
            var index = 1;
            var ca = this.colors[0];
            while(index < this.colors.length && ca.eventName != null) {
                if(eventName == ca.eventName)
                    break;
                ca = this.colors[index++];
            }
            if(index == this.colors.length) {
                this.colors.push(new ColorAssociation('rgb(' + (Math.floor(Math.random() * 256)) + ',' +
                    (Math.floor(Math.random() * 256)) + ',' + (Math.floor(Math.random() * 256)) + ')'));
                ca = this.colors[index];
            }
            ca.eventName = eventName;
            return ca.color;
        },

        reset: function() {
            this.colors = [new ColorAssociation('#226611'), new ColorAssociation('#3276b1'), new ColorAssociation('#cb7a06'),
                new ColorAssociation('#448833'), new ColorAssociation('#5498d2'), new ColorAssociation('#ed9c28'),
                new ColorAssociation('#66aa55'), new ColorAssociation('#76baf4'), new ColorAssociation('#ffbe4a')]
        }
    };

    var EventEnvironment = function(name) {
        this.name = name;
        this.event = null;
        this.claimed = false;
    };

    EventEnvironment.prototype = {
        constructor: EventEnvironment,

        claimEnvironment: function(event, cellValue) {
            this.claimed = event.endDate > cellValue;
            if(this.claimed)
                this.event = event;
        }
    };

    /**
     * Global functions of the calendar plugin
     */
    var CalGlobal = {
        isLeapYear: function(year) {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
        },

        formatDateValue: function(dateValue, format) {
            if (dateValue == null)
                return null;
            var year = CalGlobal.getYear(dateValue);
            var month = CalGlobal.getMonth(dateValue);
            var day = CalGlobal.getDay(dateValue);
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
        },

        getYear: function(dateValue) {
            return Math.floor(dateValue/10000);
        },

        getMonth: function(dateValue) {
            return Math.floor(dateValue/100) % 100 + 1;
        },

        getDay: function(dateValue) {
            return dateValue % 100;
        }
    }
}(window.jQuery);


function converToDateValue(day, month, year) {
    return (year * 10000) + (month * 100) + day;
}

function CalendarEvent(name, start, end, link, linktext) {
    this.name = name;
    this.validEvent = (start.constructor == Date && end.constructor == Date);
    if(this.validEvent) {
        this.startDate = converToDateValue(start.getDate(), start.getMonth(), start.getFullYear());
        this.endDate = converToDateValue(end.getDate(), end.getMonth(), end.getFullYear());
    }
    this.startTime = start.getHours() * 100 + start.getMinutes();
    this.endTime =  end.getHours() * 100 + end.getMinutes();
    this.link = link || null;
    this.linktext = linktext || null;
}

function compareEvents(event1, event2) {
    if (event1.startDate < event2.startDate || (event1.startDate == event2.startDate && event1.startTime < event2.startTime))
        return -1;
    if (event1.startDate > event2.startDate || (event1.startDate == event2.startDate && event1.startTime > event2.startTime))
        return 1;
    return 0;
}