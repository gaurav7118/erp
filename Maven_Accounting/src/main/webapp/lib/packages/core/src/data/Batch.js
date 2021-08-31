Ext.define("Ext.data.Batch",{mixins:{observable:"Ext.mixin.Observable"},config:{pauseOnException:false},current:-1,total:0,running:false,complete:false,exception:false,constructor:function(A){var B=this;B.mixins.observable.constructor.call(B,A);B.operations=[];B.exceptions=[]},add:function(B){var D=this,C,A;if(Ext.isArray(B)){for(C=0,A=B.length;C<A;++C){D.add(B[C])}}else{D.total++;B.setBatch(D);D.operations.push(B)}return D},sort:function(){this.operations.sort(this.sortFn)},sortFn:function(C,A){var B=C.order-A.order;if(B){return B}var F=C.entityType,D=A.entityType,E;if(!F||!D){return 0}if(!(E=F.rank)){F.schema.rankEntities();E=F.rank}return(E-D.rank)*C.foreignKeyDirection},start:function(A){var B=this;if(!B.operations.length||B.running){return B}B.exceptions.length=0;B.exception=false;B.running=true;return B.runOperation(Ext.isDefined(A)?A:B.current+1)},retry:function(){return this.start(this.current)},runNextOperation:function(){var A=this;if(A.running){A.runOperation(A.current+1)}return A},pause:function(){this.running=false;return this},getOperations:function(){return this.operations},getExceptions:function(){return this.exceptions},getCurrent:function(){var A=null,B=this.current;if(!(B===-1||this.complete)){A=this.operations[B]}return A},getTotal:function(){return this.total},isRunning:function(){return this.running},isComplete:function(){return this.complete},hasException:function(){return this.exception},runOperation:function(C){var D=this,B=D.operations,A=B[C];if(A===undefined){D.running=false;D.complete=true;D.fireEvent("complete",D,B[B.length-1])}else{D.current=C;A.setInternalCallback(D.onOperationComplete);A.setInternalScope(D);A.execute()}return D},onOperationComplete:function(A){var C=this,B=A.hasException();if(B){C.exception=true;C.exceptions.push(A);C.fireEvent("exception",C,A)}if(B&&C.getPauseOnException()){C.pause()}else{C.fireEvent("operationcomplete",C,A);C.runNextOperation()}}})