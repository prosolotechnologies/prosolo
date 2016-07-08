$(function () {
	//tooltips
	$('[data-toggle="tooltip"]').tooltip({
	    'placement': 'top'
	});
	$('[data-toggle="popover"]').popover({
	    trigger: 'hover',
	        'placement': 'top'
	});
});

var context = '';

var custom = {
	setContext: function(ctx) {
		context = ctx;
	}
}

//comment form hide/show
function displaySubmitButton(inputElem) {
    if (($(inputElem).is('input') && $(inputElem).val().length == 0) ||
    		($(inputElem).is('div') && $(inputElem).html().length == 0)) {
        $(inputElem).next('button.btn-green').addClass('hidden');
    } else {
        $(inputElem).next('button.btn-green').removeClass('hidden');
    }
}

function toggleReplyInput(replyBtn) {
	$(replyBtn).parent().parent().next('.replyInput').toggleClass('hidden');
	$(replyBtn).parent().parent().next('.replyInput').get(0).focus();
    return false;
}

function showEditComment(editBtn) {
    $(editBtn).parent().prev('.editComment').toggleClass('hidden');
    $(editBtn).parent().prevAll('.commentText').toggleClass('hidden');
    $(editBtn).addClass('hidden');
    return false;
}

function hideEditComment(cancelBtn) {
    var editCommentContainer = $(cancelBtn).parent();
    var commentContainer = $(cancelBtn).parent().prev('.commentText');

    editCommentContainer.addClass('hidden');
    commentContainer.removeClass('hidden');

    var textarea1 = editCommentContainer.find('textarea').first();
    textarea1.val(commentContainer.text());

    $(cancelBtn).parent().next('.commentOptions').find('a.edit').removeClass('hidden');
    return false;
}

function showCommentReplies(elem) {
	$(elem).hide();
	$(elem).nextAll('.media').show();
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

function scrollTo(elementId) {
    $('html, body').animate({
        scrollTop: $("#"+elementId).offset().top
    }, 2000);
}

function scrollToNewestComment(hiddenFieldId) {
	var id = document.getElementById(hiddenFieldId).value;
	scrollTo('comment_'+id);
}

function showLoader(comp) {
	$(comp).css('text-align', 'center');
	$(comp).html('<img src="' + context + '/resources/images/style/ajax-loader-black.gif"/>');
	$(comp).show();
};

function hideLoader(comp) {
	var loaderContainer = $(comp);
	loaderContainer.hide();
	loaderContainer.empty();
};

function roundImages(){ //needed as a separate function because of a partial page reload
	$('img.imageRound').each(function() {
		var imgClass = $(this).attr('class');
		$(this).wrap('<span class="image-wrap ' + imgClass + '" style="width: auto; height: auto;"/>');
		$(this).removeAttr('class');
	});
};

function removeCssClassesFromElement(elementId, cssClasses) {
	//If desired class/classes are not provided, this function removes all of them from supplied element
	
	if(elementId) { 
		var element = $('#' + elementId);
		if(cssClasses) {
			if($.isArray(cssClasses)) { 
				$.each(cssClasses, function(i, value) {
					element.removeClass(value);
				});
			} else {
				element.removeClass(cssClasses);
			}  
		} else {
			element.removeClass();
		}
	}
};

function addClassToElement(elementId, cssClass) {
	if(elementId) {
		var element = $('#' + elementId);
		element.addClass(cssClass);
	}
};

function escapeColons(text){
	return text.replace(/:/g, '\\:');
}

function showJustPostedComment(topLevelCommentContainerSelector) {
	var newestComment = $('[data-newest-comment="true"');
	if (newestComment.length) {
		newestComment.show();
		newestComment.prevAll('.media').show();
		$(topLevelCommentContainerSelector + ' .loadReplies').hide();
	}
}

