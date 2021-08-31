Ext.define("Ext.grid.header.DropZone",{extend:"Ext.dd.DropZone",colHeaderCls:Ext.baseCSSPrefix+"column-header",proxyOffsets:[-4,-9],constructor:function(B){var A=this;A.headerCt=B;A.ddGroup=A.getDDGroup();A.autoGroup=true;A.callParent([B.el])},destroy:function(){this.callParent();Ext.destroy(this.topIndicator,this.bottomIndicator)},getDDGroup:function(){return"header-dd-zone-"+this.headerCt.up("[scrollerOwner]").id},getTargetFromEvent:function(A){return A.getTarget("."+this.colHeaderCls)},getTopIndicator:function(){if(!this.topIndicator){this.topIndicator=Ext.getBody().createChild({role:"presentation",cls:Ext.baseCSSPrefix+"col-move-top","data-sticky":true,html:"&#160;"});this.indicatorXOffset=Math.floor((this.topIndicator.dom.offsetWidth+1)/2)}return this.topIndicator},getBottomIndicator:function(){if(!this.bottomIndicator){this.bottomIndicator=Ext.getBody().createChild({role:"presentation",cls:Ext.baseCSSPrefix+"col-move-bottom","data-sticky":true,html:"&#160;"})}return this.bottomIndicator},getLocation:function(D,B){var A=D.getXY()[0],C=Ext.fly(B).getRegion(),E;if((C.right-A)<=(C.right-C.left)/2){E="after"}else{E="before"}return{pos:E,header:Ext.getCmp(B.id),node:B}},positionIndicator:function(W,M,S){var V=this,N=W.header,E=V.getLocation(S,M),H=E.header,D=E.pos,C,R,J,P,Q,A,B,I,K,U,T,L,G,O,F;if(H===V.lastTargetHeader&&D===V.lastDropPos){return }C=N.nextSibling("gridcolumn:not([hidden])");R=N.previousSibling("gridcolumn:not([hidden])");V.lastTargetHeader=H;V.lastDropPos=D;if(!H.draggable&&D==="before"&&H.getIndex()===0){return false}W.dropLocation=E;if((N!==H)&&((D==="before"&&C!==H)||(D==="after"&&R!==H))&&!H.isDescendantOf(N)){L=Ext.dd.DragDropManager.getRelated(V);G=L.length;O=0;for(;O<G;O++){F=L[O];if(F!==V&&F.invalidateDrop){F.invalidateDrop()}}V.valid=true;J=V.getTopIndicator();P=V.getBottomIndicator();if(D==="before"){Q="bc-tl";A="tc-bl"}else{Q="bc-tr";A="tc-br"}B=J.getAlignToXY(H.el,Q);I=P.getAlignToXY(H.el,A);K=V.headerCt.el;U=K.getX()-V.indicatorXOffset;T=K.getX()+K.getWidth();B[0]=Ext.Number.constrain(B[0],U,T);I[0]=Ext.Number.constrain(I[0],U,T);J.setXY(B);P.setXY(I);J.show();P.show()}else{V.invalidateDrop()}},invalidateDrop:function(){this.valid=false;this.hideIndicators()},onNodeOver:function(C,F,E,D){var G=this,I=D.header,A,J,B,H;if(D.header.el.dom===C){A=false}else{D.isLock=D.isUnlock=D.crossPanel=false;J=G.getLocation(E,C).header;A=(I.ownerCt===J.ownerCt);if(!A&&(!I.ownerCt.sealed&&!J.ownerCt.sealed)){A=true;B=I.up("tablepanel");H=J.up("tablepanel");if(B!==H){D.crossPanel=true;D.isLock=H.isLocked&&!B.isLocked;D.isUnlock=!H.isLocked&&B.isLocked;if((D.isUnlock&&I.lockable===false)||(D.isLock&&!I.isLockable())){A=false}}}}if(A){G.positionIndicator(D,C,E)}else{G.valid=false}return G.valid?G.dropAllowed:G.dropNotAllowed},hideIndicators:function(){var A=this;A.getTopIndicator().hide();A.getBottomIndicator().hide();A.lastTargetHeader=A.lastDropPos=null},onNodeOut:function(){this.hideIndicators()},getNestedHeader:function(D,B){var A=D.items,C;if(D.isGroupHeader&&A.length){C=!B?"first":"last";D=this.getNestedHeader(A[C](),B)}return D},onNodeDrop:function(K,C,O,S){this.headerCt.blockNextEvent();if(!this.valid){return }var P=this,L=S.header,G=S.dropLocation,N=G.pos,H=G.header,R=L.ownerCt,J=R.getRootHeaderCt(),A=H.ownerCt,D=P.headerCt.visibleColumnManager,M=D.getHeaderIndex(L),B,I,Q,F,E;if(S.isLock||S.isUnlock){F=R.up("[scrollerOwner]");B=A.items.indexOf(H);if(N==="after"){B++}if(S.isLock){F.lock(L,B,A)}else{F.unlock(L,B,A)}}else{B=N==="after"?D.getHeaderIndex(P.getNestedHeader(H,1))+1:D.getHeaderIndex(P.getNestedHeader(H,0));P.invalidateDrop();E=L.getWidth();Ext.suspendLayouts();R.isDDMoveInGrid=A.isDDMoveInGrid=!S.crossPanel;if(L.isGroupHeader&&H.isGroupHeader){L.setNestedParent(H)}if(N==="before"){H.insertNestedHeader(L)}else{Q="move"+N.charAt(0).toUpperCase()+N.substr(1);A[Q](L,H)}if(B>=0&&!(H.isGroupHeader&&(!H.items||!H.items.length))&&M!==B){I=L.isGroupHeader?L.query(":not([hidden]):not([isGroupHeader])").length:1;if((M<=B)&&I>1){B-=I}A.getRootHeaderCt().grid.view.moveColumn(M,B,I)}J.fireEvent("columnmove",R,L,M,B);R.isDDMoveInGrid=A.isDDMoveInGrid=false;if(A.isGroupHeader&&!R.isGroupHeader){if(R!==A){L.savedFlex=L.flex;delete L.flex;L.width=E}}else{if(!R.isGroupHeader){if(L.savedFlex){L.flex=L.savedFlex;delete L.width}}}Ext.resumeLayouts(true)}}})