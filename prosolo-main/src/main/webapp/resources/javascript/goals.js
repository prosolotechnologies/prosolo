function hideLearningGoals(goalsDiv, limit){
	var goalList = $("#"+goalsDiv);
	var goals = goalList.children("li");
	var goalCount = goals.length;
	if (goalCount > limit) {
		for (var i=limit-1; i<goalCount; i++) {
			if (i == limit-1) {
				$(goals[i]).after('<div id="hiddenGoals"/>');
			}
			if (i>limit-1) {
				$('#hiddenGoals').append($(goals[i]));
			}
		}
		$('#hiddenGoals').hide();

		goalList.after('<a id="moreGoals" class="navDown" href="javascript:void(0);">expand</a>');
	}
}
