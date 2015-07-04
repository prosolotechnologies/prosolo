function checkIfAllAreOn(dropdowns) {
	for (var i=0; i<dropdowns.length; i++) {
		if (!$(dropdowns.get(i)).is(":checked")) {
			return false;
		}
	}
	return true;
}