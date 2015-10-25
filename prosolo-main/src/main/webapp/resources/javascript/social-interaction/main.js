require(['/resources/javascript/prosolo.require-config.js'], function (config) {	
	require(['social-interaction/graph'], function(graph) {
				graph.load({
					width : 800,
					height : 600,
					links : 100,
					selector : "#graph",
					charge: -300,
					distance: 50
				});
	});
});