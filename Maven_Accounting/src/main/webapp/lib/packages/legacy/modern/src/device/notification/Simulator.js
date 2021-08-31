Ext.define("Ext.device.notification.Simulator",{extend:"Ext.device.notification.Abstract",requires:["Ext.MessageBox","Ext.util.Audio"],msg:null,show:function(){var A=this.callParent(arguments),E=[],D=A.buttons.length,C,B,F;for(B=0;B<D;B++){C=A.buttons[B];if(Ext.isString(C)){C={text:A.buttons[B],itemId:A.buttons[B].toLowerCase()}}E.push(C)}this.msg=Ext.create("Ext.MessageBox");F=function(G){if(A.callback){A.callback.apply(A.scope,[G])}};this.msg.show({title:A.title,message:A.message,scope:this.msg,buttons:E,fn:F})},alert:function(){var A=this.callParent(arguments);if(A.buttonName){A.buttons=[A.buttonName]}this.show(A)},confirm:function(){var A=this.callParent(arguments);this.show(A)},prompt:function(){var A=this.callParent(arguments),E=[],D=A.buttons.length,C,B,F;for(B=0;B<D;B++){C=A.buttons[B];if(Ext.isString(C)){C={text:A.buttons[B],itemId:A.buttons[B].toLowerCase()}}E.push(C)}this.msg=Ext.create("Ext.MessageBox");F=function(G,H){if(A.callback){A.callback.apply(A.scope,[G,H])}};this.msg.prompt(A.title,A.message,F,this.msg,A.multiLine,A.value,A.prompt)},beep:function(B){if(!Ext.isNumber(B)){B=1}var A=0;var C=function(){if(A<B){Ext.defer(function(){Ext.util.Audio.beep(C)},50)}A++};C()},vibrate:function(){var C=["@-webkit-keyframes vibrate{","    from {","        -webkit-transform: rotate(-2deg);","    }","    to{","        -webkit-transform: rotate(2deg);","    }","}","body {","    -webkit-animation: vibrate 50ms linear 10 alternate;","}"];var B=document.getElementsByTagName("head")[0];var A=document.createElement("style");A.innerHTML=C.join("\n");B.appendChild(A);Ext.defer(function(){B.removeChild(A)},400)}})