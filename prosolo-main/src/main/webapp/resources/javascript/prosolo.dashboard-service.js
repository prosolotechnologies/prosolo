// Requires jQuery

var service = {
	create : function(configuration) {
		function get(callback) {
			$.ajax({
				url : configuration.url,
				type : "GET",
				data : configuration.parameters(),
				crossDomain : true,
				dataType : 'json'
			}).done(function(data) {
				callback(data.map(configuration.data));
			});
		}
		return {
			get : get
		}
	}
}