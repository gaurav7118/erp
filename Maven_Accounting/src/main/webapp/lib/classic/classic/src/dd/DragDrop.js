Ext.define("Ext.dd.DragDrop",{requires:["Ext.dd.DragDropManager"],constructor:function(C,A,B){if(C){this.init(C,A,B)}},id:null,config:null,dragElId:null,handleElId:null,invalidHandleTypes:null,invalidHandleIds:null,invalidHandleClasses:null,startPageX:0,startPageY:0,groups:null,locked:false,lock:function(){this.locked=true},moveOnly:false,unlock:function(){this.locked=false},isTarget:true,padding:null,_domRef:null,__ygDragDrop:true,constrainX:false,constrainY:false,minX:0,maxX:0,minY:0,maxY:0,maintainOffset:false,xTicks:null,yTicks:null,primaryButtonOnly:true,available:false,hasOuterHandles:false,triggerEvent:"mousedown",b4StartDrag:function(A,B){},startDrag:function(A,B){},b4Drag:function(A){},onDrag:function(A){},onDragEnter:function(A,B){},b4DragOver:function(A){},onDragOver:function(A,B){},b4DragOut:function(A){},onDragOut:function(A,B){},b4DragDrop:function(A){},onDragDrop:function(A,B){},onInvalidDrop:function(A){},b4EndDrag:function(A){},endDrag:function(A){},b4MouseDown:function(A){},onMouseDown:function(A){},onMouseUp:function(A){},onAvailable:function(){},defaultPadding:{left:0,right:0,top:0,bottom:0},constrainTo:function(D,B,J){if(Ext.isNumber(B)){B={left:B,right:B,top:B,bottom:B}}B=B||this.defaultPadding;var F=Ext.get(this.getEl()).getBox(),K=Ext.get(D),I=K.getScroll(),E,G=K.dom,H,C,A;if(G===document.body){E={x:I.left,y:I.top,width:Ext.Element.getViewportWidth(),height:Ext.Element.getViewportHeight()}}else{H=K.getXY();E={x:H[0],y:H[1],width:G.clientWidth,height:G.clientHeight}}C=F.y-E.y;A=F.x-E.x;this.resetConstraints();this.setXConstraint(A-(B.left||0),E.width-A-F.width-(B.right||0),this.xTickSize);this.setYConstraint(C-(B.top||0),E.height-C-F.height-(B.bottom||0),this.yTickSize)},getEl:function(){if(!this._domRef){this._domRef=Ext.getDom(this.id)}return this._domRef},getDragEl:function(){return Ext.getDom(this.dragElId)},init:function(D,A,B){var C=this;C.el=C.el||Ext.get(D);C.initTarget(D,A,B);Ext.get(C.id).on(C.triggerEvent,C.handleMouseDown,C)},initTarget:function(C,A,B){this.config=B||{};this.DDMInstance=Ext.dd.DragDropManager;this.groups={};if(typeof C!=="string"){C=Ext.id(C)}this.id=C;this.addToGroup((A)?A:"default");this.handleElId=C;this.setDragElId(C);this.invalidHandleTypes={A:"A"};this.invalidHandleIds={};this.invalidHandleClasses=[];this.applyConfig();this.handleOnAvailable()},applyConfig:function(){this.padding=this.config.padding||[0,0,0,0];this.isTarget=(this.config.isTarget!==false);this.maintainOffset=(this.config.maintainOffset);this.primaryButtonOnly=(this.config.primaryButtonOnly!==false)},handleOnAvailable:function(){this.available=true;this.resetConstraints();this.onAvailable()},setPadding:function(C,A,D,B){if(!A&&0!==A){this.padding=[C,C,C,C]}else{if(!D&&0!==D){this.padding=[C,A,C,A]}else{this.padding=[C,A,D,B]}}},setInitPosition:function(D,C){var E=this.getEl(),B,A,F;if(!this.DDMInstance.verifyEl(E)){return }B=D||0;A=C||0;F=Ext.fly(E).getXY();this.initPageX=F[0]-B;this.initPageY=F[1]-A;this.lastPageX=F[0];this.lastPageY=F[1];this.setStartPosition(F)},setStartPosition:function(B){var A=B||Ext.fly(this.getEl()).getXY();this.deltaSetXY=null;this.startPageX=A[0];this.startPageY=A[1]},addToGroup:function(A){this.groups[A]=true;this.DDMInstance.regDragDrop(this,A)},removeFromGroup:function(A){if(this.groups[A]){delete this.groups[A]}this.DDMInstance.removeDDFromGroup(this,A)},setDragElId:function(A){this.dragElId=A},setHandleElId:function(A){if(typeof A!=="string"){A=Ext.id(A)}this.handleElId=A;this.DDMInstance.regHandle(this.id,A)},setOuterHandleElId:function(A){if(typeof A!=="string"){A=Ext.id(A)}Ext.get(A).on(this.triggerEvent,this.handleMouseDown,this);this.setHandleElId(A);this.hasOuterHandles=true},unreg:function(){var B=this,A;if(B._domRef){A=Ext.fly(B.id);if(A){A.un(B.triggerEvent,B.handleMouseDown,B)}}B._domRef=null;B.DDMInstance._remove(B,B.autoGroup)},destroy:function(){this.unreg();this.callParent()},isLocked:function(){return(this.DDMInstance.isLocked()||this.locked)},handleMouseDown:function(C,B){var A=this;if((A.primaryButtonOnly&&C.button)||A.isLocked()){return }A.DDMInstance.refreshCache(A.groups);if(A.hasOuterHandles||A.DDMInstance.isOverTarget(C.getPoint(),A)){if(A.clickValidator(C)){A.setStartPosition();A.b4MouseDown(C);A.onMouseDown(C);A.DDMInstance.handleMouseDown(C,A);A.DDMInstance.stopEvent(C)}}},clickValidator:function(B){var A=B.getTarget();return(this.isValidHandleChild(A)&&(this.id===this.handleElId||this.DDMInstance.handleWasClicked(A,this.id)))},addInvalidHandleType:function(A){var B=A.toUpperCase();this.invalidHandleTypes[B]=B},addInvalidHandleId:function(A){if(typeof A!=="string"){A=Ext.id(A)}this.invalidHandleIds[A]=A},addInvalidHandleClass:function(A){this.invalidHandleClasses.push(A)},removeInvalidHandleType:function(A){var B=A.toUpperCase();delete this.invalidHandleTypes[B]},removeInvalidHandleId:function(A){if(typeof A!=="string"){A=Ext.id(A)}delete this.invalidHandleIds[A]},removeInvalidHandleClass:function(B){var D=this.invalidHandleClasses,A=D.length,C;for(C=0;C<A;++C){if(D[C]===B){delete D[C]}}},isValidHandleChild:function(D){var C=true,F,B,A;try{F=D.nodeName.toUpperCase()}catch(E){F=D.nodeName}C=C&&!this.invalidHandleTypes[F];C=C&&!this.invalidHandleIds[D.id];for(B=0,A=this.invalidHandleClasses.length;C&&B<A;++B){C=!Ext.fly(D).hasCls(this.invalidHandleClasses[B])}return C},setXTicks:function(D,A){this.xTicks=[];this.xTickSize=A;var C={},B;for(B=this.initPageX;B>=this.minX;B=B-A){if(!C[B]){this.xTicks[this.xTicks.length]=B;C[B]=true}}for(B=this.initPageX;B<=this.maxX;B=B+A){if(!C[B]){this.xTicks[this.xTicks.length]=B;C[B]=true}}Ext.Array.sort(this.xTicks,this.DDMInstance.numericSort)},setYTicks:function(D,A){this.yTicks=[];this.yTickSize=A;var C={},B;for(B=this.initPageY;B>=this.minY;B=B-A){if(!C[B]){this.yTicks[this.yTicks.length]=B;C[B]=true}}for(B=this.initPageY;B<=this.maxY;B=B+A){if(!C[B]){this.yTicks[this.yTicks.length]=B;C[B]=true}}Ext.Array.sort(this.yTicks,this.DDMInstance.numericSort)},setXConstraint:function(C,B,A){this.leftConstraint=C;this.rightConstraint=B;this.minX=this.initPageX-C;this.maxX=this.initPageX+B;if(A){this.setXTicks(this.initPageX,A)}this.constrainX=true},clearConstraints:function(){this.constrainX=false;this.constrainY=false;this.clearTicks()},clearTicks:function(){this.xTicks=null;this.yTicks=null;this.xTickSize=0;this.yTickSize=0},setYConstraint:function(A,C,B){this.topConstraint=A;this.bottomConstraint=C;this.minY=this.initPageY-A;this.maxY=this.initPageY+C;if(B){this.setYTicks(this.initPageY,B)}this.constrainY=true},resetConstraints:function(){if(this.initPageX||this.initPageX===0){var B=(this.maintainOffset)?this.lastPageX-this.initPageX:0,A=(this.maintainOffset)?this.lastPageY-this.initPageY:0;this.setInitPosition(B,A)}else{this.setInitPosition()}if(this.constrainX){this.setXConstraint(this.leftConstraint,this.rightConstraint,this.xTickSize)}if(this.constrainY){this.setYConstraint(this.topConstraint,this.bottomConstraint,this.yTickSize)}},getTick:function(G,D){if(!D){return G}else{if(D[0]>=G){return D[0]}else{var B,A,C,F,E;for(B=0,A=D.length;B<A;++B){C=B+1;if(D[C]&&D[C]>=G){F=G-D[B];E=D[C]-G;return(E>F)?D[B]:D[C]}}return D[D.length-1]}}},toString:function(){return("DragDrop "+this.id)}})