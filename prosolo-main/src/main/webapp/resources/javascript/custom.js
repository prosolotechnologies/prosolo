// mode can be 'click' (default) or 'hover'
function enableDropdown(elem) {
	enableDropdown(elem, null);
}

function enableDropdown(elem, mode) {
	var m = (mode) ? mode : 'click';

	$(elem).on(m, function(e){
		e.stopPropagation();

		$('.dropdown:not(.searchSystem .dropdown)').addClass('hidden');	
		
		if ($(this).hasClass('pressed')) {
			$(this).nextAll('.dropdown').addClass('hidden');
			$(this).removeClass('pressed');
		} else {
			$(this).nextAll('.dropdown').removeClass('hidden');
			$(this).addClass('pressed');
		}
	});
}

function roundImages(){ //needed as a separate function because of a partial page reload
	$('img.imageRound').each(function() {
		var imgClass = $(this).attr('class');
		$(this).wrap('<span class="image-wrap ' + imgClass + '" style="width: auto; height: auto;"/>');
		$(this).removeAttr('class');
	});
}

function addLoader(div, message){
	$(div).html('');
	$(div).append('<div style="text-align: center;">'+message+'&nbsp;&nbsp;<img src="resources/images/style/ajax-loader-black.gif" style="margin-top: 9px;"/></div>');
}

function addLoaderWithClass(div, message, withLoader, onlyRemoveContent, loaderClass){
	if (withLoader) {
		$(div).html('<div class="'+loaderClass+'">'+message+'&nbsp;&nbsp;<img src="resources/images/style/ajax-loader-black.gif"/></div>').show();
	} else if (onlyRemoveContent) {
		$(div).html('').show();
	}
}

function addWhiteLoaderWithClass(div, message, withLoader, onlyRemoveContent, loaderClass){
	if (withLoader) {
		$(div).html('<div class="'+loaderClass+'">'+message+'&nbsp;&nbsp;<img src="resources/images/style/ajax-loader-white.gif"/></div>').show();
	} else if (onlyRemoveContent) {
		$(div).html('').show();
	}
}

function toggleEditMode(elem1, elem2, focusOnElement) {
	$(elem1 + ", "+ elem2).toggleClass("hidden");
	
	if (focusOnElement) {
		$(focusOnElement).focus();
	}
}

function escapeColons(text){
	return text.replace(/:/g, '\\:');
}

function stripTagsExceptBr(html) {
	var html = html.replace("<br>","||br||");  
	var tmp = document.createElement("DIV");
	tmp.innerHTML = html;
	html = tmp.textContent||tmp.innerText;
	return html.replace("||br||","<br>");  
};

function addQueryParam(key, value) {
	var newUrl,
		url = window.location.href,
		re = new RegExp("([?&])" + key + "=(.*?)(&|#|$)(.*)", "gi"),
		separator = url.indexOf('?') !== -1 ? "&" : "?";
	
	if (url.match(re)) {
		newUrl = url.replace(re, '$1' + key + "=$2," + value + '$3$4');
	} else {
		newUrl = url + separator + key + "=" + value;
	}
	history.replaceState({}, null, newUrl);
}

function removeValueFromQueryParam(key, value) {
	var currentParamValue = getParameterByName(key),
		newParamValue = '',
		values = currentParamValue.split(','),
		index = values.indexOf(value);
	
	if (index > -1) {
		values.splice(index, 1);
	}
	
	for (var i in values) {
		newParamValue += values[i];
		
		if (i < values.length-1)
			newParamValue += ',';
	}
	setQueryParam(key, newParamValue);
}

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

function removeQueryParameter(key) {
	var url = window.location.href;
    //prefer to use l.search if you have a location/link object
    var urlparts= url.split('?');   
    if (urlparts.length>=2) {

        var prefix= encodeURIComponent(key)+'=';
        var pars= urlparts[1].split(/[&;]/g);

        //reverse iteration as may be destructive
        for (var i= pars.length; i-- > 0;) {    
            //idiom for string.startsWith
            if (pars[i].lastIndexOf(prefix, 0) !== -1) {  
                pars.splice(i, 1);
            }
        }

        url= urlparts[0]+'?'+pars.join('&');
        return url;
    } else {
        return url;
    }
}

function getHashValue(key) {
	var match = location.hash.match(new RegExp(key+'=([^&]*)'));
	
	if (match && match[1]) {
		return location.hash.match(new RegExp(key+'=([^&]*)'))[1];
	}
	return null;
}

function positionDialogCenter(dialogSelector) {
	var dialogElem = $(dialogSelector);
	
	//if (dialogElem.dialog( "isOpen" ))
	//	dialogElem.dialog({ position: 'center' });
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function setCaretAtEndOfEditableDiv(el) {
	el.focus();
    if (typeof window.getSelection != "undefined"
            && typeof document.createRange != "undefined") {
        var range = document.createRange();
        range.selectNodeContents(el);
        range.collapse(false);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (typeof document.body.createTextRange != "undefined") {
        var textRange = document.body.createTextRange();
        textRange.moveToElementText(el);
        textRange.collapse(false);
        textRange.select();
    }
}

function returnAsCSV(array) {
	var output = '';
	for (var i=0; i<array.length; i++) {
		output += array[i];
		if (i != array.length-1) {
			output += ',';
		}
	}
	return output;
}

$(document).ready(function() {
	roundImages();
	
	//dropdown
	enableDropdown('.arrow, .postOptArrow, .filterDrop, .shareDrop, .loginLink');
	
	$(document).on('click', function(){											// when clicking anywhere on the site
		$('.dropdown:not(.searchSystem .dropdown)').addClass('hidden');			// add class 'hidden' to all dropdowns
		//$('.arrow').removeClass('pressed');
		//$('.postOptArrow').removeClass('pressed');
		//$('.drop').removeClass('pressed');
		//$('.compsArrow').removeClass('pressed');
		//$('.filterDrop').removeClass('pressed');
		//$('.compDrop').removeClass('pressed');
		//$('.navMore').removeClass('pressed');
		$('.pressed').removeClass('pressed');
	});
	
});