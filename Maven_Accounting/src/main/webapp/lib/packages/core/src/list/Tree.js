Ext.define("Ext.list.Tree",{extend:"Ext.Widget",xtype:"treelist",requires:["Ext.list.RootTreeItem"],expanderFirstCls:Ext.baseCSSPrefix+"treelist-expander-first",expanderOnlyCls:Ext.baseCSSPrefix+"treelist-expander-only",highlightPathCls:Ext.baseCSSPrefix+"treelist-highlight-path",microCls:Ext.baseCSSPrefix+"treelist-micro",uiPrefix:Ext.baseCSSPrefix+"treelist-",element:{reference:"element",cls:Ext.baseCSSPrefix+"treelist "+Ext.baseCSSPrefix+"unselectable",listeners:{click:"onClick",mouseenter:"onMouseEnter",mouseleave:"onMouseLeave",mouseover:"onMouseOver"},children:[{reference:"toolsElement",cls:Ext.baseCSSPrefix+"treelist-toolstrip",listeners:{click:"onToolStripClick",mouseover:"onToolStripMouseOver"}}]},cachedConfig:{animation:{duration:500,easing:"ease"},expanderFirst:true,expanderOnly:true},config:{defaults:{xtype:"treelistitem"},highlightPath:null,iconSize:null,indent:null,micro:null,overItem:null,selection:null,selectOnExpander:false,singleExpand:null,store:null,ui:null},twoWayBindable:{selection:1},publishes:{selection:1},defaultBindProperty:"store",constructor:function(A){this.callParent([A]);this.publishState("selection",this.getSelection())},beforeLayout:function(){this.syncIconSize()},destroy:function(){var A=this;A.destroying=true;A.unfloatAll();A.activeFloater=null;A.setSelection(null);A.setStore(null);A.callParent()},updateOverItem:function(E,A){var D={},C=2,F,B;for(F=E;F;F=this.getItem(B.parentNode)){B=F.getNode();D[B.internalId]=true;F.setOver(C);C=1}if(A){for(F=A;F;F=this.getItem(B.parentNode)){B=F.getNode();if(D[B.internalId]){break}F.setOver(0)}}},applySelection:function(C,A){var B=this.getStore();if(!B){C=null}if(C&&C.get("selectable")===false){C=A}return C},updateSelection:function(B,A){var D=this,C;if(!D.destroying){C=D.getItem(A);if(C){C.setSelected(false)}C=D.getItem(B);if(C){C.setSelected(true)}D.fireEvent("selectionchange",D,B)}},applyStore:function(A){return A&&Ext.StoreManager.lookup(A,"tree")},updateStore:function(B,D){var C=this,A;if(D){if(D.getAutoDestroy()){D.destroy()}else{C.storeListeners.destroy()}C.removeRoot();C.storeListeners=null}if(B){C.storeListeners=B.on({destroyable:true,scope:C,nodeappend:C.onNodeAppend,nodecollapse:C.onNodeCollapse,nodeexpand:C.onNodeExpand,nodeinsert:C.onNodeInsert,noderemove:C.onNodeRemove,rootchange:C.onRootChange,update:C.onNodeUpdate});A=B.getRoot();if(A){C.createRootItem(A)}}if(!C.destroying){C.updateLayout()}},updateExpanderFirst:function(A){this.element.toggleCls(this.expanderFirstCls,A)},updateExpanderOnly:function(A){this.element.toggleCls(this.expanderOnlyCls,!A)},updateHighlightPath:function(A){this.element.toggleCls(this.highlightPathCls,A)},updateMicro:function(A){var B=this;if(!A){B.unfloatAll();B.activeFloater=null}B.element.toggleCls(B.microCls,A)},updateUi:function(D,A){var C=this.element,B=this.uiPrefix;if(A){C.removeCls(B+A)}if(D){C.addCls(B+D)}delete this.iconSize;this.syncIconSize()},getItem:function(B){var C=this.itemMap,A;if(B&&C){A=C[B.internalId]}return A||null},getItemConfig:function(B,A){return Ext.apply({parentItem:A.isRootListItem?null:A,owner:this,node:B,indent:this.getIndent()},this.getDefaults())},privates:{checkForOutsideClick:function(B){var A=this.activeFloater;if(!A.element.contains(B.target)){this.unfloatAll()}},collapsingForExpand:false,createItem:function(D,B){var C=Ext.create(this.getItemConfig(D,B)),A;if(B.isRootListItem){A=C.getToolElement();if(A){this.toolsElement.appendChild(A);A.dom.setAttribute("data-recordId",D.internalId);A.isTool=true}}return(this.itemMap[D.internalId]=C)},createRootItem:function(A){var C=this,B;C.itemMap={};C.rootItem=B=new Ext.list.RootTreeItem({indent:C.getIndent(),node:A,owner:C});C.element.appendChild(B.element);C.itemMap[A.internalId]=B},floatItem:function(D,B){var C=this,A;if(D.getFloated()){return }C.unfloatAll();C.activeFloater=A=D;C.floatedByHover=B;D.setFloated(true);if(B){D.getToolElement().on("mouseleave",C.checkForMouseLeave,C);A.element.on("mouseleave",C.checkForMouseLeave,C)}else{Ext.on("mousedown",C.checkForOutsideClick,C)}},onClick:function(B){var A=B.getTarget("[data-recordId]"),C;if(A){C=A.getAttribute("data-recordId");A=this.itemMap[C];if(A){A.onClick(B)}}},onMouseEnter:function(A){this.onMouseOver(A)},onMouseLeave:function(){this.setOverItem(null)},onMouseOver:function(B){var A=Ext.Component.fromElement(B.getTarget());this.setOverItem(A&&A.isTreeListItem&&A)},checkForMouseLeave:function(C){var B=this.activeFloater,A=C.getRelatedTarget();if(B){if(A!==B.getToolElement().dom&&!B.element.contains(A)){this.unfloatAll()}}},onNodeAppend:function(A,C){if(A){var B=this.itemMap[A.internalId];if(B){B.nodeInsert(C,null)}}},onNodeCollapse:function(B){var A=this.itemMap[B.internalId];if(A){A.nodeCollapse(B,this.collapsingForExpand)}},onNodeExpand:function(F){var E=this,D=E.itemMap[F.internalId],G,B,C,A,H;if(D){if(!D.isRootItem&&E.getSingleExpand()){E.collapsingForExpand=true;A=(D.getParentItem()||E.rootItem).getNode();G=A.childNodes;for(C=0,B=G.length;C<B;++C){H=G[C];if(H!==F){H.collapse()}}E.collapsing=false}D.nodeExpand(F)}},onNodeInsert:function(A,D,B){var C=this.itemMap[A.internalId];if(C){C.nodeInsert(D,B)}},onNodeRemove:function(A,D,B){if(A&&!B){var C=this.itemMap[A.internalId];if(C){C.nodeRemove(D)}}},onNodeUpdate:function(A,E,B,D){var C=this.itemMap[E.internalId];if(C){C.nodeUpdate(E,D)}},onRootChange:function(A){this.removeRoot();if(A){this.createRootItem(A)}this.updateLayout()},removeItem:function(A){var B=this.itemMap;if(B){delete B[A.internalId]}},removeRoot:function(){var B=this,A=B.rootItem;if(A){B.element.removeChild(A.element);B.rootItem=B.itemMap=Ext.destroy(A)}},onToolStripClick:function(B){var A=B.getTarget("[data-recordId]"),C;if(A){C=A.getAttribute("data-recordId");A=this.itemMap[C];if(A){if(A===this.activeFloater){this.unfloatAll()}else{this.floatItem(A,false)}}}},onToolStripMouseOver:function(B){var A=B.getTarget("[data-recordId]"),C;if(A){C=A.getAttribute("data-recordId");A=this.itemMap[C];if(A){this.floatItem(A,true)}}},syncIconSize:function(){var B=this,A=B.iconSize||(B.iconSize=parseInt(B.element.getStyle("background-position"),10));B.setIconSize(A)},unfloatAll:function(){var B=this,A=B.activeFloater;if(A){A.setFloated(false);B.activeFloater=null;if(B.floatedByHover){A.element.un("mouseleave",B.checkForMouseLeave,B)}else{Ext.un("mousedown",B.checkForOutsideClick,B)}}},defaultIconSize:22,updateIconSize:function(A){this.setIndent(A||this.defaultIconSize)},updateIndent:function(B){var A=this.rootItem;if(A){A.setIndent(B)}}}})