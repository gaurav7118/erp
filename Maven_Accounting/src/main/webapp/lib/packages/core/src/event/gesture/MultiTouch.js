Ext.define("Ext.event.gesture.MultiTouch",{extend:"Ext.event.gesture.Recognizer",requiredTouchesCount:2,isTracking:false,isStarted:false,onTouchStart:function(D){var A=this.requiredTouchesCount,C=D.touches,B=C.length;if(B===A){this.start(D)}else{if(B>A){this.end(D)}}},onTouchEnd:function(A){this.end(A)},onTouchCancel:function(A){this.end(A,true);return false},start:function(){if(!this.isTracking){this.isTracking=true;this.isStarted=false}},end:function(B,A){if(this.isTracking){this.isTracking=false;if(this.isStarted){this.isStarted=false;this[A?"fireCancel":"fireEnd"](B)}}},reset:function(){this.isTracking=this.isStarted=false}})