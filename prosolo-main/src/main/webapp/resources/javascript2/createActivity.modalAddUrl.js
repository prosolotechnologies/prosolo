function updateLinkPreview() {
	var link = $('#createActivity\\:formModalAddUrl\\:inputUrl').val();
	
	if (link && link.length > 0) {
		$('#addParameterContainer .linkPreview .url').text(link);
		
		var paramUrl = $('#createActivity\\:formModalAddUrl\\:idParameterName').val();
		
		if (paramUrl && paramUrl.length > 0) {
			$('#addParameterContainer .linkPreview .param').text('?' + paramUrl + '=[USER ID]');
		} else {
			$('#addParameterContainer .linkPreview .param').text('');
		}
	} else {
		$('#addParameterContainer .linkPreview .url').text('');
	}
}
$(function(){
	$('#createActivity\\:formModalAddUrl\\:inputUrl, #createActivity\\:formModalAddUrl\\:idParameterName').on("change paste keyup", function() {
		updateLinkPreview(); 
	});
	
	$('#checkPassID').change(function(){
		if ($(this).is(":checked")) {
			$('#addParameterContainer').fadeIn();
		} else {
			$('#addParameterContainer').fadeOut();
			$('#addParameterContainer .linkPreview .param').text('');
		}
	});
});
