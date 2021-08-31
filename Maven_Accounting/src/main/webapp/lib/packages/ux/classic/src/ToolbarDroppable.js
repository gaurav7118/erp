Ext.define("Ext.ux.ToolbarDroppable",{constructor:function(A){Ext.apply(this,A)},init:function(A){this.toolbar=A;this.toolbar.on({scope:this,render:this.createDropTarget})},createDropTarget:function(){this.dropTarget=Ext.create("Ext.dd.DropTarget",this.toolbar.getEl(),{notifyOver:Ext.Function.bind(this.notifyOver,this),notifyDrop:Ext.Function.bind(this.notifyDrop,this)})},addDDGroup:function(A){this.dropTarget.addToGroup(A)},calculateEntryIndex:function(G){var I=0,J=this.toolbar,H=J.items.items,E=H.length,B=G.getXY()[0],F=0,C,D,A,K;for(;F<E;F++){C=H[F].getEl();D=C.getXY()[0];A=C.getWidth();K=D+A/2;if(B<K){I=F;break}else{I=F+1}}return I},canDrop:function(A){return true},notifyOver:function(A,B,C){return this.canDrop.apply(this,arguments)?this.dropTarget.dropAllowed:this.dropTarget.dropNotAllowed},notifyDrop:function(A,D,E){var C=this.canDrop(A,D,E),F=this.toolbar;if(C){var B=this.calculateEntryIndex(D);F.insert(B,this.createItem(E));this.afterLayout()}return C},createItem:function(A){Ext.raise("The createItem method must be implemented in the ToolbarDroppable plugin")},afterLayout:Ext.emptyFn})