Ext.define("Ext.chart.series.Gauge",{alias:"series.gauge",extend:"Ext.chart.series.Polar",type:"gauge",seriesType:"pieslice",requires:["Ext.draw.sprite.Sector"],config:{needle:false,needleLength:90,needleWidth:4,donut:30,showInLegend:false,value:null,colors:null,sectors:null,minimum:0,maximum:100,rotation:0,totalAngle:Math.PI/2,rect:[0,0,1,1],center:[0.5,0.75],radius:0.5,wholeDisk:false},coordinateX:function(){return this.coordinate("X",0,2)},coordinateY:function(){return this.coordinate("Y",1,2)},updateNeedle:function(B){var A=this,D=A.getSprites(),C=A.valueToAngle(A.getValue());if(D&&D.length){D[0].setAttributes({startAngle:(B?C:0),endAngle:C,strokeOpacity:(B?1:0),lineWidth:(B?A.getNeedleWidth():0)});A.doUpdateStyles()}},themeColorCount:function(){var C=this,A=C.getStore(),B=A&&A.getCount()||0;return B+(C.getNeedle()?0:1)},updateColors:function(A,B){var F=this,H=F.getSectors(),I=H&&H.length,E=F.getSprites(),C=Ext.Array.clone(A),G=A&&A.length,D;if(!G||!A[0]){return }for(D=0;D<I;D++){C[D+1]=H[D].color||C[D+1]||A[D%G]}if(E.length){E[0].setAttributes({strokeStyle:C[0]})}this.setSubStyle({fillStyle:C,strokeStyle:C});this.doUpdateStyles()},updateRect:function(F){var D=this.getWholeDisk(),C=D?Math.PI:this.getTotalAngle()/2,G=this.getDonut()/100,E,B,A;if(C<=Math.PI/2){E=2*Math.sin(C);B=1-G*Math.cos(C)}else{E=2;B=1-Math.cos(C)}A=Math.min(F[2]/E,F[3]/B);this.setRadius(A);this.setCenter([F[2]/2,A+(F[3]-B*A)/2])},updateCenter:function(A){this.setStyle({centerX:A[0],centerY:A[1],rotationCenterX:A[0],rotationCenterY:A[1]});this.doUpdateStyles()},updateRotation:function(A){this.setStyle({rotationRads:A-(this.getTotalAngle()+Math.PI)/2});this.doUpdateStyles()},doUpdateShape:function(B,F){var A,D=this.getSectors(),C=(D&&D.length)||0,E=this.getNeedleLength()/100;A=[B*E,B];while(C--){A.push(B)}this.setSubStyle({endRho:A,startRho:B/100*F});this.doUpdateStyles()},updateRadius:function(A){var B=this.getDonut();this.doUpdateShape(A,B)},updateDonut:function(B){var A=this.getRadius();this.doUpdateShape(A,B)},valueToAngle:function(A){A=this.applyValue(A);return this.getTotalAngle()*(A-this.getMinimum())/(this.getMaximum()-this.getMinimum())},applyValue:function(A){return Math.min(this.getMaximum(),Math.max(A,this.getMinimum()))},updateValue:function(B){var A=this,C=A.getNeedle(),E=A.valueToAngle(B),D=A.getSprites();D[0].rendererData.value=B;D[0].setAttributes({startAngle:(C?E:0),endAngle:E});A.doUpdateStyles()},processData:function(){var F=this,J=F.getStore(),A,D,H,B,G,E=J&&J.first(),C,I;if(E){C=F.getXField();if(C){I=E.get(C)}}if(A=F.getXAxis()){D=A.getMinimum();H=A.getMaximum();B=A.getSprites()[0].fx;G=B.getDuration();B.setDuration(0);if(Ext.isNumber(D)){F.setMinimum(D)}else{A.setMinimum(F.getMinimum())}if(Ext.isNumber(H)){F.setMaximum(H)}else{A.setMaximum(F.getMaximum())}B.setDuration(G)}if(!Ext.isNumber(I)){I=F.getMinimum()}F.setValue(I)},getDefaultSpriteConfig:function(){return{type:this.seriesType,renderer:this.getRenderer(),fx:{customDurations:{translationX:0,translationY:0,rotationCenterX:0,rotationCenterY:0,centerX:0,centerY:0,startRho:0,endRho:0,baseRotation:0}}}},normalizeSectors:function(F){var D=this,C=(F&&F.length)||0,B,E,G,A;if(C){for(B=0;B<C;B++){E=F[B];if(typeof E==="number"){F[B]={start:(B>0?F[B-1].end:D.getMinimum()),end:Math.min(E,D.getMaximum())};if(B==(C-1)&&F[B].end<D.getMaximum()){F[B+1]={start:F[B].end,end:D.getMaximum()}}}else{if(typeof E.start==="number"){G=Math.max(E.start,D.getMinimum())}else{G=(B>0?F[B-1].end:D.getMinimum())}if(typeof E.end==="number"){A=Math.min(E.end,D.getMaximum())}else{A=D.getMaximum()}F[B].start=G;F[B].end=A}}}else{F=[{start:D.getMinimum(),end:D.getMaximum()}]}return F},getSprites:function(){var I=this,L=I.getStore(),K=I.getValue(),C,G;if(!L&&!Ext.isNumber(K)){return[]}var H=I.getChart(),B=I.getAnimation()||H&&H.getAnimation(),F=I.sprites,J=0,N,M,E,D,A=[];if(F&&F.length){F[0].setAnimation(B);return F}D={store:L,field:I.getXField(),angleField:I.getXField(),value:K,series:I};N=I.createSprite();N.setAttributes({zIndex:10},true);N.rendererData=D;N.rendererIndex=J++;A.push(I.getNeedleWidth());I.getLabel().getTemplate().setField(true);M=I.normalizeSectors(I.getSectors());for(C=0,G=M.length;C<G;C++){E={startAngle:I.valueToAngle(M[C].start),endAngle:I.valueToAngle(M[C].end),label:M[C].label,fillStyle:M[C].color,strokeOpacity:0,doCallout:false,labelOverflowPadding:-1};Ext.apply(E,M[C].style);N=I.createSprite();N.rendererData=D;N.rendererIndex=J++;N.setAttributes(E,true);A.push(E.lineWidth)}I.setSubStyle({lineWidth:A});I.doUpdateStyles();return F}})