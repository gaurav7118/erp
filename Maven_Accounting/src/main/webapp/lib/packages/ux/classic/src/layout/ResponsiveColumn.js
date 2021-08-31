Ext.define("Ext.ux.layout.ResponsiveColumn",{extend:"Ext.layout.container.Auto",alias:"layout.responsivecolumn",states:{small:1000,large:0},_responsiveCls:Ext.baseCSSPrefix+"responsivecolumn",initLayout:function(){this.innerCtCls+=" "+this._responsiveCls;this.callParent()},beginLayout:function(D){var I=this,H=Ext.Element.getViewportWidth(),J=I.states,G=Infinity,B=I.innerCt,C=I._currentState,A,F,E;for(A in J){F=J[A]||Infinity;if(H<=F&&F<=G){G=F;E=A}}if(E!==C){B.replaceCls(C,E,I._responsiveCls);I._currentState=E}I.callParent(arguments)},onAdd:function(A){this.callParent([A]);var B=A.responsiveCls;if(B){A.addCls(B)}}},function(A){if(Ext.isIE8){A.override({responsiveSizePolicy:{readsWidth:0,readsHeight:0,setsWidth:1,setsHeight:0},setsItemSize:true,calculateItems:function(E,B){var N=this,F=E.targetContext,M=E.childItems,K=M.length,G=B.gotWidth,C=B.width,I,O,H,D,J,L;if(G===false){F.domBlock(N,"width");return false}if(!G){return true}for(H=0;H<K;++H){D=M[H];L=parseInt(D.el.getStyle("background-position-x"),10);J=parseInt(D.el.getStyle("background-position-y"),10);D.setWidth((L/100*(C-J))-J)}E.setContentWidth(C+E.paddingContext.getPaddingInfo().width);return true},getItemSizePolicy:function(){return this.responsiveSizePolicy}})}})