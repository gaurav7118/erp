Ext.define("Ext.chart.series.sprite.Scatter",{alias:"sprite.scatterSeries",extend:"Ext.chart.series.sprite.Cartesian",renderClipped:function(Q,R,V,T){if(this.cleanRedraw){return }var Z=this,P=Z.attr,K=P.dataX,H=P.dataY,W=P.labels,I=Z.getSeries(),B=W&&Z.getMarker("labels"),S=Z.attr.matrix,C=S.getXX(),O=S.getYY(),L=S.getDX(),J=S.getDY(),M={},a,Y,D=Q.getInherited().rtl&&!P.flipXY?-1:1,A,X,N,E,G,F,U;if(P.flipXY){A=T[1]-C*D;X=T[1]+T[3]+C*D;N=T[0]-O;E=T[0]+T[2]+O}else{A=T[0]-C*D;X=T[0]+T[2]+C*D;N=T[1]-O;E=T[1]+T[3]+O}for(U=0;U<K.length;U++){G=K[U];F=H[U];G=G*C+L;F=F*O+J;if(A<=G&&G<=X&&N<=F&&F<=E){if(P.renderer){M={type:"items",translationX:G,translationY:F};Y=[Z,M,{store:Z.getStore()},U];a=Ext.callback(P.renderer,null,Y,0,I);M=Ext.apply(M,a)}else{M.translationX=G;M.translationY=F}Z.putMarker("items",M,U,!P.renderer);if(B&&W[U]){Z.drawLabel(W[U],G,F,U,T)}}}},drawLabel:function(J,H,G,P,A){var R=this,M=R.attr,D=R.getMarker("labels"),C=D.getTemplate(),L=R.labelCfg||(R.labelCfg={}),B=R.surfaceMatrix,F,E,I=M.labelOverflowPadding,O=M.flipXY,K,N,S,Q;L.text=J;N=R.getMarkerBBox("labels",P,true);if(!N){R.putMarker("labels",L,P);N=R.getMarkerBBox("labels",P,true)}if(O){L.rotationRads=Math.PI*0.5}else{L.rotationRads=0}K=N.height/2;F=H;switch(C.attr.display){case"under":E=G-K-I;break;case"rotate":F+=I;E=G-I;L.rotationRads=-Math.PI/4;break;default:E=G+K+I}L.x=B.x(F,E);L.y=B.y(F,E);if(C.attr.renderer){Q=[J,D,L,{store:R.getStore()},P];S=Ext.callback(C.attr.renderer,null,Q,0,R.getSeries());if(typeof S==="string"){L.text=S}else{Ext.apply(L,S)}}R.putMarker("labels",L,P)}})