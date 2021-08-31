Ext.define("Ext.grid.column.Widget",{extend:"Ext.grid.column.Column",alias:"widget.widgetcolumn",config:{defaultWidgetUI:{}},ignoreExport:true,sortable:false,onWidgetAttach:null,preventUpdate:true,stopSelection:true,initComponent:function(){var A=this,B;A.callParent(arguments);B=A.widget;if(!B||B.isComponent){Ext.raise("column.Widget requires a widget configuration.")}A.widget=B=Ext.apply({},B);if(!B.ui){B.ui=A.getDefaultWidgetUI()[B.xtype]||"default"}A.isFixedSize=Ext.isNumber(B.width)},processEvent:function(E,G,H,A,F,D,B,I){var C;if(this.stopSelection&&E==="click"){C=D.getTarget(G.innerSelector);if(C&&C!==D.target){D.stopSelection=true}}},beforeRender:function(){var A=this,C=A.tdCls,B;A.listenerScopeFn=function(D){if(D==="this"){return this}return A.resolveListenerScope(D)};A.liveWidgets={};A.cachedStyles={};A.freeWidgetStack=[B=A.getFreeWidget()];C=C?C+" ":"";A.tdCls=C+B.getTdCls();A.setupViewListeners(A.getView());A.callParent()},afterRender:function(){var A=this.getView();this.callParent();if(A&&A.viewReady&&!A.ownerGrid.reconfiguring){this.onViewRefresh(A,A.getViewRange())}},defaultRenderer:Ext.emptyFn,updater:function(A,C,B){this.updateWidget(B)},onResize:function(E){var D=this,B=D.liveWidgets,C=D.getView(),F,A;if(!D.isFixedSize&&D.rendered&&C&&C.viewReady){A=C.getEl().down(D.getCellInnerSelector());if(A){E-=parseInt(D.getCachedStyle(A,"padding-left"),10)+parseInt(D.getCachedStyle(A,"padding-right"),10);for(F in B){B[F].setWidth(E)}}}},onAdded:function(){var B=this,A;B.callParent(arguments);A=B.getView();if(A){B.setupViewListeners(A);if(A&&A.viewReady&&B.rendered&&A.getEl().down(B.getCellSelector())){B.onViewRefresh(A,A.getViewRange())}}},onRemoved:function(C){var D=this,A=D.liveWidgets,B=D.viewListeners,E;if(D.rendered){D.viewListeners=B&&Ext.destroy(B);if(!C){for(E in A){A[E].detachFromBody()}}}D.callParent(arguments)},onDestroy:function(){var C=this,G=C.liveWidgets,F=C.freeWidgetStack,E,D,B,A;if(C.rendered){for(E in G){D=G[E];D.$widgetRecord=D.$widgetColumn=null;delete D.getWidgetRecord;delete D.getWidgetColumn;D.destroy()}for(B=0,A=F.length;B<A;++B){F[B].destroy()}}C.freeWidgetStack=C.liveWidgets=null;C.callParent()},getWidget:function(B){var A=this.liveWidgets,C;if(B&&A){C=A[B.internalId]}return C||null},privates:{getCachedStyle:function(B,A){var C=this.cachedStyles;return C[A]||(C[A]=Ext.fly(B).getStyle(A))},getFreeWidget:function(){var B=this,A=B.freeWidgetStack?B.freeWidgetStack.pop():null;if(!A){A=Ext.widget(B.widget);A.resolveListenerScope=B.listenerScopeFn;A.getWidgetRecord=B.widgetRecordDecorator;A.getWidgetColumn=B.widgetColumnDecorator;A.dataIndex=B.dataIndex;A.measurer=B;A.ownerCmp=B.getView();A.isLayoutChild=B.returnFalse}return A},onBeforeRefresh:function(){var A=this.liveWidgets,B;for(B in A){A[B].detachFromBody()}},onItemAdd:function(C,I,K){var L=this,M=L.getView(),J=!!L.onWidgetAttach,N=L.dataIndex,O=L.isFixedSize,H=C.length,D,F,Q,P,E,B,G,A;if(L.isVisible(true)){for(D=0;D<H;D++){F=C[D];if(F.isNonData){continue}Q=M.getRowFromItem(K[D]);if(Q){P=Q.cells[L.getVisibleIndex()].firstChild;if(!O&&!A){A=L.lastBox.width-parseInt(L.getCachedStyle(P,"padding-left"),10)-parseInt(L.getCachedStyle(P,"padding-right"),10)}E=L.liveWidgets[F.internalId]=L.getFreeWidget();E.$widgetColumn=L;E.$widgetRecord=F;Ext.fly(P).empty();if(E.defaultBindProperty&&N){E.setConfig(E.defaultBindProperty,F.get(N))}if(J){Ext.callback(L.onWidgetAttach,L.scope,[L,E,F],0,L)}B=E.el||E.element;if(B){P.appendChild(B.dom);if(!O){E.setWidth(A)}E.reattachToBody()}else{if(!O){E.width=A}E.render(P)}G=E.getFocusEl();if(G){if(M.actionableMode){if(!G.isTabbable()){G.restoreTabbableState()}}else{if(G.isTabbable()){G.saveTabbableState()}}}}}}},onItemRemove:function(B,H,I){var J=this,C=J.liveWidgets,E,K,A,G,D,F;if(J.rendered){I=Ext.Array.from(I);G=I.length;for(D=0;D<G;D++){K=I[D];A=K.getAttribute("data-recordId");if(A&&(E=C[A])){delete C[A];J.freeWidgetStack.unshift(E);E.$widgetRecord=E.$widgetColumn=null;F=E.getFocusEl();if(F){if(F.isTabbable(true)){F.saveTabbableState({includeHidden:true})}F.blur()}E.detachFromBody()}}}},onItemUpdate:function(A,B,C){this.updateWidget(A)},onViewRefresh:function(J,M){var Q=this,H=J.all,G=!!Q.onWidgetAttach,I=Q.liveWidgets,R=Q.dataIndex,S=Q.isFixedSize,B,F,C,L,A,P,E,D,K,O,N;if(Q.isVisible(true)){Q.liveWidgets={};Ext.suspendLayouts();for(P=H.startIndex,E=0;P<=H.endIndex;P++,E++){D=M[E];if(D.isNonData){continue}A=D.internalId;B=J.getRow(H.item(P)).cells[Q.getVisibleIndex()].firstChild;F=Q.liveWidgets[A]=I[A]||Q.getFreeWidget();F.$widgetRecord=D;F.$widgetColumn=Q;delete I[A];O=Q.lastBox;if(O&&!S&&L===undefined){L=O.width-parseInt(Q.getCachedStyle(B,"padding-left"),10)-parseInt(Q.getCachedStyle(B,"padding-right"),10)}if(F.defaultBindProperty&&R){F.setConfig(F.defaultBindProperty,M[E].get(R))}if(G){Ext.callback(Q.onWidgetAttach,Q.scope,[Q,F,D],0,Q)}C=F.el||F.element;if(C){N=C.dom;if(N.parentNode!==B){Ext.fly(B).empty();B.appendChild(C.dom)}if(!S){F.setWidth(L)}F.reattachToBody()}else{if(!S){F.width=L}Ext.fly(B).empty();F.render(B)}}Ext.resumeLayouts(true);for(K in I){F=I[K];F.$widgetRecord=F.$widgetColumn=null;Q.freeWidgetStack.unshift(F);F.detachFromBody()}}},returnFalse:function(){return false},setupViewListeners:function(A){var B=this;B.viewListeners=A.on({refresh:B.onViewRefresh,itemupdate:B.onItemUpdate,itemadd:B.onItemAdd,itemremove:B.onItemRemove,scope:B,destroyable:true});if(Ext.isIE8){A.on("beforerefresh",B.onBeforeRefresh,B)}},updateWidget:function(A){var B=this.dataIndex,C;if(this.rendered){C=this.liveWidgets[A.internalId];if(C&&C.defaultBindProperty&&B){C.setConfig(C.defaultBindProperty,A.get(B))}}},widgetRecordDecorator:function(){return this.$widgetRecord},widgetColumnDecorator:function(){return this.$widgetColumn}}})