Ext.define("Ext.draw.sprite.AttributeDefinition",{requires:["Ext.draw.sprite.AttributeParser","Ext.draw.sprite.AnimationParser"],config:{defaults:{$value:{},lazy:true},aliases:{},animationProcessors:{},processors:{$value:{},lazy:true},dirtyTriggers:{},triggers:{},updaters:{}},inheritableStatics:{processorFactoryRe:/^(\w+)\(([\w\-,]*)\)$/},spriteClass:null,constructor:function(A){var B=this;B.initConfig(A)},applyDefaults:function(B,A){A=Ext.apply(A||{},this.normalize(B));return A},applyAliases:function(B,A){return Ext.apply(A||{},B)},applyProcessors:function(E,I){this.getAnimationProcessors();var J=I||{},H=Ext.draw.sprite.AttributeParser,A=this.self.processorFactoryRe,G={},D,B,C,F;for(B in E){F=E[B];if(typeof F==="string"){C=F.match(A);if(C){F=H[C[1]].apply(H,C[2].split(","))}else{if(H[F]){G[B]=F;D=true;F=H[F]}}}if(!Ext.isFunction(F)){Ext.raise(this.spriteClass.$className+": processor '"+B+"' has not been found.")}J[B]=F}if(D){this.setAnimationProcessors(G)}return J},applyAnimationProcessors:function(C,A){var E=Ext.draw.sprite.AnimationParser,B,D;if(!A){A={}}for(B in C){D=C[B];if(D==="none"){A[B]=null}else{if(Ext.isString(D)&&!(B in A)){if(D in E){while(Ext.isString(E[D])){D=E[D]}A[B]=E[D]}}else{if(Ext.isObject(D)){A[B]=D}}}}return A},updateDirtyTriggers:function(A){this.setTriggers(A)},applyTriggers:function(B,C){if(!C){C={}}for(var A in B){C[A]=B[A].split(",")}return C},applyUpdaters:function(B,A){return Ext.apply(A||{},B)},batchedNormalize:function(F,M){if(!F){return{}}var I=this.getProcessors(),D=this.getAliases(),A=F.translation||F.translate,N={},G,H,B,E,O,C,L,K,J;if("rotation" in F){O=F.rotation}else{O=("rotate" in F)?F.rotate:undefined}if("scaling" in F){C=F.scaling}else{C=("scale" in F)?F.scale:undefined}if(typeof C!=="undefined"){if(Ext.isNumber(C)){N.scalingX=C;N.scalingY=C}else{if("x" in C){N.scalingX=C.x}if("y" in C){N.scalingY=C.y}if("centerX" in C){N.scalingCenterX=C.centerX}if("centerY" in C){N.scalingCenterY=C.centerY}}}if(typeof O!=="undefined"){if(Ext.isNumber(O)){O=Ext.draw.Draw.rad(O);N.rotationRads=O}else{if("rads" in O){N.rotationRads=O.rads}else{if("degrees" in O){if(Ext.isArray(O.degrees)){N.rotationRads=Ext.Array.map(O.degrees,function(P){return Ext.draw.Draw.rad(P)})}else{N.rotationRads=Ext.draw.Draw.rad(O.degrees)}}}if("centerX" in O){N.rotationCenterX=O.centerX}if("centerY" in O){N.rotationCenterY=O.centerY}}}if(typeof A!=="undefined"){if("x" in A){N.translationX=A.x}if("y" in A){N.translationY=A.y}}if("matrix" in F){L=Ext.draw.Matrix.create(F.matrix);J=L.split();N.matrix=L;N.rotationRads=J.rotation;N.rotationCenterX=0;N.rotationCenterY=0;N.scalingX=J.scaleX;N.scalingY=J.scaleY;N.scalingCenterX=0;N.scalingCenterY=0;N.translationX=J.translateX;N.translationY=J.translateY}for(B in F){E=F[B];if(typeof E==="undefined"){continue}else{if(Ext.isArray(E)){if(B in D){B=D[B]}if(B in I){N[B]=[];for(G=0,H=E.length;G<H;G++){K=I[B].call(this,E[G]);if(typeof K!=="undefined"){N[B][G]=K}}}else{if(M){N[B]=E}}}else{if(B in D){B=D[B]}if(B in I){E=I[B].call(this,E);if(typeof E!=="undefined"){N[B]=E}}else{if(M){N[B]=E}}}}}return N},normalize:function(I,J){if(!I){return{}}var F=this.getProcessors(),D=this.getAliases(),A=I.translation||I.translate,K={},B,E,L,C,H,G;if("rotation" in I){L=I.rotation}else{L=("rotate" in I)?I.rotate:undefined}if("scaling" in I){C=I.scaling}else{C=("scale" in I)?I.scale:undefined}if(A){if("x" in A){K.translationX=A.x}if("y" in A){K.translationY=A.y}}if(typeof C!=="undefined"){if(Ext.isNumber(C)){K.scalingX=C;K.scalingY=C}else{if("x" in C){K.scalingX=C.x}if("y" in C){K.scalingY=C.y}if("centerX" in C){K.scalingCenterX=C.centerX}if("centerY" in C){K.scalingCenterY=C.centerY}}}if(typeof L!=="undefined"){if(Ext.isNumber(L)){L=Ext.draw.Draw.rad(L);K.rotationRads=L}else{if("rads" in L){K.rotationRads=L.rads}else{if("degrees" in L){K.rotationRads=Ext.draw.Draw.rad(L.degrees)}}if("centerX" in L){K.rotationCenterX=L.centerX}if("centerY" in L){K.rotationCenterY=L.centerY}}}if("matrix" in I){H=Ext.draw.Matrix.create(I.matrix);G=H.split();K.matrix=H;K.rotationRads=G.rotation;K.rotationCenterX=0;K.rotationCenterY=0;K.scalingX=G.scaleX;K.scalingY=G.scaleY;K.scalingCenterX=0;K.scalingCenterY=0;K.translationX=G.translateX;K.translationY=G.translateY}for(B in I){E=I[B];if(typeof E==="undefined"){continue}if(B in D){B=D[B]}if(B in F){E=F[B].call(this,E);if(typeof E!=="undefined"){K[B]=E}}else{if(J){K[B]=E}}}return K},setBypassingNormalization:function(A,C,B){return C.pushDown(A,B)},set:function(A,C,B){B=this.normalize(B);return this.setBypassingNormalization(A,C,B)}})