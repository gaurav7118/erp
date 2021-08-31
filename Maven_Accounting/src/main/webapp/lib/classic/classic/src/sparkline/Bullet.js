Ext.define("Ext.sparkline.Bullet",{extend:"Ext.sparkline.Base",alias:"widget.sparklinebullet",config:{targetColor:"#f33",targetWidth:3,performanceColor:"#33f",rangeColors:["#d3dafe","#a8b6ff","#7f94ff"],base:null,tipTpl:new Ext.XTemplate("{fieldkey:this.fields} - {value}",{fields:function(A){if(A==="r"){return"Range"}if(A==="p"){return"Performance"}if(A==="t"){return"Target"}}})},applyValues:function(A){A=Ext.Array.map(Ext.Array.from(A),this.normalizeValue);this.disabled=!(A&&A.length);return A},onUpdate:function(){var D=this,B=D.values,C,A,F,E=D.getBase();D.callParent(arguments);F=B.slice();F[0]=F[0]===null?F[2]:F[0];F[1]=B[1]===null?F[2]:F[1];C=Math.min.apply(Math,B);A=Math.max.apply(Math,B);if(E==null){C=C<0?C:0}else{C=E}D.min=C;D.max=A;D.range=A-C;D.shapes={};D.valueShapes={};D.regiondata={};if(!B.length){D.disabled=true}},getRegion:function(A,C){var B=this.canvas.getShapeAt(A,C);return(B!==undefined&&this.shapes[B]!==undefined)?this.shapes[B]:undefined},getRegionFields:function(A){return{fieldkey:A.substr(0,1),value:this.values[A.substr(1)],region:A}},renderHighlight:function(A){switch(A.substr(0,1)){case"r":this.renderRange(A.substr(1),true).append();break;case"p":this.renderPerformance(true).append();break;case"t":this.renderTarget(true).append();break}},renderRange:function(E,B){var D=this.values[E],C=Math.round(this.getWidth()*((D-this.min)/this.range)),A=this.getRangeColors()[E-2];if(B){A=this.calcHighlightColor(A)}return this.canvas.drawRect(0,0,C-1,this.getHeight()-1,A,A)},renderPerformance:function(B){var D=this.values[1],C=Math.round(this.getWidth()*((D-this.min)/this.range)),A=this.getPerformanceColor();if(B){A=this.calcHighlightColor(A)}return this.canvas.drawRect(0,Math.round(this.getHeight()*0.3),C-1,Math.round(this.getHeight()*0.4)-1,A,A)},renderTarget:function(C){var G=this.values[0],E=this.getTargetWidth(),A=Math.round(this.getWidth()*((G-this.min)/this.range)-(E/2)),F=Math.round(this.getHeight()*0.1),D=this.getHeight()-(F*2),B=this.getTargetColor();if(C){B=this.calcHighlightColor(B)}return this.canvas.drawRect(A,F,E-1,D-1,B,B)},renderGraph:function(){var F=this,G=F.values.length,D=F.canvas,E,C,B=F.shapes||(F.shapes={}),A=F.valueShapes||(F.valueShapes={});if(!F.callParent()){return }for(E=2;E<G;E++){C=F.renderRange(E).append();B[C.id]="r"+E;A["r"+E]=C.id}if(F.values[1]!==null){C=F.renderPerformance().append();B[C.id]="p1";A.p1=C.id}if(F.values[0]!==null){C=this.renderTarget().append();B[C.id]="t0";A.t0=C.id}if(F.currentPageXY&&F.el.getRegion().contains(F.currentPageXY)){F.updateDisplay()}D.render()}})