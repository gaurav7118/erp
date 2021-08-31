Ext.define("Ext.fx.Queue",{requires:["Ext.util.HashMap"],constructor:function(){this.targets=new Ext.util.HashMap();this.fxQueue={}},getFxDefaults:function(A){var B=this.targets.get(A);if(B){return B.fxDefaults}return{}},setFxDefaults:function(A,C){var B=this.targets.get(A);if(B){B.fxDefaults=Ext.apply(B.fxDefaults||{},C)}},stopAnimation:function(B){var E=this,A=E.getFxQueue(B),D=A.length,C;while(D){C=A[D-1];if(C){C.end()}D--}},getActiveAnimation:function(B){var A=this.getFxQueue(B);return(A&&!!A.length)?A[0]:false},hasFxBlock:function(B){var A=this.getFxQueue(B);return A&&A[0]&&A[0].block},getFxQueue:function(B){if(!B){return false}var C=this,A=C.fxQueue[B],D=C.targets.get(B);if(!D){return false}if(!A){C.fxQueue[B]=[];if(D.type!=="element"){D.target.on("destroy",function(){C.fxQueue[B]=[]})}}return C.fxQueue[B]},queueFx:function(D){var C=this,E=D.target,A,B;if(!E){return }A=C.getFxQueue(E.getId());B=A.length;if(B){if(D.concurrent){D.paused=false}else{A[B-1].on("afteranimate",function(){D.paused=false})}}else{D.paused=false}D.on("afteranimate",function(){Ext.Array.remove(A,D);if(A.length===0){C.targets.remove(D.target)}if(D.remove){if(E.type==="element"){var F=Ext.get(E.id);if(F){F.destroy()}}}},C,{single:true});A.push(D)}})