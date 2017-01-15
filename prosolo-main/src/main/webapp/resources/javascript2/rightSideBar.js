function isPublishOrScheduledPublish(status) {
	return status === "SCHEDULED_PUBLISH" || status === "PUBLISHED";
}

function saveChangesBtnClick(status) {
	if(isPublishOrScheduledPublish(status)) {
		$('#linkPublishChildResources').click();
	} else {
		$('.saveChangesSelector').click()
	}
}