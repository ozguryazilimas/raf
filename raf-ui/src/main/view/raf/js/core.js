(function(window) {

    if (window.RafFaces) {
        window.RafFaces.debug("RafFaces already loaded, ignoring duplicate execution.");
        return;
    }

    var RafFaces = {
        __dialogUtils: {
            __closableTitleCommandButtonEl:       '<a class="ui-dialog-titlebar-icon ui-dialog-titlebar-close ui-corner-all" href="#" role="button"><span class="ui-icon ui-icon-closethick"></span></a>',
            __minimizableTitleCommandButtonEl:    '<a class="ui-dialog-titlebar-icon ui-dialog-titlebar-minimize ui-corner-all" href="#" role="button"><span class="ui-icon ui-icon-minus"></span></a>',
            __maximizableTitleCommandButtonEl:    '<a class="ui-dialog-titlebar-icon ui-dialog-titlebar-maximize ui-corner-all" href="#" role="button"><span class="ui-icon ui-icon-extlink"></span></a>',
            __uiDialogContentEl:                  '<div class="ui-dialog-content ui-widget-content ui-df-content" style="height: auto;"><iframe style="border:0 none" frameborder="0"/></div>',

            getDialogId: (cfg) => cfg.sourceComponentId + '_dlg',
            getDialogWidgetVar: (cfg) => cfg.sourceComponentId.replace(/:/g, '_') + '_dlgwidget',

            getDialogDom: (cfg) => {
                return $('<div id="' + RafFaces.__dialogUtils.getDialogId(cfg) + '" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-shadow ui-hidden-container ui-overlay-hidden raf-ui-dialog-documentView"' +
                    ' data-pfdlgcid="' + cfg.pfdlgcid + '" data-widgetvar="' + RafFaces.__dialogUtils.getDialogWidgetVar(cfg) + '"></div>')
                    .append('<div class="ui-dialog-titlebar ui-widget-header ui-helper-clearfix ui-corner-top"><span class="ui-dialog-title"></span></div>');
            },

            __prepareDialogTitlebar: (titlebar, cfg) => {
                titlebar.append(RafFaces.__dialogUtils.__closableTitleCommandButtonEl);
                cfg.options.minimizable && titlebar.append(RafFaces.__dialogUtils.__minimizableTitleCommandButtonEl);
                cfg.options.maximizable && titlebar.append(RafFaces.__dialogUtils.__maximizableTitleCommandButtonEl);
            },
            __getConversationInputEl: (cid, conversationId) => `<input type="hidden" id="${cid}:pfdlgcid" name="pfdlgcid" value="${conversationId}"></input>`

        },

        openDocumentViewDialog: function(cfg) {
            var rootWindow = PrimeFaces.dialog.DialogHandler.findRootWindow(),
                dialogId = RafFaces.__dialogUtils.getDialogId(cfg);

            if(rootWindow.document.getElementById(dialogId)) {
                return;
            }

            var dialogWidgetVar = RafFaces.__dialogUtils.getDialogWidgetVar(cfg),
                dialogDOM = RafFaces.__dialogUtils.getDialogDom(cfg);

            //Create and prepare titlebar
            var titlebar = dialogDOM.children('.ui-dialog-titlebar');
            this.__dialogUtils.__prepareDialogTitlebar(titlebar, cfg);

            dialogDOM.append(RafFaces.__dialogUtils.__uiDialogContentEl);
            //dialogDOM.appendTo(rootWindow.document.body);
            dialogDOM.prependTo(PrimeFaces.expressions.SearchExpressionFacade.resolveComponentsAsSelector('@(.content-wrapper > #documentViewDialog-wrapper)'));

            var dialogFrame = dialogDOM.find('iframe'),
                symbol = cfg.url.indexOf('?') === -1 ? '?' : '&',
                frameURL = cfg.url.indexOf('pfdlgcid') === -1 ? cfg.url + symbol + 'pfdlgcid=' + cfg.pfdlgcid: cfg.url,
                frameWidth = '100%',
                frameHeight = '100%';

            dialogFrame.width(frameWidth);
            dialogFrame.height(frameHeight);

            dialogFrame.on('load', function() {
                var $frame = $(this),
                    headerElement = $frame.contents().find('title'),
                    isCustomHeader = false;

                if(cfg.options.headerElement) {
                    var customHeaderId = PrimeFaces.escapeClientId(cfg.options.headerElement),
                        customHeaderElement = dialogFrame.contents().find(customHeaderId);

                    if(customHeaderElement.length) {
                        headerElement = customHeaderElement;
                        isCustomHeader = true;
                    }
                }

                if(!$frame.data('initialized')) {
                    PrimeFaces.cw.call(rootWindow.PrimeFaces, 'DynamicDialog', dialogWidgetVar, {
                        id: dialogId,
                        sourceComponentId: cfg.sourceComponentId,
                        sourceWidgetVar: cfg.sourceWidgetVar,
                        onHide: function() {
                            var $dialogWidget = this,
                                dialogFrame = this.content.children('iframe');

                            if(dialogFrame.get(0).contentWindow.PrimeFaces) {
                                this.destroyIntervalId = setInterval(function() {
                                    if(dialogFrame.get(0).contentWindow.PrimeFaces.ajax.Queue.isEmpty()) {
                                        clearInterval($dialogWidget.destroyIntervalId);
                                        dialogFrame.attr('src','about:blank');
                                        $dialogWidget.jq.remove();
                                    }
                                }, 10);
                            }
                            else {
                                dialogFrame.attr('src','about:blank');
                                $dialogWidget.jq.remove();
                            }

                            rootWindow.PF[dialogWidgetVar] = undefined;
                        },
                        modal: false,
                        resizable: false,
                        hasIframe: true,
                        draggable: false,
                        width: '100%',
                        height: '100%',
                        minimizable: false,
                        maximizable: false,
                        headerElement: cfg.options.headerElement
                    });
                }

                const conversationInputEl =
                $frame.contents().find('form').each((idx, el) => {
                    $(el).append(RafFaces.__dialogUtils.__getConversationInputEl($(el).attr('id'), cfg.pfdlgcid));
                });

                var title = rootWindow.PF(dialogWidgetVar).titlebar.children('span.ui-dialog-title');
                if(headerElement.length > 0) {
                    if(isCustomHeader) {
                        title.append(headerElement);
                        headerElement.show();
                    }
                    else {
                        title.text(headerElement.text());
                    }
                }

                dialogFrame.data('initialized', true);

                rootWindow.PF(dialogWidgetVar).show();

                //adjust height
                var frameHeight = $frame.get(0).contentWindow.document.body.scrollHeight + (PrimeFaces.env.browser.webkit ? 5 : 25);
                $frame.css('height', frameHeight + 50);
            })
                .attr('src', frameURL);
        },

        closeDocumentViewDialog: function(cfg) {
            var rootWindow = PrimeFaces.dialog.DialogHandler.findRootWindow(),
                dlgs = $('#documentViewDialog_' + cfg.pfdlgcid + '_dlg', rootWindow.document).not('[data-queuedforremoval]'),
                dlgsLength = dlgs.length,
                dlg = dlgs.eq(dlgsLength - 1),
                dlgWidget = rootWindow.PF(dlg.data('widgetvar'));

            dlg.attr('data-queuedforremoval', true);
            dlgWidget.hide();
        }

    };

    window.RafFaces = RafFaces;

})(window);
