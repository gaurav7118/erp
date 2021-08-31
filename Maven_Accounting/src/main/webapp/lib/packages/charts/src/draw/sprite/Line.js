Ext.define("Ext.draw.sprite.Line",{extend:"Ext.draw.sprite.Sprite",alias:"sprite.line",type:"line",inheritableStatics:{def:{processors:{fromX:"number",fromY:"number",toX:"number",toY:"number"},defaults:{fromX:0,fromY:0,toX:1,toY:1,strokeStyle:"black"},aliases:{x1:"fromX",y1:"fromY",x2:"toX",y2:"toY"}}},updateLineBBox:function(B,I,R,G,Q,F){var O=this.attr,P=O.matrix,H=O.lineWidth/2,M,L,D,C,K,J,N;if(I){N=P.transformPoint([R,G]);R=N[0];G=N[1];N=P.transformPoint([Q,F]);Q=N[0];F=N[1]}M=Math.min(R,Q);D=Math.max(R,Q);L=Math.min(G,F);C=Math.max(G,F);var S=Math.atan2(D-M,C-L),A=Math.sin(S),E=Math.cos(S),K=H*E,J=H*A;M-=K;L-=J;D+=K;C+=J;B.x=M;B.y=L;B.width=D-M;B.height=C-L},updatePlainBBox:function(B){var A=this.attr;this.updateLineBBox(B,false,A.fromX,A.fromY,A.toX,A.toY)},updateTransformedBBox:function(B,C){var A=this.attr;this.updateLineBBox(B,true,A.fromX,A.fromY,A.toX,A.toY)},render:function(B,C){var A=this.attr,E=this.attr.matrix;E.toContext(C);C.beginPath();C.moveTo(A.fromX,A.fromY);C.lineTo(A.toX,A.toY);C.stroke();var D=A.debug||this.statics().debug||Ext.draw.sprite.Sprite.debug;if(D){this.attr.inverseMatrix.toContext(C);D.bbox&&this.renderBBox(B,C)}}})