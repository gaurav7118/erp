Ext.define("Ext.grid.locking.RowSynchronizer",{constructor:function(A,B){var C=this,D;C.view=A;C.rowEl=B;C.els={};C.add("data",A.rowSelector);for(D=A.rowTpl;D;D=D.nextTpl){if(D.beginRowSync){D.beginRowSync(C)}}},add:function(B,A){var C=Ext.fly(this.rowEl).down(A,true);if(C){this.els[B]={el:C}}},finish:function(F){var G=this,C=G.els,I=F.els,E,H=0,B=0,J,A,D;for(A in C){E=I[A];D=E?E.height:0;J=D-C[A].height;if(J>0){H+=J;Ext.fly(C[A].el).setHeight(D)}else{B-=J}}D=F.rowHeight+B;if(Ext.isIE9&&G.view.ownerGrid.rowLines){D--}if(G.rowHeight+H<D){Ext.fly(G.rowEl).setHeight(D)}},measure:function(){var C=this,B=C.els,A;C.rowHeight=C.rowEl.offsetHeight;for(A in B){B[A].height=B[A].el.offsetHeight}},reset:function(){var B=this.els,A;this.rowEl.style.height="";for(A in B){B[A].el.style.height=""}}})