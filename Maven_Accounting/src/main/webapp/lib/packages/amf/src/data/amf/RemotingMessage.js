Ext.define("Ext.data.amf.RemotingMessage",{alias:"data.amf.remotingmessage",config:{$flexType:"flex.messaging.messages.RemotingMessage",body:[],clientId:"",destination:"",headers:[],messageId:"",operation:"",source:"",timestamp:[],timeToLive:[]},constructor:function(A){this.initConfig(A)},encodeMessage:function(){var A=Ext.create("Ext.data.amf.XmlEncoder"),B;B=Ext.copyTo({},this,"$flexType,body,clientId,destination,headers,messageId,operation,source,timestamp,timeToLive",true);A.writeObject(B);return A.body}})