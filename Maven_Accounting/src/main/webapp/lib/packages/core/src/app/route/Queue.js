Ext.define("Ext.app.route.Queue",{queue:null,token:null,constructor:function(A){Ext.apply(this,A);this.queue=new Ext.util.MixedCollection()},queueAction:function(A,B){this.queue.add({route:A,args:B})},clearQueue:function(){this.queue.removeAll()},runQueue:function(){var A=this.queue,C=A.removeAt(0),B;if(C){B=C&&C.route;B.execute(this.token,C.args,this.onActionExecute,this)}},onActionExecute:function(A){if(A){this.clearQueue()}else{this.runQueue()}}})