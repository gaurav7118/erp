Ext.define("Ext.window.Toast",{extend:"Ext.window.Window",xtype:"toast",isToast:true,cls:Ext.baseCSSPrefix+"toast",bodyPadding:10,autoClose:true,plain:false,draggable:false,resizable:false,shadow:false,focus:Ext.emptyFn,anchor:null,useXAxis:false,align:"br",animate:true,spacing:6,paddingX:30,paddingY:10,slideInAnimation:"easeIn",slideBackAnimation:"bounceOut",slideInDuration:500,slideBackDuration:500,hideDuration:500,autoCloseDelay:3000,stickOnClick:true,stickWhileHover:true,closeOnMouseDown:false,closable:false,isHiding:false,isFading:false,destroyAfterHide:false,closeOnMouseOut:false,xPos:0,yPos:0,initComponent:function(){var A=this;if(A.autoClose&&!A.hasOwnProperty("closable")){A.closable=false}A.updateAlignment(A.align);A.setAnchor(A.anchor);A.callParent()},onRender:function(){var A=this;A.callParent(arguments);A.el.hover(A.onMouseEnter,A.onMouseLeave,A);if(A.closeOnMouseDown){Ext.getDoc().on("mousedown",A.onDocumentMousedown,A)}},alignmentProps:{br:{paddingFactorX:-1,paddingFactorY:-1,siblingAlignment:"br-br",anchorAlign:"tr-br"},bl:{paddingFactorX:1,paddingFactorY:-1,siblingAlignment:"bl-bl",anchorAlign:"tl-bl"},tr:{paddingFactorX:-1,paddingFactorY:1,siblingAlignment:"tr-tr",anchorAlign:"br-tr"},tl:{paddingFactorX:1,paddingFactorY:1,siblingAlignment:"tl-tl",anchorAlign:"bl-tl"},b:{paddingFactorX:0,paddingFactorY:-1,siblingAlignment:"b-b",useXAxis:0,anchorAlign:"t-b"},t:{paddingFactorX:0,paddingFactorY:1,siblingAlignment:"t-t",useXAxis:0,anchorAlign:"b-t"},l:{paddingFactorX:1,paddingFactorY:0,siblingAlignment:"l-l",useXAxis:1,anchorAlign:"r-l"},r:{paddingFactorX:-1,paddingFactorY:0,siblingAlignment:"r-r",useXAxis:1,anchorAlign:"l-r"},x:{br:{anchorAlign:"bl-br"},bl:{anchorAlign:"br-bl"},tr:{anchorAlign:"tl-tr"},tl:{anchorAlign:"tr-tl"}}},updateAlignment:function(E){var C=this,A=C.alignmentProps,B=A[E],D=A.x[E];if(D&&C.useXAxis){Ext.applyIf(C,D)}Ext.applyIf(C,B)},getXposAlignedToAnchor:function(){var C=this,F=C.align,A=C.anchor,D=A&&A.el,B=C.el,E=0;if(D&&D.dom){if(!C.useXAxis){E=B.getLeft()}else{if(F==="br"||F==="tr"||F==="r"){E+=D.getAnchorXY("r")[0];E-=(B.getWidth()+C.paddingX)}else{E+=D.getAnchorXY("l")[0];E+=C.paddingX}}}return E},getYposAlignedToAnchor:function(){var D=this,F=D.align,A=D.anchor,E=A&&A.el,B=D.el,C=0;if(E&&E.dom){if(D.useXAxis){C=B.getTop()}else{if(F==="br"||F==="bl"||F==="b"){C+=E.getAnchorXY("b")[1];C-=(B.getHeight()+D.paddingY)}else{C+=E.getAnchorXY("t")[1];C+=D.paddingY}}}return C},getXposAlignedToSibling:function(B){var C=this,E=C.align,A=C.el,D;if(!C.useXAxis){D=A.getLeft()}else{if(E==="tl"||E==="bl"||E==="l"){D=(B.xPos+B.el.getWidth()+B.spacing)}else{D=(B.xPos-A.getWidth()-C.spacing)}}return D},getYposAlignedToSibling:function(B){var D=this,E=D.align,A=D.el,C;if(D.useXAxis){C=A.getTop()}else{if(E==="tr"||E==="tl"||E==="t"){C=(B.yPos+B.el.getHeight()+B.spacing)}else{C=(B.yPos-A.getHeight()-B.spacing)}}return C},getToasts:function(){var A=this.anchor,C=this.anchorAlign,B=A.activeToasts||(A.activeToasts={});return B[C]||(B[C]=[])},setAnchor:function(A){var C=this,B;C.anchor=A=((typeof A==="string")?Ext.getCmp(A):A);if(!A){B=Ext.window.Toast;C.anchor=B.bodyAnchor||(B.bodyAnchor={el:Ext.getBody()})}},beforeShow:function(){var A=this;if(A.stickOnClick){A.body.on("click",function(){A.cancelAutoClose()})}if(A.autoClose){if(!A.closeTask){A.closeTask=new Ext.util.DelayedTask(A.doAutoClose,A)}}A.el.setX(-10000);A.el.setOpacity(1)},afterShow:function(){var E=this,B=E.el,D,A,C,F;E.callParent(arguments);D=E.getToasts();C=D.length;A=C&&D[C-1];if(A){B.alignTo(A.el,E.siblingAlignment,[0,0]);E.xPos=E.getXposAlignedToSibling(A);E.yPos=E.getYposAlignedToSibling(A)}else{B.alignTo(E.anchor.el,E.anchorAlign,[(E.paddingX*E.paddingFactorX),(E.paddingY*E.paddingFactorY)],false);E.xPos=E.getXposAlignedToAnchor();E.yPos=E.getYposAlignedToAnchor()}Ext.Array.include(D,E);if(E.animate){F=B.getXY();B.animate({from:{x:F[0],y:F[1]},to:{x:E.xPos,y:E.yPos,opacity:1},easing:E.slideInAnimation,duration:E.slideInDuration,dynamic:true,callback:E.afterPositioned,scope:E})}else{E.setLocalXY(E.xPos,E.yPos);E.afterPositioned()}},afterPositioned:function(){if(this.autoClose){this.closeTask.delay(this.autoCloseDelay)}},onDocumentMousedown:function(A){if(this.isVisible()&&!this.owns(A.getTarget())){this.hide()}},slideBack:function(){var E=this,B=E.anchor,F=B&&B.el,C=E.el,D=E.getToasts(),A=Ext.Array.indexOf(D,E);if(!E.isHiding&&C&&C.dom&&F&&F.isVisible()){if(A){E.xPos=E.getXposAlignedToSibling(D[A-1]);E.yPos=E.getYposAlignedToSibling(D[A-1])}else{E.xPos=E.getXposAlignedToAnchor();E.yPos=E.getYposAlignedToAnchor()}E.stopAnimation();if(E.animate){C.animate({to:{x:E.xPos,y:E.yPos},easing:E.slideBackAnimation,duration:E.slideBackDuration,dynamic:true})}}},update:function(){var A=this;if(A.isVisible()){A.isHiding=true;A.hide()}A.callParent(arguments);A.show()},cancelAutoClose:function(){var A=this.closeTask;if(A){A.cancel()}},doAutoClose:function(){var A=this;if(!(A.stickWhileHover&&A.mouseIsOver)){A.close()}else{A.closeOnMouseOut=true}},onMouseEnter:function(){this.mouseIsOver=true},onMouseLeave:function(){var A=this;A.mouseIsOver=false;if(A.closeOnMouseOut){A.closeOnMouseOut=false;A.close()}},removeFromAnchor:function(){var C=this,B,A;if(C.anchor){B=C.getToasts();A=Ext.Array.indexOf(B,C);if(A!==-1){Ext.Array.erase(B,A,1);for(;A<B.length;A++){B[A].slideBack()}}}},getFocusEl:Ext.emptyFn,hide:function(){var B=this,A=B.el;B.cancelAutoClose();if(B.isHiding){if(!B.isFading){B.callParent(arguments);B.removeFromAnchor();B.isHiding=false}}else{B.isHiding=true;B.isFading=true;B.cancelAutoClose();if(A){if(B.animate){A.fadeOut({opacity:0,easing:"easeIn",duration:B.hideDuration,listeners:{afteranimate:function(){B.isFading=false;B.hide(B.animateTarget,B.doClose,B)}}})}else{B.isFading=false;B.hide(B.animateTarget,B.doClose,B)}}}return B}},function(A){Ext.toast=function(E,F,G,D){var C=E,B;if(Ext.isString(E)){C={title:F,html:E,iconCls:D};if(G){C.align=G}}B=new A(C);B.show();return B}})