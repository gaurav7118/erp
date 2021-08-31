Ext.define("Ext.dd.ScrollManager",{singleton:true,requires:["Ext.dd.DragDropManager"],dirTrans:{up:-1,left:-1,down:1,right:1},constructor:function(){var A=Ext.dd.DragDropManager;A.fireEvents=Ext.Function.createSequence(A.fireEvents,this.onFire,this);A.stopDrag=Ext.Function.createSequence(A.stopDrag,this.onStop,this);this.doScroll=this.doScroll.bind(this);this.ddmInstance=A;this.els={};this.dragEl=null;this.proc={}},onStop:function(A){var B=Ext.dd.ScrollManager;B.dragEl=null;B.clearProc()},triggerRefresh:function(){if(this.ddmInstance.dragCurrent){this.ddmInstance.refreshCache(this.ddmInstance.dragCurrent.groups)}},doScroll:function(){var F=this;if(F.ddmInstance.dragCurrent){var A=F.proc,E=A.el,C=A.component,G=A.el.ddScrollConfig,H=G&&G.increment?G.increment:F.increment,B=G&&"animate" in G?G.animate:F.animate,D=function(){F.triggerRefresh()};if(B){if(B===true){B={callback:D}}else{B.callback=B.callback?Ext.Function.createSequence(B.callback,D):D}}if(C){H=H*F.dirTrans[A.dir];if(A.dir==="up"||A.dir==="down"){C.scrollBy(0,H,B)}else{C.scrollBy(H,0,B)}}else{E.scroll(A.dir,H,B)}if(!B){D()}}},clearProc:function(){var A=this.proc;if(A.id){clearInterval(A.id)}A.id=0;A.el=null;A.dir=""},startProc:function(C,B){var D=this,A=D.proc,F,E;D.clearProc();A.el=C;A.dir=B;F=C.ddScrollConfig?C.ddScrollConfig.ddGroup:undefined;E=(C.ddScrollConfig&&C.ddScrollConfig.frequency)?C.ddScrollConfig.frequency:D.frequency;if(F===undefined||D.ddmInstance.dragCurrent.ddGroup===F){A.id=Ext.interval(D.doScroll,E)}},onFire:function(F,I){var H=this,J,G,D,A,B,E,C;if(I||!H.ddmInstance.dragCurrent){return }if(!H.dragEl||H.dragEl!==H.ddmInstance.dragCurrent){H.dragEl=H.ddmInstance.dragCurrent;H.refreshCache()}J=F.getPoint();G=H.proc;D=H.els;for(A in D){B=D[A];E=B._region;C=B.ddScrollConfig||H;if(E&&E.contains(J)&&B.isScrollable()){if(E.bottom-J.y<=C.vthresh){if(G.el!==B){H.startProc(B,"down")}return }else{if(E.right-J.x<=C.hthresh){if(G.el!==B){H.startProc(B,"right")}return }else{if(J.y-E.top<=C.vthresh){if(G.el!==B){H.startProc(B,"up")}return }else{if(J.x-E.left<=C.hthresh){if(G.el!==B){H.startProc(B,"left")}return }}}}}}H.clearProc()},register:function(C){if(Ext.isArray(C)){for(var B=0,A=C.length;B<A;B++){this.register(C[B])}}else{C=Ext.get(C);this.els[C.id]=C}},unregister:function(C){if(Ext.isArray(C)){for(var B=0,A=C.length;B<A;B++){this.unregister(C[B])}}else{C=Ext.get(C);delete this.els[C.id]}},vthresh:25*(window.devicePixelRatio||1),hthresh:25*(window.devicePixelRatio||1),increment:100,frequency:500,animate:true,animDuration:0.4,ddGroup:undefined,refreshCache:function(){var A=this.els,B;for(B in A){if(typeof A[B]==="object"){A[B]._region=A[B].getRegion()}}}})