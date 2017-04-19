/**
 * Created by merce on 2017/2/19.
 */
;
(function ($) {
    $.widget('org.pce', {
        options: {
            dataSource: function () {
                return [];
            },
            height: $(window).height() + "px"
        },
        _currentConnect: null,
        _create: function () {
            var args = this.options;
            var el = this.element;
            var that = this;

            //插入需要的元素
            $(el)
                .append('<div class="row"><div class="col-md-2 pce-tree"/><div class="col-md-10 pce-body"><div class="pce-canvas ' +
                    'canvas-wide jtk-surface jtk-surface-nopan" id="pce-canvas"></div></div></div>')
                .append('<div class="modal fade" id="nodeEditorModal" tabindex="-1" role="dialog" aria-labelledby=' +
                    '"myModalLabel"><div class="modal-dialog" role="document"><div class="modal-content"><div class=' +
                    '"modal-header"><button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
                    '<span aria-hidden="true">&times;</span></button><h4 class="modal-title" id="myModalLabel">' +
                    '编辑节点</h4></div><div class="modal-body"><form class="" id="nodeEditorForm" name="nodeEditorForm"' +
                    '><div class="form-group"><label>节点ID</label><span id="nodeId"></span></div><div class="form-group"' +
                    '><label>节点类型</label><span id="nodeType"></span></div><div class="form-group"><label for=' +
                    '"nodeName1">节点名称</label><input type="text" class="form-control" id="nodeName1" name="nodeName1"' +
                    ' placeholder="节点名称"></div><div class="form-group"><label for="nodeDescribe">节点描述</label>' +
                    '<input type="text" class="form-control" id="nodeDescribe" name="nodeDescribe" placeholder=' +
                    '"节点描述"></div><div id="viewArea"></div></form></div><div class="modal-footer"><button type=' +
                    '"button" class="btn btn-default" data-dismiss="modal"><i class="fa fa-close"></i>&nbsp;取消</button>' +
                    ' <button type="button" class="btn btn-primary" id="nodeEditorSaveButton"><i class="fa fa-save"></i>' +
                    '&nbsp;保存</button></div></div></div></div>')
                .append('<div class="modal fade" id="connectEditorModal" tabindex="-1" role="dialog" aria-labelledby=' +
                    '"myModalLabel"><div class="modal-dialog" role="document"><div class="modal-content"><div class=' +
                    '"modal-header"><button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
                    '<span aria-hidden="true">&times;</span></button><h4 class="modal-title" id="connectEditorModalLabel"' +
                    '>编辑连接</h4></div><div class="modal-body"><form class="" id="connectEditorForm" ' +
                    'name="nodeEditorForm"><div class="form-group"><label>源</label><span id="connectSource"></span></div>' +
                    '<div class="form-group"><label>目标</label><span id="connectTarget"></span></div><div class="form-group">' +
                    '<label for="connectName">名称</label><input class="form-control" id="connectName" name="connectName"' +
                    ' placeholder="名称"></div><div class="form-group"><label for="connectExpression">条件表达式</label>' +
                    '<textarea class="form-control" id="connectExpression" name="connectExpression" placeholder="条件表达式">' +
                    'true</textarea></div></form></div><div class="modal-footer"><button type="button" class=' +
                    '"btn btn-default" data-dismiss="modal"><i class="fa fa-close"></i>&nbsp;取消</button> ' +
                    '<button type="button" class="btn btn-primary" id="connectEditorSaveButton"><i class="fa fa-save"></i>' +
                    '&nbsp;保存</button></div></div></div></div>')
                .append('<div id="pceContextmenu"></div>');

            //左菜单
            var tree = {};//树形结构
            $.each(that._clone(args.dataSource()), function (k, v) {
                var group = v.group;

                //将当前数据加入对应的分组下
                if (typeof(tree[group]) == 'undefined') {
                    //如果没有，加入
                    tree[group] = [];
                }
                tree[group].push({
                    name: v.name,
                    value: v.config.node
                });
            });

            var pceTree = $(".pce-tree", el);
            $.each(tree, function (k, v) {
                var root = $('<div class="pce-view-tree">' + k + '</div>');
                $.each(v, function (i, n) {
                    $('<div class="pce-view-leaf">' + n.value.html + '</div>')
                        .click(function () {
                            n.value.type = n.name;
                            that._addNode(n.value);
                        }).appendTo(root);
                });
                pceTree.append(root);
            });

            //节点保存
            var nodeEditorSaveButton = $("#nodeEditorModal #nodeEditorSaveButton", el);
            nodeEditorSaveButton.click(function () {
                var params = {};
                //类型
                params.type = $("#nodeEditorModal #nodeEditorForm span#nodeType", el).text();
                //名称
                params.name = $("#nodeEditorModal #nodeEditorForm input#nodeName1", el).val();
                //描述
                params.describe = $("#nodeEditorModal #nodeEditorForm input#nodeDescribe", el).val();
                //其它参数
                var _p = $("#nodeEditorModal #nodeEditorForm", el).serializeArray();
                $.each(_p, function (i, n) {
                    if (typeof(params.properties) == 'undefined') {
                        params.properties = [];
                    }
                    if (n.name != "nodeName1" && n.name != "nodeDescribe") {
                        params.properties.push(n);
                    }
                });

                var t = $("#" + $("#nodeEditorModal", el).data("target"));//得到要保存的节点

                t.find("i.fa").text(params.name);//修改节点名

                t.data("params", params);//保存节点数据
                //var connectionList = instance.getConnections();
                $('#nodeEditorModal', el).removeData("target").modal('hide');
            });


            //连接保存
            var connectEditorSaveButton = $("#connectEditorModal #connectEditorSaveButton", el);
            connectEditorSaveButton.click(function () {
                //名字
                var connectName = $("#connectEditorModal #connectEditorForm #connectName", el).val();
                that._setLabel(connectName);

                //表达式
                var connectExpression = $("#connectEditorModal #connectEditorForm #connectExpression", el).val();
                if (typeof(connectExpression) != 'undefined' && connectExpression != null)
                    that._currentConnect.setParameter("expression", connectExpression);

                //隐藏
                $('#connectEditorModal', el).modal('hide');
            });

            //窗口关闭，清空当前连接
            $('#connectEditorModal', el).on('hidden.bs.modal', function (e) {
                that._currentConnect = null;
            });

            //右键菜单默认隐藏
            $("#pceContextmenu", el).hide();
            //当点击任何地方的时候隐藏右键菜单
            $(":not(#pceContextmenu)", "body").click(function () {
                $("#pceContextmenu", el).hide();
            });

            //设置画布的高度
            function setCanvasHeight() {
                $(".pce-canvas", el).css({
                    "height": args.height
                });
            }

            //当窗口变化时
            $(window).resize(function () {
                setCanvasHeight();
            });
            setCanvasHeight();

            //开始初始化
            that._initJsplumb();
        },
        _init: function () {
            var args = this.options;
            var el = this.element;
            this._initJsplumb()
        },
        _setOption: function (key, value) {
            this.options[key] = value;
            return this;
        },
        _destroy: function () {
            var el = this.element;
            $(el).empty();
        },
        _instance: null, //Jsplumb实例
        /**
         * 初始化Jsplumb
         * @private
         */
        _initJsplumb: function () {
            var args = this.options;
            var el = this.element;
            var that = this;
            jsPlumb.ready(function () {

                // setup some defaults for jsPlumb.
                that._instance = jsPlumb.getInstance({
                    Endpoint: ["Dot", {radius: 2}],
                    Connector: "StateMachine",
                    HoverPaintStyle: {stroke: "#1e8151", strokeWidth: 2},
                    ConnectionOverlays: [
                        ["Arrow", {
                            location: 1,
                            id: "arrow",
                            length: 14,
                            foldback: 0.8
                        }]
                    ],
                    Container: "pce-canvas"
                });

                that._instance.registerConnectionType("basic", {anchor: "Continuous", connector: "StateMachine"});

                window.jsp = that._instance;

                that._instance.bind("connection", function (info) {
                    //info.connection.getOverlay("label").setLabel(info.connection.id);

                    var _expression = info.connection.getParameter("expression");
                    if (_expression === null || _expression === "")
                        info.connection.setParameter("expression", "true");

                    info.connection.overlays = [
                        ["Arrow", {
                            location: 1,
                            id: "arrow",
                            length: 14,
                            foldback: 0.8
                        }]
                    ];

                    //双击弹出编辑窗口
                    info.connection.bind("dblclick", function (c) {
                        if (c.type == "Label") {//如果是Label，找到对应的连接
                            c = c.component;
                        }

                        //源
                        var sourceId = c.sourceId;
                        if (typeof (sourceId) != 'undefined')
                            $('#connectEditorModal #connectSource', el).text(sourceId);

                        //目标
                        var targetId = c.targetId;
                        if (typeof (sourceId) != 'undefined')
                            $('#connectEditorModal #connectTarget', el).text(targetId);

                        //名字
                        var label = c.getOverlay("label");
                        if (typeof (label) != 'undefined')
                            $('#connectEditorModal #connectName', el).val(label.getLabel());

                        //参数
                        var connectExpression = c.getParameter("expression");
                        if (typeof (connectExpression) != 'undefined')
                            $('#connectEditorModal #connectExpression', el).val(connectExpression);

                        //显示窗口
                        $('#connectEditorModal', el).modal('show');

                        //保存当前连接对象
                        that._currentConnect = c;
                    });

                    //右键菜单
                    info.connection.bind("contextmenu", function (c, e) {
                        that._showContextMenu(e, [
                            {
                                name: "删除当前连接",
                                handler: function () {
                                    //如果是Label，找到对应的连接
                                    if (c.type == "Label") {
                                        c = c.component;
                                    }
                                    that._instance.detach(c);//断开连接
                                }
                            }
                        ]);
                    });
                });

                jsPlumb.fire("jsPlumbLoaded", that._instance);
            });

        },
        /**
         * 右键菜单通用方法
         * @param e
         * @param p
         * @private
         */
        _showContextMenu: function (e, p) {
            var el = this.element;
            //屏蔽系统右键菜单
            document.oncontextmenu = function () {
                return false;
            };
            if (event.button == 2) {
                var pceContextMenu = $("#pceContextmenu", el);
                //位置+清空
                pceContextMenu
                    .css({
                        "left": e.clientX + "px",
                        "top": e.clientY + "px"
                    })
                    .empty();

                //插入菜单
                $.each(p, function (i, n) {
                    pceContextMenu.append($("<div/>")
                        .html(n.name)
                        .click(function () {
                            n.handler();
                            pceContextMenu.hide();
                        }));
                });

                //显示
                pceContextMenu.show();
            }
        },
        /**
         * 设置label的值
         * @param value
         * @private
         */
        _setLabel: function (value) {
            var label = this._currentConnect.getOverlay("label");
            if (typeof(label) == 'undefined')
                this._currentConnect.setLabel(value);
            else label.setLabel(value);
        },
        /**
         * 得到线上的Label
         * @param connect
         * @returns {*}
         * @private
         */
        _getLabel: function (connect) {
            var label = connect.getOverlay("label");
            if (typeof(label) != "undefined")
                return label.getLabel();
            else return "";
        },
        /**
         * 批量连线
         * @param p
         * @private
         */
        _batchConnect: function (p) {
            var that = this;
            if (typeof (p) != "undefined" && p != null && p.length > 0) {
                that._instance.batch(function () {
                    $.each(p, function (i, n) {
                        //连接
                        that._instance.connect({
                            source: n.source,
                            target: n.target,
                            parameters: n.parameters,
                            type: "basic",
                            overlays: [
                                ["Label", {label: n.name, id: "label", cssClass: "aLabel"}]
                            ]
                        });
                    });
                });

            }
        },
        /**
         * 批量插入节点
         * @param pList
         * @private
         */
        _batchAddNode: function (pList) {
            var that = this;
            that._instance.batch(function () {
                $.each(pList, function (i, p) {
                    that._addNode(p);
                });
            });
        },
        //插入节点
        _addNode: function (_params) {
            var args = this.options;
            var el = this.element;
            var that = this;
            var params = $.extend({
                id: jsPlumbUtil.uuid(),
                name: null,
                type: null,
                describe: null,
                html: "",
                dd: true,
                isSource: true,
                isTarget: true,
                x: 0,
                y: 0,
                properties: []
            }, _params);

            //如果有遗失的属性，则加上
            var nodeConfig = $.grep(that._clone(args.dataSource()), function (n, i) {
                return (n.name == params.type)
            });
            $.each(nodeConfig[0].config.view, function (i, n) {
                var lostProperty = $.grep(params.properties, function (m, j) {
                    return (m.name == n.name)
                });
                if (lostProperty.length == 0) {
                    params.properties.push({
                        name: n.name,
                        value: (typeof(n.value) == 'undefined' ? "" : n.value)
                    });
                }
            });

            var node = document.createElement("div");
            node.className = "w";
            node.id = params.id;
            if (params.name == null) params.name = $(params.html).text();
            params.html = $(params.html)
                .css("padding-right", "2px")
                .text(params.name)
                .get(0).outerHTML;
            node.innerHTML = params.html + '&nbsp;<div class="ep"></div>';
            node.style.left = params.x + "px";
            node.style.top = params.y + "px";
            $(node).data("params", params)//数据
                .mouseup(function (e) {//右键菜单
                    that._showContextMenu(e, [{
                        name: "删除节点",
                        handler: function () {
                            var self = $(e.target);
                            if (!self.hasClass("w")) {
                                self = self.parents(".w");
                            }
                            that._instance.remove(self.attr("id"));
                        }
                    }])
                });
            that._instance.getContainer().appendChild(node);

            //是否可拖拽
            if (params.dd) {
                // initialise draggable elements.
                that._instance.draggable(node);
            }

            //是否是源
            if (params.isSource) {
                that._instance.makeSource(node, {
                    filter: ".ep",
                    anchor: "Continuous",
                    connectorStyle: {stroke: "#5c96bc", strokeWidth: 2, outlineStroke: "transparent", outlineWidth: 4},
                    connectionType: "basic",
                    extract: {
                        "action": "the-action"
                    }
                });
            }

            //是否是目标
            if (params.isTarget) {
                that._instance.makeTarget(node, {
                    dropOptions: {hoverClass: "dragHover"},
                    anchor: "Continuous",
                    allowLoopback: true
                });
            }

            //各种事件
            $(node).dblclick(function (e) {
                var self = $(e.target);
                //如果事件是由w中的某个元素出发，则向中找到w元素
                if (!self.hasClass("w"))
                    self = self.parents(".w");
                //得到保存的参数
                var nodeParams = self.data("params");
                if (typeof(nodeParams) == "undefined") nodeParams = {};
                var nodeConfig = $.grep(that._clone(args.dataSource()), function (n, i) {
                    return (n.name == nodeParams.type)
                });
                var view = nodeConfig[0].config.view;
                //默认的属性
                $("#nodeEditorModal #nodeEditorForm span#nodeId", el).text(node.id);

                //从data中得到type
                $("#nodeEditorModal #nodeEditorForm span#nodeType", el).text(nodeParams.type);

                //从data中得到nodeName
                $("#nodeEditorModal #nodeEditorForm input#nodeName1", el).val(nodeParams.name);

                //从data里得到nodeDescribe
                $("#nodeEditorModal #nodeEditorForm input#nodeDescribe", el).val(nodeParams.describe);

                //清空
                $("#nodeEditorModal #nodeEditorForm #viewArea", el).empty();
                //自定义的属性
                $.each(view, function (i, n) {
                    //构建页面
                    var group = $('<div class="form-group"/>');
                    var label = $('<label for="' + n.name + '">' + n.label + '</label>');
                    var val;
                    if (typeof(n.tagHtml) != 'undefined') {
                        val = $(n.tagHtml);
                    } else if (typeof(n.tag) != 'undefined') {
                        val = $(document.createElement(n.tag))
                            .attr({
                                id: n.name,
                                name: n.name,
                                class: "form-control",
                                placeholder: n.label
                            });
                        if (typeof(n.value) != 'undefined') {
                            val.val(n.value);
                        }
                    }
                    //从data里获得数据
                    var dataVal = that._getValue(nodeParams.properties, n.name);
                    if (dataVal != null) {
                        val.val(dataVal);
                    }

                    //插入文档树
                    group.append(label).append(val);
                    $("#nodeEditorModal #nodeEditorForm #viewArea", el).append(group);
                });
                //打开模态窗
                $('#nodeEditorModal', el).data("target", self.attr("id")).modal('show');
            });

            that._instance.fire("jsPlumbNodeAdded", node);
        },
        /**
         * 得到结果
         * @param nodeParams
         * @param key
         * @returns {null}
         * @private
         */
        _getValue: function (nodeParams, key) {
            var valueList = $.grep(nodeParams, function (n, i) {
                return (n.name == key)
            });
            if (typeof(valueList) != "undefined" && valueList.length > 0) {
                return valueList[0].value
            }
            return null;
        },
        /**
         * 渲染画布
         * @param xml
         */
        renderCanvas: function (xml) {
            var args = this.options;
            var that = this;
            var xmlDoc = $.parseXML(xml);
            var $xml = $(xmlDoc);

            /**
             * 添加节点
             */
            function addNodes() {
                var $nodes = $xml.find("nodes > *");
                var nodes = [];
                $nodes.each(function (i, n) {
                    var p = {};
                    var nType = $(n).attr("type");

                    var nodeConfig = $.grep(that._clone(args.dataSource()), function (n, i) {
                        return (n.name == nType)
                    });

                    p = nodeConfig[0].config.node;//得到该类型的参数
                    p.id = $(n).attr("id");

                    //查找位置
                    var $view = $xml.find("view#" + p.id);
                    p.x = $view.attr("left");
                    p.y = $view.attr("top");

                    //固定参数
                    p.type = nType;
                    p.name = $(n).attr("name");
                    p.describe = $(n).attr("describe");

                    //动态参数
                    $(n).find("properties").each(function (i, n) {
                        if (typeof(p.properties) == "undefined") p.properties = [];
                        p.properties.push({
                            name: $(n).attr("name"),
                            value: $(n).text()
                        });
                    });
                    nodes.push(p);
                });
                that._batchAddNode(nodes);//批量添加节点
            }

            addNodes();//添加节点

            /**
             * 添加连接
             */
            function addConnects() {
                var $connects = $xml.find("connects > connect");
                var connects = [];
                $connects.each(function (i, n) {
                    //更多参数
                    var parameters = {};
                    $(n).find("properties").each(function (i, n) {
                        var name = $(n).attr("name");
                        var value = $(n).text();
                        parameters[name] = value;
                    });
                    //连接相关属性
                    connects.push({
                        source: $(n).attr("from"),
                        target: $(n).attr("to"),
                        name: $(n).attr("name"),
                        parameters: parameters
                    });
                });
                that._batchConnect(connects);//批量添加连接
            }

            addConnects();//添加连接
        },
        /**
         * 克隆
         * @param obj
         * @returns {*}
         * @private
         */
        _clone: function (obj) {
            var that = this;
            var o;
            if (typeof obj == "object") {
                if (obj === null) {
                    o = null;
                } else {
                    if (obj instanceof Array) {
                        o = [];
                        for (var i = 0, len = obj.length; i < len; i++) {
                            o.push(that._clone(obj[i]));
                        }
                    } else {
                        o = {};
                        for (var k in obj) {
                            o[k] = that._clone(obj[k]);
                        }
                    }
                }
            } else {
                o = obj;
            }
            return o;
        },
        /**
         * 清理画布
         * @private
         */
        cleanCanvas: function () {
            var el = this.element;
            var that = this;
            that._instance.detachEveryConnection();//断开所有连接
            $(".pce-canvas .w", el).each(function (i, n) {
                that._instance.remove($(n).attr("id")); //删除所有节点
            });
        },
        /**
         * 得到xml
         * @returns {*|HTMLElement}
         */
        getXml: function () {
            var el = this.element;
            var that = this;

            //生成最外层结构
            var xKmc = $("<kmc/>");

            var xFlow = $("<flow/>");
            xFlow.appendTo(xKmc);

            var xViews = $("<views/>");
            xViews.appendTo(xFlow);

            var xConnects = $("<connects/>");
            xConnects.appendTo(xFlow);

            var xNodes = $("<nodes/>");
            xNodes.appendTo(xFlow);

            //得到所有节点
            $(".w", el).each(function (i, n) {
                var nodeParams = $(n).data("params");
                //生成node节点
                var xNode = $("<node/>")
                    .attr({
                        id: $(n).attr("id"),
                        name: nodeParams.name,
                        describe: nodeParams.describe,
                        type: nodeParams.type
                    }).appendTo(xNodes);
                //生成node的properties节点
                $.each(nodeParams.properties, function (j, property) {
                    //if (m.name != "nodeName1" && m.name != "nodeDescribe" && m.name != "nodeType") {
                    $("<properties/>")
                        .attr("name", property.name)
                        .text("<![CDATA[" + property.value + "]]>")
                        .appendTo(xNode);
                    //}
                });

                //生成节点位置信息
                var position = $(n).position();//节点的位置
                $("<view/>")
                    .attr({
                        id: $(n).attr("id"),
                        left: position.left,
                        top: position.top
                    }).appendTo(xViews);

            });

            //生成连接
            $.each(that._instance.getConnections(), function (i, c) {//得到所有连接
                //生成连接节点
                var xConnect = $("<connect/>")
                    .attr({
                        id: c.id,
                        name: that._getLabel(c),
                        from: c.sourceId,
                        to: c.targetId
                    }).appendTo(xConnects);
                //生成属性
                var expression = c.getParameter("expression");
                if (typeof(expression) == "undefined")
                    expression = "";
                $("<properties/>")
                    .attr("name", "expression")
                    .text("<![CDATA[" + expression + "]]>")
                    .appendTo(xConnect);
            });
            return xKmc;
        }
    });
})(jQuery);