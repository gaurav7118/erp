Ext.define("Ext.fx.target.CompositeSprite",{extend:"Ext.fx.target.Sprite",getAttr:function(A,G){var B=[],F=[].concat(this.target.items),E=F.length,D,C;for(D=0;D<E;D++){C=F[D];B.push([C,G!==undefined?G:this.getFromPrim(C,A)])}return B}})