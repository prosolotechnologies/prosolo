var require = {
	"baseUrl": 	document.querySelector("script[src$='prosolo.require-config.js']").dataset.baseUrl,
    "paths": {
    	"jquery": 'jquery-1.11.3.min',
    	'jqueryui': 'jquery-ui-1.11.2.min',
    	'd3': 'd3.min',
    	'underscore': 'underscore-min',
    	'bootstrap': 'bootstrap/bootstrap.min',
    	'bootstrap-select': 'bootstrap/bootstrap-select.min'
    },
    "shim": {
    	'bootstrap':{deps: ['jquery', 'jqueryui']},
    	'bootstrap-select': {
    		deps: ['bootstrap'],
    		exports: ['$.fn.selectpicker','$.fn.triggerNative']
    	}
    }

};