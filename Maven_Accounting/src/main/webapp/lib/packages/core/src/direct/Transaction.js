Ext.define("Ext.direct.Transaction",{alias:"direct.transaction",statics:{TRANSACTION_ID:0},constructor:function(A){var B=this;Ext.apply(B,A);B.id=B.tid=++B.self.TRANSACTION_ID;B.retryCount=0},send:function(){var A=this;A.provider.queueTransaction(A)},retry:function(){var A=this;A.retryCount++;A.send()},getProvider:function(){return this.provider}})