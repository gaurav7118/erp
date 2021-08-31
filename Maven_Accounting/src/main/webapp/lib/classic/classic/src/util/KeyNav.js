Ext.define("Ext.util.KeyNav",{alternateClassName:"Ext.KeyNav",requires:["Ext.util.KeyMap"],disabled:false,defaultEventAction:false,forceKeyDown:false,eventName:"keypress",statics:{keyOptions:{left:37,right:39,up:38,down:40,space:32,pageUp:33,pageDown:34,del:46,backspace:8,home:36,end:35,enter:13,esc:27,tab:9}},constructor:function(A){var B=this;if(arguments.length===2){B.legacyConstructor.apply(B,arguments);return }B.doConstruction(A)},legacyConstructor:function(B,A){this.doConstruction(Ext.apply({target:B},A))},doConstruction:function(A){var C=this,B={target:A.target,ignoreInputFields:A.ignoreInputFields,eventName:C.getKeyEvent("forceKeyDown" in A?A.forceKeyDown:C.forceKeyDown,A.eventName),capture:A.capture},D;if(C.map){C.map.destroy()}C.initConfig(A);if(A.processEvent){B.processEvent=A.processEvent;B.processEventScope=A.processEventScope||C}if(A.priority){B.priority=A.priority}if(A.keyMap){D=C.map=A.keyMap}else{D=C.map=new Ext.util.KeyMap(B);C.destroyKeyMap=true}this.addBindings(A);D.disable();if(!A.disabled){D.enable()}},addBindings:function(H){var C=this,B,F,E=C.map,A=Ext.util.KeyNav.keyOptions,D,G=H.scope||C;for(B in H){F=H[B];D=A[B];if(D!=null){B=D}if(F&&(B.length===1||!isNaN(B=parseInt(B,10)))){if(typeof F==="function"){F={handler:F,defaultEventAction:(H.defaultEventAction!==undefined)?H.defaultEventAction:C.defaultEventAction}}E.addBinding({key:B,ctrl:F.ctrl,shift:F.shift,alt:F.alt,handler:Ext.Function.bind(C.handleEvent,F.scope||G,[F.handler||F.fn,C],true),defaultEventAction:(F.defaultEventAction!==undefined)?F.defaultEventAction:C.defaultEventAction})}}},handleEvent:function(D,C,B,A){A.lastKeyEvent=C;return B.call(this,C)},destroy:function(B){var A=this;if(A.destroyKeyMap){A.map.destroy(B)}delete A.map;A.callParent()},enable:function(){if(this.map){this.map.enable();this.disabled=false}},disable:function(){if(this.map){this.map.disable()}this.disabled=true},setDisabled:function(A){this.map.setDisabled(A);this.disabled=A},getKeyEvent:function(B,A){if(B||(Ext.supports.SpecialKeyDownRepeat&&!A)){return"keydown"}else{return A||this.eventName}}})