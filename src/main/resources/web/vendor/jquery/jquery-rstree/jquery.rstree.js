;(function( $ ) {
    'use strict';
    String.prototype.rsReplaceAll = function(search, replace) {return this.split(search).join(replace);};

    var defaultOptions = {
        treeData : {
            text : 'item1',
            children : [
                {text : 'item2'},
                {text : 'item3'}
            ]
        },
        parentPath : undefined,
        rsUpdateElements : [],
        nameChildrenList : 'children',
        listRetrieveText : ['text'],
        plugins : [],
        contextmenu : {
            items : {
                'item1' : {
                    'label' : 'Cut',
                    'action' : function (data, path, tree) {
                        tree.cut(path);
                    }
                },
                'item2' : {
                    'label' : 'Copy',
                    'action' : function (data, path, tree) {
                        tree.copy(path);
                    }
                },
                'item3' : {
                    'label' : 'Past',
                    "_disabled" : function (data, path, tree) {
                        return (tree.getOptions().copyPath == undefined
                            && tree.getOptions().cutObject == undefined);
                    },
                    'action' : function (data, path, tree) {
                        tree.past(path);
                    }
                },
                'item4' : {
                    'label' : 'Node',
                    'submenu' : {
                        'item4_1' : {
                            'label' : 'Add new node',
                            'action' : function (data, path, tree) {
                                var obj = {};
                                obj[tree.getOptions().listRetrieveText[0]] = 'Node name';
                                tree.addSub(path, obj);
                            }
                        }
                    }
                },
                'item5' : {
                    'label' : 'Delete',
                    'action' : function (data, path, tree) {
                        tree.remove(path);
                    }
                }
            }
        },
        skipChildrenListFunc : function(object, path, parentPath) {
            return true;
        },
        skipViewFunc : function(object, path, parentPath) {
            return true;
        },
        addClassLiFunc : function(object, path, parentPath) {
            return "";
        },
        anotherFunc : function(object, path, parentPath) {
        },
        viewFunc : function(object, options, path, parentPath) {
            return object[options.listRetrieveText[0]];
        },
        handlerClickFunc : undefined,
        handlerDoubleClickFunc : function() {
        },
        handlerSelectRemove : function() {
        },
        handlerSelectAdd : function() {
        }
    };
    var rsTree;
    var rsTreeThis;
    var rsTreeGlobalOptions;
    var methods = {

        init : function( initOptions ) {
            return this.each(function() {
                if($(this).data('rstree') == undefined || $(this).data('rstree').rsTree == undefined) {
                    var $this = $(this),
                        data = $this.data('rstree'),
                        rstree = $('<div />', {
                            text : $this.attr('title')
                        });

                    if ( !data ) {
                        $(this).data('rstree', {
                            target : $this,
                            rstree : rstree
                        });
                    }
                    $(rsTreeThis).addClass('rs-tree');
                    rsTree = new RSTree();
                    $(this).data('rstree').rsTree = rsTree;

                    rsTree.setOptions(defaultOptions);
                    rsTree.setOptions({
                        viewFunc: function(object, options) {
                            var result = '';
                            for (var x = 0; x < options.listRetrieveText.length; x++) {
                                if (object[options.listRetrieveText[x]] != undefined) {
                                result += ' ' + object[options.listRetrieveText[x]];
                                }
                            }
                            return result;
                        }
                    });
                    rsTree.setOptions(initOptions);
                    if(rsTreeGlobalOptions != undefined && rsTreeGlobalOptions.rsTreeElementsArray.length == 0) {
                        rsTreeGlobalOptions = undefined;
                    }
                    if(rsTreeGlobalOptions == undefined) {
                        rsTreeGlobalOptions = $(this).data('rstree').rsTree.getOptions();
                        rsTreeGlobalOptions.rsTreeElementsArray = [];
                        rsTreeGlobalOptions.rsTreeElementsArray.push(this);
                        $(this).data('rstree').rsTree.getOptions().rsUpdateElements = rsTreeGlobalOptions.rsTreeElementsArray;
                    } else {
                        if(initOptions != undefined && initOptions.treeData == undefined) {
                            rsTree.setOptions({treeData: rsTreeGlobalOptions.treeData});
                            rsTreeGlobalOptions.rsTreeElementsArray.push(this);
                            $(this).data('rstree').rsTree.getOptions().rsUpdateElements = rsTreeGlobalOptions.rsTreeElementsArray;
                        } else {
                            $(this).data('rstree').rsTree.getOptions().rsUpdateElements = [this];
                        }
                    }
                    var rsTreeHtml = new RSTreeHtml(rsTree.getOptions());
                    $(this).html(rsTreeHtml.formATree(rsTree.getOptions().treeData[rsTree.getOptions().nameChildrenList], rsTree.getOptions().parentPath));
                    rsTree.initEvents(this);
                } else {
                    $(this).data('rstree').rsTree.setOptions(initOptions);
                }
            });
        },

        destroy : function( ) {

            return this.each(function() {
                if($(this).data('rstree') != undefined && $(this).data('rstree').rsTree != undefined) {
                    for (var i = 0; i < rsTreeGlobalOptions.rsTreeElementsArray.length; i++) {
                        if(rsTreeGlobalOptions.rsTreeElementsArray[i] == this)
                            rsTreeGlobalOptions.rsTreeElementsArray.splice(i, 1);
                    }
                    var $this = $(this), data = $this.data('rstree');
                    $(window).unbind('rstree');
                    data.rstree.remove();
                    $this.removeData('rstree');
                }
                $(this).unbind('rstree');
                $(this).html('');
            });
        },

        /**
         * Generates a tree from the transferred object
         * @public
         * @name formATree(treeObject, parentPath)
         * @param {Object} treeObject : the tree object. Must contain a list of child objects
         * @param {text} parentPath : the initial value of the path. Template "1.2.3"
         */
        formATree : function(treeObject, parentPath) {
            var rsTreeHtml = new RSTreeHtml(rsTree.getOptions());
            var html = rsTreeHtml.formATree(treeObject, parentPath);
            setTimeout(function() {rsTree.initEvents(rsTreeThis); }, 0);
            return html;
        },

        /**
         * Shifts the path to tree elements by one
         * @public
         * @name pathMoveFunc(path, direction)
         * @param {text} path : the initial value of the shear path. Template " 1.2.3"
         * @param {text} direction : up/down
         */
        pathMoveFunc : function(path, direction) {
            var rsTreeHtml = new RSTreeHtml(rsTree.getOptions());
            rsTreeHtml.pathMoveFunc(path, direction);
        },

        /**
         * Returns a part of the tree object along the passed path
         * @public
         * @name getObject(path)
         * @param {text} path : the initial value of the shear path. Template " 1.2.3"
         * @return object
         */
        getObject : function(path) {
            var rsTreeObject = new RSTreeObject(rsTree.getOptions());
            return rsTreeObject.getObject(path);
        },

        /**
         * To add an object to the tree
         * @public
         * @name add(path, object)
         * @param {text} path : the initial value of the path. Template "1.2.3"
         * @param {text} object : Object to add. Can contain branches. Sample: { text: 'item8', children: [ {text: 'item9'}, {text: 'item10'} ] }
         */
        add : function(path, object) {
            rsTree.add(path, object);
        },

        /**
         * Adds an object to a tree object node.
         * @public
         * @name addSub(path, object)
         * @param {text} path : the initial value of the path. Template "1.2.3"
         * @param {text} object : Object to add. Can contain branches. Sample: { text: 'item8', children: [ {text: 'item9'}, {text: 'item10'} ] }
         */
        addSub : function(path, object) {
            rsTree.addSub(path, object);
        },

        /**
         * Removes an item in the tree
         * @public
         * @name remove(element, path)
         * @param {html element} element : the page element in which the tree is built
         * @param {text} path : the initial value of the path. Template "1.2.3"
         */
        remove : function(path) {
            rsTree.remove(path);
        },

        copy : function(path) {
            rsTree.copy(path);
        },

        cut : function(path) {
            rsTree.cut(path);
        },

        past : function(path) {
            rsTree.past(path);
        },

        pastSub : function(path) {
            rsTree.past(path);
        },

        initEvents : function() {
            rsTree.initEvents(rsTreeThis);
        },

        getOptions : function() {
            return rsTree.getOptions();
        }
    };

    $.fn.rstree = function( method ) {
        if($(this).data('rstree') != undefined && $(this).data('rstree').rsTree != undefined) {
            rsTree = $(this).data('rstree').rsTree;
        }

        rsTreeThis = this;

        if ( methods[method] ) {
            return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.jstree' );
        }
    };

    /****************** CLASSES ******************/

    /*CLASS*/
    var RSTree = function() {
        var options = {},
            rsTreeObject = new RSTreeObject(options),
            rsTreeHtml = new RSTreeHtml(options),
            rsTreeEvents = new RSTreeEvents();

        var setOptions = function(newOptions) {
            options = $.extend(options, newOptions);
            rsTreeObject = new RSTreeObject(options),
            rsTreeHtml = new RSTreeHtml(options);
        }

        var initEvents = function(element) {
            var opt = $(element).data('rstree').rsTree.getOptions();

            if(opt.handlerClickFunc != undefined)
                rsTreeEvents.setClick(element, opt.handlerClickFunc);

            if(opt.handlerDoubleClickFunc != undefined)
                rsTreeEvents.setDoubleClick(element, opt.handlerDoubleClickFunc);

            rsTreeEvents.setToggleGroupLi(element);

            if(opt.plugins.indexOf('contextmenu') != -1)
                rsTreeEvents.setContextMenu(element, opt.contextmenu);
        }

        var update = function(path, object) {
            add(path, object);
            remove(path);
           /* rsTreeObject.update(path, object);
            for (var i = 0; i < options.rsUpdateElements.length; i++) {//перебор всех элементов с общим объектом
                $(options.rsUpdateElements[i]).find("*[path='" + path + "']").html(options.viewFunc(object, options, path));
                if(object[options.nameChildrenList] == undefined) {
                    var elm = $(options.rsUpdateElements[i]).find("*[path='" + path + "']").first().parent();
                    elm.find("ul").remove();
                    elm.find("i").removeClass("rsTree-i-group-close").removeClass("rsTree-i-group-open");
                }
            }*/
        }

        var add = function(path, object) {
            var id = path.split('.').length - 1;
                id = path.split('.')[id]; // получить последний элемент пути
            var newPath = path.substring(0, path.length - id.length); // копия path без последнего элемента пути
            id++;

            for (var i = 0; i < options.rsUpdateElements.length; i++) {//перебор всех элементов с общим объектом
                rsTreeHtml.pathMoveFunc(options.rsUpdateElements[i], newPath + id, 'down');
                var selectElement = $(options.rsUpdateElements[i]).find("*[path='" + path + "']");//$(options.rsUpdateElements[i]).find(".rsTree-selected").first();
                if(selectElement.attr("path") == path ) {
                    rsTreeHtml.add(options.rsUpdateElements[i], path, object);
                    initEvents(options.rsUpdateElements[i]);
                    rsTreeObject.add(path, object);
                    /*rsTreeEvents.setActiveNode(options.rsUpdateElements[i], selectElement.parent().next().find("*[path]").first());
                    var rsTreeE = $(options.rsUpdateElements[i]).data('rstree').rsTree;
                    var opt = rsTreeE.getOptions();
                    if(opt.handlerClickFunc != undefined) opt.handlerClickFunc(selectElement.parent().next().find("*[path]").first(), rsTreeE);*/
                    //selectElement.trigger( "click" );
                    selectElement.parent().next().find("*[path]").first().trigger( "click" );
                }
            }
        }

        var addSub = function(path, object) {
            for (var i = 0; i < options.rsUpdateElements.length; i++) {//перебор всех элементов с общим объектом
                var selectElement = $(options.rsUpdateElements[i]).find(".rsTree-selected").first();
                rsTreeHtml.addSub(options.rsUpdateElements[i], path, object);
                initEvents(options.rsUpdateElements[i]);

                if(selectElement.attr("path") == path ) {
                    rsTreeObject.addSub(path, object);
                    selectElement.parent().find('i').first().addClass("rsTree-i-group-open").removeClass("rsTree-i-group-close"); //заменить картинку раскрытия группы
                    selectElement.parent().find("ul").first().show('fast');
                    selectElement.parent().find("ul").first().find("li").last().find("*[path]").first().trigger( "click" );
                }
            }
        }

        var _remove = function(path) {
            if(path != undefined && path.length > 0) {
                rsTreeObject.remove(path);
                for (var i = 0; i < options.rsUpdateElements.length; i++) {//перебор всех элементов с общим объектом
                    rsTreeHtml.remove(options.rsUpdateElements[i], path);
                    rsTreeHtml.pathMoveFunc(options.rsUpdateElements[i], path, 'up');
                }
            }
        }

        var remove = function(path) {
            _remove(path);
            options.copyPath = undefined;
            options.cutObject = undefined;
        }

        /**
         * Saves the path of the copied object
         * @public
         * @name copy(path)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         */
        var copy = function(path) {
            options.cutObject = undefined;
            options.cutPath = undefined;
            var element = $("*[path='" + path + "']");
            options.copyPath = function() {
                return element.attr('path');
            }
        }

        /**
         * Saves the cut feature. Removes an object from the tree.
         * @public
         * @name cut(path)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         */
        var cut = function(path) {
            options.copyPath = undefined;
            options.cutObject = {};
            options.cutObject = rsTreeObject.cloneDR(rsTreeObject.getObject(path));
            _remove(path);
        }

        /**
         * Adds the copied or cut object to the tree
         * @public
         * @name past(path)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         */
        var past = function(path) {
            if(options.copyPath != undefined) {
                add(path, rsTreeObject.getObject(options.copyPath()));
            }
            if(options.cutObject != undefined) {
                add(path, options.cutObject);
                options.cutObject = undefined;
            }
        }

        /**
         * Adds a copied or cut object to a node
         * @public
         * @name pastSub(path)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         */
        var pastSub = function(path) {
            if(options.copyPath != undefined) {
                addSub(path, rsTreeObject.getObject(options.copyPath()));
            }
            if(options.cutObject != undefined) {
                addSub(path, options.cutObject);
                options.cutObject = undefined;
            }
        }

        return {
            getOptions : function() { return options; },
            setOptions : setOptions,
            initEvents : initEvents,
            update : update,
            add : add,
            addSub : addSub,
            remove : remove,
            copy : copy,
            cut : cut,
            past : past,
            pastSub : pastSub,
            rsTreeObject : rsTreeObject,
            rsTreeEvents : rsTreeEvents
        }
    };

    /*CLASS*/
    var RSTreeObject = function(newOptions) {
        var options = {
            treeData : {
                text : 'item1',
                children : [
                    {text : 'item2'},
                    {text : 'item3'}
                ]
            },
            nameChildrenList : 'children'
        }

        options = newOptions;
        //options = $.extend(options, newOptions);

        var cloneDR = function(obj) {
            if(obj == null || typeof(obj) != 'object')
                return obj;
            var temp = new obj.constructor();
            for(var key in obj)
                temp[key] = cloneDR(obj[key]);
            return temp;
        }

        /**
         * To update an object
         * @public
         * @name update(path, object)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         * @param {text} object : Object to add. Can contain branches. Sample: { text: 'item8', children: [ {text: 'item9'}, {text: 'item10'} ] }
         */
        var update = function(path, object) {
            var clone = cloneDR(object);
            var pathArray = path.split('.');
            var newObject = options.treeData;
            for (var i = 0; i < pathArray.length; i++) {
                if(newObject[options.nameChildrenList] != undefined) {
                    if(i == pathArray.length -1) {
                        newObject[options.nameChildrenList].splice(pathArray[i], 1, clone);
                        return;
                    }
                    newObject = newObject[options.nameChildrenList][pathArray[i]];
                }
            }
        };

        /**
         * Adds an object to the tree object.
         * @public
         * @name add(path, object)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         * @param {text} object : Object to add. Can contain branches. Sample: { text: 'item8', children: [ {text: 'item9'}, {text: 'item10'} ] }
         */
        var add = function(path, object) {
            var clone = cloneDR(object);
            var pathArray = path.split('.');
            var newObject = options.treeData;
            for (var i = 0; i < pathArray.length; i++) {
                if(newObject[options.nameChildrenList] != undefined) {
                    if(i == pathArray.length -1) {
                        newObject[options.nameChildrenList].splice(++pathArray[i], 0, clone);
                        return;
                    }
                    newObject = newObject[options.nameChildrenList][pathArray[i]];
                }
            }
        };

        /**
         * Adds an object to a tree object node..
         * @public
         * @name addSub(path, object)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         * @param {text} object : Object to add. Can contain branches. Sample: { text: 'item8', children: [ {text: 'item9'}, {text: 'item10'} ] }
         */
        var addSub = function(path, object) {
            var clone = cloneDR(object);
            var newObject = getObject(path);
            if(newObject[options.nameChildrenList] == undefined) newObject[options.nameChildrenList] = [];
            newObject[options.nameChildrenList].push(clone);
            return newObject[options.nameChildrenList].length;
        };

        /**
         * Removing an item from an object
         * @public
         * @name remove(path)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         */
        var remove = function(path) {
            var pathArray = path.split('.');
            var nameChildren = options.nameChildrenList;
            var object = options.treeData;
            for (var i = 0; i < pathArray.length; i++) {
                if(object != undefined && object[options.nameChildrenList] != undefined) {
                    if(i == pathArray.length - 1) {
                        object[options.nameChildrenList].splice(pathArray[i], 1); //удалит элемент с индексом pathArray[i] в количестве 1
                    } else {
                        object = object[options.nameChildrenList][pathArray[i]];
                    }
                }
            }
        };

        /**
         * Returns a part of the tree object along the passed path
         * @public
         * @name getObject(path)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         * @return object
         */
        var getObject = function(path) {
            if(path == undefined) return rsTree.getOptions().treeData
            var pathArray = path.split('.');
            var chl = rsTree.getOptions().nameChildrenList;
            var object = rsTree.getOptions().treeData;
            for (var i = 0; i < pathArray.length; i++) object = object[chl][pathArray[i]];
            return object;
        }

        /**
         * Returns true if the object is present
         * @public
         * @name pressObject(path)
         * @param {text} path : the initial value of the shear path. Template "1.2.3"
         * @return object
         */
        var pressObject = function(path) {
            if(path == undefined) return false;
            var pathArray = path.split('.');
            var chl = rsTree.getOptions().nameChildrenList;
            var object = rsTree.getOptions().treeData;
            for (var i = 0; i < pathArray.length; i++) {
                if(object == undefined || object[chl] == undefined || object[chl][pathArray[i]] == undefined) return false;
                object = object[chl][pathArray[i]];
            }
            return true;
        }

        return {
            add : add,
            addSub : addSub,
            remove : remove,
            update : update,
            getObject : getObject,
            pressObject: pressObject,
            cloneDR : cloneDR
        };
    };

    /*CLASS*/
    var RSTreeHtml = function(newOptions) {
        var options = {
            treeData : {
                text : 'item1',
                children : [
                    {text : 'item2'},
                    {text : 'item3'}
                ]
            },
            nameChildrenList : 'children',
            listRetrieveText : ['text'],
            parentPath : undefined,
            skipChildrenListFunc : function(object, path, parentPath) {
                return true;
            },
            skipViewFunc : function(object, path, parentPath) {
                return true;
            },
            anotherFunc : function(object, path, parentPath) {
            },
            viewFunc : function(object, options, path) {
            }
        }
        options = newOptions;
        //options = $.extend(options, newOptions);

        /**
         * Generates a tree from the transferred object
         * @public
         * @name formATree(object, parentPathId, initialPath)
         * @param {Object} object : the tree object. Must contain a list of child objects
         * @param {text} parentPathId : the parent value of the path. Template "1.2.3"
         * @param {text} initialPath : the initial value of the path. Template "1.2.3"
         * @return html
         */
        var formATree = function (object, parentPathId, initialPath) {
            var addClassLi = '';
            var listCls = '';
            var liCls = '';
            var iCls = '';
            var path = (parentPathId != undefined ? parentPathId : '')
                     + (parentPathId != undefined && parentPathId.length > 0 ? '.' : '');
            var outText = '';
            var pressElm = false;
            var id = 0;

            if(initialPath != undefined) {
                id = initialPath.split('.').length - 1;
                id = initialPath.split('.')[id]; // получить последний элемент пути
                path = initialPath.substring(0, initialPath.length - id.length); // копия initialPath без последнего элемента пути
            }
            var maxLengthObj = object.length;
            for (var i = 0; i < object.length; i++) {
                pressElm = false;

                if(initialPath != undefined) {
                    id++;
                } else {
                    id = i;
                }

                var l = 0;
                if( options.parentPath != undefined) l = options.parentPath.length;
                var rPath = (path + id).substring(l, (path + id).length);
                if(rPath.indexOf('.') == 0) rPath = rPath.substring(1, rPath.length);

                var isPressList =   object[i][options.nameChildrenList] != undefined
                                    && object[i][options.nameChildrenList].length > 0
                                    && options.skipChildrenListFunc(object[i], rPath, options.parentPath);
                var tree = '';
                if (isPressList) {
                    tree = formATree(object[i][options.nameChildrenList], path + id );
                }
                var isPressListForSkip = isPressList && tree.indexOf('<li') == 0
                            && options.skipViewFunc(object[i], rPath, options.parentPath);

                /* Определение последнего элемента в массиве */
                var isLastElmInArr = true;
                for (var y = i + 1; y < object.length; y++) {
                    for (var x = 0; x < options.listRetrieveText.length; x++) {
                        if (object[y][options.listRetrieveText[x]] != undefined
                            && object[y][options.listRetrieveText[x]] != false
                            && options.skipViewFunc(object[y], rPath, options.parentPath)
                            ) {
                            isLastElmInArr = false;
                            break;
                        }
                    }
                    if(!isLastElmInArr) break;
                }
                addClassLi = options.addClassLiFunc(object[i], path + id, options.parentPath);
                if( isLastElmInArr ) {
                    liCls ='class="' + addClassLi + '"';
                } else {
                    liCls = 'class="' + addClassLi + ' rsTree-li"'; //дорисовывать линию дерева
                }
                /*********************************************/

                for (var x = 0; x < options.listRetrieveText.length; x++) {
                    if (object[i][options.listRetrieveText[x]] != undefined
                        && object[i][options.listRetrieveText[x]] != false
                        ) {

                        listCls = ' class="rsTree rsTree-' + options.listRetrieveText[x];

                        /* Добавление класса статуса */
                        var prefix = ' rsTree-';
                        if( object[i].state != undefined ) {
                            if(object[i].state.selected == true) listCls += prefix + 'selected';
                            if(object[i].state.opened == true) listCls += prefix + 'opened';
                            if(object[i].state.loaded == true) listCls += prefix + 'loaded';
                            if(object[i].state.disabled == true) listCls += prefix + 'disabled';
                        }
                        /*****************************/

                        if(isPressListForSkip ) {
                            listCls += " rsTreeList";
                            listCls += " rsTreeList-" + options.listRetrieveText[x];
                            if( object[i].state != undefined && object[i].state.opened == true) {
                                iCls = ' class="rsTree-i-group-open"';
                            } else {
                                iCls = ' class="rsTree-i-group-close"';
                            }
                        } else {
                            iCls = ' class="rsTree-i"';
                        }
                        listCls += '"';
                        if(options.skipViewFunc(object[i], rPath, options.parentPath)) {

                            outText += '<li ' + liCls + '><i' + iCls + '></i><span'
                                    + listCls
                                    + ' path="'
                                    + path + id
                                    + '">'
                                    + options.viewFunc(object[i], options, rPath, options.parentPath);
                            pressElm = true;
                            options.anotherFunc(object[i], path + id, options.parentPath);
                        }
                        break;
                    }
                }

                if (pressElm && options.skipViewFunc(object[i], rPath, options.parentPath)) outText += '</span>';

                if (isPressList) {
                    if(outText.length > 0 && tree.indexOf('<li') == 0 && options.skipViewFunc(object[i], rPath, options.parentPath)) {
                        outText += '<ul>' + tree + '</ul>';
                    } else {
                        outText += tree;
                    }
                }

                if (pressElm && options.skipViewFunc(object[i], rPath, options.parentPath)) outText += '</li>';
            }
            return outText;
        }

        /**
         * Shifts the path to tree elements by one
         * @public
         * @name pathMoveFunc(element, path, direction)
         * @param {html element} element : the page element in which the tree is built
         * @param {text} path : the initial value of the shear path. Template " 1.2.3"
         * @param {text} direction : up/down
         */
        var pathMoveFunc = function(element, path, direction) {
            var titleElements;
            var newPath;
            var lastPath;
            var resultPath;
            var lengthPath;
            var moveId;

            lengthPath = path.split('.').length - 1;
            newPath = path.split('.')[lengthPath];
            lastPath = newPath;
            path = path.substring(0, path.length - newPath.length);
            titleElements = $(element).find("span");

            for (var y = 0; y < titleElements.length; y++) {
                resultPath = titleElements[y].getAttribute('path');

                if($(titleElements[y]).hasClass('rsTree') && resultPath != null && resultPath.split('.').length >= lengthPath) {
                    newPath = resultPath.split('.')[lengthPath];

                    if (titleElements[y].getAttribute('path').indexOf(path) == 0 && (parseInt(newPath) >= parseInt(lastPath))) {
                        moveId = newPath;
                        if(direction == 'up') moveId--;
                        if(direction == 'down') moveId++;
                        resultPath = path + moveId + resultPath.substring((path + newPath).length, resultPath.length);
                        titleElements[y].setAttribute("path", resultPath);
                    }
                }
            }
        }

        /**
         * To add an object to the tree
         * @public
         * @name add(element, path, object)
         * @param {html element} element : the page element in which the tree is built
         * @param {text} path : the initial value of the path. Template "1.2.3"
         * @param {text} object : Object to add. Can contain branches. Sample: { text: 'item8', children: [ {text: 'item9'}, {text: 'item10'} ] }
         * @return jQuery : added element
         */
        var add = function(element, path, object) {
            var resultElm = undefined;
            if (options.viewFunc(object, options).length > 0) {
                var html = formATree([object], '', path);
                var liElm = $(element).find("*[path='" + path + "']").parent();
                liElm.after(html);
                resultElm = liElm.next();
                liElm.addClass('rsTree-li');
                if(liElm.next().next().html() != undefined) {
                    liElm.next().addClass('rsTree-li');// дорисовать линию дерева
                }
            }
            return resultElm;
        };

        /**
         * Adds an object to a tree object node.
         * @public
         * @name addSub(element, path, object, otherPathLastChild)
         * @param {html element} element : the page element in which the tree is built
         * @param {text} path : the initial value of the path. Template "1.2.3"
         * @param {text} object : Object to add. Can contain branches. Sample: { text: 'item8', children: [ {text: 'item9'}, {text: 'item10'} ] }
         * @return jQuery : added element
         */
        var addSub = function(element, path, object) {
            var resultElm = undefined;
            var html, id, pathId, pathLastChild, parent;
            for (var x = 0; x < options.listRetrieveText.length; x++) {
                if (options.viewFunc(object, options).length > 0) {
                    $(element).find("*[path='" + path + "']")
                        .addClass("rsTreeList-" + options.listRetrieveText[x])
                        .addClass("rsTreeList");

                    $(element).find("*[path='" + path + "']").each(function(ind, elm) {
                        parent = $(elm).parent();
                        if(parent.find("ul").length == 0) parent.append("<ul></ul>");

                        /* Получение количество дочерних элементов для формирования пути новому элементу */
                            var data = $(element).data('rstree');
                            if(data != undefined && data.rsTree != undefined) {
                                var parentObject = data.rsTree.rsTreeObject.getObject(path);
                                if(parentObject != undefined && parentObject[data.rsTree.getOptions().nameChildrenList] != undefined) {
                                    var countChild = parentObject[data.rsTree.getOptions().nameChildrenList].length;
                                    if(countChild > 0 ) {
                                        pathLastChild = path + '.' + (countChild - 1);
                                    }
                                }
                            }
                        /********************************************************************************/

                        if( pathLastChild == undefined) {
                            html = formATree([object], path);
                        } else {
                            html = formATree([object], '', pathLastChild);
                        }

                        parent.find("ul").first().append(html);
                        var el = parent.find("ul").first().children().last();

                        resultElm = el;
                        //resultElm = parent.find('ul').first().find('li').last();
                        parent.find('i').first().addClass("rsTree-i-group-close").removeClass("rsTree-i-group-open"); //заменить картинку раскрытия группы
                        parent.find('ul').first().find('li').last().prev().addClass('rsTree-li'); // дорисовать линию дерева
                        resultElm.prev().addClass('rsTree-li'); // дорисовать линию дерева
                    });
                    break;
                }
            }
            return resultElm;
        };

        /**
         * Removes an item in the tree
         * @public
         * @name remove(element, path)
         * @param {html element} element : the page element in which the tree is built
         * @param {text} path : the initial value of the path. Template "1.2.3"
         */
        var remove = function(element, path) {
            $(element).find("*[path^='" + path + ".'], *[path='" + path + "']").each(function(ind, elm) {
                var parentGroup = $(elm).parent().parent().parent();
                var prevLi = $(elm).parent().prev();
                var elmUl = $(elm).parent().parent();

                $(elm).parent().remove();

                if( $(elmUl).data('rstree') == undefined && elmUl.html() != undefined && elmUl.html().length == 0 ) {
                    elmUl.remove();
                }

                if(parentGroup.find('ul').first().html() == undefined) {// удалить icon группы
                    parentGroup.find('i').first().removeClass('rsTree-i-group-open').removeClass('rsTree-i-group-close').addClass("rsTree-i");
                    parentGroup.find("*[path]").first().removeClass("rsTreeList");

                    for (var x = 0; x < options.listRetrieveText.length; x++) {//перебор массива с именами отображаемых элементов объекта
                        parentGroup.find("*[path]").first().removeClass("rsTreeList-" + options.listRetrieveText[x]);
                    }
                }

                parentGroup.find('ul').first().find('li').last().removeClass('rsTree-li');// удалить продолжение картинки дерева
                if(prevLi.next().html() == undefined) {
                    prevLi.removeClass('rsTree-li');// удалить продолжение картинки дерева
                }
            });
        };

        return {
            formATree : formATree,
            pathMoveFunc : pathMoveFunc,
            add : add,
            addSub : addSub,
            remove : remove
        };
    }

    /*CLASS*/
    var RSTreeEvents = function() {
        /**
         * Sets the click event
         * @public
         * @name setClick(element, handler)
         * @param {html element} element : the page element in which the tree is built
         * @param {function} handler
         */
        var setClick = function(element, handler) {
            $(element).find(".rsTree").unbind('click');
            $(element).find(".rsTree").on("click", function (e) {
                var rsTreeE = $(element).data('rstree').rsTree;
                rsTreeThis = element;
                setActiveNode(element, e.target);
                if(handler != undefined) handler(e.target, rsTreeE);
            });
        };

        /**
         * Sets the active node (tree branch)
         * @public
         * @name setActiveNode(RSTreeElement, element)
         * @param {html element} RSTreeElement : html element where the tree is built
         * @param {html element} element : html element, tree branch
         */
        var setActiveNode = function(RSTreeElement, element) {
            var rsTreeE = $(RSTreeElement).data('rstree').rsTree;
            var activeCLS = "rsTree-selected";

            $(RSTreeElement).find("." + activeCLS).each(function(ind, elm) {
                var object = rsTreeE.rsTreeObject.getObject($(elm).attr("path"));
                if(object.state == undefined) object.state = {};
                object.state.selected = false;
                $(elm).removeClass(activeCLS);
                rsTreeE.getOptions().handlerSelectRemove(elm, rsTreeE);
            });

            var obj = rsTreeE.rsTreeObject.getObject($(element).attr("path"));
            if(obj != undefined) {
                if(obj.state == undefined) obj.state = {};
                obj.state.selected = true;
                $(element).addClass(activeCLS);
                rsTreeE.getOptions().handlerSelectAdd(element, rsTreeE);
            }
        }

        /**
         * Sets the double-click event
         * @public
         * @name setDoubleClick(element, handler)
         * @param {html element} element : the page element in which the tree is built
         * @param {function} handler
         */
        var setDoubleClick = function(element, handler) {
            var rsTreeE = $(element).data('rstree').rsTree;
            $(element).find(".rsTree").unbind('dblclick');
            $(element).find(".rsTree").dblclick(function(e) {
                rsTreeThis = element;

                var liTagI = $(this).parent().find("i").first();
                if(liTagI.hasClass("rsTree-i-group-close")) {
                    liTagI.removeClass("rsTree-i-group-close");
                    liTagI.addClass("rsTree-i-group-open");
                } else {
                    if(liTagI.hasClass("rsTree-i-group-open")) {
                        liTagI.removeClass("rsTree-i-group-open");
                        liTagI.addClass("rsTree-i-group-close");
                    }
                }

                var dbElm = $(this).parent().find("ul").first();
                if(dbElm.html() != undefined) {
                    if(dbElm.css("display") == 'none') {
                        //dbElm.show();
                        $(this).parent().find('ul').first().show('fast');
                        $(this).addClass("rsTree-opened");
                        /* Установить метку раскрытия ветки у выбранного объекта */
                            var obj = rsTreeE.rsTreeObject.getObject($(e.target).attr("path"));
                            if(obj != undefined) {
                                if(obj.state == undefined) obj.state = {};
                                var state = obj.state;
                                state.opened = true;
                            }
                        /**************************************/
                    } else {
                        //dbElm.hide();
                        $(this).parent().find('ul').first().hide('fast');
                        $(this).removeClass("rsTree-opened");
                        /* Снять метку раскрытия ветки у выбранного объекта */
                            var obj = rsTreeE.rsTreeObject.getObject($(e.target).attr("path"));
                            if(obj != undefined) {
                                if(obj.state == undefined) obj.state = {};
                                var state = obj.state;
                                state.opened = false;
                            }
                        /**************************************/
                    }
                }
                if(handler != undefined) handler(this, rsTreeE);
            });
        };

        /**
         * Sets the event click the group icon
         * @public
         * @name setToggleGroupLi(element)
         * @param {html element} element : the page element in which the tree is built
         */
        var setToggleGroupLi = function(element) {
            $(element).find("i").unbind('click');
            $(element).find("i").on("click", function (event) {
                $( event.target ).parent().find("*[path]").first().trigger( "dblclick" );
            });
        };

        /**
         * Sets the ContextMenu
         * @public
         * @name setContextMenu(element, contextmenuObj)
         * @param {html element} element : the page element in which the tree is built
         * @param {object or function} contextmenuObj : {'item1' : { 'label' : 'Вставить', "_disabled" : function (data) { return true; }, 'action' : function (data) { } }}
         */
        var setContextMenu = function(element, contextmenuObj) {
            $(element).find(".rsTree").unbind('contextmenu');
            $(element).find(".rsTree").on("contextmenu", false);

            setEventDeleteContextMenu();

            $(element).find(".rsTree").contextmenu(function(event) {
                var items
                if(typeof contextmenuObj == 'function') {
                    var tree = $(element).data('rstree').rsTree;
                    var path = $( event.target ).attr('path');
                    var data;
                    if( tree != undefined && path != undefined) {
                        data = tree.rsTreeObject.getObject(path);
                    }
                    items = contextmenuObj(data, path, tree, event).items;
                } else {
                    items = contextmenuObj.items;
                }
                rsTreeThis = element;
                $( event.target ).trigger( "click" );

                $(".rsTree-context-menu").detach();
                $('<div/>', {// Создаем меню:
                    class: 'rsTree-context-menu rsTree-context-menu-dialog' // Присваиваем блоку css класс контекстного меню:
                })
                .css({
                    left: event.pageX + 'px', // Задаем позицию меню на X
                    top: event.pageY + 'px' // Задаем позицию меню по Y
                })
                .appendTo('body') // Присоединяем наше меню к body документа:
                .append(getHtmlItems(items, $(event.target).attr('path'), element)) // Добавляем пункты меню:

                toChangePositionDialogMenu(event, $(".rsTree-context-menu"));
                $(".rsTree-context-menu").show('fast');
                execEventItems(items, element);
            });
        };

        /**
         * To change the position of the dialog box (context menu)
         * to move the position of the dialog when it goes out the window
         * @private
         * @name toChangePositionDialogMenu(element)
         * @param {jQuery element} element : element of the page in which the context menu is built
         */
        var toChangePositionDialogMenu = function(event, element) { // сдвинуть меню если выходит за пределы окна
             if(element != undefined && element.offset() != undefined) {
                var winWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
                var winHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
                var menuWidth = element.outerWidth();
                var menuHeight = element.outerHeight();

                var pageX = element.offset().left == 0 ? event.pageX : element.offset().left
                var pageY = element.offset().top  == 0 ? event.pageY : element.offset().top

                if(pageX + menuWidth - winWidth > 0) {
                    var leftCss = element.css('left').split('p')[0];
                    element.css({left: (leftCss - ((pageX + menuWidth) - winWidth)) + 'px'}) // Задаем позицию меню на X
                }
                if(pageY + menuHeight - winHeight > 0) {
                    var topCss = element.css('top').split('p')[0];
                    element.css({top: (topCss - ((pageY + menuHeight) - winHeight)) + 'px'}) // Задаем позицию меню на Y
                }
            }
        }

        /**
         * To change the position of the dialog box (context menu)
         * move the position relative to the parent element
         * @private
         * @name toChangePositionForParent(element, parentElement)
         * @param {jQuery element} element : move the element
         * @param {jQuery element} parentElement : regarding which element move
         */
        var toChangePositionForParent = function(element, parentElement) { // сдвинуть меню если выходит за пределы окна
            element.css({left: (parentElement.outerWidth() + 3) + 'px'}) // Задаем позицию меню на X
            element.css({top: parentElement.position().top + 'px'}) // Задаем позицию меню на Y
        }

        /**
         * Set event to remove the context menu from the page
         * @private
         * @name setEventDeleteContextMenu()
         */
        var setEventDeleteContextMenu = function() {
            $('body').click(function(event) {// удалить окно меню
                var isMenuClick = false;
                $(event).each(function (index, elm) {
                    if($(elm.target).hasClass("rsTree-context-menu")
                    || $(elm.target).hasClass("rsTree-context-menu-parent")
                    ) isMenuClick = true;
                });
                if(!isMenuClick) deleteContextMenu();
            })
        }

        /**
         * Set event to remove the context menu from the page
         * @private
         * @name deleteContextMenu()
         */
        var deleteContextMenu = function() {
            $(".rsTree-context-menu").detach();
        }


        /**
         * Returns an html tree with menu items
         * @private
         * @name getHtmlItems(items, path, element)
         * @param {object} items : object with parameters of menu items
         * @param {text} path : path to the object on which the context menu is called
         * @param {html element} element : element of the page on which the tree is built
         * @return html
         */
        var getHtmlItems = function(items, path, element) {
            var rsTreeE = $(element).data('rstree').rsTree;
            var result = "";
            for (var key in items) {
                var cls = '';
                var clsParent = '';
                var data =  rsTreeE.rsTreeObject.getObject(path);
                if(items[key]._disabled != undefined && items[key]._disabled(data, path, rsTreeE)) {
                    cls = ' rsTree-context-menu-disabled';
                }
                if(items[key].submenu != undefined && cls == '') {
                    clsParent = ' rsTree-context-menu-parent'
                }
                if(items[key].submenu != undefined && cls != '') {
                    clsParent = ' rsTree-context-menu-parent-disabled'
                }
                result  +='<li><span'
                        + ' class="' + cls + clsParent + '"'
                        + ' itemName="' + key + '"'
                        + ' id="rsTree-context-menu-' + key + '"'
                        + ' element-path="' + path + '"'
                        + ' >'
                        + items[key].label
                        + '</span>';

                if(items[key].submenu != undefined && cls == '') {
                    result  += '<ul class="rsTree-context-menu-dialog rsTree-context-submenu">'
                            + getHtmlItems(items[key].submenu, path, element)
                            + "</ul>";
                }
                result  +='</li>';
            }
            return result
        }

        /**
         * Creates a click event that calls a function from a menu item object
         * @private
         * @name execEventItems(items, element)
         * @param {object} items : object with parameters of menu items
         * @param {html element} element : element of the page on which the tree is built
         */
        var execEventItems = function(items, element) {
            for (var key in items) {
                $(".rsTree-context-menu-dialog").on("contextmenu", false);
                $(".rsTree-context-submenu").on("contextmenu", false);
                $(".rsTree-context-menu").on("contextmenu", false);
                $("#rsTree-context-menu-" + key).on("contextmenu", false);

                $("#rsTree-context-menu-" + key).unbind('click');
                $("#rsTree-context-menu-" + key).click(function(e) {
                    rsTreeThis = element;
                    var rsTreeE = $(element).data('rstree').rsTree;
                    var path = $(e.target).attr('element-path');
                    var data =  rsTreeE.rsTreeObject.getObject(path);

                     for (var item in items) {
                        if($(e.target).attr("itemName") == item) {

                            if(items[item].action != undefined) {
                                if(items[item]._disabled == undefined) {
                                    items[item].action(data, path, rsTreeE);
                                } else {
                                    if( !items[item]._disabled(data, path, rsTreeE)) {
                                        items[item].action(data, path, rsTreeE);
                                    }
                                }
                            }

                            if(items[item].submenu != undefined) {
                                $(e.target).parent().parent().find('li ul').hide('fast');//закрыть все пункты текущего блока
                                if($(e.target).parent().find('ul').first().is(':visible')) {
                                    $(e.target).parent().find('ul').hide('fast');
                                } else {
                                    /* передвинуть окно */
                                    $(e.target).parent().find('ul').first().show();
                                    toChangePositionForParent($(e.target).parent().find('ul').first(), $(e.target));
                                    toChangePositionDialogMenu(e, $(e.target).parent().find('ul').first());
                                    $(e.target).parent().find('ul').first().hide();
                                    /********************/
                                    $(e.target).parent().find('ul').first().show('fast');
                                }
                            }
                        }
                    }
                });
                if(items[key].submenu != undefined) {
                    execEventItems(items[key].submenu, element);
                }
            }
        }

        return {
            setDoubleClick : setDoubleClick,
            setClick : setClick,
            setContextMenu : setContextMenu,
            setActiveNode : setActiveNode,
            setToggleGroupLi : setToggleGroupLi
        };
    }

})( jQuery );