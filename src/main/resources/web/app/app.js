"use strict";

/**
 * @ngdoc object
 * @name RSAutotest
 * @description
 *
 * Главный модуль приложения RSAutotest
**/

var app = angular.module('RSAutotest', ['ngRoute']);

app.config(function($routeProvider, $locationProvider) {
	$routeProvider
	/*.when('/admin', {
		templateUrl: 'app/views/admin.html',
		controller: 'AdminController',
		controllerAs: 'ctrl'
	})*/
	.otherwise({
		redirectTo: '/'
	});

	$locationProvider.html5Mode(true);
});

app.config(function($httpProvider) {
	$httpProvider.defaults.useXDomain = true;
	delete $httpProvider.defaults.headers.common['X-Requested-With'];
});