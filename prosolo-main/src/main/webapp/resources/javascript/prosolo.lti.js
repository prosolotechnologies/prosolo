function sign(oForm,consSecret) {
    var accessor = { consumerSecret: consSecret };
    var message = { method: 'POST', action: oForm.action, 
    	    parameters: OAuth.decodeForm('ltiLaunchForm')};
    for (var e = 0; e < oForm.elements.length; ++e) {
        var input = oForm.elements[e];
        if(input.name=="javax.faces.ViewState"){
        	oForm.removeChild(input);
        }else if (input.name !=null && input.name !="oauth_signature" && input.name !="oauth_signature_method" && input.name !="ltiLaunchForm"
        && input.value !="start" && input.value != null && input.value != ""  && input.name != "javax.faces.ViewState" && input.name != "submit_form_button")
            { 
        		message.parameters.push([input.name, input.value]);     
        	}
        OAuth.SignatureMethod.sign(message, accessor);
    }
    oForm.oauth_signature.value = OAuth.getParameter(message.parameters,"oauth_signature");
}
function doOnLoad(consSecret, launchUrl, oForm) {
	 console.log("doOnLoad called:"+launchUrl);
	freshNonce(oForm);
	freshTimestamp(oForm);
    sign(oForm,consSecret);
    /*
    var formData=new FormData(oForm);
    console.log("doOnLoad called 2");
    $.ajax({
    	type: "POST",
    	url: launchUrl,
    	cache: false,
    	data: formData,
    	processData: false,
        contentType: false,
    	success: function(result){
    	}
    	});
*/
  oForm.submit();
     }

function freshTimestamp(oForm) {
	oForm.oauth_timestamp.value = OAuth.timestamp();
}
function freshNonce(oForm) {
      oForm.oauth_nonce.value = OAuth.nonce(11);
}