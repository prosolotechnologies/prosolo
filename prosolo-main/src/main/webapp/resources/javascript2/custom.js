$(function () {
	//tooltips
    $('[data-toggle="tooltip"]').tooltip({
        container: 'body'
    });
    $('[data-toggle="popover"]').popover({
        trigger: 'hover',
        placement: 'top',
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
    if ((($(inputElem).is('input') || $(inputElem).is('textarea')) && $(inputElem).val().length == 0) ||
			($(inputElem).is('div') && !stripTags($(inputElem).html()).trim())) {
        $(inputElem).parent().find('.submitBtn').addClass('hidden');
     } else {
    	$(inputElem).parent().find('.submitBtn').removeClass('hidden');
     }
}

function stripTags(text) {
    return text.replace(/<\/?[^>]+>|&nbsp;/gi, '');
};

function toggleReplyInput(replyBtn) {	
	$(replyBtn).parent().parent().next('.replyInput').toggleClass('hidden');
	$(replyBtn).parent().parent().next('.replyInput').get(0).focus();
    return false;
}

function showEditComment(editBtn) {
    $(editBtn).parent().parent().find('.editComment').toggleClass('hidden');
    $(editBtn).parent().parent().find('.commentText').toggleClass('hidden');
    $(editBtn).addClass('hidden');
    return false;
}

function hideEditComment(cancelBtn) {
    var editCommentContainer = $(cancelBtn).parent();
    var commentContainer = $(cancelBtn).parent().prev('.commentText');

    editCommentContainer.addClass('hidden');
    commentContainer.removeClass('hidden');

    var textarea1 = editCommentContainer.find('.contentEditableComment').first();
    textarea1.html(commentContainer.text());

    $(cancelBtn).parent().next('.commentOptions').find('a.edit').removeClass('hidden');
    return false;
}

function hideEditPrivateConversationComment(cancelBtn) {
	    var editCommentContainer = $(cancelBtn).parent();
	    var commentContainer = $(cancelBtn).parent().prev('.commentText');

	    editCommentContainer.addClass('hidden');
	    commentContainer.removeClass('hidden');

	    var textarea1 = editCommentContainer.find('.contentEditableComment').first();
	    textarea1.val(commentContainer.text());

	    $(cancelBtn).parent().next('.commentOptions').find('a.edit').removeClass('hidden');
	    return false;
	}

function showCommentReplies(elem) {
	$(elem).hide();
	$(elem).nextAll('.media').show();
}

function getQueryParam(name) {
    var url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
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

function showLoader(elem, context) {
    $(elem).hide();
    $(elem).css('text-align', 'center');
    $(elem).html('<img class="loaderSvg" src="' + context + '/resources/images2/loader.svg" width="20" height="20"/>')
    $(elem).show();
};

function showLoaderNextToElem(elem, context) {
    $(elem).after('<img class="loaderSvg" src="' + context + '/resources/images2/loader.svg" width="20" height="20"/>')
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

function replaceInlineUserLink(selector) {
	$(selector).find('a').each(function() {
		$(this).attr('href', 'profile/'+$(this).attr('data-id'))
			   .attr('target', '_blank');
	});
}

function hideModal(dialogId) {
	$('#'+dialogId).modal('hide');
	$("body").removeAttr("class").removeAttr("style");
	$("div.modal-backdrop.fade.in").remove();
}

function getSelectorFromClassesString(classes) {
	return '.' + classes.split(/\s+/).join('.')
}

//slide menu
function showPopupSidebar (sidebarSelector) {
    $(sidebarSelector).addClass('cbp-spmenu-open');
};

function hidePopupSidebar (sidebarSelector) {
    $(sidebarSelector).removeClass('cbp-spmenu-open');
};

function preventDoubleClick(elem) {
    $(elem).prop("onclick", "").off("click");
    $(elem).attr("onclick", "return false;").unbind("click");
}

function bindCheckAllOnclick(checkAllCheckboxId) {
	$('#' + checkAllCheckboxId).on('change', function() {
        $('[data-check-id="' + checkAllCheckboxId + '"]').prop("checked", $(this).is(':checked'));
	});
	$('[data-check-id="' + checkAllCheckboxId + '"]').on('change', function() {
		$('#' + checkAllCheckboxId).prop("checked", $('[data-check-id="' + checkAllCheckboxId + '"]:checked').length == $('[data-check-id="' + checkAllCheckboxId + '"]').length);
	});
}