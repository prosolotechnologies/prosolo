var service = (function () {
	return {
		create : function(configuration) {
			return {
				get : function (callback) {
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
			}
		}
	};
})();