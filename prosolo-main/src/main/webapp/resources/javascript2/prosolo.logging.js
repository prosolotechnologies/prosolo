function sendLogPageNavigation(pageUrl, learningContext){
	$("#logging_form_page_navigation .link").val(pageUrl);
	$("#logging_form_page_navigation .parameters").val('');
	$("#logging_form_page_navigation .learningContext").val(learningContext);
	$('#logging_form_page_navigation .submitButton').click();
}

function sendLogPageNavigation(pageUrl, page, learningContext, service) {
	$("#logging_form_page_navigation .link").val(pageUrl);
	$("#logging_form_page_navigation .parameters").val('');
	$("#logging_form_page_navigation .page").val(page);
	$("#logging_form_page_navigation .learningContext").val(learningContext);
	$("#logging_form_page_navigation .service").val(service);
	$('#logging_form_page_navigation .submitButton').click();
}

function sendLogPageNavigationWithParameters(pageUrl, parameters){
	$("#logging_form_page_navigation .link").val(pageUrl);
	$("#logging_form_page_navigation .parameters").val(JSON.stringify(parameters));
	$("#logging_form_page_navigation .learningContext").val('');
	$('#logging_form_page_navigation .submitButton').click();
}
function logTabNavigation(tab, learningContext){
	$("#logging_form_page_navigation .link").val(tab);
	$("#logging_form_page_navigation .parameters").val('');
	$("#logging_form_page_navigation .learningContext").val(learningContext);
	$('#logging_form_page_navigation .loggingTabButton').click();
}
function sendServiceUse(componentName,parameters){
	$("#logging_form_serviceuse .component").val(componentName);
	$("#logging_form_serviceuse .parameters").val(JSON.stringify(parameters));
	$("#logging_form_serviceuse .learningContext").val('');
	$('#logging_form_serviceuse .submitButton').click();
}

function sendServiceUse(componentName,parameters, page, learningContext, service){
	$("#logging_form_serviceuse .component").val(componentName);
	$("#logging_form_serviceuse .parameters").val(JSON.stringify(parameters));
	$("#logging_form_serviceuse .learningContext").val(learningContext);
	$("#logging_form_serviceuse .page").val(page);
	$("#logging_form_serviceuse .service").val(service);
	$('#logging_form_serviceuse .submitButton').click();
}
