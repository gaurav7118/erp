Ext.define("Ext.event.gesture.SingleTouch",{extend:"Ext.event.gesture.Recognizer",inheritableStatics:{NOT_SINGLE_TOUCH:"Not Single Touch",TOUCH_MOVED:"Touch Moved",EVENT_CANCELED:"Event Canceled"},onTouchStart:function(A){if(A.touches.length>1){return this.fail(this.self.NOT_SINGLE_TOUCH)}},onTouchCancel:function(){return false}})