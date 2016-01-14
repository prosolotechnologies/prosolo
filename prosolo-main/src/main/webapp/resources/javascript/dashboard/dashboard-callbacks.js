// Returns callbacks constructor function.
// Constructor creates new object with following methods:
// * subscribe(callback) 	- registers new callback
// * notify(changes) - notifies all registered callbacks of changes
var Callbacks = function() {
	var callbacks = [];

	this.notify = function(changes) {
		callbacks.forEach(function(callback) {
			callback(changes);
		});
	};

	this.subscribe = function(callback) {
		callbacks.push(callback);
	};
};