Ext.define("Ext.container.Monitor",{target:null,selector:"",scope:null,addHandler:null,removeHandler:null,invalidateHandler:null,disabled:0,constructor:function(A){Ext.apply(this,A)},bind:function(B){var A=this;A.target=B;B.on("beforedestroy",A.disable,A);A.onContainerAdd(B)},unbind:function(){var A=this,B=A.target;if(B){B.un("beforedestroy",A.disable,A)}A.items=null},disable:function(){++this.disabled},enable:function(){if(this.disabled>0){--this.disabled}},handleAdd:function(B,A){if(!this.disabled){if(A.is(this.selector)){this.onItemAdd(A.ownerCt,A)}if(A.isQueryable){this.onContainerAdd(A)}}},onItemAdd:function(C,B){var E=this,A=E.items,D=E.addHandler;if(!E.disabled){if(D){D.call(E.scope||B,B)}if(A){A.add(B)}}},onItemRemove:function(C,B){var E=this,A=E.items,D=E.removeHandler;if(!E.disabled){if(D){D.call(E.scope||B,B)}if(A){A.remove(B)}}},onContainerAdd:function(F,B){var I=this,H,G,C=I.handleAdd,A=I.handleRemove,D,E;if(F.isContainer){F.on("add",C,I);F.on("dockedadd",C,I);F.on("remove",A,I);F.on("dockedremove",A,I)}if(B!==true){H=F.query(I.selector);for(D=0,G=H.length;D<G;++D){E=H[D];I.onItemAdd(E.ownerCt,E)}}H=F.query(">container");for(D=0,G=H.length;D<G;++D){I.onContainerAdd(H[D],true)}},handleRemove:function(B,A){var C=this;if(!C.disabled){if(A.is(C.selector)){C.onItemRemove(B,A)}if(A.isQueryable){C.onContainerRemove(B,A)}}},onContainerRemove:function(E,C){var G=this,B,D,A,F;if(!C.destroyed&&!C.destroying&&C.isContainer){G.removeCtListeners(C);B=C.query(G.selector);for(D=0,A=B.length;D<A;++D){F=B[D];G.onItemRemove(F.ownerCt,F)}B=C.query("container");for(D=0,A=B.length;D<A;++D){G.removeCtListeners(B[D])}}else{G.invalidateItems(true)}},removeCtListeners:function(A){var B=this;A.un("add",B.handleAdd,B);A.un("dockedadd",B.handleAdd,B);A.un("remove",B.handleRemove,B);A.un("dockedremove",B.handleRemove,B)},getItems:function(){var B=this,A=B.items;if(!A){A=B.items=new Ext.util.MixedCollection();A.addAll(B.target.query(B.selector))}return A},invalidateItems:function(C){var B=this,A=B.invalidateHandler;if(C&&A){A.call(B.scope||B,B)}B.items=null}})