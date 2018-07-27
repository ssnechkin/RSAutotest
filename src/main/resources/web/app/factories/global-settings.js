"use strict";

app.factory('$globalSettings', [function() {
    var fact = this;
    fact.hostPort = window.location.origin;

    var buttonsArr = []
    var tree = {
        treeLeft : {
            nameElementId : '',
            nameChildrenList : 'list',
            listRetrieveText : ['suite', 'test'],
            dialogs : {
                SuiteEdit : { nameElement : ""}
            }
        },
        treeRight : {
            nameElementId : '',
            nameChildrenList : 'list',
            listRetrieveText : ['step', 'key', 'beginCycle', 'endCycle'],
            dialogs : {
            }
        }
    }
    var requests = {
        addFile : { url: fact.hostPort + '/rstree/addFile', type: 'POST', contentType: 'multipart/form-data' },
        getFile : { url: fact.hostPort + '/rstree/getFile', type: 'POST', contentType: 'multipart/form-data' },
        removeFile : { url: fact.hostPort + '/removeFile', type: 'POST', contentType: 'multipart/form-data' },
        getMethods : { url: fact.hostPort + '/getRSMethods', type: 'GET' },
        downloadFile : { url: fact.hostPort + '/downloadFile', type: 'GET' }, // .../?fileName=file.name
        tree : { url: fact.hostPort + '/rstree/tree', type: 'POST', contentType: 'multipart/form-data' },
        execute : { url: fact.hostPort + '/execute', type: 'POST', contentType: 'multipart/form-data' }
    }

	return {
        requests : requests,
        buttonsArr : buttonsArr,
        tree : tree
    }
}]);