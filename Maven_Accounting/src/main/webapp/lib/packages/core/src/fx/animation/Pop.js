Ext.define("Ext.fx.animation.Pop",{extend:"Ext.fx.animation.Abstract",alias:["animation.pop","animation.popIn"],alternateClassName:"Ext.fx.animation.PopIn",config:{out:false,before:{display:null,opacity:0},after:{opacity:null}},getData:function(){var C=this.getTo(),B=this.getFrom(),A=this.getOut();if(A){B.set("opacity",1);B.setTransform({scale:1});C.set("opacity",0);C.setTransform({scale:0})}else{B.set("opacity",0);B.setTransform({scale:0});C.set("opacity",1);C.setTransform({scale:1})}return this.callParent(arguments)}})