$(function () {
	initializePageJS();
});

function initializePageJS() {
	/* custom select initialization*/
	$('.selectpicker').selectpicker();
	 
	 
	/* set equal heights to columns */
	var getHeight = $(".indexColumn").height();
	$(".cloneHeight").css("height",getHeight);
	 
	 
	/* Add shadow on focus for keyword fields */
	$(".jq_tags_editor_input").focus(function(){
	    $(this).parent().addClass("focusedTags");
	}).blur(function(){
	    $(this).parent().removeClass("focusedTags");
	})
}

function hideDialogOnSuccess(args, dialogId) {
    if (args && !args.validationFailed) {
    	$('#' + dialogId).modal('hide');
    }
}

function showLoader(elem, context) {
    $(elem).css('text-align', 'center');
    $(elem).html('<img class="loaderSvg" src="' + context + '/resources/images2/loader.svg" width="20" height="20"/>')
    $(elem).show();
};

function hideLoader() {
	var loaderContainer = $('#loaderContainer');
	loaderContainer.hide();
	loaderContainer.empty();
};

function showComp(compId) {
	$(document.getElementById(compId)).show();
}

function hideComp(compId) {
	$(document.getElementById(compId)).hide();
}