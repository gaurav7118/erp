Ext.define("Ext.util.Scheduler",{mixins:["Ext.mixin.Observable"],requires:["Ext.util.Bag"],busyCounter:0,lastBusyCounter:0,destroyed:false,firing:null,notifyIndex:-1,nextId:0,orderedItems:null,passes:0,scheduledCount:0,validIdRe:null,config:{cycleLimit:5,preSort:null,tickDelay:5},suspendOnNotify:true,constructor:function(A){if(Ext.util.Scheduler.instances){Ext.util.Scheduler.instances.push(this)}else{Ext.util.Scheduler.instances=[this]}this.id=Ext.util.Scheduler.count=(Ext.util.Scheduler.count||0)+1;this.mixins.observable.constructor.call(this,A);this.items=new Ext.util.Bag()},destroy:function(){var A=this,B=A.timer;if(B){window.clearTimeout(B);A.timer=null}A.items.destroy();A.items=A.orderedItems=null;A.callParent();Ext.Array.remove(Ext.util.Scheduler.instances,this)},add:function(C){var B=this,A=B.items;if(A===B.firing){B.items=A=A.clone()}C.id=C.id||++B.nextId;C.scheduler=B;A.add(C);if(!B.sortMap){B.orderedItems=null}},remove:function(C){var B=this,A=B.items;if(B.destroyed){return }if(B.sortMap){Ext.raise("Items cannot be removed during sort")}if(A===B.firing){B.items=A=A.clone()}if(C.scheduled){B.unscheduleItem(C);C.scheduled=false}A.remove(C);B.orderedItems=null},sort:function(){var D=this,A=D.items,E={},F=D.getPreSort(),B,C;D.orderedItems=[];D.sortMap=E;D.sortStack=[];if(F){A.sort(F)}A=A.items;for(B=0;B<A.length;++B){C=A[B];if(!E[C.id]){D.sortItem(C)}}D.sortMap=null;D.sortStack=null},sortItem:function(E){var D=this,F=D.sortMap,A=D.orderedItems,G;if(!E.scheduler){D.add(E)}G=E.id;if(E.scheduler!==D){Ext.raise("Item "+G+" belongs to another Scheduler")}D.sortStack.push(E);if(F[G]===0){for(var C=[],B=0;B<D.sortStack.length;++B){C[B]=D.sortStack[B].getFullName()}Ext.raise("Dependency cycle detected: "+C.join("\n --> "))}if(!(G in F)){F[G]=0;if(!E.sort.$nullFn){E.sort()}F[G]=1;E.order=D.orderedItems.length;A.push(E)}D.sortStack.pop();return D},sortItems:function(A){var B=this,C=B.sortItem;if(A){if(A instanceof Array){Ext.each(A,C,B)}else{Ext.Object.eachValue(A,C,B)}}return B},applyPreSort:function(F){if(typeof F==="function"){return F}var E=F.split(","),D=[],C=E.length,G,A,B;for(A=0;A<C;++A){D[A]=1;B=E[A];if((G=B.charAt(0))==="-"){D[A]=-1}else{if(G!=="+"){G=0}}if(G){E[A]=B.substring(1)}}return function(H,N){var I=0,J,M,L,K;for(J=0;!I&&J<C;++J){M=E[J];L=H[M];K=N[M];I=D[J]*((L<K)?-1:((K<L)?1:0))}return I}},notify:function(){var I=this,B=I.timer,D=I.getCycleLimit(),C=Ext.GlobalEvents,G=I.suspendOnNotify,K,E,J,H,F,A;if(B){window.clearTimeout(B);I.timer=null}if(I.firing){Ext.raise("Notify cannot be called recursively")}if(G){Ext.suspendLayouts()}while(I.scheduledCount){if(D){--D}else{I.firing=null;if(I.onCycleLimitExceeded){I.onCycleLimitExceeded()}break}if(!A){A=true;if(C.hasListeners.beforebindnotify){C.fireEvent("beforebindnotify",I)}}++I.passes;if(!(F=I.orderedItems)){I.sort();F=I.orderedItems}H=F.length;if(H){I.firing=I.items;for(E=0;E<H;++E){J=F[E];if(J.scheduled){J.scheduled=false;--I.scheduledCount;I.notifyIndex=E;J.react();if(!I.scheduledCount){break}}}}}I.firing=null;I.notifyIndex=-1;if(G){Ext.resumeLayouts(true)}if((K=I.busyCounter)!==I.lastBusyCounter){if(!(I.lastBusyCounter=K)){I.fireEvent("idle",I)}}},onTick:function(){this.timer=null;this.notify()},scheduleItem:function(B){var A=this;++A.scheduledCount;if(!A.timer&&!A.firing){A.scheduleTick()}},scheduleTick:function(){var A=this;if(!A.destroyed&&!A.timer){A.timer=Ext.Function.defer(A.onTick,A.getTickDelay(),A)}},unscheduleItem:function(A){if(this.scheduledCount){--this.scheduledCount}},adjustBusy:function(A){var B=this,C=B.busyCounter+A;B.busyCounter=C;if(C){if(!B.lastBusyCounter){B.lastBusyCounter=C;B.fireEvent("busy",B)}}else{if(B.lastBusyCounter&&!B.timer){B.scheduleTick()}}},isBusy:function(){return !this.isIdle()},isIdle:function(){return !(this.busyCounter+this.lastBusyCounter)},debugHooks:{$enabled:false,onCycleLimitExceeded:function(){Ext.raise("Exceeded cycleLimit "+this.getCycleLimit())},scheduleItem:function(A){if(!A){Ext.raise("scheduleItem: Invalid argument")}Ext.log("Schedule item: "+A.getFullName()+" - "+(this.scheduledCount+1));if(A.order<=this.notifyIndex){Ext.log.warn("Suboptimal order: "+A.order+" < "+this.notifyIndex)}this.callParent([A])},unscheduleItem:function(A){if(!this.scheduledCount){Ext.raise("Invalid scheduleCount")}this.callParent([A]);Ext.log("Unschedule item: "+A.getFullName()+" - "+this.scheduledCount)}}})