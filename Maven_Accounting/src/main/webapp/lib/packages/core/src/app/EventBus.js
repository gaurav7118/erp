Ext.define("Ext.app.EventBus",{singleton:true,requires:["Ext.app.domain.Component"],constructor:function(){var B=this,A=Ext.app.EventDomain.instances;B.callParent();B.domains=A;B.bus=A.component.bus},control:function(B,A){return this.domains.component.listen(B,A)},listen:function(D,B){var A=this.domains,C;for(C in D){if(D.hasOwnProperty(C)){A[C].listen(D[C],B)}}},unlisten:function(C){var A=Ext.app.EventDomain.instances,B;for(B in A){A[B].unlisten(C)}}})