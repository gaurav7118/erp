Ext.define("Ext.chart.interactions.Abstract",{xtype:"interaction",mixins:{observable:"Ext.mixin.Observable"},config:{gestures:{tap:"onGesture"},chart:null,enabled:true},throttleGap:0,stopAnimationBeforeSync:false,constructor:function(A){var B=this,C;A=A||{};if("id" in A){C=A.id}else{if("id" in B.config){C=B.config.id}else{C=B.getId()}}B.setId(C);B.mixins.observable.constructor.call(B,A)},initialize:Ext.emptyFn,updateChart:function(C,A){var B=this;if(A===C){return }if(A){A.unregister(B);B.removeChartListener(A)}if(C){C.register(B);B.addChartListener()}},updateEnabled:function(A){var C=this,B=C.getChart();if(B){if(A){C.addChartListener()}else{C.removeChartListener(B)}}},onGesture:Ext.emptyFn,getItemForEvent:function(D){var B=this,A=B.getChart(),C=A.getEventXY(D);return A.getItemForPoint(C[0],C[1])},getItemsForEvent:function(D){var B=this,A=B.getChart(),C=A.getEventXY(D);return A.getItemsForPoint(C[0],C[1])},addChartListener:function(){var C=this,B=C.getChart(),E=C.getGestures(),A;if(!C.getEnabled()){return }function D(F,G){B.addElementListener(F,C.listeners[F]=function(J){var I=C.getLocks(),H;if(C.getEnabled()&&(!(F in I)||I[F]===C)){H=(Ext.isFunction(G)?G:C[G]).apply(this,arguments);if(H===false&&J&&J.stopPropagation){J.stopPropagation()}return H}},C)}C.listeners=C.listeners||{};for(A in E){D(A,E[A])}},removeChartListener:function(C){var D=this,E=D.getGestures(),B;function A(F){var G=D.listeners[F];if(G){C.removeElementListener(F,G);delete D.listeners[F]}}if(D.listeners){for(B in E){A(B)}}},lockEvents:function(){var D=this,C=D.getLocks(),A=Array.prototype.slice.call(arguments),B=A.length;while(B--){C[A[B]]=D}},unlockEvents:function(){var C=this.getLocks(),A=Array.prototype.slice.call(arguments),B=A.length;while(B--){delete C[A[B]]}},getLocks:function(){var A=this.getChart();return A.lockedEvents||(A.lockedEvents={})},isMultiTouch:function(){if(Ext.browser.is.IE10){return true}return !Ext.os.is.Desktop},initializeDefaults:Ext.emptyFn,doSync:function(){var B=this,A=B.getChart();if(B.syncTimer){clearTimeout(B.syncTimer);B.syncTimer=null}if(B.stopAnimationBeforeSync){A.animationSuspendCount++}A.redraw();if(B.stopAnimationBeforeSync){A.animationSuspendCount--}B.syncThrottle=Date.now()+B.throttleGap},sync:function(){var A=this;if(A.throttleGap&&Ext.frameStartTime<A.syncThrottle){if(A.syncTimer){return }A.syncTimer=Ext.defer(function(){A.doSync()},A.throttleGap)}else{A.doSync()}},getItemId:function(){return this.getId()},isXType:function(A){return A==="interaction"},destroy:function(){var A=this;A.setChart(null);delete A.listeners;A.callParent()}},function(){if(Ext.os.is.Android4){this.prototype.throttleGap=40}})