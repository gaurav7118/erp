Ext.define("Ext.grid.NavigationModel",{extend:"Ext.view.NavigationModel",alias:"view.navigation.grid",focusCls:Ext.baseCSSPrefix+"grid-item-focused",getViewListeners:function(){var A=this;return{focusmove:{element:"el",fn:A.onFocusMove},containermousedown:A.onContainerMouseDown,cellmousedown:A.onCellMouseDown,cellclick:A.onCellClick,itemmousedown:A.onItemMouseDown,itemclick:A.onItemClick,itemcontextmenu:A.onItemClick,scope:A}},initKeyNav:function(A){var B=this;if(!B.keyNav){B.keyNav=[];B.position=new Ext.grid.CellContext(A)}B.keyNav.push(new Ext.util.KeyNav({target:A,ignoreInputFields:true,eventName:"itemkeydown",defaultEventAction:"stopEvent",processEvent:B.processViewEvent,up:B.onKeyUp,down:B.onKeyDown,right:B.onKeyRight,left:B.onKeyLeft,pageDown:B.onKeyPageDown,pageUp:B.onKeyPageUp,home:B.onKeyHome,end:B.onKeyEnd,space:B.onKeySpace,enter:B.onKeyEnter,esc:B.onKeyEsc,113:B.onKeyF2,tab:B.onKeyTab,A:{ctrl:true,handler:B.onSelectAllKeyPress},scope:B}))},addKeyBindings:function(C){var A=this.keyNav.length,B;for(B=0;B<A;B++){this.keyNav[B].addBindings(C)}},enable:function(){var A=this.keyNav.length,B;for(B=0;B<A;B++){this.keyNav[B].enable()}this.disabled=false},disable:function(){var A=this.keyNav.length,B;for(B=0;B<A;B++){this.keyNav[B].disable()}this.disabled=true},processViewEvent:function(B,A,F,C,E){var D=E.getKey();if(B.actionableMode){this.map.ignoreInputFields=false;if(D===E.TAB||D===E.ESC||D===E.F2){return E}}else{this.map.ignoreInputFields=true;return D===E.TAB?null:E}},onCellMouseDown:function(F,G,E,D,I,C,B){var A=Ext.Component.fromElement(B.target,G),H;if(F.actionableMode&&(B.getTarget(null,null,true).isTabbable()||((H=Ext.ComponentManager.getActiveComponent())&&H.owns(B)))){return }if(B.pointerType!=="touch"){this.setPosition(B.position,null,B)}if(A&&A.isFocusable&&A.isFocusable()){F.setActionableMode(true,B.position);A.focus()}},onCellClick:function(F,G,E,C,J,B,H){var D=this,A=Ext.Component.fromElement(H.target,G),I=A&&A.isFocusable&&A.isFocusable();if(F.actionableMode){if(!H.position.isEqual(F.actionPosition)){if(!I){D.setPosition(H.position,null,H)}}D.fireEvent("navigate",{view:F,navigationModel:D,keyEvent:H,previousPosition:D.previousPosition,previousRecordIndex:D.previousRecordIndex,previousRecord:D.previousRecord,previousItem:D.previousItem,previousCell:D.previousCell,previousColumnIndex:D.previousColumnIndex,previousColumn:D.previousColumn,position:H.position,recordIndex:H.position.rowIdx,record:H.position.record,item:H.item,cell:H.position.cellElement,columnIndex:H.position.colIdx,column:H.position.column})}else{if(D.position.isEqual(H.position)||I){D.fireNavigateEvent(H)}else{D.setPosition(H.position,null,H)}}},onFocusMove:function(F){var A=F.target,C=Ext.Component.fromElement(F.delegatedTarget,null,"tableview"),A=F.target,B,E,D;if(C&&Ext.fly(A).is(C.cellSelector)){if(C.actionableModeTabbing){return }C.ownerGrid.setActionableMode(false);B=C.getRecord(A);E=C.getHeaderByCell(A);if(B&&E){D=new Ext.grid.CellContext(C).setPosition(B,E);if(!D.isEqual(this.position)){this.setPosition(D)}}}},onItemMouseDown:function(B,A,F,D,C){var E=this;if(!C.position.cellElement&&(C.pointerType!=="touch")){E.getClosestCell(C);E.setPosition(C.position,null,C)}},onItemClick:function(C,B,E,D,A){if(!A.position.cellElement){this.getClosestCell(A);if(A.pointerType==="touch"){this.setPosition(A.position,null,A)}this.fireNavigateEvent(A)}},getClosestCell:function(A){var F=A.position,B=F.cellElement,I,D,G,E,C,H;if(!B){I=A.getX();D=F.view.getVisibleColumnManager().getColumns();G=D.length;for(E=0;E<G;E++){C=D[E];H=D[E].getBox();if(I>=H.left&&I<H.right){F.setColumn(D[E]);F.rowElement=F.getRow(true);F.cellElement=F.getCell(true);return }}}},deferSetPosition:function(C,B,D,F,A,G){var E=this.view.getFocusTask();E.delay(C,this.setPosition,this,[B,D,F,A,G]);return E},setPosition:function(F,L,M,Q,H){var N=this,P,J,I,A,C,B,D,K,G,O=F==null&&L==null,E=N.record==null&&N.recordIndex==null&&N.item==null;if(F&&F.isCellContext){P=F.view}else{if(M&&M.view){P=M.view}else{if(N.lastFocused){P=N.lastFocused.view}else{P=N.view}}}P.getFocusTask().cancel();if(P.destroyed||!P.refreshCounter||!P.ownerCt||O&&E||!P.all.getCount()){return }I=P.getSelectionModel();A=P.dataSource;C=P.getVisibleColumnManager();if(F&&F.isCellContext){K=F.record;B=F.rowIdx;D=F.colIdx;G=F.column;if(A.indexOf(K)===-1){J=P.getScrollable();N.recordIndex=-1;if(J.getPosition().y>=J.getMaxPosition().y-P.all.last(true).offsetHeight){F.rowIdx--}B=Math.min(F.rowIdx,A.getCount()-1);D=Math.min(D,C.getColumns().length);K=A.getAt(B);G=C.getColumns()[D]}}else{if(O){K=B=null}else{if(L==null){L=N.lastFocused?N.lastFocused.column:0}if(typeof F==="number"){B=Math.max(Math.min(F,A.getCount()-1),0);K=A.getAt(F)}else{if(F.isEntity){K=F;B=A.indexOf(K)}else{if(F.tagName){K=P.getRecord(F);B=A.indexOf(K);if(B===-1){K=null}}else{if(E){return }O=true;K=B=null}}}}if(K){if(B===-1){N.recordIndex=-1;K=A.getAt(0);B=0;L=null}if(L==null){if(!(G=N.column)){D=0;G=C.getColumns()[0]}}else{if(typeof L==="number"){G=C.getColumns()[L];D=L}else{G=L;D=C.indexOf(L)}}}else{O=true;G=D=null}}if(B===N.recordIndex&&D===N.columnIndex&&P===N.position.view){return N.focusPosition(N.position)}if(N.cell){N.cell.removeCls(N.focusCls)}N.previousRecordIndex=N.recordIndex;N.previousRecord=N.record;N.previousItem=N.item;N.previousCell=N.cell;N.previousColumn=N.column;N.previousColumnIndex=N.columnIndex;N.previousPosition=N.position.clone();N.selectionStart=I.selectionStart;N.position.setAll(P,N.recordIndex=B,N.columnIndex=D,N.record=K,N.column=G);if(O){N.item=N.cell=null}else{N.focusPosition(N.position,H)}if(!Q){I.fireEvent("focuschange",I,N.previousRecord,N.record);P.fireEvent("rowfocus",N.record,N.item,N.recordIndex);P.fireEvent("cellfocus",N.record,N.cell,N.position)}if(M&&!H&&N.cell!==N.previousCell){N.fireNavigateEvent(M)}},focusPosition:function(A){var C=this,B,D;C.item=C.cell=null;if(A&&A.record&&A.column){B=A.view;if(A.rowElement){D=C.item=A.rowElement}else{D=B.getRowByRecord(A.record)}if(D){C.cell=A.cellElement||Ext.fly(D).down(A.column.getCellSelector(),true);if(C.cell){C.cell=new Ext.dom.Fly(C.cell);B.lastFocused=C.lastFocused=C.position.clone();C.focusItem(C.cell);B.focusEl=C.cell}else{C.position.setAll();C.record=C.column=C.recordIndex=C.columnIndex=null}}else{D=B.dataSource.indexOf(A.record);C.position.setAll();C.record=C.column=C.recordIndex=C.columnIndex=null;if(D!==-1&&B.bufferedRenderer){C.lastKeyEvent=null;B.bufferedRenderer.scrollTo(D,false,C.afterBufferedScrollTo,C)}}}},focusItem:function(A){A.addCls(this.focusCls);A.focus()},getCell:function(){return this.cell},getPosition:function(C){var E=this,A=E.position,D,B,F;if(A.record&&A.column){if(C){return A}B=A.view;F=B.dataSource;D=F.indexOf(A.record);if(D===-1){D=A.rowIdx;if(!(A.record=F.getAt(D))){D=-1}}if(D===-1||B.getVisibleColumnManager().indexOf(A.column)===-1){A.setAll();E.record=E.column=E.recordIndex=E.columnIndex=null}else{return A}}return null},getLastFocused:function(){var C=this,A,B=C.lastFocused;if(B&&B.record&&B.column){A=B.view;if(A.dataSource.indexOf(B.record)!==-1&&A.getVisibleColumnManager().indexOf(B.column)!==-1){return B}}},onKeyTab:function(E){var B=!E.shiftKey,C=E.position.clone(),F=C.view,J=E.position.cellElement,G=Ext.fly(J).findTabbableElements(),H,I=F.ownerGrid.actionables,D=I.length,A;E.preventDefault();H=G[Ext.Array.indexOf(G,E.target)+(B?1:-1)];while(!H&&(J=J[B?"nextSibling":"previousSibling"])){C.setColumn(F.getHeaderByCell(J));for(A=0;A<D;A++){I[A].activateCell(C)}if((G=Ext.fly(J).findTabbableElements()).length){H=G[B?0:G.length-1]}}if(H){this.actionPosition=C.view.actionPosition=C;Ext.fly(H).focus();return }if(Ext.isIE){F.el.focus()}F.onRowExit(E.item,E.item[B?"nextSibling":"previousSibling"],B)},onKeyUp:function(B){var A=B.view.walkRecs(B.record,-1),C=this.getPosition();if(A){C.setRow(A);if(!C.getCell(true)){C.navigate(-1)}this.setPosition(C,null,B)}},onKeyDown:function(B){var A=B.record.isExpandingOrCollapsing?null:B.view.walkRecs(B.record,1),C=this.getPosition();if(A){C.setRow(A);if(!C.getCell(true)){C.navigate(-1)}this.setPosition(C,null,B)}},onKeyRight:function(B){var A=this.move("right",B);if(A){this.setPosition(A,null,B)}},onKeyLeft:function(B){var A=this.move("left",B);if(A){this.setPosition(A,null,B)}},onKeyEnter:function(B){var A=["cellclick",B.view,B.position.cellElement,B.position.colIdx,B.record,B.position.rowElement,B.recordIndex,B],C=B.position.getCell();if(C){if(!C.query('[tabIndex="-1"]').length){B.stopEvent();B.view.fireEvent.apply(B.view,A);A[0]="celldblclick";B.view.fireEvent.apply(B.view,A)}if(!this.view.actionableMode){this.view.ownerGrid.setActionableMode(true,this.getPosition())}}},onKeyF2:function(B){var A=this.view.ownerGrid,C=A.actionableMode;A.setActionableMode(!C,C?null:this.getPosition())},onKeyEsc:function(A){this.view.ownerGrid.setActionableMode(false)},move:function(B,D){var C=this,A=C.getPosition();if(A&&A.record){return A.view.walkCells(A,B,D.shiftKey&&(B==="right"||B==="left")?C.vetoRowChange:null,C)}return null},vetoRowChange:function(A){return this.getPosition().record===A.record},onKeyPageDown:function(E){var D=this,A=E.view,F=D.getRowsVisible(),C,B;if(F){if(A.bufferedRenderer){C=Math.min(E.recordIndex+F,A.dataSource.getCount()-1);D.lastKeyEvent=E;A.bufferedRenderer.scrollTo(C,false,D.afterBufferedScrollTo,D)}else{B=A.walkRecs(E.record,F);D.setPosition(B,null,E)}}},onKeyPageUp:function(E){var D=this,A=E.view,F=D.getRowsVisible(),C,B;if(F){if(A.bufferedRenderer){C=Math.max(E.recordIndex-F,0);D.lastKeyEvent=E;A.bufferedRenderer.scrollTo(C,false,D.afterBufferedScrollTo,D)}else{B=A.walkRecs(E.record,-F);D.setPosition(B,null,E)}}},onKeyHome:function(C){var B=this,A=C.view;if(C.altKey){if(A.bufferedRenderer){B.lastKeyEvent=C;A.bufferedRenderer.scrollTo(0,false,B.afterBufferedScrollTo,B)}else{B.setPosition(A.walkRecs(C.record,-A.dataSource.indexOf(C.record)),null,C)}}else{B.setPosition(C.record,0,C)}},afterBufferedScrollTo:function(B,A){this.setPosition(A,null,this.lastKeyEvent,null,!this.lastKeyEvent)},onKeyEnd:function(C){var B=this,A=C.view;if(C.altKey){if(A.bufferedRenderer){B.lastKeyEvent=C;A.bufferedRenderer.scrollTo(A.store.getCount()-1,false,B.afterBufferedScrollTo,B)}else{B.setPosition(A.walkRecs(C.record,A.dataSource.getCount()-1-A.dataSource.indexOf(C.record)),null,C)}}else{B.setPosition(C.record,C.view.getVisibleColumnManager().getColumns().length-1,C)}},getRowsVisible:function(){var E=false,A=this.view,D=A.all.first(),B,C;if(D){B=D.getHeight();C=A.el.getHeight();E=Math.floor(C/B)}return E},fireNavigateEvent:function(B){var A=this;A.fireEvent("navigate",{view:A.position.view,navigationModel:A,keyEvent:B||new Ext.event.Event({}),previousPosition:A.previousPosition,previousRecordIndex:A.previousRecordIndex,previousRecord:A.previousRecord,previousItem:A.previousItem,previousCell:A.previousCell,previousColumnIndex:A.previousColumnIndex,previousColumn:A.previousColumn,position:A.position,recordIndex:A.recordIndex,record:A.record,selectionStart:A.selectionStart,item:A.item,cell:A.cell,columnIndex:A.columnIndex,column:A.column})}})