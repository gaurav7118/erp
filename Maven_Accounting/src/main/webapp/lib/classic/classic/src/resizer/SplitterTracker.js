Ext.define("Ext.resizer.SplitterTracker",{extend:"Ext.dd.DragTracker",requires:["Ext.util.Region"],enabled:true,overlayCls:Ext.baseCSSPrefix+"resizable-overlay",createDragOverlay:function(){var A,B=Ext.dom.Element;A=this.overlay=Ext.getBody().createChild({role:"presentation",cls:this.overlayCls,html:"&#160;"});A.unselectable();A.setSize(B.getDocumentWidth(),B.getDocumentHeight());A.show()},getPrevCmp:function(){var A=this.getSplitter();return A.previousSibling(":not([hidden])")},getNextCmp:function(){var A=this.getSplitter();return A.nextSibling(":not([hidden])")},onBeforeStart:function(G){var D=this,E=D.getPrevCmp(),A=D.getNextCmp(),C=D.getSplitter().collapseEl,F=G.getTarget(),B;if(!E||!A){return false}if(C&&F===C.dom){return false}if(A.collapsed||E.collapsed){return false}D.prevBox=E.getEl().getBox();D.nextBox=A.getEl().getBox();D.constrainTo=B=D.calculateConstrainRegion();if(!B){return false}return B},onStart:function(B){var A=this.getSplitter();this.createDragOverlay();A.addCls(A.baseCls+"-active")},onResizeKeyDown:function(E){var B=this,D=B.getSplitter(),A=E.getKey(),C=D.orientation==="vertical"?0:1,G=A===E.UP||A===E.LEFT?-1:1,F;if(!B.active&&B.onBeforeStart(E)){Ext.fly(E.target).on("keyup",B.onResizeKeyUp,B);B.triggerStart(E);B.onMouseDown(E);B.startXY=D.getXY();B.lastKeyDownXY=Ext.Array.slice(B.startXY);F=B.easing=new Ext.fx.easing.Linear();F.setStartTime(Ext.Date.now());F.setStartValue(1);F.setEndValue(4);F.setDuration(2000)}if(B.active){B.lastKeyDownXY[C]=Math.round(B.lastKeyDownXY[C]+(G*B.easing.getValue()));B.lastXY=B.lastKeyDownXY;D.setXY(B.getXY("dragTarget"))}},onResizeKeyUp:function(A){this.onMouseUp(A)},calculateConstrainRegion:function(){var G=this,A=G.getSplitter(),H=A.getWidth(),I=A.defaultSplitMin,B=A.orientation,E=G.prevBox,J=G.getPrevCmp(),C=G.nextBox,F=G.getNextCmp(),L,K,D;if(B==="vertical"){D={prevCmp:J,nextCmp:F,prevBox:E,nextBox:C,defaultMin:I,splitWidth:H};L=new Ext.util.Region(E.y,G.getVertPrevConstrainRight(D),E.bottom,G.getVertPrevConstrainLeft(D));K=new Ext.util.Region(C.y,G.getVertNextConstrainRight(D),C.bottom,G.getVertNextConstrainLeft(D))}else{L=new Ext.util.Region(E.y+(J.minHeight||I),E.right,(J.maxHeight?E.y+J.maxHeight:C.bottom-(F.minHeight||I))+H,E.x);K=new Ext.util.Region((F.maxHeight?C.bottom-F.maxHeight:E.y+(J.minHeight||I))-H,C.right,C.bottom-(F.minHeight||I),C.x)}return L.intersect(K)},performResize:function(K,F){var M=this,A=M.getSplitter(),G=A.orientation,N=M.getPrevCmp(),L=M.getNextCmp(),B=A.ownerCt,I=B.query(">[flex]"),J=I.length,C=G==="vertical",H=0,E=C?"width":"height",D=0,O,P;for(;H<J;H++){O=I[H];P=C?O.getWidth():O.getHeight();D+=P;O.flex=P}F=C?F[0]:F[1];if(N){P=M.prevBox[E]+F;if(N.flex){N.flex=P}else{N[E]=P}}if(L){P=M.nextBox[E]-F;if(L.flex){L.flex=P}else{L[E]=P}}B.updateLayout()},endDrag:function(){var A=this;if(A.overlay){A.overlay.destroy();delete A.overlay}A.callParent(arguments)},onEnd:function(C){var A=this,B=A.getSplitter();B.removeCls(B.baseCls+"-active");A.performResize(C,A.getResizeOffset())},onDrag:function(E){var C=this,F=C.getOffset("dragTarget"),D=C.getSplitter(),B=D.getEl(),A=D.orientation;if(A==="vertical"){B.setX(C.startRegion.left+F[0])}else{B.setY(C.startRegion.top+F[1])}},getSplitter:function(){return this.splitter},getVertPrevConstrainRight:function(A){return(A.prevCmp.maxWidth?A.prevBox.x+A.prevCmp.maxWidth:A.nextBox.right-(A.nextCmp.minWidth||A.defaultMin))+A.splitWidth},getVertPrevConstrainLeft:function(A){return A.prevBox.x+(A.prevCmp.minWidth||A.defaultMin)},getVertNextConstrainRight:function(A){return A.nextBox.right-(A.nextCmp.minWidth||A.defaultMin)},getVertNextConstrainLeft:function(A){return(A.nextCmp.maxWidth?A.nextBox.right-A.nextCmp.maxWidth:A.prevBox.x+(A.prevBox.minWidth||A.defaultMin))-A.splitWidth},getResizeOffset:function(){return this.getOffset("dragTarget")}})