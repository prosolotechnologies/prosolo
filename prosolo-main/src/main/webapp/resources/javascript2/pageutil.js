function setQueryParam(key, value) {
    var uri = window.location.href;
    var newUri = setQueryParamOfUri(uri, key, value);
    history.replaceState({}, null, newUri);
}

function setQueryParamOfUri(uri, key, value) {
	var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
	var separator = uri.indexOf('?') !== -1 ? "&" : "?";
	if (uri.match(re)) {
		return uri.replace(re, '$1' + key + "=" + value + '$2');
	} else {
		return uri + separator + key + "=" + value;
	}
}