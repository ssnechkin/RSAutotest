"use strict";

/**
 * @ngdoc
 * @name RSAutotest:$contextMenu
 * @description
 *
 * Устанавливает объект для контекстного меню
**/
app.factory('$contextMenu', ['$senderTree', '$globalSettings', '$dialogs', function($senderTree, $globalSettings, $dialogs) {
    var fact = this;
    var view = {
        suiteAndTest : function(itemData, itemPath, itemTree, itemEvent) {
            var result = {
                items : {
                    'suiteEdit' : {
                        'label' : 'Редактировать',
                        'action' : function (data, path, tree) {
                            $dialogs.view.suiteEdit.open(itemEvent, data, path, tree);
                        }
                    },
                    'testEdit' : {
                        'label' : 'Редактировать',
                        'action' : function (data, path, tree) {
                            $dialogs.view.testEdit.open(itemEvent, data, path, tree);
                        }
                    },
                    'cut' : {
                        'label' : 'Вырезать',
                        'action' : function (data, path, tree) {
                            $senderTree.remove(path);
                            tree.cut(path);
                        }
                    },
                    'copy' : {
                        'label' : 'Копировать',
                        'action' : function (data, path, tree) {
                            tree.copy(path);
                        }
                    },
                    'past' : {
                        'label' : 'Вставить',
                        "_disabled" : function (data, path, tree) {
                            var isTestOrSuite = ( // запрет вставки набора под тест
                                data.test != undefined
                                && ((tree.getOptions().cutObject != undefined && tree.getOptions().cutObject.suite != undefined)
                                    || (tree.getOptions().copyPath != undefined && tree.rsTreeObject.getObject(tree.getOptions().copyPath()).suite != undefined)
                                )
                            );
                            return (tree.getOptions().copyPath == undefined && tree.getOptions().cutObject == undefined)
                                || isTestOrSuite;
                        },
                        'action' : function (data, path, tree) {
                            if( // вставка теста под набор
                                data.suite != undefined
                                && (
                                (tree.getOptions().cutObject != undefined && tree.getOptions().cutObject.test != undefined)
                                    || (tree.getOptions().copyPath != undefined && tree.rsTreeObject.getObject(tree.getOptions().copyPath()).test != undefined)
                                )) {
                                if(tree.getOptions().cutObject != undefined) $senderTree.addSub(path, tree.getOptions().cutObject);
                                if(tree.getOptions().copyPath != undefined) $senderTree.addSub(path, tree.rsTreeObject.getObject(tree.getOptions().copyPath()));
                                tree.pastSub(path);
                            } else {
                                if(tree.getOptions().cutObject != undefined) $senderTree.add(path, tree.getOptions().cutObject);
                                if(tree.getOptions().copyPath != undefined) $senderTree.add(path, tree.rsTreeObject.getObject(tree.getOptions().copyPath()));
                                tree.past(path);
                            }
                       }
                    },
                    'pastItems' : {
                        'label' : 'Вставить',
                        "_disabled" : function (data, path, tree) {
                            var isTestOrSuite = ( // запрет вставки теста под набор
                                data.suite != undefined
                                && (
                                    (tree.getOptions().cutObject != undefined && tree.getOptions().cutObject.test != undefined)
                                    || (tree.getOptions().copyPath != undefined && tree.rsTreeObject.getObject(tree.getOptions().copyPath()).test != undefined)
                                )
                            ) || ( // запрет вставки набора под тест
                                data.test != undefined
                                && (
                                    (tree.getOptions().cutObject != undefined && tree.getOptions().cutObject.suite != undefined)
                                    || (tree.getOptions().copyPath != undefined && tree.rsTreeObject.getObject(tree.getOptions().copyPath()).suite != undefined)
                                )
                            );
                           return (tree.getOptions().copyPath == undefined && tree.getOptions().cutObject == undefined) || isTestOrSuite;
                        },
                        'submenu' : {
                            'past' : {
                                'label' : 'Вставить',
                                "_disabled" : function (data, path, tree) {
                                    var isTestOrSuite = ( // запрет вставки теста под набор
                                        data.suite != undefined
                                        && (
                                            (tree.getOptions().cutObject != undefined && tree.getOptions().cutObject.test != undefined)
                                            || (tree.getOptions().copyPath != undefined && tree.rsTreeObject.getObject(tree.getOptions().copyPath()).test != undefined)
                                        )
                                    ) || ( // запрет вставки набора под тест
                                        data.test != undefined
                                        && (
                                            (tree.getOptions().cutObject != undefined && tree.getOptions().cutObject.suite != undefined)
                                            || (tree.getOptions().copyPath != undefined && tree.rsTreeObject.getObject(tree.getOptions().copyPath()).suite != undefined)
                                        )
                                    );
                                    return (tree.getOptions().copyPath == undefined && tree.getOptions().cutObject == undefined) || isTestOrSuite;
                                },
                                'action' : function (data, path, tree) {
                                    if(tree.getOptions().cutObject != undefined) $senderTree.add(path, tree.getOptions().cutObject);
                                    if(tree.getOptions().copyPath != undefined) $senderTree.add(path, tree.rsTreeObject.getObject(tree.getOptions().copyPath()));
                                    tree.past(path);
                                }
                            },
                            'pastSub' : {
                                'label' : 'Вставить в тест',
                                "_disabled" : function (data, path, tree) {
                                    var isTestOrSuite = ( // запрет вставки набора под тест
                                        data.test != undefined && (
                                            (tree.getOptions().cutObject != undefined && tree.getOptions().cutObject.suite != undefined)
                                            || (tree.getOptions().copyPath != undefined && tree.rsTreeObject.getObject(tree.getOptions().copyPath()).suite != undefined)
                                        )
                                    );
                                    return (tree.getOptions().copyPath == undefined && tree.getOptions().cutObject == undefined)
                                        || isTestOrSuite;
                                },
                                'action' : function (data, path, tree) {
                                    if(tree.getOptions().cutObject != undefined) $senderTree.addSub(path, tree.getOptions().cutObject);
                                    if(tree.getOptions().copyPath != undefined) $senderTree.addSub(path, tree.rsTreeObject.getObject(tree.getOptions().copyPath()));
                                    tree.pastSub(path);
                                }
                            }
                        }
                    },
                    'add' : {
                        'label' : 'Добавить',
                        'submenu' : {
                            'add_suite' : {
                                'label' : 'Набор',
                                'action' : function (data, path, tree) {
                                    $dialogs.view.suiteAdd.open(itemEvent, data, path, tree);
                                }
                            },
                            'add_test' : {
                                'label' : 'Тест',
                                'action' : function (data, path, tree) {
                                    $dialogs.view.testAdd.open(itemEvent, data, path, tree);
                                }
                            },
                            'add_sub_test' : {
                                'label' : 'Тест в тест',
                                'action' : function (data, path, tree) {
                                    $dialogs.view.testAddInTest.open(itemEvent, data, path, tree);
                                }
                            },
                            'add_test_in_suite' : {
                                'label' : 'Тест',
                                'action' : function (data, path, tree) {
                                    $dialogs.view.testAddInSuite.open(itemEvent, data, path, tree);
                                }
                            },
                            'add_step_in_test' : {
                                'label' : 'Шаг',
                                'action' : function (data, path, tree) {
                                    $dialogs.view.stepAddInTest.open(itemEvent, data, path, tree);
                                }
                            },
                        }
                    },
                    'executeSuite' : {
                        'label' : 'Выполнить',
                        "_disabled" : function (data, path, tree) {
                            return data.shortName == undefined || data.shortName.length == 0;
                        },
                        'action' : function (data, path, tree) {
                            $senderTree.executeSuite(data.shortName, function(response) {
                                $("<center>" + data.suite + "<hr>" + response + "</center>").dialog({resizable: true, width:'auto', height: 'auto', title: "Результат запроса (" + data.shortName + ")"});
                            });
                        }
                    },
                    'executeTest' : {
                        'label' : 'Выполнить',
                        "_disabled" : function (data, path, tree) {
                            return data.shortName == undefined || data.shortName.length == 0;
                        },
                        'action' : function (data, path, tree) {
                            $senderTree.executeTest(data.shortName, function(response) {
                                $("<center>" + data.test + "<hr>" + response + "</center>").dialog({resizable: true, width:'auto', height: 'auto', title: "Результат запроса (" + data.shortName + ")"});
                            });
                        }
                    },
                    'delete' : {
                        'label' : 'Удалить',
                        'action' : function (data, path, tree) {
                            $dialogs.view.remove.open(itemEvent, data, path, tree);
                        }
                    }
                }
            }

            if(itemData.suite != undefined) {
                delete result.items.add.submenu.add_test;
                delete result.items.add.submenu.add_sub_test;
                delete result.items.pastItems;
                delete result.items.testEdit;
                delete result.items.executeTest;
                delete result.items.add.submenu.add_step_in_test;
            }
            if(itemData.test != undefined) {
                delete result.items.add.submenu.add_suite;
                delete result.items.add.submenu.add_test_in_suite;
                delete result.items.past;
                delete result.items.suiteEdit;
                delete result.items.executeSuite;
            }
            return result;
        },

        stepAndKey : function(itemData, itemPath, itemTree, itemEvent) {
            var result = {
                items : {
                    'stepEdit' : {
                        'label' : 'Редактировать',
                        'action' : function (data, path, tree) {
                            $dialogs.view.stepEdit.open(itemEvent, data, path, tree);
                        }
                    },
                    'cut' : {
                        'label' : 'Вырезать',
                        'action' : function (data, path, tree) {
                            $senderTree.remove(path);
                            tree.cut(path);
                        }
                    },
                    'copy' : {
                        'label' : 'Копировать',
                        'action' : function (data, path, tree) {
                            tree.copy(path);
                        }
                    },
                    'past' : {
                        'label' : 'Вставить',
                        "_disabled" : function (data, path, tree) {
                            return (tree.getOptions().copyPath == undefined
                                && tree.getOptions().cutObject == undefined);
                        },
                        'action' : function (data, path, tree) {
                            if(tree.getOptions().cutObject != undefined) $senderTree.add(path, tree.getOptions().cutObject);
                            if(tree.getOptions().copyPath != undefined) $senderTree.add(path, tree.rsTreeObject.getObject(tree.getOptions().copyPath()));
                            tree.past(path);
                        }
                    },
                    'add' : {
                        'label' : 'Добавить',
                        'submenu' : {
                            'addStep' : {
                                'label' : 'Шаг',
                                "_disabled" : function (data, path, tree) {
                                    return false;
                                },
                                'action' : function (data, path, tree) {
                                    $dialogs.view.stepAdd.open(itemEvent, data, path, tree);
                                }
                            },
                            'addSubStep' : {
                                'label' : 'Шаг в ветку',
                                "_disabled" : function (data, path, tree) {
                                    return false;
                                },
                                'action' : function (data, path, tree) {
                                  $dialogs.view.stepAddInStep.open(itemEvent, data, path, tree);
                                }
                            }
                        }
                    },
                    'delete' : {
                        'label' : 'Удалить',
                        'action' : function (data, path, tree) {
                            $dialogs.view.remove.open(itemEvent, data, path, tree);
                        }
                    }
                }
            }

            return result;
        }
    };

    return view;
}]);