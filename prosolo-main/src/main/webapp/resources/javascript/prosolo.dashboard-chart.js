// Requires tauCharts

var chart = {
	create : function(configuration) {
		var charts = [];

		function container() {
			return document.querySelector(configuration.container);
		}

		function destroy() {
			container().innerHTML = "";
			charts.map(function(chart) {
				chart.destroy();
			});
			charts = [];
		}

		return {
			show : function(data) {
				destroy();
				var chart = new tauCharts.Chart({
					data : data,
					type : 'line',
					x : configuration.x,
					y : configuration.y,
					color : configuration.color,
					plugins : [ tauCharts.api.plugins.get('tooltip')({
						fields : configuration.tooltip.fields
					}) ]
				});
				chart.renderTo(configuration.container);
				charts.push(chart);
			}
		};
	}
}
