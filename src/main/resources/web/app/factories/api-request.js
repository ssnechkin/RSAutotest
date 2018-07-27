"use strict";

/**
 * @ngdoc service
 * @name site.service:$apiRequest
 * @param settingsObject: {url: fact.hostPort + '/ResSendFile', type: 'POST', contentType: 'multipart/form-data'}
 * @param data: datas (object or text or form)
 * @description
 *
 * Сервис для обмена данными с API
**/
app.factory('$apiRequest', ['$http', function($http) {
	function apiRequest(settingsObject, data, callback) {
	    var url = settingsObject.url;

        switch (settingsObject.contentType) {
            case 'multipart/form-data': {
                var requestSettings = {
                    transformRequest: angular.identity, headers: {
                        'Content-Type': undefined
                    }
                };
                break;
            }
            default:                        { var requestSettings = {}; break;}
        }

	    switch (settingsObject.type.toUpperCase()) {
            case 'DELETE':  { var request = $http.delete(   url, data, requestSettings); break;}
            case 'GET':     { var request = $http.get(      url + data, requestSettings); break;}
            case 'HEAD':    { var request = $http.head(     url, data, requestSettings); break;}
            case 'JSONP':   { var request = $http.jsonp(    url, data, requestSettings); break;}
            case 'PATCH':   { var request = $http.patch(    url, data, requestSettings); break;}
            case 'POST':    { var request = $http.post(     url, data, requestSettings); break;}
            case 'PUT':     { var request = $http.put(      url, data, requestSettings); break;}
            default:        { var request = $http.post(     url, data, requestSettings); break;}
        }

		request
			.success(function(result) {
				if(typeof callback == 'function') callback(result);
			})
			.error(function(data, code) {
				if(code == 403) {
					console.log("Доступ запрещён.");
				} else {
					console.log("Сервер не доступен.");
				}
				callback(null);
			});
	}

	return apiRequest;
}]);