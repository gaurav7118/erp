Ext.define(null,{override:"Ext.event.publisher.Focus",compatibility:Ext.isIE10m,doDelegatedEvent:function(D,C){var A=document.body,B=Ext.synchronouslyFocusing;if(B&&((D.type==="focusout"&&D.srcElement===B&&D.toElement===A)||(D.type==="focusin"&&D.srcElement===A&&D.fromElement===B&&D.toElement===null))){return }return this.callParent([D,C])}})