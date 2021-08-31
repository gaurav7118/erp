Ext.define("Ext.grid.column.Action",{extend:"Ext.grid.column.Column",alias:["widget.actioncolumn"],alternateClassName:"Ext.grid.ActionColumn",stopSelection:true,actionIdRe:new RegExp(Ext.baseCSSPrefix+"action-col-(\\d+)"),altText:"",menuText:"<i>Actions</i>",ignoreExport:true,sortable:false,innerCls:Ext.baseCSSPrefix+"grid-cell-inner-action-col",actionIconCls:Ext.baseCSSPrefix+"action-col-icon",constructor:function(D){var F=this,B=Ext.apply({},D),C=B.items||F.items||[F],G,E,A;F.origRenderer=B.renderer||F.renderer;F.origScope=B.scope||F.scope;F.renderer=F.scope=B.renderer=B.scope=null;B.items=null;F.callParent([B]);F.items=C;for(E=0,A=C.length;E<A;++E){if(C[E].getClass){G=true;break}}if(F.origRenderer||G){F.hasCustomRenderer=true}},initComponent:function(){var A=this;A.callParent();if(A.sortable&&!A.dataIndex){A.sortable=false}},defaultRenderer:function(H,K,C,B,J,F,I){var Q=this,A=Q.origScope||Q,L=Q.items,N=L.length,M,P,R,D,E,G,O;R=Ext.isFunction(Q.origRenderer)?Q.origRenderer.apply(A,arguments)||"":"";K.tdCls+=" "+Ext.baseCSSPrefix+"action-col-cell";for(M=0;M<N;M++){P=L[M];O=P.icon;D=P.disabled||(P.isDisabled?P.isDisabled.call(P.scope||A,I,B,J,P,C):false);E=D?null:(P.tooltip||(P.getTip?P.getTip.apply(P.scope||A,arguments):null));G=P.getAltText?P.getAltText.apply(P.scope||A,arguments):P.altText||Q.altText;if(!P.hasActionConfiguration){P.stopSelection=Q.stopSelection;P.disable=Ext.Function.bind(Q.disableAction,Q,[M],0);P.enable=Ext.Function.bind(Q.enableAction,Q,[M],0);P.hasActionConfiguration=true}R+="<"+(O?"img":"div")+' tabIndex="0" role="button"'+(O?(' alt="'+G+'" src="'+P.icon+'"'):"")+' class="'+Q.actionIconCls+" "+Ext.baseCSSPrefix+"action-col-"+String(M)+" "+(D?Q.disabledCls+" ":" ")+(Ext.isFunction(P.getClass)?P.getClass.apply(P.scope||A,arguments):(P.iconCls||Q.iconCls||""))+'"'+(E?' data-qtip="'+E+'"':"")+(O?"/>":"></div>")}return R},updater:function(A,E,C,B,F){var D={};Ext.fly(A).addCls(D.tdCls).down(this.getView().innerSelector,true).innerHTML=this.defaultRenderer(E,D,C,null,null,F,B)},enableAction:function(B,A){var C=this;if(!B){B=0}else{if(!Ext.isNumber(B)){B=Ext.Array.indexOf(C.items,B)}}C.items[B].disabled=false;C.up("tablepanel").el.select("."+Ext.baseCSSPrefix+"action-col-"+B).removeCls(C.disabledCls);if(!A){C.fireEvent("enable",C)}},disableAction:function(B,A){var C=this;if(!B){B=0}else{if(!Ext.isNumber(B)){B=Ext.Array.indexOf(C.items,B)}}C.items[B].disabled=true;C.up("tablepanel").el.select("."+Ext.baseCSSPrefix+"action-col-"+B).addCls(C.disabledCls);if(!A){C.fireEvent("disable",C)}},beforeDestroy:function(){this.renderer=this.items=null;return this.callParent(arguments)},processEvent:function(I,K,M,B,J,G,D,O){var H=this,F=G.getTarget(),L=I==="keydown"&&G.getKey(),C,N,A,E=Ext.fly(M);G.stopSelection=!L&&H.stopSelection;if(L&&(F===M||!E.contains(F))){F=E.query("."+H.actionIconCls,true);if(F.length===1){F=F[0]}else{return }}if(F&&(C=F.className.match(H.actionIdRe))){N=H.items[parseInt(C[1],10)];A=N.disabled||(N.isDisabled?N.isDisabled.call(N.scope||H.origScope||H,K,B,J,N,D):false);if(N&&!A){if(I==="mousedown"&&!H.getView().actionableMode){G.preventDefault()}else{if(I==="click"||(L===G.ENTER||L===G.SPACE)){Ext.callback(N.handler||H.handler,N.scope||H.origScope,[K,B,J,N,G,D,O],undefined,H);if(!K.el.contains(Ext.Element.getActiveElement())){return false}}}}}return H.callParent(arguments)},cascade:function(B,A){B.call(A||this,this)},getRefItems:function(){return[]},privates:{getFocusables:function(){return[]},shouldUpdateCell:function(){return 2}}})