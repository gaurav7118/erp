Ext.define("Ext.chart.interactions.PanZoom",{extend:"Ext.chart.interactions.Abstract",type:"panzoom",alias:"interaction.panzoom",requires:["Ext.draw.Animator"],config:{axes:{top:{},right:{},bottom:{},left:{}},minZoom:null,maxZoom:null,showOverflowArrows:true,panGesture:"drag",zoomGesture:"pinch",zoomOnPanGesture:false,modeToggleButton:{xtype:"segmentedbutton",width:200,defaults:{ui:"default-toolbar"},cls:Ext.baseCSSPrefix+"panzoom-toggle",items:[{text:"Pan"},{text:"Zoom"}]},hideLabelInGesture:false},stopAnimationBeforeSync:true,applyAxes:function(B,A){return Ext.merge(A||{},B)},applyZoomOnPanGesture:function(A){this.getChart();if(this.isMultiTouch()){return false}return A},updateZoomOnPanGesture:function(B){var A=this.getModeToggleButton();if(!this.isMultiTouch()){A.show();A.setValue(B?1:0)}else{A.hide()}},toggleMode:function(){var A=this;if(!A.isMultiTouch()){A.setZoomOnPanGesture(!A.getZoomOnPanGesture())}},applyModeToggleButton:function(C,B){var D=this,A=Ext.factory(C,"Ext.button.Segmented",B);if(!A&&B){B.destroy()}if(A&&!B){A.addListener("toggle",function(E){D.setZoomOnPanGesture(E.getValue()===1)})}return A},getGestures:function(){var C=this,E={},D=C.getPanGesture(),B=C.getZoomGesture(),A=Ext.supports.Touch;E[B]="onZoomGestureMove";E[B+"start"]="onZoomGestureStart";E[B+"end"]="onZoomGestureEnd";E[D]="onPanGestureMove";E[D+"start"]="onPanGestureStart";E[D+"end"]="onPanGestureEnd";E.doubletap="onDoubleTap";return E},onDoubleTap:function(G){var E=this,C=E.getChart(),F=C.getAxes(),B,A,D;for(A=0,D=F.length;A<D;A++){B=F[A];B.setVisibleRange([0,1])}C.redraw()},onPanGestureStart:function(D){if(!D||!D.touches||D.touches.length<2){var B=this,A=B.getChart().getInnerRect(),C=B.getChart().element.getXY();B.startX=D.getX()-C[0]-A[0];B.startY=D.getY()-C[1]-A[1];B.oldVisibleRanges=null;B.hideLabels();B.getChart().suspendThicknessChanged();B.lockEvents(B.getPanGesture());return false}},onPanGestureMove:function(D){var B=this;if(B.getLocks()[B.getPanGesture()]===B){var A=B.getChart().getInnerRect(),C=B.getChart().element.getXY();if(B.getZoomOnPanGesture()){B.transformAxesBy(B.getZoomableAxes(D),0,0,(D.getX()-C[0]-A[0])/B.startX,B.startY/(D.getY()-C[1]-A[1]))}else{B.transformAxesBy(B.getPannableAxes(D),D.getX()-C[0]-A[0]-B.startX,D.getY()-C[1]-A[1]-B.startY,1,1)}B.sync();return false}},onPanGestureEnd:function(B){var A=this,C=A.getPanGesture();if(A.getLocks()[C]===A){A.getChart().resumeThicknessChanged();A.showLabels();A.sync();A.unlockEvents(C);return false}},onZoomGestureStart:function(B){if(B.touches&&B.touches.length===2){var C=this,H=C.getChart().element.getXY(),E=C.getChart().getInnerRect(),G=H[0]+E[0],D=H[1]+E[1],I=[B.touches[0].point.x-G,B.touches[0].point.y-D,B.touches[1].point.x-G,B.touches[1].point.y-D],F=Math.max(44,Math.abs(I[2]-I[0])),A=Math.max(44,Math.abs(I[3]-I[1]));C.getChart().suspendThicknessChanged();C.lastZoomDistances=[F,A];C.lastPoints=I;C.oldVisibleRanges=null;C.hideLabels();C.lockEvents(C.getZoomGesture());return false}},onZoomGestureMove:function(D){var E=this;if(E.getLocks()[E.getZoomGesture()]===E){var H=E.getChart().getInnerRect(),M=E.getChart().element.getXY(),J=M[0]+H[0],G=M[1]+H[1],N=Math.abs,C=E.lastPoints,L=[D.touches[0].point.x-J,D.touches[0].point.y-G,D.touches[1].point.x-J,D.touches[1].point.y-G],F=Math.max(44,N(L[2]-L[0])),B=Math.max(44,N(L[3]-L[1])),A=this.lastZoomDistances||[F,B],K=F/A[0],I=B/A[1];E.transformAxesBy(E.getZoomableAxes(D),H[2]*(K-1)/2+L[2]-C[2]*K,H[3]*(I-1)/2+L[3]-C[3]*I,K,I);E.sync();return false}},onZoomGestureEnd:function(C){var B=this,A=B.getZoomGesture();if(B.getLocks()[A]===B){B.getChart().resumeThicknessChanged();B.showLabels();B.sync();B.unlockEvents(A);return false}},hideLabels:function(){if(this.getHideLabelInGesture()){this.eachInteractiveAxes(function(A){A.hideLabels()})}},showLabels:function(){if(this.getHideLabelInGesture()){this.eachInteractiveAxes(function(A){A.showLabels()})}},isEventOnAxis:function(C,A){var B=A.getSurface().getRect();return B[0]<=C.getX()&&C.getX()<=B[0]+B[2]&&B[1]<=C.getY()&&C.getY()<=B[1]+B[3]},getPannableAxes:function(D){var G=this,A=G.getAxes(),E=G.getChart().getAxes(),C,F=E.length,I=[],H=false,B;if(D){for(C=0;C<F;C++){if(this.isEventOnAxis(D,E[C])){H=true;break}}}for(C=0;C<F;C++){B=A[E[C].getPosition()];if(B&&B.allowPan!==false&&(!H||this.isEventOnAxis(D,E[C]))){I.push(E[C])}}return I},getZoomableAxes:function(E){var H=this,A=H.getAxes(),F=H.getChart().getAxes(),J=[],D,G=F.length,C,I=false,B;if(E){for(D=0;D<G;D++){if(this.isEventOnAxis(E,F[D])){I=true;break}}}for(D=0;D<G;D++){C=F[D];B=A[C.getPosition()];if(B&&B.allowZoom!==false&&(!I||this.isEventOnAxis(E,C))){J.push(C)}}return J},eachInteractiveAxes:function(C){var D=this,B=D.getAxes(),E=D.getChart().getAxes();for(var A=0;A<E.length;A++){if(B[E[A].getPosition()]){if(false===C.call(this,E[A])){return }}}},transformAxesBy:function(D,I,G,H,E){var F=this.getChart().getInnerRect(),A=this.getAxes(),J,B=this.oldVisibleRanges,K=false;if(!B){this.oldVisibleRanges=B={};this.eachInteractiveAxes(function(L){B[L.getId()]=L.getVisibleRange()})}if(!F){return }for(var C=0;C<D.length;C++){J=A[D[C].getPosition()];K=this.transformAxisBy(D[C],B[D[C].getId()],I,G,H,E,this.minZoom||J.minZoom,this.maxZoom||J.maxZoom)||K}return K},transformAxisBy:function(C,O,R,Q,K,I,H,M){var S=this,B=O[1]-O[0],L=C.getVisibleRange(),G=H||S.getMinZoom()||C.config.minZoom,J=M||S.getMaxZoom()||C.config.maxZoom,A=S.getChart().getInnerRect(),F,P;if(!A){return }var D=C.isSide(),E=D?A[3]:A[2],N=D?-Q:R;B/=D?I:K;if(B<0){B=-B}if(B*G>1){B=1}if(B*J<1){B=1/J}F=O[0];P=O[1];L=L[1]-L[0];if(B===L&&L===1){return }C.setVisibleRange([(O[0]+O[1]-B)*0.5-N/E*B,(O[0]+O[1]+B)*0.5-N/E*B]);return(Math.abs(F-C.getVisibleRange()[0])>1e-10||Math.abs(P-C.getVisibleRange()[1])>1e-10)},destroy:function(){this.setModeToggleButton(null);this.callParent()}})