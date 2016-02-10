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