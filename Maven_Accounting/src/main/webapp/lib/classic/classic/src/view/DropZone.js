Ext.define("Ext.view.DropZone",{extend:"Ext.dd.DropZone",indicatorCls:Ext.baseCSSPrefix+"grid-drop-indicator",indicatorHtml:['<div class="',Ext.baseCSSPrefix,'grid-drop-indicator-left" role="presentation"></div>','<div class="'+Ext.baseCSSPrefix+'grid-drop-indicator-right" role="presentation"></div>'].join(""),constructor:function(A){var B=this;Ext.apply(B,A);if(!B.ddGroup){B.ddGroup="view-dd-zone-"+B.view.id}B.callParent([B.view.el])},fireViewEvent:function(){var B=this,A;B.lock();A=B.view.fireEvent.apply(B.view,arguments);B.unlock();return A},getTargetFromEvent:function(H){var G=H.getTarget(this.view.getItemSelector()),D,C,B,E,A,F;if(!G){D=H.getY();for(E=0,C=this.view.getNodes(),A=C.length;E<A;E++){B=C[E];F=Ext.fly(B).getBox();if(D<=F.bottom){return B}}}return G},getIndicator:function(){var A=this;if(!A.indicator){A.indicator=new Ext.Component({ariaRole:"presentation",html:A.indicatorHtml,cls:A.indicatorCls,ownerCt:A.view,floating:true,shadow:false})}return A.indicator},getPosition:function(C,A){var E=C.getXY()[1],B=Ext.fly(A).getRegion(),D;if((B.bottom-E)>=(B.bottom-B.top)/2){D="before"}else{D="after"}return D},containsRecordAtOffset:function(D,B,F){if(!B){return false}var A=this.view,C=A.indexOf(B),E=A.getNode(C+F),G=E?A.getRecord(E):null;return G&&Ext.Array.contains(D,G)},positionIndicator:function(B,C,D){var E=this,G=E.view,F=E.getPosition(D,B),I=G.getRecord(B),A=C.records,H;if(!Ext.Array.contains(A,I)&&(F==="before"&&!E.containsRecordAtOffset(A,I,-1)||F==="after"&&!E.containsRecordAtOffset(A,I,1))){E.valid=true;if(E.overRecord!==I||E.currentPosition!==F){H=Ext.fly(B).getY()-G.el.getY()-1;if(F==="after"){H+=Ext.fly(B).getHeight()}if(G.touchScroll===2){H+=G.getScrollY()}E.getIndicator().setWidth(Ext.fly(G.el).getWidth()).showAt(0,H);E.overRecord=I;E.currentPosition=F}}else{E.invalidateDrop()}},invalidateDrop:function(){if(this.valid){this.valid=false;this.getIndicator().hide()}},onNodeOver:function(C,A,E,D){var B=this;if(!Ext.Array.contains(D.records,B.view.getRecord(C))){B.positionIndicator(C,D,E)}return B.valid?B.dropAllowed:B.dropNotAllowed},notifyOut:function(C,A,E,D){var B=this;B.callParent(arguments);B.overRecord=B.currentPosition=null;B.valid=false;if(B.indicator){B.indicator.hide()}},onContainerOver:function(A,F,E){var D=this,B=D.view,C=B.dataSource.getCount();if(C){D.positionIndicator(B.all.last(),E,F)}else{D.overRecord=D.currentPosition=null;D.getIndicator().setWidth(Ext.fly(B.el).getWidth()).showAt(0,0);D.valid=true}return D.dropAllowed},onContainerDrop:function(A,C,B){return this.onNodeDrop(A,null,C,B)},onNodeDrop:function(G,A,F,E){var D=this,C=false,B={wait:false,processDrop:function(){D.invalidateDrop();D.handleNodeDrop(E,D.overRecord,D.currentPosition);C=true;D.fireViewEvent("drop",G,E,D.overRecord,D.currentPosition)},cancelDrop:function(){D.invalidateDrop();C=true}},H=false;if(D.valid){H=D.fireViewEvent("beforedrop",G,E,D.overRecord,D.currentPosition,B);if(B.wait){return }if(H!==false){if(!C){B.processDrop()}}}return H},destroy:function(){this.indicator=Ext.destroy(this.indicator);this.callParent()}})