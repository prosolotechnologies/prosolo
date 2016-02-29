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

function showLoader(comp) {
	$(comp).css('text-align', 'center');
	$(comp).html('<img src="' + context + '/resources/images/style/ajax-loader-black.gif"/>');
	$(comp).show();
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