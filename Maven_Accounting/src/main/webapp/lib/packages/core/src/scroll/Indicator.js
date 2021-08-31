Ext.define("Ext.scroll.Indicator",{extend:"Ext.Widget",xtype:"scrollindicator",config:{axis:null,hideAnimation:true,hideDelay:0,scroller:null,minLength:24},defaultHideAnimation:{to:{opacity:0},duration:300},names:{x:{side:"l",getSize:"getHeight",setLength:"setWidth",translate:"translateX"},y:{side:"t",getSize:"getWidth",setLength:"setHeight",translate:"translateY"}},oppositeAxis:{x:"y",y:"x"},cls:Ext.baseCSSPrefix+"scroll-indicator",applyHideAnimation:function(A){if(A){A=Ext.mergeIf({onEnd:this.onHideAnimationEnd,scope:this},this.defaultHideAnimation,A)}return A},constructor:function(A){var C=this,B;C.callParent([A]);B=C.getAxis();C.names=C.names[B];C.element.addCls(C.cls+" "+C.cls+"-"+B)},hide:function(){var B=this,A=B.getHideDelay();if(A){B._hideTimer=Ext.defer(B.doHide,A,B)}else{B.doHide()}},setValue:function(M){var K=this,C=K.element,J=K.names,D=K.getAxis(),H=K.getScroller(),G=H.getMaxUserPosition()[D],N=H.getElementSize()[D],I=K.length,B=K.getMinLength(),A=I,F=N-I-K.sizeAdjust,O=Math.round,L=Math.max,E;if(M<0){A=O(L(I+(I*M/N),B));E=0}else{if(M>G){A=O(L(I-(I*(M-G)/N),B));E=F+I-A}else{E=O(M/G*F)}}K[J.translate](E);C[J.setLength](A)},show:function(){var B=this,A=B.element,C=A.getActiveAnimation();if(C){C.end()}if(!B._inDom){B.getScroller().getElement().appendChild(A);B._inDom=true;if(!B.size){B.cacheStyles()}}B.refreshLength();clearTimeout(B._hideTimer);A.setStyle("opacity","")},privates:{cacheStyles:function(){var B=this,A=B.element,C=B.names;B.size=A[C.getSize]();B.margin=A.getMargin(C.side)},doHide:function(){var B=this.getHideAnimation(),A=this.element;if(B){A.animate(B)}else{A.setStyle("opacity",0)}},hasOpposite:function(){return this.getScroller().isAxisEnabled(this.oppositeAxis[this.getAxis()])},onHideAnimationEnd:function(){this.element.setStyle("opacity","0")},refreshLength:function(){var I=this,H=I.names,D=I.getAxis(),F=I.getScroller(),A=F.getSize()[D],J=F.getElementSize()[D],G=J/A,C=I.margin*2,E=I.hasOpposite()?(C+I.size):C,B=Math.max(Math.round((J-E)*G),I.getMinLength());I.sizeAdjust=E;I.length=B;I.element[H.setLength](B)},translateX:function(A){this.element.translate(A)},translateY:function(A){this.element.translate(0,A)}}})