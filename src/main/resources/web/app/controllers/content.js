"use strict"

app.controller('ContentController', ['$scope', '$apiRequest', '$globalSettings', '$splitter', '$tree', '$senderTree', '$dialogs', function($scope, $apiRequest, $globalSettings, $splitter, $tree, $senderTree, $dialogs) {
	var ctrl = this;
	var leftTreeNameElement = "";
	$scope.dialogs = $dialogs;

	this.init = function() {
	    //$("body").on("contextmenu", false);
		ctrl.views.splitter.init();
		ctrl.views.loadTree();
		$tree.view.leftTreeNameElement = ctrl.leftTreeNameElement;
		$tree.view.rightTreeNameElement = ctrl.rightTreeNameElement;

		$( ctrl.downloaFileElement ).button();
		$( ctrl.deletFileElement).button();
		$( ctrl.addNewFileElement).button();
		$( ctrl.addFileButtonElement).button();
		//$( ctrl.fileList).selectmenu();

		$scope.dialogs.options.ctrlScope = $scope;

		$apiRequest(
            $globalSettings.requests.getMethods,
            '',
            function ( response ) {
                $dialogs.options.listMethds = response.sort(function(a, b) {
                    if (a.name < b.name) {
                        return -1;
                    }
                    if (a.name > b.name) {
                        return 1;
                    }
                    return 0;
                });
            }
        );
	}

	this.views = {
	    handleClick: function(event) {
	        if(event.which == 3  // нажата правая кнопка мыши
	            && $(ctrl.leftTreeNameElement).html().length == 0
	            && ctrl.views.fileList.selectedFile.length != 0) {
	            $dialogs.view.suiteAddInFile.open(ctrl.fileList);
	        }
        },
	    setActiveFileFunc : function(fileName) {
	        $senderTree.setFileName(fileName);
	        ctrl.views.fileList.selectedFile = '';
            ctrl.views.fileList.list.forEach(function(button) {
                if(button.text == fileName) {
                    button.selected = true;
                    ctrl.views.fileList.selectedFile = button.text;
                    $(ctrl.downloaFileElement).attr("href", $globalSettings.requests.downloadFile.url + "?fileName=" + button.text);
                } else {
                    button.selected = false;
                }
            });
        },

        setFileListArrFunc : function(strArr) {
            ctrl.views.fileList.list.splice(0, ctrl.views.fileList.list.length);
            strArr.forEach(function(fileName) {
                ctrl.views.fileList.list.push({text: fileName});
            });
        },

        loadTree : function() {
            setTimeout(function() {
                ctrl.views.setActiveFileFunc();
                $tree.view.reLoadLeftTree();
                var data = new FormData();
                $apiRequest(
                    $globalSettings.requests.getFile,
                    data,
                    function ( response ) {
                        if(response.fileList.length > 0) {
                            ctrl.views.setFileListArrFunc(response.fileList);
                            ctrl.views.setActiveFileFunc(response.fileList[0]);
                            $tree.view.reLoadLeftTree(response.fileValue);
                        }
                    }
                );
            }, 1);
        },

        fileList:{
            list : $globalSettings.buttonsArr,
            selectedFile : {},
            change: function() {
                var data = new FormData();
                data.append( "fileName", ctrl.views.fileList.selectedFile);
                $apiRequest(
                    $globalSettings.requests.getFile,
                    data,
                    function ( response ) {
                        ctrl.views.setFileListArrFunc(response.fileList);
                        if(response.isError == true && response.fileList.length > 0) {
                            ctrl.views.setActiveFileFunc(response.fileList[0]);
                        } else {
                            ctrl.views.setActiveFileFunc(ctrl.views.fileList.selectedFile);
                        }
                        $tree.view.reLoadLeftTree(response.fileValue);

                        for (var key in $dialogs.options) {//Закрыть все диалоговые окна
                            if($dialogs.options[key].element != undefined && $($dialogs.options[key].element).is(':visible') ) {
                                $($dialogs.options[key].element).dialog("close");
                            }
                        }
                    }
                );
            },
            clickButtonAddFile: function(){
                $( ctrl.addFileElement).trigger('click');
            },
            addNewFile : function(event) {
                $dialogs.view.fileNewAdd.open(ctrl.addNewFileElement, $tree);
            },
            addFile : {
                change : function(element) {
                    var data = new FormData();
                    element = $(element);
                    if(element[0].files[0] != undefined) {
                        data.append( "fileName", element[0].files[0].name);
                        data.append( "content", element[0].files[0]);

                        $apiRequest(
                            $globalSettings.requests.addFile,
                            data,
                            function ( response ) {
                                ctrl.views.setFileListArrFunc(response.fileList);
                                ctrl.views.setActiveFileFunc(element[0].files[0].name);
                                $tree.view.reLoadLeftTree(response.fileValue);
                            }
                        );
                    }
                }
            },
            download:function() {
                $apiRequest(
                    $globalSettings.requests.downloadFile,
                    '?fileName=' + ctrl.views.fileList.selectedFile,
                    function ( response ) {
                    }
                );
            },
            remove:function() {
                $($dialogs.options.remove.element).dialog({resizable: false, width:'auto', title: "Удаление",
                    buttons: [
                        { text: "Да", icons: {primary: "ui-icon-check"},
                            click: function() {
                                var data = new FormData();
                                data.append( "fileName", ctrl.views.fileList.selectedFile);
                                $apiRequest(
                                    $globalSettings.requests.removeFile,
                                    data,
                                    function ( response ) {
                                        ctrl.views.setFileListArrFunc(response.fileList);
                                        ctrl.views.loadTree();
                                         }
                                );
                                $( this ).dialog( "close" );
                            }
                        },
                        { text: "Нет", icons: {primary: "ui-icon-cancel"},
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right top", at: "right bottom", of: $(ctrl.deletFileElement)}
                });


            }
        },
		splitter : $splitter
	}

}]);