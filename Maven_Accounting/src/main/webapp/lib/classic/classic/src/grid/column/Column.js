Ext.define("Ext.grid.column.Column",{extend:"Ext.grid.header.Container",xtype:"gridcolumn",requires:["Ext.grid.ColumnComponentLayout","Ext.grid.ColumnLayout","Ext.app.bind.Template"],alternateClassName:"Ext.grid.Column",config:{triggerVisible:false,sorter:null},baseCls:Ext.baseCSSPrefix+"column-header",hoverCls:Ext.baseCSSPrefix+"column-header-over",ariaRole:"columnheader",enableFocusableContainer:false,sortState:null,possibleSortStates:["ASC","DESC"],ariaSortStates:{ASC:"ascending",DESC:"descending"},childEls:["titleEl","triggerEl","textEl","textContainerEl"],headerWrap:false,renderTpl:['<div id="{id}-titleEl" data-ref="titleEl" role="presentation"','{tipMarkup}class="',Ext.baseCSSPrefix,'column-header-inner<tpl if="!$comp.isContainer"> ',Ext.baseCSSPrefix,"leaf-column-header</tpl>",'<tpl if="empty"> ',Ext.baseCSSPrefix,'column-header-inner-empty</tpl>">','<div id="{id}-textContainerEl" data-ref="textContainerEl" role="presentation" class="',Ext.baseCSSPrefix,'column-header-text-container">','<div role="presentation" class="',Ext.baseCSSPrefix,'column-header-text-wrapper">','<div id="{id}-textEl" data-ref="textEl" role="presentation" class="',Ext.baseCSSPrefix,"column-header-text",'{childElCls}">','<span role="presentation" class="',Ext.baseCSSPrefix,'column-header-text-inner">{text}</span>',"</div>","</div>","</div>",'<tpl if="!menuDisabled">','<div id="{id}-triggerEl" data-ref="triggerEl" role="presentation" class="',Ext.baseCSSPrefix,"column-header-trigger",'{childElCls}" style="{triggerStyle}"></div>',"</tpl>","</div>","{%this.renderContainer(out,values)%}"],dataIndex:null,text:"&#160;",menuText:null,emptyCellText:"&#160;",sortable:true,resizable:true,hideable:true,menuDisabled:false,renderer:false,align:"left",draggable:true,tooltipType:"qtip",initDraggable:Ext.emptyFn,tdCls:"",producesHTML:true,ignoreExport:false,isHeader:true,isColumn:true,tabIndex:-1,ascSortCls:Ext.baseCSSPrefix+"column-header-sort-ASC",descSortCls:Ext.baseCSSPrefix+"column-header-sort-DESC",componentLayout:"columncomponent",groupSubHeaderCls:Ext.baseCSSPrefix+"group-sub-header",groupHeaderCls:Ext.baseCSSPrefix+"group-header",clickTargetName:"titleEl",detachOnRemove:true,initResizable:Ext.emptyFn,rendererNames:{column:"renderer",edit:"editRenderer",summary:"summaryRenderer"},formatterNames:{column:"formatter",edit:"editFormatter",summary:"summaryFormatter"},initComponent:function(){var A=this;if(!A.rendererScope){A.rendererScope=A.scope}if(A.header!=null){A.text=A.header;A.header=null}if(A.cellWrap){A.tdCls=(A.tdCls||"")+" "+Ext.baseCSSPrefix+"wrap-cell"}if(A.columns!=null){A.isGroupHeader=true;A.ariaRole="presentation";if(A.dataIndex){Ext.raise("Ext.grid.column.Column: Group header may not accept a dataIndex")}if((A.width&&A.width!==Ext.grid.header.Container.prototype.defaultWidth)||A.flex){Ext.raise("Ext.grid.column.Column: Group header does not support setting explicit widths or flexs. The group header width is calculated by the sum of its children.")}A.items=A.columns;A.columns=A.flex=A.width=null;A.cls=(A.cls||"")+" "+A.groupHeaderCls;A.sortable=A.resizable=false;A.align="center"}else{if(A.flex){A.minWidth=A.minWidth||Ext.grid.plugin.HeaderResizer.prototype.minColWidth}}A.addCls(Ext.baseCSSPrefix+"column-header-align-"+A.align);A.setupRenderer();A.setupRenderer("edit");A.setupRenderer("summary");A.callParent(arguments)},onAdded:function(B,G,A){var C=this,F,D,E;C.callParent([B,G,A]);if(!C.headerId){D=C.up("tablepanel");E=D?D.ownerGrid:C.getRootHeaderCt();E.headerCounter=(E.headerCounter||0)+1;C.headerId="h"+E.headerCounter}if(!C.stateId){C.stateId=C.initialConfig.id||C.headerId}F=C.getSorter();if(F&&!F.initialConfig.id){F.setId((C.dataIndex||C.stateId)+"-sorter")}},applySorter:function(A){return this.getRootHeaderCt().up("tablepanel").store.getData().getSorters().decodeSorter(A)},bindFormatter:function(B){var A=this;return function(C){return B.format(C,B.scope||A.rendererScope||A.resolveListenerScope())}},bindRenderer:function(B){var A=this;if(B in Ext.util.Format){Ext.log.warn('Use "formatter" config instead of "renderer" to use Ext.util.Format to format cell values')}A.hasCustomRenderer=true;return function(){return Ext.callback(B,A.rendererScope,arguments,0,A)}},setupRenderer:function(B){B=B||"column";var C=this,F=C[C.formatterNames[B]],D=C[C.rendererNames[B]],A=B==="column",G,E;if(!F){if(D){if(typeof D==="string"){D=C[C.rendererNames[B]]=C.bindRenderer(D);E=true}if(A){C.hasCustomRenderer=E||D.length>1}}else{if(A&&C.defaultRenderer){C.renderer=C.defaultRenderer;C.usingDefaultRenderer=true}}}else{G=F.indexOf("this.")===0;if(G){F=F.substring(5)}F=Ext.app.bind.Template.prototype.parseFormat(F);C[C.formatterNames[B]]=null;if(G){F.scope=null}else{if(!Ext.util.Format[F.fmt]){Ext.raise('Invalid formatter specified: "'+F.fmt+'"')}}C[C.rendererNames[B]]=C.bindFormatter(F)}},getView:function(){var A=this.getRootHeaderCt();if(A){return A.view}},onFocusLeave:function(A){this.callParent([A]);if(this.activeMenu){this.activeMenu.hide()}},initItems:function(){var A=this;A.callParent(arguments);if(A.isGroupHeader){if(A.config.hidden||!A.hasVisibleChildColumns()){A.hide()}}},hasVisibleChildColumns:function(){var B=this.items.items,A=B.length,C,D;for(C=0;C<A;++C){D=B[C];if(D.isColumn&&!D.hidden){return true}}return false},onAdd:function(B){var A=this;if(B.isColumn){B.isSubHeader=true;B.addCls(A.groupSubHeaderCls)}if(A.isGroupHeader&&A.hidden&&A.hasVisibleChildColumns()){A.show()}A.callParent([B])},onRemove:function(C,A){var B=this;if(C.isSubHeader){C.isSubHeader=false;C.removeCls(B.groupSubHeaderCls)}B.callParent([C,A]);if(!(B.destroyed||B.destroying)&&!B.hasVisibleChildColumns()&&!B.ownerCt.isNested()){B.hide()}},initRenderData:function(){var B=this,E="",C=B.tooltip,D=B.text,A=B.tooltipType==="qtip"?"data-qtip":"title";if(!Ext.isEmpty(C)){E=A+'="'+C+'" '}return Ext.applyIf(B.callParent(arguments),{text:D,empty:D==="&#160;"||D===" "||D==="",menuDisabled:B.menuDisabled,tipMarkup:E,triggerStyle:this.getTriggerVisible()?"display:block":""})},applyColumnState:function(A,C){var G=this,I=G.getSorter(),F=C&&C.sorters,E,D,B,H;if(I&&F&&(E=F.length)){H=I.getId();for(D=0;!B&&D<E;D++){if(F[D].id===H){I.setDirection(F[D].direction);F[D]=I;break}}}G.applyColumnsState(A.columns);if(A.hidden!=null){G.hidden=A.hidden}if(A.locked!=null){G.locked=A.locked}if(A.sortable!=null){G.sortable=A.sortable}if(A.width!=null){G.flex=null;G.width=A.width}else{if(A.flex!=null){G.width=null;G.flex=A.flex}}},getColumnState:function(){var E=this,B=E.items.items,A=B?B.length:0,D,C=[],F={id:E.getStateId()};E.savePropsToState(["hidden","sortable","locked","flex","width"],F);if(E.isGroupHeader){for(D=0;D<A;D++){C.push(B[D].getColumnState())}if(C.length){F.columns=C}}if("width" in F){delete F.flex}return F},setText:function(A){this.text=A;if(this.rendered){this.textEl.setHtml(A)}},getIndex:function(){return this.isGroupColumn?false:this.getRootHeaderCt().getHeaderIndex(this)},getVisibleIndex:function(){return this.visibleIndex!=null?this.visibleIndex:this.isGroupColumn?false:Ext.Array.indexOf(this.getRootHeaderCt().getVisibleGridColumns(),this)},getLabelChain:function(){var C=this,B=[],A;while(A=C.up("headercontainer")){if(A.text){B.unshift(Ext.util.Format.stripTags(A.text))}C=A}return B},beforeRender:function(){var C=this,A=C.getRootHeaderCt(),D=C.isSortable(),E=[],B;C.callParent();if(!D&&!C.groupable&&!C.lockable&&(A.grid.enableColumnHide===false||!A.getHideableColumns().length)){C.menuDisabled=true}if(C.cellWrap){C.variableRowHeight=true}B=C.ariaRenderAttributes||(C.ariaRenderAttributes={});B["aria-readonly"]=true;if(D){B["aria-sort"]=C.ariaSortStates[C.sortState]}if(C.isSubHeader){E=C.getLabelChain();if(C.text){E.push(Ext.util.Format.stripTags(C.text))}if(E.length){B["aria-label"]=E.join(" ")}}C.protoEl.unselectable()},getTriggerElWidth:function(){var C=this,B=C.triggerEl,A=C.self.triggerElWidth;if(B&&A===undefined){B.setStyle("display","block");A=C.self.triggerElWidth=B.getWidth();B.setStyle("display","")}return A},afterComponentLayout:function(D,A,B,F){var E=this,C=E.getRootHeaderCt();E.callParent(arguments);if(C&&(B!=null||E.flex)&&D!==B){C.onHeaderResize(E,D)}},onDestroy:function(){var A=this;Ext.destroy(A.field);A.field=null;A.callParent(arguments)},onTitleMouseOver:function(){this.titleEl.addCls(this.hoverCls)},onTitleMouseOut:function(){this.titleEl.removeCls(this.hoverCls)},onDownKey:function(A){if(this.triggerEl){this.onTitleElClick(A,this.triggerEl.dom||this.el.dom)}},onEnterKey:function(A){this.onTitleElClick(A,this.el.dom)},onTitleElDblClick:function(D){var B=this,A,C,E;if(B.isAtStartEdge(D)){A=B.previousNode("gridcolumn:not([hidden]):not([isGroupHeader])");if(A&&A.getRootHeaderCt()===B.getRootHeaderCt()){A.autoSize()}}else{if(B.isAtEndEdge(D)){if(B.isGroupHeader&&D.getPoint().isContainedBy(B.layout.innerCt)){C=B.query("gridcolumn:not([hidden]):not([isGroupHeader])");B.getRootHeaderCt().autoSizeColumn(C[C.length-1]);return }else{E=B.getRootHeaderCt();if(E.visibleColumnManager.getColumns().length===1&&E.forceFit){return }}B.autoSize()}}},autoSize:function(){var B=this,C,E,A,D;if(B.isGroupHeader){C=B.query("gridcolumn:not([hidden]):not([isGroupHeader])");E=C.length;D=B.getRootHeaderCt();Ext.suspendLayouts();for(A=0;A<E;A++){D.autoSizeColumn(C[A])}Ext.resumeLayouts(true);return }B.getRootHeaderCt().autoSizeColumn(B)},onTitleElClick:function(F,C,D){var E=this,A,B;if(F.pointerType==="touch"){B=E.previousSibling(":not([hidden])");if(!E.menuDisabled&&E.isAtEndEdge(F,parseInt(E.triggerEl.getStyle("width"),10))){if(!E.menuDisabled){A=E}}else{if(B&&!B.menuDisabled&&E.isAtStartEdge(F)){A=B}}}else{A=E.triggerEl&&(F.target===E.triggerEl.dom||C===E.triggerEl||F.within(E.triggerEl))?E:null}if(D!==false&&(!A&&!E.isAtStartEdge(F)&&!E.isAtEndEdge(F)||F.getKey())){E.toggleSortState()}return A},processEvent:function(E,B,A,C,D,F){return this.fireEvent.apply(this,arguments)},isSortable:function(){var B=this.getRootHeaderCt(),A=B?B.grid:null,C=this.sortable;if(A&&A.sortableColumns===false){C=false}return C},toggleSortState:function(){if(this.isSortable()){this.sort()}},sort:function(D){var C=this,B=C.up("tablepanel"),A=B.store,E=C.getSorter();Ext.suspendLayouts();C.sorting=true;if(E){if(D){E.setDirection(D)}A.sort(E,B.multiColumnSort?"multi":"replace")}else{A.sort(C.getSortParam(),D,B.multiColumnSort?"multi":"replace")}delete C.sorting;Ext.resumeLayouts(true)},getSortParam:function(){return this.dataIndex},setSortState:function(H){var D=this,E=H&&H.getDirection(),G=D.ascSortCls,B=D.descSortCls,C=D.getRootHeaderCt(),A=D.ariaEl.dom,F;switch(E){case"DESC":if(!D.hasCls(B)){D.addCls(B);D.sortState="DESC";F=true}D.removeCls(G);break;case"ASC":if(!D.hasCls(G)){D.addCls(G);D.sortState="ASC";F=true}D.removeCls(B);break;default:D.removeCls([G,B]);D.sortState=null;break}if(A){if(D.sortState){A.setAttribute("aria-sort",D.ariaSortStates[D.sortState])}else{A.removeAttribute("aria-sort")}}if(F){C.fireEvent("sortchange",C,D,E)}},isHideable:function(){var A={hideCandidate:this,result:this.hideable};if(A.result){this.ownerCt.bubble(this.hasOtherMenuEnabledChildren,null,[A])}return A.result},hasOtherMenuEnabledChildren:function(A){var B,C;if(!this.isXType("headercontainer")){A.result=false;return false}B=this.query(">gridcolumn:not([hidden]):not([menuDisabled])");C=B.length;if(Ext.Array.contains(B,A.hideCandidate)){C--}if(C){return false}A.hideCandidate=this},isLockable:function(){var A={result:this.lockable!==false};if(A.result){this.ownerCt.bubble(this.hasMultipleVisibleChildren,null,[A])}return A.result},isLocked:function(){return this.locked||!!this.up("[isColumn][locked]","[isRootHeader]")},hasMultipleVisibleChildren:function(A){if(!this.isXType("headercontainer")){A.result=false;return false}if(this.query(">gridcolumn:not([hidden])").length>1){return false}},hide:function(){var C=this,B=C.getRootHeaderCt(),A=C.getRefOwner();if(A.constructing){C.callParent();return C}if(C.rendered&&!C.isVisible()){return C}if(B.forceFit){C.visibleSiblingCount=B.getVisibleGridColumns().length-1;if(C.flex){C.savedWidth=C.getWidth();C.flex=null}}B.beginChildHide();Ext.suspendLayouts();if(A.isGroupHeader){if(C.isNestedGroupHeader()){A.hide()}if(C.isSubHeader&&!C.isGroupHeader&&A.query(">gridcolumn:not([hidden])").length===1){A.lastHiddenHeader=C}}C.callParent();B.endChildHide();B.onHeaderHide(C);Ext.resumeLayouts(true);return C},show:function(){var C=this,A=C.getRootHeaderCt(),B=C.getRefOwner();if(C.isVisible()){return C}if(B.isGroupHeader){B.lastHiddenHeader=null}if(C.rendered){if(A.forceFit){A.applyForceFit(C)}}Ext.suspendLayouts();if(C.isSubHeader&&B.hidden){B.show(false,true)}C.callParent(arguments);if(C.isGroupHeader){C.maybeShowNestedGroupHeader()}B=C.getRootHeaderCt();if(B){B.onHeaderShow(C)}Ext.resumeLayouts(true);return C},shouldUpdateCell:function(B,D){if(!this.preventUpdate){if(this.hasCustomRenderer){return 1}if(D){var A=D.length,C,E;for(C=0;C<A;++C){E=D[C];if(E===this.dataIndex||E===B.idProperty){return 2}}}else{return 2}}},getCellWidth:function(){var B=this,A;if(B.rendered&&B.componentLayout&&B.componentLayout.lastComponentSize){A=B.componentLayout.lastComponentSize.width}else{if(B.width){A=B.width}else{if(!B.isColumn){A=B.getTableWidth()}}}return A},getCellId:function(){return Ext.baseCSSPrefix+"grid-cell-"+this.getItemId()},getCellSelector:function(){var A=this.getView();return(A?A.getCellSelector():"")+"."+this.getCellId()},getCellInnerSelector:function(){return this.getCellSelector()+" ."+Ext.baseCSSPrefix+"grid-cell-inner"},isAtStartEdge:function(A){var B=A.getXY()[0]-this.getX();if(B<0&&this.getIndex()===0){return false}return(B<this.getHandleWidth(A))},isAtEndEdge:function(B,A){return(this.getX()+this.getWidth()-B.getXY()[0]<=(A||this.getHandleWidth(B)))},getHandleWidth:function(A){return A.pointerType==="touch"?10:4},setMenuActive:function(A){this.activeMenu=A;this.titleEl[A?"addCls":"removeCls"](this.headerOpenCls)},deprecated:{5:{methods:{bindRenderer:function(A){return function(B){return Ext.util.Format[A](B)}}}}}})