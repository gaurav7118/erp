Ext.define("Ext.chart.interactions.RotatePie3D",{extend:"Ext.chart.interactions.Rotate",type:"rotatePie3d",alias:"interaction.rotatePie3d",getAngle:function(F){var A=this.getChart(),E=A.getInherited().rtl,D=E?-1:1,G=F.getXY(),C=A.element.getXY(),B=A.getMainRect();return D*Math.atan2(G[1]-C[1]-B[3]*0.5,G[0]-C[0]-B[2]*0.5)},getRadius:function(H){var E=this.getChart(),A=E.getRadius(),D=E.getSeries(),G=D.length,C=0,B,F;for(;C<G;C++){B=D[C];if(B.isPie3D){F=B.getRadius();if(F>A){A=F}}}return A}})