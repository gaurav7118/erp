Ext.define("Ext.mixin.Queryable",{mixinId:"queryable",isQueryable:true,query:function(A){A=A||"*";return Ext.ComponentQuery.query(A,this.getQueryRoot())},queryBy:function(F,E){var C=[],B=this.getQueryRoot().getRefItems(true),D=0,A=B.length,G;for(;D<A;++D){G=B[D];if(F.call(E||G,G)!==false){C.push(G)}}return C},queryById:function(A){return this.down(Ext.makeIdSelector(A))},child:function(A){var B=this.getQueryRoot().getRefItems();if(A&&A.isComponent){return this.matchById(B,A.getItemId())}if(A){B=Ext.ComponentQuery.query(A,B)}if(B.length){return B[0]}return null},down:function(A){if(A&&A.isComponent){return this.matchById(this.getRefItems(true),A.getItemId())}A=A||"";return this.query(A)[0]||null},visitPreOrder:function(A,D,C,B){Ext.ComponentQuery._visit(true,A,this.getQueryRoot(),D,C,B)},visitPostOrder:function(A,D,C,B){Ext.ComponentQuery._visit(false,A,this.getQueryRoot(),D,C,B)},getRefItems:function(){return[]},getQueryRoot:function(){return this},privates:{matchById:function(B,E){var A=B.length,C,D;for(C=0;C<A;++C){D=B[C];if(D.getItemId()===E){return D}}return null}}})