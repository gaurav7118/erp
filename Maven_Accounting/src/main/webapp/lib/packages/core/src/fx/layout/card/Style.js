Ext.define("Ext.fx.layout.card.Style",{extend:"Ext.fx.layout.card.Abstract",requires:["Ext.fx.Animation"],config:{inAnimation:{before:{visibility:null},preserveEndState:false,replacePrevious:true},outAnimation:{preserveEndState:false,replacePrevious:true}},constructor:function(B){var C,A;this.callParent([B]);this.endAnimationCounter=0;C=this.getInAnimation();A=this.getOutAnimation();C.on("animationend","incrementEnd",this);A.on("animationend","incrementEnd",this)},updateDirection:function(A){this.getInAnimation().setDirection(A);this.getOutAnimation().setDirection(A)},updateDuration:function(A){this.getInAnimation().setDuration(A);this.getOutAnimation().setDuration(A)},updateReverse:function(A){this.getInAnimation().setReverse(A);this.getOutAnimation().setReverse(A)},incrementEnd:function(){this.endAnimationCounter++;if(this.endAnimationCounter>1){this.endAnimationCounter=0;this.fireEvent("animationend",this)}},applyInAnimation:function(B,A){return Ext.factory(B,Ext.fx.Animation,A)},applyOutAnimation:function(B,A){return Ext.factory(B,Ext.fx.Animation,A)},updateInAnimation:function(A){A.setScope(this)},updateOutAnimation:function(A){A.setScope(this)},onActiveItemChange:function(F,D,H,B){var E=this.getInAnimation(),C=this.getOutAnimation(),G,A;if(D&&H&&H.isPainted()){G=D.renderElement;A=H.renderElement;E.setElement(G);C.setElement(A);C.setOnEnd(function(){B.resume()});G.dom.style.setProperty("visibility","hidden","important");D.show();Ext.Animator.run([C,E]);B.pause()}},destroy:function(){Ext.destroy(this.getInAnimation(),this.getOutAnimation());this.callParent()}})