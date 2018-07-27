"use strict";

/**
 * @ngdoc
 * @name RSAutotest:$splitter
 * @description
 *
 * Формирует сплитер на странице
**/
app.factory('$splitter', [function() {
	this.init = function() {
		$("#workspace").splitter({
			"orientation": "horizontal",
			"limit": 100
		});
		$("#leftPanel").splitter({
			"orientation": "vertical",
			"limit": 150
		});
		$("#filePanel").splitter({
			"orientation": "vertical",
			"limit": 57
		});
		$("#rightPanel").splitter({
			"orientation": "vertical",
			"limit": 1
		});
	}

	return {
		init: this.init
	}
}]);