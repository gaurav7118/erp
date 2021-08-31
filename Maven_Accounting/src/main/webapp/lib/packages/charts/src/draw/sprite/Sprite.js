Ext.define("Ext.draw.sprite.Sprite",{alias:"sprite.sprite",mixins:{observable:"Ext.mixin.Observable"},requires:["Ext.draw.Draw","Ext.draw.gradient.Gradient","Ext.draw.sprite.AttributeDefinition","Ext.draw.modifier.Target","Ext.draw.modifier.Animation","Ext.draw.modifier.Highlight"],isSprite:true,statics:{defaultHitTestOptions:{fill:true,stroke:true},debug:false},inheritableStatics:{def:{processors:{debug:"default",strokeStyle:"color",fillStyle:"color",strokeOpacity:"limited01",fillOpacity:"limited01",lineWidth:"number",lineCap:"enums(butt,round,square)",lineJoin:"enums(round,bevel,miter)",lineDash:"data",lineDashOffset:"number",miterLimit:"number",shadowColor:"color",shadowOffsetX:"number",shadowOffsetY:"number",shadowBlur:"number",globalAlpha:"limited01",globalCompositeOperation:"enums(source-over,destination-over,source-in,destination-in,source-out,destination-out,source-atop,destination-atop,lighter,xor,copy)",hidden:"bool",transformFillStroke:"bool",zIndex:"number",translationX:"number",translationY:"number",rotationRads:"number",rotationCenterX:"number",rotationCenterY:"number",scalingX:"number",scalingY:"number",scalingCenterX:"number",scalingCenterY:"number",constrainGradients:"bool"},aliases:{"stroke":"strokeStyle","fill":"fillStyle","color":"fillStyle","stroke-width":"lineWidth","stroke-linecap":"lineCap","stroke-linejoin":"lineJoin","stroke-miterlimit":"miterLimit","text-anchor":"textAlign","opacity":"globalAlpha",translateX:"translationX",translateY:"translationY",rotateRads:"rotationRads",rotateCenterX:"rotationCenterX",rotateCenterY:"rotationCenterY",scaleX:"scalingX",scaleY:"scalingY",scaleCenterX:"scalingCenterX",scaleCenterY:"scalingCenterY"},defaults:{hidden:false,zIndex:0,strokeStyle:"none",fillStyle:"none",lineWidth:1,lineDash:[],lineDashOffset:0,lineCap:"butt",lineJoin:"miter",miterLimit:10,shadowColor:"none",shadowOffsetX:0,shadowOffsetY:0,shadowBlur:0,globalAlpha:1,strokeOpacity:1,fillOpacity:1,transformFillStroke:false,translationX:0,translationY:0,rotationRads:0,rotationCenterX:null,rotationCenterY:null,scalingX:1,scalingY:1,scalingCenterX:null,scalingCenterY:null,constrainGradients:false},triggers:{zIndex:"zIndex",globalAlpha:"canvas",globalCompositeOperation:"canvas",transformFillStroke:"canvas",strokeStyle:"canvas",fillStyle:"canvas",strokeOpacity:"canvas",fillOpacity:"canvas",lineWidth:"canvas",lineCap:"canvas",lineJoin:"canvas",lineDash:"canvas",lineDashOffset:"canvas",miterLimit:"canvas",shadowColor:"canvas",shadowOffsetX:"canvas",shadowOffsetY:"canvas",shadowBlur:"canvas",translationX:"transform",translationY:"transform",rotationRads:"transform",rotationCenterX:"transform",rotationCenterY:"transform",scalingX:"transform",scalingY:"transform",scalingCenterX:"transform",scalingCenterY:"transform",constrainGradients:"canvas"},updaters:{bbox:"bboxUpdater",zIndex:function(A){A.dirtyZIndex=true},transform:function(A){A.dirtyTransform=true;A.bbox.transform.dirty=true}}}},config:{parent:null,surface:null},onClassExtended:function(D,C){var B=D.superclass.self.def.initialConfig,E=C.inheritableStatics&&C.inheritableStatics.def,A;if(E){A=Ext.Object.merge({},B,E);D.def=new Ext.draw.sprite.AttributeDefinition(A);delete C.inheritableStatics.def}else{D.def=new Ext.draw.sprite.AttributeDefinition(B)}D.def.spriteClass=D},constructor:function(D){if(Ext.getClassName(this)==="Ext.draw.sprite.Sprite"){throw"Ext.draw.sprite.Sprite is an abstract class"}var F=this,E=F.self.def,G=E.getDefaults(),C;D=Ext.isObject(D)?D:{};F.id=D.id||Ext.id(null,"ext-sprite-");F.attr={};F.mixins.observable.constructor.apply(F,arguments);C=Ext.Array.from(D.modifiers,true);F.prepareModifiers(C);F.initializeAttributes();F.setAttributes(G,true);var A=E.getProcessors();for(var B in D){if(B in A&&F["get"+B.charAt(0).toUpperCase()+B.substr(1)]){Ext.raise("The "+F.$className+" sprite has both a config and an attribute with the same name: "+B+".")}}F.setAttributes(D)},getDirty:function(){return this.attr.dirty},setDirty:function(B){this.attr.dirty=B;if(B){var A=this.getParent();if(A){A.setDirty(true)}}},addModifier:function(A,B){var C=this;if(!(A instanceof Ext.draw.modifier.Modifier)){A=Ext.factory(A,null,null,"modifier")}A.setSprite(C);if(A.preFx||A.config&&A.config.preFx){if(C.fx.getPrevious()){C.fx.getPrevious().setNext(A)}A.setNext(C.fx)}else{C.topModifier.getPrevious().setNext(A);A.setNext(C.topModifier)}if(B){C.initializeAttributes()}return A},prepareModifiers:function(D){var C=this,A,B;C.topModifier=new Ext.draw.modifier.Target({sprite:C});C.fx=new Ext.draw.modifier.Animation({sprite:C});C.fx.setNext(C.topModifier);for(A=0,B=D.length;A<B;A++){C.addModifier(D[A],false)}},getAnimation:function(){return this.fx},setAnimation:function(A){this.fx.setConfig(A)},initializeAttributes:function(){this.topModifier.prepareAttributes(this.attr)},callUpdaters:function(D){var E=this,H=D.pendingUpdaters,I=E.self.def.getUpdaters(),C=false,A=false,B,G,F;E.callUpdaters=Ext.emptyFn;do{C=false;for(G in H){C=true;B=H[G];delete H[G];F=I[G];if(typeof F==="string"){F=E[F]}if(F){F.call(E,D,B)}}A=A||C}while(C);delete E.callUpdaters;if(A){E.setDirty(true)}},scheduleUpdaters:function(A,E,C){var F;if(C){for(var B=0,D=E.length;B<D;B++){F=E[B];this.scheduleUpdater(A,F,C)}}else{for(F in E){C=E[F];this.scheduleUpdater(A,F,C)}}},scheduleUpdater:function(A,C,B){B=B||[];var D=A.pendingUpdaters;if(C in D){if(B.length){D[C]=Ext.Array.merge(D[C],B)}}else{D[C]=B}},setAttributes:function(D,G,C){var A=this.attr,B,E,F;if(G){if(C){this.topModifier.pushDown(A,D)}else{F={};for(B in D){E=D[B];if(E!==A[B]){F[B]=E}}this.topModifier.pushDown(A,F)}}else{this.topModifier.pushDown(A,this.self.def.normalize(D))}},setAttributesBypassingNormalization:function(B,A){return this.setAttributes(B,true,A)},bboxUpdater:function(B){var C=B.rotationRads!==0,A=B.scalingX!==1||B.scalingY!==1,D=B.rotationCenterX===null||B.rotationCenterY===null,E=B.scalingCenterX===null||B.scalingCenterY===null;B.bbox.plain.dirty=true;B.bbox.transform.dirty=true;if(C&&D||A&&E){this.scheduleUpdater(B,"transform")}},getBBox:function(D){var E=this,A=E.attr,F=A.bbox,C=F.plain,B=F.transform;if(C.dirty){E.updatePlainBBox(C);C.dirty=false}if(!D){E.applyTransformations();if(B.dirty){E.updateTransformedBBox(B,C);B.dirty=false}return B}return C},updatePlainBBox:Ext.emptyFn,updateTransformedBBox:function(A,B){this.attr.matrix.transformBBox(B,0,A)},getBBoxCenter:function(A){var B=this.getBBox(A);if(B){return[B.x+B.width*0.5,B.y+B.height*0.5]}else{return[0,0]}},hide:function(){this.attr.hidden=true;this.setDirty(true);return this},show:function(){this.attr.hidden=false;this.setDirty(true);return this},useAttributes:function(I,F){this.applyTransformations();var D=this.attr,H=D.canvasAttributes,E=H.strokeStyle,G=H.fillStyle,B=H.lineDash,C=H.lineDashOffset,A;if(E){if(E.isGradient){I.strokeStyle="black";I.strokeGradient=E}else{I.strokeGradient=false}}if(G){if(G.isGradient){I.fillStyle="black";I.fillGradient=G}else{I.fillGradient=false}}if(B){I.setLineDash(B)}if(Ext.isNumber(C+I.lineDashOffset)){I.lineDashOffset=C}for(A in H){if(H[A]!==undefined&&H[A]!==I[A]){I[A]=H[A]}}this.setGradientBBox(I,F)},setGradientBBox:function(B,C){var A=this.attr;if(A.constrainGradients){B.setGradientBBox({x:C[0],y:C[1],width:C[2],height:C[3]})}else{B.setGradientBBox(this.getBBox(A.transformFillStroke))}},applyTransformations:function(B){if(!B&&!this.attr.dirtyTransform){return }var R=this,K=R.attr,P=R.getBBoxCenter(true),G=P[0],F=P[1],Q=K.translationX,O=K.translationY,J=K.scalingX,I=K.scalingY===null?K.scalingX:K.scalingY,M=K.scalingCenterX===null?G:K.scalingCenterX,L=K.scalingCenterY===null?F:K.scalingCenterY,S=K.rotationRads,E=K.rotationCenterX===null?G:K.rotationCenterX,D=K.rotationCenterY===null?F:K.rotationCenterY,C=Math.cos(S),A=Math.sin(S),N,H;if(J===1&&I===1){M=0;L=0}if(S===0){E=0;D=0}N=M*(1-J)-E;H=L*(1-I)-D;K.matrix.elements=[C*J,A*J,-A*I,C*I,C*N-A*H+E+Q,A*N+C*H+D+O];K.matrix.inverse(K.inverseMatrix);K.dirtyTransform=false;K.bbox.transform.dirty=true},transform:function(B,C){var A=this.attr,E=A.matrix,D;if(B&&B.isMatrix){D=B.elements}else{D=B}if(!(Ext.isArray(D)&&D.length===6)){Ext.raise("An instance of Ext.draw.Matrix or an array of 6 numbers is expected.")}E.prepend.apply(E,D.slice());E.inverse(A.inverseMatrix);if(C){this.updateTransformAttributes()}A.dirtyTransform=false;A.bbox.transform.dirty=true;this.setDirty(true);return this},updateTransformAttributes:function(){var A=this.attr,B=A.matrix.split();A.rotationRads=B.rotate;A.rotationCenterX=0;A.rotationCenterY=0;A.scalingX=B.scaleX;A.scalingY=B.scaleY;A.scalingCenterX=0;A.scalingCenterY=0;A.translationX=B.translateX;A.translationY=B.translateY},resetTransform:function(B){var A=this.attr;A.matrix.reset();A.inverseMatrix.reset();if(!B){this.updateTransformAttributes()}A.dirtyTransform=false;A.bbox.transform.dirty=true;this.setDirty(true);return this},setTransform:function(A,B){this.resetTransform(true);this.transform.call(this,A,B);return this},preRender:Ext.emptyFn,render:Ext.emptyFn,renderBBox:function(A,B){var C=this.getBBox();B.beginPath();B.moveTo(C.x,C.y);B.lineTo(C.x+C.width,C.y);B.lineTo(C.x+C.width,C.y+C.height);B.lineTo(C.x,C.y+C.height);B.closePath();B.strokeStyle="red";B.strokeOpacity=1;B.lineWidth=0.5;B.stroke()},hitTest:function(B,C){if(this.isVisible()){var A=B[0],F=B[1],E=this.getBBox(),D=E&&A>=E.x&&A<=(E.x+E.width)&&F>=E.y&&F<=(E.y+E.height);if(D){return{sprite:this}}}return null},isVisible:function(){var E=this.attr,F=this.getParent(),G=F&&(F.isSurface||F.isVisible()),D=G&&!E.hidden&&E.globalAlpha,B=Ext.draw.Color.NONE,A=Ext.draw.Color.RGBA_NONE,C=E.fillOpacity&&E.fillStyle!==B&&E.fillStyle!==A,I=E.strokeOpacity&&E.strokeStyle!==B&&E.strokeStyle!==A,H=D&&(C||I);return !!H},repaint:function(){var A=this.getSurface();if(A){A.renderFrame()}},remove:function(){var A=this.getSurface();if(A&&A.isSurface){return A.remove(this)}return null},destroy:function(){var B=this,A=B.topModifier,C;while(A){C=A;A=A.getPrevious();C.destroy()}delete B.attr;B.remove();if(B.fireEvent("beforedestroy",B)!==false){B.fireEvent("destroy",B)}B.callParent()}},function(){this.def=new Ext.draw.sprite.AttributeDefinition(this.def);this.def.spriteClass=this})