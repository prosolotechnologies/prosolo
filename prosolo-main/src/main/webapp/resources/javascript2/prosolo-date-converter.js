function convertTime(millis, format, selector) {
    convertTime(millis, format, selector, '-');
}

function convertTime(millis, format, selector, empty) {
    var formattedDate;
    if (millis >= 0) {
        formattedDate = convertTimeAndReturn(millis, format);
    } else {
        formattedDate = empty;
    }
    var el =  $(escapeColons(selector));
    if (el.is("input")) {
        el.val(formattedDate);
    } else {
        el.text(formattedDate);
    }
}

function convertTimeAndReturn(millis, format) {
	var f = format != null ? format : "DD MM YYYY, h:mm:ss a";
	switch (f) {
		case "rel":
			return convertToRelativeTime(millis);
        default:
            return moment(millis).format(f);
	}
}

function convertToRelativeTime(millis) {
    var suffix = " ago";
    var now = new Date();
    var diff = now.getTime() - millis;

    //future date
    if (diff < 0) {
        // check if the diff is over a year in order to display the year too
        if (moment(millis).diff(moment(), "years")) {
            return moment(millis).format("MMM DD, YYYY [at] h:mm A");
        } else {
            return moment(millis).format("MMM DD [at] h:mm A");
        }
    }

    // Calculate difference in days
    var diffDays = Math.floor(diff / (24 * 60 * 60 * 1000));
    if (diffDays > 0) {
        // check if it was over a year ago in order to display the year too
        if (moment().diff(moment(millis), "years")) {
            return moment(millis).format("MMM DD, YYYY [at] h:mm A");
        } else {
            return moment(millis).format("MMM DD [at] h:mm A");
        }
    }

    // Calculate difference in hours
    var diffHours = Math.floor(diff / (60 * 60 * 1000));

    if (diffHours > 0) {
        if (diffHours == 1) {
            return diffHours + " hr" + suffix;
        }
        return diffHours + " hrs" + suffix;
    }

    // Calculate difference in minutes
    var diffMinutes = Math.floor(diff / (60 * 1000));

    if (diffMinutes > 0) {
        if (diffMinutes == 1) {
            return diffMinutes + " min" + suffix;
        }
        return diffMinutes + " mins" + suffix;
    }

    // Calculate difference in seconds
    var diffSeconds = Math.floor(diff / 1000);

    if (diffSeconds >= 0) {
        return diffSeconds + " sec" + suffix;
    }
    return "";
}

function populateTimestamp(timeEl, timestampSelector, format) {
    var timeString = $(timeEl).val();
    if (timeString) {
        $(timestampSelector).val(moment(timeString, format).valueOf());
    } else {
        $(timestampSelector).val(-1);
    }
}