Ext.define("Ext.layout.component.Component",{extend:"Ext.layout.Layout",type:"component",isComponentLayout:true,nullBox:{},usesContentHeight:true,usesContentWidth:true,usesHeight:true,usesWidth:true,widthCache:{},heightCache:{},beginLayoutCycle:function(D,P){var K=this,C=K.owner,G=D.ownerCtContext,H=D.heightModel,I=D.widthModel,J=C.el.dom===document.body,F=C.lastBox||K.nullBox,M=C.el.lastBox||K.nullBox,A=!J,E=D.isTopLevel,L,N,B,O;K.callParent([D,P]);if(P){if(K.usesContentWidth){++D.consumersContentWidth}if(K.usesContentHeight){++D.consumersContentHeight}if(K.usesWidth){++D.consumersWidth}if(K.usesHeight){++D.consumersHeight}if(G&&!G.hasRawContent){L=C.ownerLayout;if(L){if(L.usesWidth){++D.consumersWidth}if(L.usesHeight){++D.consumersHeight}}}}if(I.configured){B=C[I.names.width];if(E&&I.calculatedFrom){B=F.width}if(!J){A=K.setWidthInDom||(P?B!==M.width:I.constrained)}D.setWidth(B,A)}else{if(E){if(I.calculated){N=F.width;D.setWidth(N,N!==M.width)}N=F.x;D.setProp("x",N,N!==M.x)}}if(H.configured){O=C[H.names.height];if(E&&H.calculatedFrom){O=F.height}if(!J){A=P?O!==M.height:H.constrained}D.setHeight(O,A)}else{if(E){if(H.calculated){N=F.height;D.setHeight(N,N!==M.height)}N=F.y;D.setProp("y",N,N!==M.y)}}},finishedLayout:function(B){var G=this,I=B.children,A=G.owner,E,C,H,D,F;if(I){E=I.length;for(C=0;C<E;C++){H=I[C];H.el.lastBox=H.props}}B.previousSize=G.lastComponentSize;G.lastComponentSize=A.el.lastBox=F=B.props;D=A.lastBox||(A.lastBox={});D.x=F.x;D.y=F.y;D.width=F.width;D.height=F.height;D.invalid=false;G.callParent([B])},notifyOwner:function(C){var B=this,A=B.lastComponentSize,D=C.previousSize;B.owner.afterComponentLayout(A.width,A.height,D?D.width:undefined,D?D.height:undefined)},getTarget:function(){return this.owner.el},getRenderTarget:function(){return this.owner.el},cacheTargetInfo:function(B){var A=this,D=A.targetInfo,C;if(!D){C=B.getEl("getTarget",A);A.targetInfo=D={padding:C.getPaddingInfo(),border:C.getBorderInfo()}}return D},measureAutoDimensions:function(L,H){var T=this,A=T.owner,Q=A.layout,D=L.heightModel,G=L.widthModel,C=L.boxParent,N=L.isBoxParent,U=L.target,B=L.props,I,V={gotWidth:false,gotHeight:false,isContainer:(I=!L.hasRawContent)},S=H||3,P,E,J=0,F=0,K,O,R,W,M;if(G.shrinkWrap&&L.consumersContentWidth){++J;P=!(S&1);if(I){if(P){V.contentWidth=0;V.gotWidth=true;++F}else{if((V.contentWidth=L.getProp("contentWidth"))!==undefined){V.gotWidth=true;++F}}}else{O=B.contentWidth;if(typeof O==="number"){V.contentWidth=O;V.gotWidth=true;++F}else{if(P){K=true}else{if(!L.hasDomProp("containerChildrenSizeDone")){K=false}else{if(N||!C||C.widthModel.shrinkWrap){K=true}else{K=C.hasDomProp("width")}}}if(K){if(P){R=0}else{if(Q&&Q.measureContentWidth){R=Q.measureContentWidth(L)}else{if(U.cacheWidth){W=U.xtype+"-"+U.ui;M=T.widthCache;R=M[W]||(M[W]=T.measureContentWidth(L))}else{R=T.measureContentWidth(L)}}}if(!isNaN(V.contentWidth=R)){L.setContentWidth(R,true);V.gotWidth=true;++F}}}}}else{if(G.natural&&L.consumersWidth){++J;O=B.width;if(typeof O==="number"){V.width=O;V.gotWidth=true;++F}else{if(N||!C){K=true}else{K=C.hasDomProp("width")}if(K){if(!isNaN(V.width=T.measureOwnerWidth(L))){L.setWidth(V.width,false);V.gotWidth=true;++F}}}}}if(D.shrinkWrap&&L.consumersContentHeight){++J;E=!(S&2);if(I){if(E){V.contentHeight=0;V.gotHeight=true;++F}else{if((V.contentHeight=L.getProp("contentHeight"))!==undefined){V.gotHeight=true;++F}}}else{O=B.contentHeight;if(typeof O==="number"){V.contentHeight=O;V.gotHeight=true;++F}else{if(E){K=true}else{if(!L.hasDomProp("containerChildrenSizeDone")){K=false}else{if(A.noWrap){K=true}else{if(!G.shrinkWrap){K=(L.bodyContext||L).hasDomProp("width")}else{if(N||!C||C.widthModel.shrinkWrap){K=true}else{K=C.hasDomProp("width")}}}}}if(K){if(E){R=0}else{if(Q&&Q.measureContentHeight){R=Q.measureContentHeight(L)}else{if(U.cacheHeight){W=U.xtype+"-"+U.ui;M=T.heightCache;R=M[W]||(M[W]=T.measureContentHeight(L))}else{R=T.measureContentHeight(L)}}}if(!isNaN(V.contentHeight=R)){L.setContentHeight(R,true);V.gotHeight=true;++F}}}}}else{if(D.natural&&L.consumersHeight){++J;O=B.height;if(typeof O==="number"){V.height=O;V.gotHeight=true;++F}else{if(N||!C){K=true}else{K=C.hasDomProp("width")}if(K){if(!isNaN(V.height=T.measureOwnerHeight(L))){L.setHeight(V.height,false);V.gotHeight=true;++F}}}}}if(C){L.onBoxMeasured()}V.gotAll=F===J;return V},measureContentWidth:function(A){return A.el.getWidth()-A.getFrameInfo().width},measureContentHeight:function(A){return A.el.getHeight()-A.getFrameInfo().height},measureOwnerHeight:function(A){return A.el.getHeight()},measureOwnerWidth:function(A){return A.el.getWidth()}})