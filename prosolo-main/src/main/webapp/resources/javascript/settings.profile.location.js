function initializeMap(locationName, lat, lng) {
	var mapOptions = {
		center: new google.maps.LatLng(lat, lng),
		zoom: 13,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	var map = new google.maps.Map($('.settings .profile .location .map-canvas')[0], mapOptions);
	var latitude = $('.profile .latitude');
	var longitude = $('.profile .longitude');
	
	var input = /** @type {HTMLInputElement} */$('.profile .searchLocationTextField')[0];
	var autocomplete = new google.maps.places.Autocomplete(input);
	
	autocomplete.bindTo('bounds', map);
	
	var infowindow = new google.maps.InfoWindow();
	var marker = new google.maps.Marker({
		position: new google.maps.LatLng(lat, lng),
		map: map
	});
	
	infowindow.setContent('<strong>'+locationName+'</strong>');
	infowindow.open(map, marker);
	        
	//if (lat == '' && lng == '') {
	//	locateMe();
	//}
	
	function setMarkerAndInfoWindow(place) {
		marker.setPosition(place.geometry.location);
		marker.setVisible(true);
		
		var address = '';
		if (place.address_components) {
			address = [
				(place.address_components[0] && place.address_components[0].short_name || ''),
				(place.address_components[1] && place.address_components[1].short_name || ''),
				(place.address_components[2] && place.address_components[2].short_name || '')
			].join(' ');
		}
		
		infowindow.setContent('<div><strong>' + place.name + '</strong><br>' + address);
		infowindow.open(map, marker);
	}
	
	// listener for place changed in search input field
	google.maps.event.addListener(autocomplete, 'place_changed', function() {
		infowindow.close();
		marker.setVisible(false);
		input.className = '';
		var place = autocomplete.getPlace();
		if (!place.geometry) {
			// Inform the user that the place was not found and return.
			input.className = 'notfound';
			return;
		}
		
		// If the place has a geometry, then present it on a map.
		if (place.geometry.viewport) {
			map.fitBounds(place.geometry.viewport);
		} else {
			map.setCenter(place.geometry.location);
			map.setZoom(17);  // Why 17? Because it looks good.
		}
		         
		setMarkerAndInfoWindow(place);
		latitude.val(place.geometry.location.lat());
		longitude.val(place.geometry.location.lng());
	});
}

var initialLocation;
function locateMe() {
	var browserSupportFlag =  new Boolean();
	
	var myOptions = {
		zoom: 6,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	var map = new google.maps.Map($('.settings .profile .location .map-canvas')[0], myOptions);
	
	// Try W3C Geolocation (Preferred)
	if (navigator.geolocation) {
		browserSupportFlag = true;
		navigator.geolocation.getCurrentPosition(function(position) {
			initialLocation = new google.maps.LatLng(position.coords.latitude,position.coords.longitude);
			map.setCenter(initialLocation);
		}, function() {
			handleNoGeolocation(browserSupportFlag);
		});
	}
	// Browser doesn't support Geolocation
	else {
		browserSupportFlag = false;
		handleNoGeolocation(browserSupportFlag);
	}

	function handleNoGeolocation(errorFlag) {
		if (errorFlag == true) {
			alert("Geolocation service failed.");
		} else {
			alert("Your browser doesn't support geolocation.");
		}
		map.setCenter(initialLocation);
	}
}