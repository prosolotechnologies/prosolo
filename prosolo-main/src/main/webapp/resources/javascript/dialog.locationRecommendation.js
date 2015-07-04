function initializeDialogMap(data, userIdToOpen) {
	if (data == null || data.length == 0) {
		return;
	}
	
	var map = new google.maps.Map($('#locationRecommendationDialog .map-canvas')[0], {
         zoom: 10,
         center: new google.maps.LatLng(data[0][4], data[0][5]),
         mapTypeId: google.maps.MapTypeId.ROADMAP
     });
     
     var infowindow = new google.maps.InfoWindow();
     var marker, i;
     for (i = 0; i < data.length; i++) {
         marker = new google.maps.Marker({
             position: new google.maps.LatLng(data[i][4], data[i][5]),
             map: map,
         });

         
         google.maps.event.addListener(marker, 'click', (function (marker, i) {
             return function () {
            	 var contentString = '<b><a href="'+data[i][1]+'" target ="_blank">' + data[i][2] + 
            	 '<\/a></b><br/><br />' + data[i][3];
                 infowindow.setContent(contentString);
                 infowindow.open(map, marker);
             };
         })(marker, i));
         
         if (data[i][0] == userIdToOpen) {
        	 console.log('data: '+data[i][0]+' userIdToOpen:'+userIdToOpen);
        	 google.maps.event.trigger(marker,'click');
         }
     }
}