Ext.define("Ext.layout.component.Auto",{alias:"layout.autocomponent",extend:"Ext.layout.component.Component",type:"autocomponent",setHeightInDom:false,setWidthInDom:false,waitForOuterHeightInDom:false,waitForOuterWidthInDom:false,beginLayoutCycle:function(D,A){var C=this,F=C.lastWidthModel,E=C.lastHeightModel,B=C.owner.el;C.callParent(arguments);if(F&&F.fixed&&D.widthModel.shrinkWrap){B.setWidth(null)}if(E&&E.fixed&&D.heightModel.shrinkWrap){B.setHeight(null)}},calculate:function(G){var F=this,E=F.measureAutoDimensions(G),B=G.heightModel,C=G.widthModel,D,A;if(E.gotWidth){if(C.shrinkWrap){F.publishOwnerWidth(G,E.contentWidth)}else{if(F.publishInnerWidth){F.publishInnerWidth(G,E.width)}}}else{if(!C.auto&&F.publishInnerWidth){D=F.waitForOuterWidthInDom?G.getDomProp("width"):G.getProp("width");if(D===undefined){F.done=false}else{F.publishInnerWidth(G,D)}}}if(E.gotHeight){if(B.shrinkWrap){F.publishOwnerHeight(G,E.contentHeight)}else{if(F.publishInnerHeight){F.publishInnerHeight(G,E.height)}}}else{if(!B.auto&&F.publishInnerHeight){A=F.waitForOuterHeightInDom?G.getDomProp("height"):G.getProp("height");if(A===undefined){F.done=false}else{F.publishInnerHeight(G,A)}}}if(!E.gotAll){F.done=false}},calculateOwnerHeightFromContentHeight:function(B,A){return A+B.getFrameInfo().height},calculateOwnerWidthFromContentWidth:function(B,A){return A+B.getFrameInfo().width},publishOwnerHeight:function(H,F){var E=this,B=E.owner,A=E.calculateOwnerHeightFromContentHeight(H,F),G,D,C;if(isNaN(A)){E.done=false}else{G=Ext.Number.constrain(A,B.minHeight,B.maxHeight);if(G===A){D=E.setHeightInDom}else{C=E.sizeModels[(G<A)?"constrainedMax":"constrainedMin"];A=G;if(H.heightModel.calculatedFromShrinkWrap){H.heightModel=C}else{H.invalidate({heightModel:C})}}H.setHeight(A,D)}},publishOwnerWidth:function(G,B){var F=this,A=F.owner,E=F.calculateOwnerWidthFromContentWidth(G,B),H,D,C;if(isNaN(E)){F.done=false}else{H=Ext.Number.constrain(E,A.minWidth,A.maxWidth);if(H===E){D=F.setWidthInDom}else{C=F.sizeModels[(H<E)?"constrainedMax":"constrainedMin"];E=H;if(G.widthModel.calculatedFromShrinkWrap){G.widthModel=C}else{G.invalidate({widthModel:C})}}G.setWidth(E,D)}}})