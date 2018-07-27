"use strict";

/**
 * @ngdoc
 * @name RSAutotest:$tree
 * @description
 *
 * Формирует дерево
**/
app.factory('$tree', ['$globalSettings', '$contextMenu', '$senderTree', function($globalSettings, $contextMenu, $senderTree) {
	this.init = function() {
	}

	var view = {
	    leftTreeNameElement : "",
        rightTreeNameElement : "",

	    /**** Возвращает путь теста связанного по наименованию шага ****/
        getStepAssociatedTestPath : function(stepName, treeListObject, pathObject) {
            var path = pathObject + (pathObject.length > 0 ? '.' : '');
            var resultObj;
            for (var i = 0; i < treeListObject.length; i++) {
                if(treeListObject[i].shortName == stepName) return path + i;
                if(treeListObject[i][$globalSettings.tree.treeRight.nameChildrenList] != undefined && treeListObject[i][$globalSettings.tree.treeRight.nameChildrenList].length > 0) {
                    resultObj = view.getStepAssociatedTestPath(stepName, treeListObject[i][$globalSettings.tree.treeRight.nameChildrenList], path + i);
                    if(resultObj != undefined) return resultObj;
                }
            }
            return;
        },

	    clickFunc : function(element, tree) {
	        var viewF = function(object, options, path) {
                return (object.step == undefined ? '' : object.step)
                        + (object.key == undefined ? '' : ' ' + object.key)
                        + ((object.beginCycle != undefined && object.beginCycle) ? ' Начало цикла' : '')
                        + ((object.endCycle != undefined && object.endCycle) ? ' Конец цикла' : '')
                        + (object.value == undefined ? '' : ' (' + object.value+ ')');
	        }
	        var skipViewF = function(object, path) {
	            var parentPath = $(element).attr("path");
	            //console.log( parentPath + "|" + path + "|" + JSON.stringify(object) );
	            return path.indexOf(parentPath) == 0
	        }
	        var skipChildrenListF = function(object, path) {
	            var parentPath = $(element).attr("path");
	            return  path == parentPath
	                    || parentPath.indexOf(path) == 0
	                    || (path.indexOf(parentPath) == 0 && object.test == undefined)

	        }
	        var anotherF = function(object, path) {
                if(object.step != undefined)
                setTimeout(function() {
                    var associatedTestPath = view.getStepAssociatedTestPath(object.step, $(view.leftTreeNameElement).rstree("getObject")[$globalSettings.tree.treeRight.nameChildrenList], '');

                    if (associatedTestPath != undefined) {
                        var testObject = $(view.leftTreeNameElement).rstree("getObject", associatedTestPath);
                        if (testObject[$globalSettings.tree.treeRight.nameChildrenList] != undefined && testObject[$globalSettings.tree.treeRight.nameChildrenList].length > 0) {
                            $(view.rightTreeNameElement).find('[path="' + path + '"]').each(function (index, el){
                                var element = el;
                                if(element != null) {
                                    if(element.innerHTML.indexOf('ТЕСТ: ') != 0) {
                                        if( object.state != undefined && object.state.opened == true) {
                                            $(element).parent().find('i').addClass("rsTree-i-group-open");
                                        } else {
                                            $(element).parent().find('i').addClass("rsTree-i-group-close");
                                        }

                                        element.innerHTML = 'ТЕСТ: ' + element.innerHTML + '';
                                        element = element.parentElement;
                                        element.classList.add("associatedTestClass");

                                        $(view.rightTreeNameElement).rstree({
                                            skipChildrenListFunc : function(object, path, parentPath) {
                                                return true;
                                            },
                                            skipViewFunc : function(object, path, parentPath) {
                                                return true;
                                            }
                                        });
                                        if(element.innerHTML.indexOf('<ul') != -1) {
                                            $(element).find('ul').first().find('*[path]').each(function(ind, elm) {
                                                $(elm).addClass('willNotBeExecuted');
                                                $(elm).parent().removeClass("rsTree-li");
                                                $(elm).parent().find('i').removeClass("rsTree-i");
                                                $(elm).parent().find('i').removeClass("rsTree-i-group-open");
                                                $(elm).parent().find('i').removeClass("rsTree-i-group-close");
                                            });
                                            $(element).find('ul').first().prepend($(view.rightTreeNameElement).rstree("formATree", testObject[$globalSettings.tree.treeRight.nameChildrenList], associatedTestPath));
                                        } else {
                                            element.innerHTML = element.innerHTML + '<ul>' + $(view.rightTreeNameElement).rstree("formATree", testObject[$globalSettings.tree.treeRight.nameChildrenList], associatedTestPath) + '</ul>';
                                        }
                                        $(view.rightTreeNameElement).rstree("initEvents");
                                        /*$(view.rightTreeNameElement).rstree({
                                            skipChildrenListFunc : skipChildrenListF,
                                            skipViewFunc : skipViewF
                                        });*/
                                    }
                                }
                            });
                        }
                    }
                }, 1);
            }

            //var chld = $globalSettings.tree.treeRight.nameChildrenList;
	        //var data = {};
	        //data[chld] = $(view.leftTreeNameElement).rstree("getObject", $(element).attr("path")).list;

            $(view.rightTreeNameElement).rstree("destroy");
            $(view.rightTreeNameElement).rstree({
                //treeData : data,
                //parentPath : $(element).attr("path"),
                nameChildrenList : $globalSettings.tree.treeRight.nameChildrenList,
                listRetrieveText : $globalSettings.tree.treeRight.listRetrieveText,
                viewFunc : viewF,
                skipChildrenListFunc : skipChildrenListF,
                skipViewFunc : skipViewF,
                handlerClickFunc : function(tElement, tree) {
                    /*var path = $(tElement).attr("path");
                    var object = tree.rsTreeObject.getObject(path);
                    $senderTree.update(path, object);*/
                },
                handlerDoubleClickFunc : function(tElement, tree) {
                    /*var path = $(tElement).attr("path");
                    var object = tree.rsTreeObject.getObject(path);
                    $senderTree.update(path, object);*/
                },
                handlerSelectRemove : function(tElement, tree) {
                    /*var path = $(tElement).attr("path");
                    var object = tree.rsTreeObject.getObject(path);
                    $senderTree.update(path, object);*/
                },
                addClassLiFunc : function(object) {
                    if(object.notRun) return "not-run";
                    return "";
                },
                anotherFunc : anotherF,
                plugins : ['contextmenu'],
                contextmenu : $contextMenu.stepAndKey
            });

            /*var path = $(element).attr("path");
            var object = tree.rsTreeObject.getObject(path);
            $senderTree.update(path, object);*/
	    },

	    reLoadLeftTree : function(data) {
	        $(view.rightTreeNameElement).rstree("destroy");
	        $(view.leftTreeNameElement).rstree("destroy");
	        if(data != undefined) {
                $(view.leftTreeNameElement).rstree({
                    treeData: data,
                    nameChildrenList : $globalSettings.tree.treeLeft.nameChildrenList,
                    listRetrieveText : $globalSettings.tree.treeLeft.listRetrieveText,
                    handlerClickFunc : view.clickFunc,
                    viewFunc : function(object, options) {
                        var result = '';
                        for (var x = 0; x < options.listRetrieveText.length; x++) {
                            if (object[options.listRetrieveText[x]] != undefined) {
                                result += ' ' + object[options.listRetrieveText[x]];
                                if (object.shortName != undefined && object.shortName.length > 0) {
                                    result += ' (' + object.shortName + ")";
                                }
                                if (object.value != undefined) {
                                    result += ' Данные из: ' + object.value + "";
                                }
                            }
                        }
                        return result;
                    },
                    addClassLiFunc : function(object) {
                        if(object.notRun) return "not-run";
                        return "";
                    },
                    handlerDoubleClickFunc : function(tElement, tree) {
                        /*var path = $(tElement).attr("path");
                        var object = tree.rsTreeObject.getObject(path);
                        $senderTree.update(path, object);*/
                    },
                    /*handlerSelectRemove : function(tElement, tree) {
                        var path = $(tElement).attr("path");
                        var object = tree.rsTreeObject.getObject(path);
                        $senderTree.update(path, object);
                    },*/
                    plugins : ['contextmenu'],
                    contextmenu : $contextMenu.suiteAndTest
                });
		    }
	    }
	}

	return {
		init : this.init,
		view : view
	}
}]);