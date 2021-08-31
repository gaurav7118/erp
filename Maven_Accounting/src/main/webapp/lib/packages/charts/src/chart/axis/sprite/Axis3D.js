Ext.define("Ext.chart.axis.sprite.Axis3D",{extend:"Ext.chart.axis.sprite.Axis",alias:"sprite.axis3d",type:"axis3d",inheritableStatics:{def:{processors:{depth:"number"},defaults:{depth:0},triggers:{depth:"layout"}}},config:{fx:{customDurations:{depth:0}}},layoutUpdater:function(){var H=this,F=H.getAxis().getChart();if(F.isInitializing){return }var E=H.attr,D=H.getLayout(),C=D.isDiscrete?0:E.depth,G=F.getInherited().rtl,B=E.dataMin+(E.dataMax-E.dataMin)*E.visibleMin,I=E.dataMin+(E.dataMax-E.dataMin)*E.visibleMax,A={attr:E,segmenter:H.getSegmenter(),renderer:H.defaultRenderer};if(E.position==="left"||E.position==="right"){E.translationX=0;E.translationY=I*(E.length-C)/(I-B)+C;E.scalingX=1;E.scalingY=(-E.length+C)/(I-B);E.scalingCenterY=0;E.scalingCenterX=0;H.applyTransformations(true)}else{if(E.position==="top"||E.position==="bottom"){if(G){E.translationX=E.length+B*E.length/(I-B)+1}else{E.translationX=-B*E.length/(I-B)}E.translationY=0;E.scalingX=(G?-1:1)*(E.length-C)/(I-B);E.scalingY=1;E.scalingCenterY=0;E.scalingCenterX=0;H.applyTransformations(true)}}if(D){D.calculateLayout(A);H.setLayoutContext(A)}},renderAxisLine:function(A,J,F,C){var I=this,H=I.attr,B=H.lineWidth*0.5,F=I.getLayout(),D=F.isDiscrete?0:H.depth,K=H.position,E,G;if(H.axisLine&&H.length){switch(K){case"left":E=A.roundPixel(C[2])-B;J.moveTo(E,-H.endGap+D);J.lineTo(E,H.length+H.startGap);break;case"right":J.moveTo(B,-H.endGap);J.lineTo(B,H.length+H.startGap);break;case"bottom":J.moveTo(-H.startGap,B);J.lineTo(H.length-D+H.endGap,B);break;case"top":E=A.roundPixel(C[3])-B;J.moveTo(-H.startGap,E);J.lineTo(H.length+H.endGap,E);break;case"angular":J.moveTo(H.centerX+H.length,H.centerY);J.arc(H.centerX,H.centerY,H.length,0,Math.PI*2,true);break;case"gauge":G=I.getGaugeAngles();J.moveTo(H.centerX+Math.cos(G.start)*H.length,H.centerY+Math.sin(G.start)*H.length);J.arc(H.centerX,H.centerY,H.length,G.start,G.end,true);break}}}})