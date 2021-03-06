function searchListener(execFunction) {
	var delayedSearch = function() {
		execFunction();
	};

	window.clearTimeout(this.keystrokeTimeout);
	this.keystrokeTimeout = window.setTimeout(delayedSearch, 250);
}

function searchIfNeeded(inputField, functionToExecute) {
	if ($(inputField).val().length != 0) {
		searchListener(functionToExecute);
	}
}

function searchIfNeeded(inputField, functionToExecute, searchResultsPanel) {
    if ($(inputField).val().length != 0) {
        searchListener(functionToExecute);
    } else {
        $(searchResultsPanel).first().stop(true, true).slideUp(10);
	}
}

function showSearchResults(searchPanel, searchField) {
	if( $(searchField).val().length === 0 ) {
		$(searchPanel).first().stop(true, true).slideUp(10);
	} else if($(searchField).val().length >= 1) {
		$(searchPanel).first().stop(true, true).slideDown(10);
	}
}
