Ext.define("Ext.dd.StatusProxy",{extend:"Ext.Component",animRepair:false,childEls:["ghost"],renderTpl:['<div class="'+Ext.baseCSSPrefix+'dd-drop-icon" role="presentation"></div><div id="{id}-ghost" data-ref="ghost" class="'+Ext.baseCSSPrefix+'dd-drag-ghost" role="presentation"></div>'],repairCls:Ext.baseCSSPrefix+"dd-drag-repair",ariaRole:"presentation",skipLayout:true,constructor:function(A){var B=this;A=A||{};Ext.apply(B,{hideMode:"visibility",hidden:true,floating:true,id:B.id||Ext.id(),cls:Ext.baseCSSPrefix+"dd-drag-proxy "+this.dropNotAllowed,shadow:A.shadow||false,renderTo:Ext.getDetachedBody()});B.callParent(arguments);this.dropStatus=this.dropNotAllowed},dropAllowed:Ext.baseCSSPrefix+"dd-drop-ok",dropNotAllowed:Ext.baseCSSPrefix+"dd-drop-nodrop",setStatus:function(A){A=A||this.dropNotAllowed;if(this.dropStatus!==A){this.el.replaceCls(this.dropStatus,A);this.dropStatus=A}},reset:function(B){var C=this,A=Ext.baseCSSPrefix+"dd-drag-proxy ";C.el.replaceCls(A+C.dropAllowed,A+C.dropNotAllowed);C.dropStatus=C.dropNotAllowed;if(B){C.ghost.setHtml("")}},update:function(A){if(typeof A==="string"){this.ghost.setHtml(A)}else{this.ghost.setHtml("");A.style.margin="0";this.ghost.dom.appendChild(A)}var B=this.ghost.dom.firstChild;if(B){Ext.fly(B).setStyle("float","none")}},getGhost:function(){return this.ghost},hide:function(A){this.callParent();if(A){this.reset(true)}},stop:function(){if(this.anim&&this.anim.isAnimated&&this.anim.isAnimated()){this.anim.stop()}},sync:function(){this.el.syncUnderlays()},repair:function(C,D,A){var B=this;B.callback=D;B.scope=A;if(C&&B.animRepair!==false){B.el.addCls(B.repairCls);B.el.setUnderlaysVisible(false);B.anim=B.el.animate({duration:B.repairDuration||500,easing:"ease-out",to:{x:C[0],y:C[1]},stopAnimation:true,callback:B.afterRepair,scope:B})}else{B.afterRepair()}},afterRepair:function(){var A=this;A.hide(true);A.el.removeCls(A.repairCls);if(typeof A.callback==="function"){A.callback.call(A.scope||A)}delete A.callback;delete A.scope}})