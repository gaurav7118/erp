Ext.define("Ext.ux.FieldReplicator",{alias:"plugin.fieldreplicator",init:function(A){if(!A.replicatorId){A.replicatorId=Ext.id()}A.on("blur",this.onBlur,this)},onBlur:function(E){var B=E.ownerCt,D=E.replicatorId,G=Ext.isEmpty(E.getRawValue()),F=B.query("[replicatorId="+D+"]"),C=F[F.length-1]===E,H,A;if(G&&!C){Ext.Function.defer(E.destroy,10,E)}else{if(!G&&C){if(E.onReplicate){E.onReplicate()}H=E.cloneConfig({replicatorId:D});A=B.items.indexOf(E);B.add(A+1,H)}}}})