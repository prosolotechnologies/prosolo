function initializeLocationDialogMap(data) {
	if (data == null || data[0].length == 0) {
		return;
	}
	
	var map = new google.maps.Map($('#locationDialog .map-canvas')[0], {
         zoom: 13,
         center: new google.maps.LatLng(data[1], data[2]),
         mapTypeId: google.maps.MapTypeId.ROADMAP
     });
     
     var infowindow = new google.maps.InfoWindow();
     var marker = new google.maps.Marker({
         position: new google.maps.LatLng(data[1], data[2]),
         map: map,
     });

         
     google.maps.event.addListener(marker, 'click', (function (marker) {
         return function () {
        	 var contentString = '<b>'+data[0]+'</b>';
             infowindow.setContent(contentString);
             infowindow.open(map, marker);
         };
     })(marker));
     
	 google.maps.event.trigger(marker,'click');
}