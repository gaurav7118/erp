Ext.define("Ext.chart.series.sprite.Pie3DPart",{extend:"Ext.draw.sprite.Path",mixins:{markerHolder:"Ext.chart.MarkerHolder"},alias:"sprite.pie3dPart",inheritableStatics:{def:{processors:{centerX:"number",centerY:"number",startAngle:"number",endAngle:"number",startRho:"number",endRho:"number",margin:"number",thickness:"number",bevelWidth:"number",distortion:"number",baseColor:"color",colorSpread:"number",baseRotation:"number",part:"enums(top,bottom,start,end,innerFront,innerBack,outerFront,outerBack)",label:"string"},aliases:{rho:"endRho"},triggers:{centerX:"path,bbox",centerY:"path,bbox",startAngle:"path,partZIndex",endAngle:"path,partZIndex",startRho:"path",endRho:"path,bbox",margin:"path,bbox",thickness:"path",distortion:"path",baseRotation:"path,partZIndex",baseColor:"partZIndex,partColor",colorSpread:"partColor",part:"path,partZIndex",globalAlpha:"canvas,alpha"},defaults:{centerX:0,centerY:0,startAngle:Math.PI*2,endAngle:Math.PI*2,startRho:0,endRho:150,margin:0,thickness:35,distortion:0.5,baseRotation:0,baseColor:"white",colorSpread:1,miterLimit:1,bevelWidth:5,strokeOpacity:0,part:"top",label:""},updaters:{alpha:"alphaUpdater",partColor:"partColorUpdater",partZIndex:"partZIndexUpdater"}}},bevelParams:[],constructor:function(A){this.callParent([A]);this.bevelGradient=new Ext.draw.gradient.Linear({stops:[{offset:0,color:"rgba(255,255,255,0)"},{offset:0.7,color:"rgba(255,255,255,0.6)"},{offset:1,color:"rgba(255,255,255,0)"}]})},alphaUpdater:function(A){var D=this,C=A.globalAlpha,B=D.oldOpacity;if(C!==B&&(C===1||B===1)){D.scheduleUpdater(A,"path",["globalAlpha"]);D.oldOpacity=C}},partColorUpdater:function(A){var D=Ext.draw.Color.fly(A.baseColor),B=D.toString(),E=A.colorSpread,C;switch(A.part){case"top":C=new Ext.draw.gradient.Radial({start:{x:0,y:0,r:0},end:{x:0,y:0,r:1},stops:[{offset:0,color:D.createLighter(0.1*E)},{offset:1,color:D.createDarker(0.1*E)}]});break;case"bottom":C=new Ext.draw.gradient.Radial({start:{x:0,y:0,r:0},end:{x:0,y:0,r:1},stops:[{offset:0,color:D.createDarker(0.2*E)},{offset:1,color:D.toString()}]});break;case"outerFront":case"outerBack":C=new Ext.draw.gradient.Linear({stops:[{offset:0,color:D.createDarker(0.15*E).toString()},{offset:0.3,color:B},{offset:0.8,color:D.createLighter(0.2*E).toString()},{offset:1,color:D.createDarker(0.25*E).toString()}]});break;case"start":C=new Ext.draw.gradient.Linear({stops:[{offset:0,color:D.createDarker(0.1*E).toString()},{offset:1,color:D.createLighter(0.2*E).toString()}]});break;case"end":C=new Ext.draw.gradient.Linear({stops:[{offset:0,color:D.createDarker(0.1*E).toString()},{offset:1,color:D.createLighter(0.2*E).toString()}]});break;case"innerFront":case"innerBack":C=new Ext.draw.gradient.Linear({stops:[{offset:0,color:D.createDarker(0.1*E).toString()},{offset:0.2,color:D.createLighter(0.2*E).toString()},{offset:0.7,color:B},{offset:1,color:D.createDarker(0.1*E).toString()}]});break}A.fillStyle=C;A.canvasAttributes.fillStyle=C},partZIndexUpdater:function(A){var C=Ext.draw.sprite.AttributeParser.angle,E=A.baseRotation,D=A.startAngle,B=A.endAngle,F;switch(A.part){case"top":A.zIndex=5;break;case"outerFront":D=C(D+E);B=C(B+E);if(D>=0&&B<0){F=Math.sin(D)}else{if(D<=0&&B>0){F=Math.sin(B)}else{if(D>=0&&B>0){if(D>B){F=0}else{F=Math.max(Math.sin(D),Math.sin(B))}}else{F=1}}}A.zIndex=4+F;break;case"outerBack":A.zIndex=1;break;case"start":A.zIndex=4+Math.sin(C(D+E));break;case"end":A.zIndex=4+Math.sin(C(B+E));break;case"innerFront":A.zIndex=2;break;case"innerBack":A.zIndex=4+Math.sin(C((D+B)/2+E));break;case"bottom":A.zIndex=0;break}A.dirtyZIndex=true},updatePlainBBox:function(K){var F=this.attr,A=F.part,B=F.baseRotation,E=F.centerX,D=F.centerY,J,C,I,H,G,L;if(A==="start"){C=F.startAngle+B}else{if(A==="end"){C=F.endAngle+B}}if(Ext.isNumber(C)){G=Math.sin(C);L=Math.cos(C);I=Math.min(E+L*F.startRho,E+L*F.endRho);H=D+G*F.startRho*F.distortion;K.x=I;K.y=H;K.width=L*(F.endRho-F.startRho);K.height=F.thickness+G*(F.endRho-F.startRho)*2;return }if(A==="innerFront"||A==="innerBack"){J=F.startRho}else{J=F.endRho}K.width=J*2;K.height=J*F.distortion*2+F.thickness;K.x=F.centerX-J;K.y=F.centerY-J*F.distortion},updateTransformedBBox:function(A){if(this.attr.part==="start"||this.attr.part==="end"){return this.callParent(arguments)}return this.updatePlainBBox(A)},updatePath:function(A){if(!this.attr.globalAlpha){return }if(this.attr.endAngle<this.attr.startAngle){return }this[this.attr.part+"Renderer"](A)},render:function(B,C){var D=this,A=D.attr;if(!A.globalAlpha){return }D.callParent([B,C]);D.bevelRenderer(B,C);if(A.label&&D.getMarker("labels")){D.placeLabel()}},placeLabel:function(){var X=this,U=X.attr,T=U.attributeId,P=U.margin,C=U.distortion,I=U.centerX,H=U.centerY,J=U.baseRotation,V=U.startAngle+J,R=U.endAngle+J,M=(V+R)/2,W=U.startRho+P,O=U.endRho+P,N=(W+O)/2,A=Math.sin(M),B=Math.cos(M),E=X.surfaceMatrix,G=X.getMarker("labels"),F=G.getTemplate(),D=F.getCalloutLine(),S=D&&D.length||40,Q={},L,K;E.appendMatrix(U.matrix);Q.text=U.label;L=I+B*N;K=H+A*N*C;Q.x=E.x(L,K);Q.y=E.y(L,K);L=I+B*O;K=H+A*O*C;Q.calloutStartX=E.x(L,K);Q.calloutStartY=E.y(L,K);L=I+B*(O+S);K=H+A*(O+S)*C;Q.calloutPlaceX=E.x(L,K);Q.calloutPlaceY=E.y(L,K);Q.calloutWidth=2;X.putMarker("labels",Q,T);X.putMarker("labels",{callout:1},T)},bevelRenderer:function(B,C){var F=this,A=F.attr,E=A.bevelWidth,G=F.bevelParams,D;for(D=0;D<G.length;D++){C.beginPath();C.ellipse.apply(C,G[D]);C.save();C.lineWidth=E;C.strokeOpacity=E?1:0;C.strokeGradient=F.bevelGradient;C.stroke(A);C.restore()}},lidRenderer:function(O,M){var K=this.attr,G=K.margin,C=K.distortion,I=K.centerX,H=K.centerY,F=K.baseRotation,J=K.startAngle+F,E=K.endAngle+F,D=(J+E)/2,L=K.startRho,B=K.endRho,N=Math.sin(E),A=Math.cos(E);I+=Math.cos(D)*G;H+=Math.sin(D)*G*C;O.ellipse(I,H+M,L,L*C,0,J,E,false);O.lineTo(I+A*B,H+M+N*B*C);O.ellipse(I,H+M,B,B*C,0,E,J,true);O.closePath()},topRenderer:function(A){this.lidRenderer(A,0)},bottomRenderer:function(B){var A=this.attr;if(A.globalAlpha<1||A.shadowColor!==Ext.draw.Color.RGBA_NONE){this.lidRenderer(B,A.thickness)}},sideRenderer:function(L,S){var O=this.attr,K=O.margin,G=O.centerX,F=O.centerY,E=O.distortion,H=O.baseRotation,P=O.startAngle+H,M=O.endAngle+H,A=O.thickness,Q=O.startRho,J=O.endRho,R=(S==="start"&&P)||(S==="end"&&M),B=Math.sin(R),D=Math.cos(R),C=O.globalAlpha<1,N=S==="start"&&D<0||S==="end"&&D>0||C,I;if(N){I=(P+M)/2;G+=Math.cos(I)*K;F+=Math.sin(I)*K*E;L.moveTo(G+D*Q,F+B*Q*E);L.lineTo(G+D*J,F+B*J*E);L.lineTo(G+D*J,F+B*J*E+A);L.lineTo(G+D*Q,F+B*Q*E+A);L.closePath()}},startRenderer:function(A){this.sideRenderer(A,"start")},endRenderer:function(A){this.sideRenderer(A,"end")},rimRenderer:function(Q,E,O,J){var W=this,S=W.attr,P=S.margin,H=S.centerX,G=S.centerY,D=S.distortion,I=S.baseRotation,T=Ext.draw.sprite.AttributeParser.angle,U=S.startAngle+I,R=S.endAngle+I,K=T((U+R)/2),A=S.thickness,B=S.globalAlpha<1,C,N,V;W.bevelParams=[];U=T(U);R=T(R);H+=Math.cos(K)*P;G+=Math.sin(K)*P*D;C=U>=0&&R>=0;N=U<=0&&R<=0;function L(){Q.ellipse(H,G+A,E,E*D,0,Math.PI,U,true);Q.lineTo(H+Math.cos(U)*E,G+Math.sin(U)*E*D);V=[H,G,E,E*D,0,U,Math.PI,false];if(!O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.closePath()}function F(){Q.ellipse(H,G+A,E,E*D,0,0,R,false);Q.lineTo(H+Math.cos(R)*E,G+Math.sin(R)*E*D);V=[H,G,E,E*D,0,R,0,true];if(!O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.closePath()}function X(){Q.ellipse(H,G+A,E,E*D,0,Math.PI,R,false);Q.lineTo(H+Math.cos(R)*E,G+Math.sin(R)*E*D);V=[H,G,E,E*D,0,R,Math.PI,true];if(O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.closePath()}function M(){Q.ellipse(H,G+A,E,E*D,0,U,0,false);Q.lineTo(H+E,G);V=[H,G,E,E*D,0,0,U,true];if(O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.closePath()}if(J){if(!O||B){if(U>=0&&R<0){L()}else{if(U<=0&&R>0){F()}else{if(U<=0&&R<0){if(U>R){Q.ellipse(H,G+A,E,E*D,0,0,Math.PI,false);Q.lineTo(H-E,G);V=[H,G,E,E*D,0,Math.PI,0,true];if(!O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.closePath()}}else{if(U>R){L();F()}else{V=[H,G,E,E*D,0,U,R,false];if(C&&!O||N&&O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.lineTo(H+Math.cos(R)*E,G+Math.sin(R)*E*D+A);Q.ellipse(H,G+A,E,E*D,0,R,U,true);Q.closePath()}}}}}}else{if(O||B){if(U>=0&&R<0){X()}else{if(U<=0&&R>0){M()}else{if(U<=0&&R<0){if(U>R){X();M()}else{Q.ellipse(H,G+A,E,E*D,0,U,R,false);Q.lineTo(H+Math.cos(R)*E,G+Math.sin(R)*E*D);V=[H,G,E,E*D,0,R,U,true];if(O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.closePath()}}else{if(U>R){Q.ellipse(H,G+A,E,E*D,0,-Math.PI,0,false);Q.lineTo(H+E,G);V=[H,G,E,E*D,0,0,-Math.PI,true];if(O){W.bevelParams.push(V)}Q.ellipse.apply(Q,V);Q.closePath()}}}}}}},innerFrontRenderer:function(A){this.rimRenderer(A,this.attr.startRho,true,true)},innerBackRenderer:function(A){this.rimRenderer(A,this.attr.startRho,true,false)},outerFrontRenderer:function(A){this.rimRenderer(A,this.attr.endRho,false,true)},outerBackRenderer:function(A){this.rimRenderer(A,this.attr.endRho,false,false)}})