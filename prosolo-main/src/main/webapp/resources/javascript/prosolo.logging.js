function clearLoggingFields(){
	$(".loggingPageUserId").val('');
	$(".loggingPageUrlFieldValue").val('');
	$(".loggingPageContextFieldValue").val('');
	$(".loggingServiceUseUserId").val('');
	$(".loggingComponentNameFieldValue").val('');
	$(".loggingParametersFieldValue").val('');
}

function sendLogPageNavigation(pageUrl, context){
	$("#logging_form .loggingPageUrlFieldValue").val(pageUrl);
	$("#logging_form .loggingParametersFieldValue").val('');
	$("#logging_form .loggingPageContextFieldValue").val(context);
	$('#logging_form .loggingPageSubmitButton').click();
}
function sendLogPageNavigationWithParameters(pageUrl, parameters){
	$("#logging_form .loggingPageUrlFieldValue").val(pageUrl);
	$("#logging_form .loggingParametersFieldValue").val(JSON.stringify(parameters));
	$("#logging_form .loggingPageContextFieldValue").val('');
	$('#logging_form .loggingPageSubmitButton').click();
}
function logTabNavigation(tab, context){
	$("#logging_form .loggingPageUrlFieldValue").val(tab);
	$("#logging_form .loggingParametersFieldValue").val('');
	$("#logging_form .loggingPageContextFieldValue").val(context);
	$('#logging_form .loggingTabButton').click();
}
function logTabNavigationWithParameters(tab, parameters){
	$("#logging_form .loggingPageUrlFieldValue").val(tab);
	$("#logging_form .loggingParametersFieldValue").val(JSON.stringify(parameters));
	$("#logging_form .loggingPageContextFieldValue").val('');
	$('#logging_form .loggingTabButton').click();
}
function sendServiceUse(componentName,parameters){
	$("#logging_serviceuse_form .loggingComponentNameFieldValue").val(componentName);
	$("#logging_serviceuse_form .loggingParametersFieldValue").val(JSON.stringify(parameters));
	$("#logging_serviceuse_form .loggingPageContextFieldValue").val('');
	$('#logging_serviceuse_form .loggingServiceUseSubmitButton').click();
}

function cancelCommentEvent(componentName, page, context, service){
	$("#logging_cancelcomment_form .loggingComponentNameFieldValue").val(componentName);
	$("#logging_cancelcomment_form .loggingCancelCommentPage").val(page);
	$("#logging_cancelcomment_form .loggingCancelCommentLearningContext").val(context);
	$("#logging_cancelcomment_form .loggingCancelCommentService").val(service);
	// --
	$('#logging_cancelcomment_form .loggingCancelCommentSubmitButton').click();
}

