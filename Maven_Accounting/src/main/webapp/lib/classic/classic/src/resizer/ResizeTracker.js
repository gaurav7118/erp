Ext.define("Ext.resizer.ResizeTracker",{extend:"Ext.dd.DragTracker",dynamic:true,preserveRatio:false,constrainTo:null,proxyCls:Ext.baseCSSPrefix+"resizable-proxy",constructor:function(B){var D=this,C,A,E;if(!B.el){if(B.target.isComponent){D.el=B.target.getEl()}else{D.el=B.target}}this.callParent(arguments);if(D.preserveRatio&&D.minWidth&&D.minHeight){C=D.minWidth/D.el.getWidth();A=D.minHeight/D.el.getHeight();if(A>C){D.minWidth=D.el.getWidth()*A}else{D.minHeight=D.el.getHeight()*C}}if(D.throttle){E=Ext.Function.createThrottled(function(){Ext.resizer.ResizeTracker.prototype.resize.apply(D,arguments)},D.throttle);D.resize=function(G,H,F){if(F){Ext.resizer.ResizeTracker.prototype.resize.apply(D,arguments)}else{E.apply(null,arguments)}}}},onBeforeStart:function(A){this.startBox=this.target.getBox()},getProxy:function(){var A=this;if(!A.dynamic&&!A.proxy){A.proxy=A.createProxy(A.target||A.el);A.hideProxy=true}if(A.proxy){A.proxy.show();return A.proxy}},createProxy:function(C){var B,A=this.proxyCls;if(C.isComponent){B=C.getProxy().addCls(A)}else{B=C.createProxy({tag:"div",role:"presentation",cls:A,id:C.id+"-rzproxy"},Ext.getBody())}B.removeCls(Ext.baseCSSPrefix+"proxy-el");return B},onStart:function(A){this.activeResizeHandle=Ext.get(this.getDragTarget().id);if(!this.dynamic){this.resize(this.startBox)}},onMouseDown:function(B,A){var C=Ext.fly(A.parentNode);this.callParent(arguments);if(C&&C.shim){C.maskIframes()}},onMouseUp:function(A){var B=Ext.fly(this.dragTarget.parentNode);this.callParent(arguments);if(B&&B.shim){B.unmaskIframes()}},onDrag:function(A){if(this.dynamic||this.proxy){this.updateDimensions(A)}},updateDimensions:function(O,K){var P=this,C=P.activeResizeHandle.region,E=P.getOffset(P.constrainTo?"dragTarget":null),I=P.startBox,F,M=0,Q=0,H,N,A=0,S=0,R,G,B,D,L,J;C=P.convertRegionName(C);switch(C){case"south":Q=E[1];B=2;break;case"north":Q=-E[1];S=-Q;B=2;break;case"east":M=E[0];B=1;break;case"west":M=-E[0];A=-M;B=1;break;case"northeast":Q=-E[1];S=-Q;M=E[0];G=[I.x,I.y+I.height];B=3;break;case"southeast":Q=E[1];M=E[0];G=[I.x,I.y];B=3;break;case"southwest":M=-E[0];A=-M;Q=E[1];G=[I.x+I.width,I.y];B=3;break;case"northwest":Q=-E[1];S=-Q;M=-E[0];A=-M;G=[I.x+I.width,I.y+I.height];B=3;break}D={width:I.width+M,height:I.height+Q,x:I.x+A,y:I.y+S};H=Ext.Number.snap(D.width,P.widthIncrement);N=Ext.Number.snap(D.height,P.heightIncrement);if(H!==D.width||N!==D.height){switch(C){case"northeast":D.y-=N-D.height;break;case"north":D.y-=N-D.height;break;case"southwest":D.x-=H-D.width;break;case"west":D.x-=H-D.width;break;case"northwest":D.x-=H-D.width;D.y-=N-D.height}D.width=H;D.height=N}if(D.width<P.minWidth||D.width>P.maxWidth){D.width=Ext.Number.constrain(D.width,P.minWidth,P.maxWidth);if(A){D.x=I.x+(I.width-D.width)}}else{P.lastX=D.x}if(D.height<P.minHeight||D.height>P.maxHeight){D.height=Ext.Number.constrain(D.height,P.minHeight,P.maxHeight);if(S){D.y=I.y+(I.height-D.height)}}else{P.lastY=D.y}if(P.preserveRatio||O.shiftKey){F=P.startBox.width/P.startBox.height;L=Math.min(Math.max(P.minHeight,D.width/F),P.maxHeight);J=Math.min(Math.max(P.minWidth,D.height*F),P.maxWidth);if(B===1){D.height=L}else{if(B===2){D.width=J}else{R=Math.abs(G[0]-this.lastXY[0])/Math.abs(G[1]-this.lastXY[1]);if(R>F){D.height=L}else{D.width=J}if(C==="northeast"){D.y=I.y-(D.height-I.height)}else{if(C==="northwest"){D.y=I.y-(D.height-I.height);D.x=I.x-(D.width-I.width)}else{if(C==="southwest"){D.x=I.x-(D.width-I.width)}}}}}}P.setPosition=D.x!==P.startBox.x||D.y!==P.startBox.y;P.resize(D,K)},resize:function(D,A){var C=this,E,B=C.setPosition;if(C.dynamic||(!C.dynamic&&A)){if(B){C.target.setBox(D)}else{C.target.setSize(D.width,D.height)}}if(!A){E=C.getProxy();if(E&&E!==C.target){if(B||C.hideProxy){E.setBox(D)}else{E.setSize(D.width,D.height)}}}},onEnd:function(A){this.updateDimensions(A,true);if(this.proxy&&this.hideProxy){this.proxy.hide()}},convertRegionName:function(A){return A}})