"use strict";

/**
 * @ngdoc
 * @name RSAutotest:$senderTree
 * @description
 *
 * Отправляет изменения в дереве на сервер
**/
app.factory('$senderTree', ['$apiRequest', '$globalSettings', function($apiRequest, $globalSettings) {
    var fact = this;
    fact.fileName;

    var sender = {
        setFileName : function(fileName) {
            fact.fileName = fileName;
        },
        executeSuite: function(suiteShortName, callback) {
            var data = new FormData();
            data.append( "fileName", "edit_web_files/" + fact.fileName);
            data.append( "suite", suiteShortName);
            $apiRequest(
                $globalSettings.requests.execute,
                data,
                callback
            );
        },
        executeTest: function(testShortName, callback) {
            var data = new FormData();
            data.append( "fileName", "edit_web_files/" + fact.fileName);
            data.append( "test", testShortName);
            $apiRequest(
                $globalSettings.requests.execute,
                data,
                callback
            );
        },
        createFile : function(fileName, newObject, tree, ctrl) {
            fact.fileName = fileName + ".json";
            var data = new FormData();
            data.append( "fileName", fact.fileName);
            data.append( "content", JSON.stringify(newObject));

            $apiRequest(
                $globalSettings.requests.addFile,
                data,
                function ( response ) {
                    ctrl.views.setFileListArrFunc(response.fileList);
                    ctrl.views.setActiveFileFunc(fact.fileName);
                    tree.view.reLoadLeftTree(response.fileValue);
                }
            );

        },
        update : function(path, newObject) {
            var data = new FormData();
            data.append( "fileName", fact.fileName);
            data.append( "operation", "update");
            data.append( "path", path);
            data.append( "object", JSON.stringify(newObject));
            $apiRequest(
                $globalSettings.requests.tree,
                data,
                function ( response ) {
                }
            );
        },
        add : function(path, newObject) {
            var data = new FormData();
            data.append( "fileName", fact.fileName);
            data.append( "operation", "add");
            data.append( "path", path);
            data.append( "object", JSON.stringify(newObject));
            $apiRequest(
                $globalSettings.requests.tree,
                data,
                function ( response ) {
                }
            );
        },
        addSub : function(path, newObject) {
            var data = new FormData();
            data.append( "fileName", fact.fileName);
            data.append( "operation", "addSub");
            data.append( "path", path);
            data.append( "object", JSON.stringify(newObject));
            $apiRequest(
                $globalSettings.requests.tree,
                data,
                function ( response ) {
                }
            );
        },
        remove: function(path) {
            var data = new FormData();
            data.append( "fileName", fact.fileName);
            data.append( "operation", "remove");
            data.append( "path", path);
            $apiRequest(
                $globalSettings.requests.tree,
                data,
                function ( response ) {
                }
            );
        }
    };

    return sender;
}]);