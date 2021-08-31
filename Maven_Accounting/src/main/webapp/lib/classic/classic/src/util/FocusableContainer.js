Ext.define("Ext.util.FocusableContainer",{extend:"Ext.Mixin",requires:["Ext.util.KeyNav"],mixinConfig:{id:"focusablecontainer",before:{onAdd:"onFocusableChildAdd",onRemove:"onFocusableChildRemove",destroy:"destroyFocusableContainer",onFocusEnter:"onFocusEnter"},after:{afterRender:"initFocusableContainer",onFocusLeave:"onFocusLeave",afterShow:"activateFocusableContainerEl"}},isFocusableContainer:true,enableFocusableContainer:true,activeChildTabIndex:0,inactiveChildTabIndex:-1,privates:{initFocusableContainer:function(C){var B,D,A;if(this.enableFocusableContainer){C=C!=null?C:true;this.doInitFocusableContainer(C)}else{B=this.getFocusables();for(D=0,A=B.length;D<A;D++){B[D].focusableContainer=null}}},doInitFocusableContainer:function(A){var C=this,B,D;B=C.getFocusableContainerEl();if(A){C.clearFocusables()}D=C.findNextFocusableChild({step:1,beforeRender:true});if(D){C.activateFocusableContainerEl(B)}C.focusableContainerMouseListener=C.mon(B,"mousedown",C.onFocusableContainerMousedown,C);C.focusableKeyNav=C.createFocusableContainerKeyNav(B)},createFocusableContainerKeyNav:function(A){var B=this;return new Ext.util.KeyNav(A,{eventName:"keydown",ignoreInputFields:true,scope:B,tab:B.onFocusableContainerTabKey,enter:B.onFocusableContainerEnterKey,space:B.onFocusableContainerSpaceKey,up:B.onFocusableContainerUpKey,down:B.onFocusableContainerDownKey,left:B.onFocusableContainerLeftKey,right:B.onFocusableContainerRightKey})},destroyFocusableContainer:function(){if(this.enableFocusableContainer){this.doDestroyFocusableContainer()}},doDestroyFocusableContainer:function(){var A=this;if(A.keyNav){A.keyNav.destroy()}if(A.focusableContainerMouseListener){A.focusableContainerMouseListener.destroy()}A.focusableKeyNav=A.focusableContainerMouseListener=null},getFocusables:function(){return this.items.items},initDefaultFocusable:function(I){var F=this,H=F.activeChildTabIndex,C=false,E,G,B,D,A;E=F.getFocusables();D=E.length;if(!D){return }for(B=0;B<D;B++){G=E[B];if(G.focusable&&!G.disabled){C=true;A=G.getTabIndex();if(A!=null&&A>=H){return G}}}if(!C){return }G=F.findNextFocusableChild({beforeRender:I,items:E,step:true});if(G){F.activateFocusable(G)}return G},clearFocusables:function(){var E=this,B=E.getFocusables(),A=B.length,D,C;for(C=0;C<A;C++){D=B[C];if(D.focusable&&!D.disabled){E.deactivateFocusable(D)}}},activateFocusable:function(C,B){var A=B!=null?B:this.activeChildTabIndex;C.setTabIndex(A)},deactivateFocusable:function(C,B){var A=B!=null?B:this.inactiveChildTabIndex;C.setTabIndex(A)},onFocusableContainerTabKey:function(){return true},onFocusableContainerEnterKey:function(){return true},onFocusableContainerSpaceKey:function(){return true},onFocusableContainerUpKey:function(A){A.preventDefault();return this.moveChildFocus(A,false)},onFocusableContainerDownKey:function(A){A.preventDefault();return this.moveChildFocus(A,true)},onFocusableContainerLeftKey:function(A){A.preventDefault();return this.moveChildFocus(A,false)},onFocusableContainerRightKey:function(A){A.preventDefault();return this.moveChildFocus(A,true)},getFocusableFromEvent:function(A){var B=Ext.Component.fromElement(A.getTarget());if(!B){Ext.raise("No focusable child found for keyboard event!")}return B},moveChildFocus:function(B,A){var C=this.getFocusableFromEvent(B);return this.focusChild(C,A,B)},focusChild:function(C,A){var B=this.findNextFocusableChild({child:C,step:A});if(B){B.focus()}return B},findNextFocusableChild:function(I){var H=I.beforeRender,E,G,A,B,F,C,D;E=I.items||this.getFocusables();B=I.step!=null?I.step:1;A=I.child;F=Ext.Array.indexOf(E,A);B=B===true?1:B===false?-1:B;D=E.length;C=B>0?(F<D?F+B:0):(F>0?F+B:D-1);for(;;C+=B){if(F<0&&(C>=D||C<0)){return null}else{if(C>=D){C=-1;continue}else{if(C<0){C=D;continue}else{if(C===F){return null}}}}G=E[C];if(!G||!G.focusable||G.disabled){continue}if(H||(G.isFocusable&&G.isFocusable())){return G}}return null},getFocusableContainerEl:function(){return this.el},onFocusableChildAdd:function(A){if(this.enableFocusableContainer){return this.doFocusableChildAdd(A)}},activateFocusableContainerEl:function(A){A=A||this.getFocusableContainerEl();if(A){A.set({tabIndex:this.activeChildTabIndex})}},deactivateFocusableContainerEl:function(A){A=A||this.getFocusableContainerEl();if(A){A.set({tabIndex:undefined})}},isFocusableContainerActive:function(){var D=this,C=false,B,E,A;B=D.getFocusableContainerEl();if(B&&B.isTabbable&&B.isTabbable()){C=true}else{E=D.lastFocusedChild;A=E&&E.getFocusEl&&E.getFocusEl();if(A&&A.isTabbable&&A.isTabbable()){C=true}}return C},doFocusableChildAdd:function(A){if(A.focusable){A.focusableContainer=this}},onFocusableChildRemove:function(A){if(this.enableFocusableContainer){return this.doFocusableChildRemove(A)}A.focusableContainer=null},doFocusableChildRemove:function(A){if(A===this.lastFocusedChild){this.lastFocusedChild=null;this.activateFocusableContainerEl()}},onFocusableContainerMousedown:function(C,B){var A=Ext.Component.fromElement(B);this.mousedownTimestamp=A===this?Ext.Date.now():0;if(A===this){C.preventDefault()}},onFocusEnter:function(C){var A=this,B=C.toComponent,D=A.mousedownTimestamp,F=50,E;if(!A.enableFocusableContainer){return null}A.mousedownTimestamp=0;if(B===A){if(!D||Ext.Date.now()-D>F){E=A.initDefaultFocusable();if(E){A.deactivateFocusableContainerEl();E.focus()}}}else{A.deactivateFocusableContainerEl()}return B},onFocusLeave:function(C){var B=this,A=B.lastFocusedChild;if(!B.enableFocusableContainer){return }if(!B.destroyed&&!B.destroying){B.clearFocusables();if(A&&!A.disabled){B.activateFocusable(A)}else{B.activateFocusableContainerEl()}}},beforeFocusableChildBlur:Ext.privateFn,afterFocusableChildBlur:Ext.privateFn,beforeFocusableChildFocus:function(B){var A=this;if(!A.enableFocusableContainer){return }A.clearFocusables();A.activateFocusable(B);if(B.needArrowKeys){A.guardFocusableChild(B)}},guardFocusableChild:function(D){var C=this,A=C.activeChildTabIndex,B;B=C.findNextFocusableChild({child:D,step:-1});if(B){B.setTabIndex(A)}B=C.findNextFocusableChild({child:D,step:1});if(B){B.setTabIndex(A)}},afterFocusableChildFocus:function(A){if(!this.enableFocusableContainer){return }this.lastFocusedChild=A},beforeFocusableChildEnable:Ext.privateFn,onFocusableChildEnable:function(B){var A=this;if(!A.enableFocusableContainer){return }if(B!==A.lastFocusedChild){A.deactivateFocusable(B);if(!A.isFocusableContainerActive()){A.activateFocusableContainerEl()}}},beforeFocusableChildDisable:function(C){var B=this,A;if(!B.enableFocusableContainer||B.destroying||B.destroyed){return }if(C.hasFocus){A=B.findNextFocusableChild({child:C})||C.findFocusTarget();if(A){A.focus()}}},onFocusableChildDisable:function(D){var C=this,A=C.lastFocusedChild,B;if(!C.enableFocusableContainer||C.destroying||C.destroyed){return }if(D===A){C.activateFocusableContainerEl()}B=C.findNextFocusableChild({step:1});if(!B){C.deactivateFocusableContainerEl()}},onFocusableChildShow:Ext.privateFn,onFocusableChildHide:Ext.privateFn,onFocusableChildMasked:Ext.privateFn,onFocusableChildDestroy:Ext.privateFn,onFocusableChildUpdate:Ext.privateFn}})