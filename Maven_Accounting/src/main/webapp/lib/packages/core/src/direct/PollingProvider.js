Ext.define("Ext.direct.PollingProvider",{extend:"Ext.direct.JsonProvider",alias:"direct.pollingprovider",requires:["Ext.Ajax","Ext.util.TaskRunner","Ext.direct.ExceptionEvent"],type:"polling",interval:3000,constructor:function(A){var B=this;B.callParent([A]);B.pollTask=Ext.TaskManager.newTask({run:B.runPoll,interval:B.interval,scope:B})},destroy:function(){this.pollTask=null;this.callParent()},doConnect:function(){var C=this,A=C.url,B=C.pollFn;if(B&&Ext.isString(B)){var D=B;C.pollFn=B=Ext.direct.Manager.parseMethod(B);if(!Ext.isFunction(B)){Ext.raise("Cannot resolve Ext Direct API method "+D+" for PollingProvider")}}else{if(Ext.isFunction(A)){Ext.log.warn("Using a function for url is deprecated, use pollFn instead.");C.pollFn=B=A;C.url=A=null}}if(A||B){C.setInterval(C.interval);C.pollTask.start()}},doDisconnect:function(){this.pollTask.stop()},getInterval:function(){return this.pollTask.interval},setInterval:function(A){var B=this,C=B.pollTask;if(A<100){Ext.raise("Attempting to configure PollProvider "+B.id+" with interval that is less than 100ms.")}B.interval=C.interval=A;if(B.isConnected()){C.restart(A)}},runPoll:function(){var D=this,B=D.url,C=D.pollFn,E=D.baseParams,A;if(D.fireEvent("beforepoll",D)!==false){if(C){A=C.directCfg.method.getArgs({params:E!==undefined?E:{},callback:D.onPollFn,scope:D});C.apply(window,A)}else{Ext.Ajax.request({url:B,callback:D.onData,scope:D,params:E})}D.fireEvent("poll",D)}},onData:function(E,G,B){var F=this,D,A,C;if(G){C=F.createEvents(B);for(D=0,A=C.length;D<A;++D){F.fireEvent("data",F,C[D])}}else{C=new Ext.direct.ExceptionEvent({data:null,code:Ext.direct.Manager.exceptions.TRANSPORT,message:"Unable to connect to the server.",xhr:B});F.fireEvent("data",F,C)}},onPollFn:function(A,C,D,B){this.onData(null,D,{responseText:A})},inheritableStatics:{checkConfig:function(A){return A&&A.type==="polling"&&(A.url||A.pollFn)}}})