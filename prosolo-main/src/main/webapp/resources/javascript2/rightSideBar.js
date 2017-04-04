var isDraft = false;

function setIsDraft(draft) {
	isDraft = draft;
}

function isPublished(status) {
	return status === "PUBLISHED";
}

function saveChangesBtnClick(status) {
	if(isDraft && isPublished(status)) {
		$('#linkPublishChildResources').click();
	} else {
		$('.saveChangesSelector').click()
	}
}