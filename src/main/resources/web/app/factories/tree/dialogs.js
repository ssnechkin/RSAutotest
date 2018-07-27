"use strict";

/**
 * @ngdoc
 * @name RSAutotest:$dialogs
 * @description
 *
 * Формирует диалоговые окна
**/
app.factory('$dialogs', ['$globalSettings', '$senderTree', function($globalSettings, $senderTree) {
    var fact = this;

    var options = {
        ctrlScope: {},
        listMethds: {},

        fileNewAdd: { element: "", fileName: ""},
        
        suiteEdit: { element: "", data: {}},
        suiteAdd: { element: "", data: {}},

        testAddInSuite: { element: "", data: {}},
        testEdit: { element: "", data: {}},
        testAdd: { element: "", data: {}},
        testAddInTest: { element: "", data: {}},
        
        stepEdit: { element: "", data: {}, listSteps:{}, step:{}, flag: "", stepType: ""},
        stepAdd: { element: "", data: {}, listSteps:{}, step:{}, flag: "", stepType: "" },
        stepAddInStep: { element: "", data: {}, listSteps:{}, step:{}, flag: "", stepType: ""},
        stepAddInTest: { element: "", data: {}, listSteps:{}, step:{}, flag: "", stepType: ""},

        remove: { element: "", data: {}},

        testDepending: {},
        testDependingStatus: "",
        tests: [] // массив для заполнения списка зависимых тестов.
    }

    var view = {
        addDepending: function() {
            if(options.testDepending != undefined && options.testDepending.length > 2 && options.testDependingStatus!= undefined && options.testDependingStatus.length > 0) {
                var obj = JSON.parse(options.testDepending);
                obj.status = options.testDependingStatus;
                obj.statusDescription = view._getStatusName(options.testDependingStatus);
                options.testEdit.data.dependingOnTheTestsList.push(obj);
            }
        },

        _getStatusName: function(status) {
           switch(status) {
             case 'RUN': return "Выполняется";
             case 'COMPLETED': return "Завершён";
             case 'SUCCESSFUL': return "Выполнен успешно";
             case 'FAILURE': return "Выполнен с ошибкой";
             case 'BROKEN': return "Сломан";
             case 'CANCELLED': return "Отменён";
             default: return "";
           }
        },

        // Обновить список тестов в массиве tests
        updateTestList: function(suiteName, suiteShortName) {
            options.tests = [];
            view._addInTests($($globalSettings.tree.treeLeft.nameElementId).rstree("getObject").list, suiteName, suiteShortName, false);
        },

        // Рекурсивно заполняет массив tests тестами из набора
        _addInTests: function(list, suiteName, suiteShortName, suitePress) {
            for (var i = 0; i < list.length; i++) {
                if(list[i].suite != undefined) {
                    if(list[i].suite == suiteName && ((list[i].shortName == undefined && suiteShortName == undefined)
                        || (list[i].shortName != undefined && suiteShortName != undefined && list[i].shortName == suiteShortName))) {
                        suitePress = true;
                    } else {
                        suitePress = false;
                    }
                }
                if(list[i].test != undefined && !list[i].notRun && suitePress) {
                    options.tests.push({name: list[i].test, shortName: list[i].shortName, value: list[i].value});
                }

                if(list[i].list != undefined) {
                    view._addInTests(list[i].list, suiteName, suiteShortName, suitePress);
                }
            }
        },

        // Рекурсивно выполняет поиск и возвращает объект содержащий наименование набора (name, shortName)
        _getSuite: function(list, testObject, object) {
            for (var i = 0; i < list.length; i++) {
                if(list[i].suite != undefined) {
                   object.name = list[i].suite;
                   object.shortName = list[i].shortName;
                }

                if(list[i].test != undefined && JSON.stringify(list[i]) == JSON.stringify(testObject)) {
                    return object;
                }

                if(list[i].list != undefined) {
                   var r = view._getSuite(list[i].list, testObject, object);
                   if(r != undefined && r.name != undefined) return r;
                }
            }
            return undefined;
        },

        closeAllDialogs: function() {
            /*for (var key in options) {//Закрыть все диалоговые окна
                if(options[key].element != undefined && $(options[key].element).is(':visible') ) {
                    $(options[key].element).dialog("close");
                }
            }*/
        },

        updateLists: function(optName) {
            //options[optName].listSteps = {};
            //options[optName].step = {};
            for(var method in options.listMethds) {
                for(var step in options.listMethds[method].list) {
                    if(options.listMethds[method].list[step].name == options[optName].data.step) {
                        options[optName].listSteps = options.listMethds[method];
                        options[optName].step = options.listMethds[method].list[step];
                        options[optName].listSteps.list = options[optName].listSteps.list.sort(view.compare);
                    }
                }
            }
        },

        compare: function(a, b) {
            if (a.name < b.name) {
                return -1;
            }
            if (a.name > b.name) {
                return 1;
            }
            return 0;
        },

        updateFlags: function(optName) {
            options[optName].data["notRun"] = false;
            options[optName].data["skipBagButTestFailed"] = false;
            options[optName].data["skipBag"] = false;
            options[optName].data[options[optName].flag] = true;
        },

        cloneDR: function(obj) {
            if(obj == null || typeof(obj) != 'object')
                return obj;
            var temp = new obj.constructor();
            for(var key in obj)
                temp[key] = view.cloneDR(obj[key]);
            return temp;
        },

        fileNewAdd: {
            open: function(parentElement, tree) {
                options.fileNewAdd.fileName = "";
                $(options.fileNewAdd.element).dialog({resizable: false, width:'auto', title: "Создание файла",
                    buttons: [
                        { text: "Создать", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if(options.fileNewAdd.fileName != undefined && options.fileNewAdd.fileName.length > 0) {
                                    $senderTree.createFile(options.fileNewAdd.fileName, {"list":[{"suite":"Набор тестов","shortName":"Краткое наименование набора","list":[{"test":"Тест","shortName":"Краткое наименование теста","value":"Краткое наименование теста с данными или путь к файлу","list":[{"step":"Шаг","key":""}]}]}]},
                                     tree, options.ctrlScope.ctrl);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() {
                                $( this ).dialog( "close" );
                            }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(parentElement)}
                });
            }
        },

        suiteEdit: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.suiteEdit.data = view.cloneDR(data);
                options.ctrlScope.$apply();
                $(options.suiteEdit.element).dialog({resizable: false, width:'auto', title: "Редактирование набора",
                    buttons: [
                        { text: "Сохранить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if( $("*").find(event.target).length > 0
                                    && options.suiteEdit.data.suite != undefined && options.suiteEdit.data.suite.length > 0) {
                                    var path = $(event.target).attr("path");
                                    $senderTree.update(path, options.suiteEdit.data);
                                    tree.update(path, options.suiteEdit.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() {
                                $( this ).dialog( "close" );
                            }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            }
        },
        suiteAdd: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.suiteAdd.data = {};
                options.ctrlScope.$apply();
                $(options.suiteAdd.element).dialog({resizable: false, width:'auto', title: "Добавление набора",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0 && options.suiteAdd.data.suite != undefined && options.suiteAdd.data.suite.length > 0) {
                                    var path = $(event.target).attr("path");
                                    $senderTree.add(path, options.suiteAdd.data);
                                    tree.add(path, options.suiteAdd.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() {
                                $( this ).dialog( "close" );
                            }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            }
        },
        suiteAddInFile: {
            open: function(parentElement) {
                options.suiteAdd.data = {};
                //options.ctrlScope.$apply();
                $(options.suiteAdd.element).dialog({resizable: false, width:'auto', title: "Добавление первого набора в файл",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if(options.suiteAdd.data.suite != undefined && options.suiteAdd.data.suite.length > 0) {
                                    $senderTree.add('', options.suiteAdd.data);
                                    options.ctrlScope.ctrl.views.fileList.change();
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() {
                                $( this ).dialog( "close" );
                            }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(parentElement)}
                });
            }
        },
        testAddInSuite: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.testAddInSuite.data = {startupDependencyOnParent: "COMPLETED"};
                options.ctrlScope.$apply();
                $(options.testAddInSuite.element).dialog({resizable: false, width:'auto', title: "Добавление теста в набор",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0
                                    && options.testAddInSuite.data.test != undefined && options.testAddInSuite.data.test.length > 0) {
                                    var path = $(event.target).attr("path");
                                    $senderTree.addSub(path, options.testAddInSuite.data);
                                    tree.addSub(path, options.testAddInSuite.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            }
        },
        testEdit: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.testEdit.data = view.cloneDR(data);

                var suiteObj = view._getSuite($($globalSettings.tree.treeLeft.nameElementId).rstree("getObject").list, options.testEdit.data, {} );
                if(suiteObj != undefined) view.updateTestList(suiteObj.name, suiteObj.shortName);

                options.ctrlScope.$apply();
                $(options.testEdit.element).dialog({resizable: false, width:'auto', title: "Редактирование теста",
                    buttons: [
                        { text: "Сохранить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0
                                    && options.testEdit.data.test != undefined && options.testEdit.data.test.length > 0) {
                                    var path = $(event.target).attr("path");
                                    $senderTree.update(path, options.testEdit.data);
                                    tree.update(path, options.testEdit.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            }
        },
        testAdd: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.testAdd.data = {startupDependencyOnParent: "COMPLETED"};
                options.ctrlScope.$apply();
                $(options.testAdd.element).dialog({resizable: false, width:'auto', title: "Добавление теста",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0 && options.testAdd.data.test != undefined && options.testAdd.data.test.length > 0) {
                                    var path = $(event.target).attr("path");
                                    $senderTree.add(path, options.testAdd.data);
                                    tree.add(path, options.testAdd.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            }
        },
        testAddInTest: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.testAddInTest.data = {startupDependencyOnParent: "COMPLETED"};
                options.ctrlScope.$apply();
                $(options.testAddInTest.element).dialog({resizable: false, width:'auto', title: "Добавление теста в тест",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0 && options.testAddInTest.data.test != undefined && options.testAddInTest.data.test.length > 0) {
                                    var path = $(event.target).attr("path");
                                    $senderTree.addSub(path, options.testAddInTest.data);
                                    tree.addSub(path, options.testAddInTest.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            }
        },
        stepDataUpdate: function(optName) {
            if(options[optName].data["value"] == "") delete options[optName].data["value"];

            if(options[optName].stepType == "step") {
                delete options[optName].data["numberIterationsCycle"];
                delete options[optName].data["runTimeCycleMilliseconds"];
                delete options[optName].data["beginCycle"];
                delete options[optName].data["endCycle"];
            }

            if(options[optName].stepType == "beginCycle") {
                options[optName].data["beginCycle"] = true;
                delete options[optName].data["endCycle"];
                delete options[optName].data["step"];
                delete options[optName].data["key"];
                delete options[optName].data["value"];
                delete options[optName].data["notRun"];
                delete options[optName].data["skipBagButTestFailed"];
                delete options[optName].data["skipBag"];
            }

            if(options[optName].stepType == "endCycle") {
                options[optName].data["endCycle"] = true;
                delete options[optName].data["beginCycle"];
                delete options[optName].data["step"];
                delete options[optName].data["key"];
                delete options[optName].data["value"];
                delete options[optName].data["timeoutMilliseconds"];
                delete options[optName].data["numberIterationsCycle"];
                delete options[optName].data["runTimeCycleMilliseconds"];
                delete options[optName].data["notRun"];
                delete options[optName].data["skipBagButTestFailed"];
                delete options[optName].data["skipBag"];
            }
        },
        stepEdit: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.stepEdit.data = view.cloneDR(data);
                options.stepEdit.flag = "run";
                if(options.stepEdit.data["notRun"]) options.stepEdit.flag = "notRun";
                if(options.stepEdit.data["skipBagButTestFailed"]) options.stepEdit.flag = "skipBagButTestFailed";
                if(options.stepEdit.data["skipBag"]) options.stepEdit.flag = "skipBag";

                options.stepEdit.stepType = "step";
                if(options.stepEdit.data["beginCycle"]) options.stepEdit.stepType = "beginCycle";
                if(options.stepEdit.data["endCycle"]) options.stepEdit.stepType = "endCycle";

                view.updateLists("stepEdit");
                options.ctrlScope.$apply();

                $(options.stepEdit.element).dialog({resizable: false, width:'auto', title: "Редактирование шага",
                    buttons: [
                        { text: "Сохранить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0 //элемент присутсвует на странице
                                    && ( (options.stepEdit.stepType == "step" && ( (options.stepEdit.data.step != undefined && options.stepEdit.data.step.length > 0) || (options.stepEdit.data.key != undefined && options.stepEdit.data.key.length > 0) ) )
                                        || options.stepEdit.stepType == "beginCycle"
                                        || options.stepEdit.stepType == "endCycle"
                                       )
                                    ) {
                                    view.stepDataUpdate("stepEdit");
                                    view.updateFlags("stepEdit");
                                    var path = $(event.target).attr("path");
                                    $senderTree.update(path, options.stepEdit.data);
                                    tree.update(path, options.stepEdit.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            },
            stepChenge: function() {
                if(options.stepEdit.step != undefined)
                    options.stepEdit.data.step = view.cloneDR({name: options.stepEdit.step.name}).name;
            }
        },
        stepAdd: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.stepAdd.data = {};
                options.stepAdd.stepType = "step";
                options.stepAdd.flag = "run";
                view.updateLists("stepAdd");
                options.ctrlScope.$apply();
                $(options.stepAdd.element).dialog({resizable: false, width:'auto', title: "Добавление шага",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0 //элемент присутсвует на странице
                                    && ( (options.stepAdd.stepType == "step" && ( (options.stepAdd.data.step != undefined && options.stepAdd.data.step.length > 0) || (options.stepAdd.data.key != undefined && options.stepAdd.data.key.length > 0) ) )
                                        || options.stepAdd.stepType == "beginCycle"
                                        || options.stepAdd.stepType == "endCycle"
                                       )
                                    ) {
                                    var path = $(event.target).attr("path");
                                    view.stepDataUpdate("stepAdd");
                                    view.updateFlags("stepAdd");
                                    $senderTree.add(path, options.stepAdd.data);
                                    tree.add(path, options.stepAdd.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            },
            stepChenge: function() {
                if(options.stepAdd.step != undefined)
                    options.stepAdd.data.step = view.cloneDR({name: options.stepAdd.step.name}).name;
            }
        },
        stepAddInStep: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.stepAddInStep.data = {};
                options.stepAddInStep.stepType = "step";
                options.stepAddInStep.flag = "run";
                view.updateLists("stepAddInStep");
                options.ctrlScope.$apply();
                $(options.stepAddInStep.element).dialog({resizable: false, width:'auto', title: "Добавление шага в ветку",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0 //элемент присутсвует на странице
                                    && ( (options.stepAddInStep.stepType == "step" && ( (options.stepAddInStep.data.step != undefined && options.stepAddInStep.data.step.length > 0) || (options.stepAddInStep.data.key != undefined && options.stepAddInStep.data.key.length > 0) ) )
                                         || options.stepAddInStep.stepType == "beginCycle"
                                         || options.stepAddInStep.stepType == "endCycle"
                                        )
                                    ) {
                                    var path = $(event.target).attr("path");
                                    view.stepDataUpdate("stepAddInStep");
                                    view.updateFlags("stepAddInStep");
                                    $senderTree.addSub(path, options.stepAddInStep.data);
                                    tree.addSub(path, options.stepAddInStep.data);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            },
            stepChenge: function() {
                if(options.stepAddInStep.step != undefined)
                    options.stepAddInStep.data.step = view.cloneDR({name: options.stepAddInStep.step.name}).name;
            }
        },
        stepAddInTest: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.stepAddInTest.data = {};
                options.stepAddInTest.stepType = "step";
                options.stepAddInTest.flag = "run";
                view.updateLists("stepAddInTest");
                options.ctrlScope.$apply();
                $(options.stepAddInTest.element).dialog({resizable: false, width:'auto', title: "Добавление шага в тест",
                    buttons: [
                        { text: "Добавить", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if($("*").find(event.target).length > 0 //элемент присутсвует на странице
                                && ( (options.stepAddInTest.stepType == "step" && ( (options.stepAddInTest.data.step != undefined && options.stepAddInTest.data.step.length > 0) || (options.stepAddInTest.data.key != undefined && options.stepAddInTest.data.key.length > 0) ) )
                                     || options.stepAddInTest.stepType == "beginCycle"
                                     || options.stepAddInTest.stepType == "endCycle"
                                    )
                                ) {
                                    var path = $(event.target).attr("path");
                                    view.stepDataUpdate("stepAddInTest");
                                    view.updateFlags("stepAddInTest");
                                    $senderTree.addSub(path, options.stepAddInTest.data);
                                    tree.rsTreeObject.addSub(path, options.stepAddInTest.data);
                                    $(event.target).trigger('click');
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Отменить", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            },
            stepChenge: function() {
                if(options.stepAddInTest.step != undefined)
                    options.stepAddInTest.data.step = view.cloneDR({name: options.stepAddInTest.step.name}).name;
            }
        },
        remove: {
            open: function( event, data, path, tree) {
                view.closeAllDialogs();
                options.ctrlScope.$apply();
                $(options.remove.element).dialog({resizable: false, width:'auto', title: "Удаление",
                    buttons: [
                        { text: "Да", icons: {primary: "ui-icon-check"}, class: "ui-button-left",
                            click: function() {
                                if( $("*").find(event.target).length > 0) {
                                    var path = $(event.target).attr("path");
                                    $senderTree.remove(path);
                                    tree.remove(path);
                                    $( this ).dialog( "close" );
                                }
                            }
                        },
                        { text: "Нет", icons: {primary: "ui-icon-cancel"}, class: "ui-button-right",
                            click: function() { $( this ).dialog( "close" ); }
                        }
                    ],
                    position: {my: "right bottom", at: "right bottom", of: $(event.target)}
                });
            }
        }
    };

    return {
        options: options,
        view: view,
        globalSettings: $globalSettings
    }
}]);