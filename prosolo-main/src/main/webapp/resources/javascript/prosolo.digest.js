var prosolo = $.extend({}, prosolo, prosolo || {});

prosolo.digest = {
	selectedFilter : null,
	interval : null,
	datePickerElem : null,
	dateString : null,
	startDay : null,
	endDay : null,
	
	defaultMenuItem : null,
	
	initFilter: function() {
		var filterValue = getParameterByName('filter');
		
		prosolo.digest.updateFilter(filterValue);
	},
	
	initInterval: function() {
		var intervalParam = getParameterByName('interval');
		
		if (intervalParam.length == 0) {
			intervalParam = 'daily';
		}
		
		$('.digest .leftBox .intervals ul li a.' + intervalParam).removeClass('gray').addClass('blue');
		
		prosolo.digest.updateInterval(intervalParam);
	},
	
	initDate: function() {
		var dateParam = getParameterByName('date');
		
		if (dateParam.length == 0) {
			dateParam = new Date().toJSON().slice(0,10);
		}
		
		prosolo.digest.dateString = dateParam;
		
		// select the appropriate date in the datepicker
		var date = createDateFromString(dateParam);
		
		$(prosolo.digest.datePickerElem).datepicker("setDate", date);

		if (prosolo.digest.interval == 'daily') {
        } else if (prosolo.digest.interval == 'weekly') {
        	prosolo.digest.calculateWeekInterval(date);
        	
        	prosolo.digest.selectCurrentWeek();
        }
		
		prosolo.digest.updateDate();
	},
	
	updateDate: function() {
		$('.digest .digestDate').val(prosolo.digest.dateString);
	    setQueryParam('date', prosolo.digest.dateString);
	},
	
	changeFilter: function(newFilter) {
		prosolo.digest.updateFilter(newFilter);
		
		prosolo.digest.refreshFeeds();
	},
	
	changeInterval: function(interval) {
		// deselect previously selected interval
		$('.digest .leftBox .intervals ul li a').removeClass('blue').addClass('gray');
		$('.digest .leftBox .intervals ul li a.' + interval).removeClass('gray').addClass('blue');
		
		prosolo.digest.updateInterval(interval);
		prosolo.digest.refreshFeeds();
	},
	
	updateFilter: function(filterValue) {
		if (filterValue.length > 0) {
			// deselect all filters
			$('.digest .leftBox .filters ul li a').removeClass('blue').addClass('gray');
			$('.digest .leftBox .filters ul li a.' + filterValue).addClass('blue').removeClass('gray');
			
			prosolo.digest.selectedFilter = filterValue;
			
			if (filterValue.indexOf("course") >= 0) {
				$('.digest .settings.courses').show();
			} else {
				$('.digest .settings.courses').hide();
			}
		} else {
			$('.digest .leftBox .filters ul li a.' + prosolo.digest.defaultMenuItem).addClass('blue').removeClass('gray');
			prosolo.digest.selectedFilter = prosolo.digest.defaultMenuItem;
		}
		
		$('.digest .filterValue').val(prosolo.digest.selectedFilter);
		setQueryParam('filter', prosolo.digest.selectedFilter);
	},
	
	updateInterval: function(interval) {
		$('.digest .digestInterval').val(interval);
		setQueryParam('interval', interval);
		prosolo.digest.interval = interval;
	},
	
	refreshFeeds: function() {
		$('.digest .refreshFeeds').trigger('click');
	},
	
	
	selectCurrentWeek : function() {
        window.setTimeout(function () {
        	if (prosolo.digest.datePickerElem) {
        		$(prosolo.digest.datePickerElem).find('.ui-datepicker-current-day a').addClass('ui-state-active');
        	}
        }, 1);
    },
    
    calculateWeekInterval : function(date) {
    	prosolo.digest.startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - date.getDay() + 1);
        prosolo.digest.endDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - date.getDay() + 7);
        var dateFormat = $(prosolo.digest.datePickerElem).datepicker('option', 'dateFormat') || $.datepicker._defaults.dateFormat;
        
        prosolo.digest.dateString = $.datepicker.formatDate( dateFormat, prosolo.digest.startDate, $(prosolo.digest.datePickerElem).datepicker('option', 'all') );
    },
	
	initDatepicker: function(elem) {
		prosolo.digest.datePickerElem = elem;
		
		$(elem).datepicker({
			dateFormat: 'yy-mm-dd',
			showWeek: true,
            firstDay: 1,
            weekHeader: "",
            showOtherMonths: true,
            selectOtherMonths: true,
            onSelect: function(dateText, inst) {
                if (prosolo.digest.interval == 'daily') {
                	prosolo.digest.dateString = $(this).val();
                } else if (prosolo.digest.interval == 'weekly') {
                	var date = $(this).datepicker('getDate');
                	
                	prosolo.digest.calculateWeekInterval(date);
	                
	                prosolo.digest.selectCurrentWeek();
                }
                
                prosolo.digest.updateDate();
		    	prosolo.digest.refreshFeeds();
            },
            beforeShowDay: function(date) {
                var cssClass = '';
                if(date >= prosolo.digest.startDate && date <= prosolo.digest.endDate)
                    cssClass = 'ui-datepicker-current-day';
                return [true, cssClass];
            },
            onChangeMonthYear: function(year, month, inst) {
                prosolo.digest.selectCurrentWeek();
            }
		});
		
		$(elem + " td.ui-datepicker-week-col").click(function(e){
			var el = this;
			
			// Simulate a click on the first day of the week
			$(this).next().click();
			
			var selectedDate = $(elem).datepicker("getDate");
			$("#inputStartDate").datepicker("setDate", selectedDate);
			var toDate = selectedDate;
			toDate.setDate(toDate.getDate() + 6);
			$("#inputEndDate").datepicker("setDate", toDate);
		});
		$("div.ui-datepicker").css( { "font-size": "15px" } );
	},
	
	init : function(datePickerElem, defaultMenuItem) {
		prosolo.digest.defaultMenuItem = defaultMenuItem;
		
		prosolo.digest.initInterval();
		prosolo.digest.initDatepicker(datePickerElem);
		prosolo.digest.initFilter();
		prosolo.digest.initDate();
	}
};

function createDateFromString(dateString) {
	var year   = parseInt(dateString.substring(0,4));
	var month  = parseInt(dateString.substring(5,7));
	var day   = parseInt(dateString.substring(8,10));
	return new Date(year, month-1, day);
}
