Ext.define("Ext.chart.PolarChart",{extend:"Ext.chart.AbstractChart",requires:["Ext.chart.grid.CircularGrid","Ext.chart.grid.RadialGrid"],xtype:"polar",isPolar:true,config:{center:[0,0],radius:0,innerPadding:0},getDirectionForAxis:function(A){return A==="radial"?"Y":"X"},applyCenter:function(A,B){if(B&&A[0]===B[0]&&A[1]===B[1]){return }return[+A[0],+A[1]]},updateCenter:function(A){var G=this,H=G.getAxes(),D=G.getSeries(),C,F,E,B;for(C=0,F=H.length;C<F;C++){E=H[C];E.setCenter(A)}for(C=0,F=D.length;C<F;C++){B=D[C];B.setCenter(A)}},applyInnerPadding:function(B,A){return Ext.isNumber(B)?B:A},doSetSurfaceRect:function(B,C){var A=this.getMainRect();B.setRect(C);B.matrix.set(1,0,0,1,A[0]-C[0],A[1]-C[1]);B.inverseMatrix.set(1,0,0,1,C[0]-A[0],C[1]-A[1])},applyAxes:function(F,H){var E=this,G=Ext.Array.from(E.config.series)[0],B,D,C,A;if(G.type==="radar"&&F&&F.length){for(B=0,D=F.length;B<D;B++){C=F[B];if(C.position==="angular"){A=true;break}}if(!A){F.push({type:"category",position:"angular",fields:G.xField||G.angleField,style:{estStepSize:1},grid:true})}}return this.callParent(arguments)},performLayout:function(){var d=this,F=true;try{d.animationSuspendCount++;if(this.callParent()===false){F=false;return }d.suspendThicknessChanged();var G=d.getSurface("chart").getRect(),T=d.getInsetPadding(),f=d.getInnerPadding(),J=Ext.apply({},T),D,Q=G[2]-T.left-T.right,P=G[3]-T.top-T.bottom,V=[T.left,T.top,Q,P],S=d.getSeries(),N,R=Q-f*2,U=P-f*2,b=[R*0.5+f,U*0.5+f],H=Math.min(R,U)*0.5,Y=d.getAxes(),E,A,I,K=[],M=[],c=H-f,X,L,B,O,W,C,a;d.setMainRect(V);d.doSetSurfaceRect(d.getSurface(),V);for(X=0,L=d.surfaceMap.grid&&d.surfaceMap.grid.length;X<L;X++){d.doSetSurfaceRect(d.surfaceMap.grid[X],G)}for(X=0,L=Y.length;X<L;X++){E=Y[X];switch(E.getPosition()){case"angular":K.push(E);break;case"radial":M.push(E);break}}for(X=0,L=K.length;X<L;X++){E=K[X];O=E.getFloating();W=O?O.value:null;d.doSetSurfaceRect(E.getSurface(),G);A=E.getThickness();for(D in J){J[D]+=A}Q=G[2]-J.left-J.right;P=G[3]-J.top-J.bottom;B=Math.min(Q,P)*0.5;if(X===0){c=B-f}E.setMinimum(0);E.setLength(B);E.getSprites();I=E.sprites[0].attr.lineWidth*0.5;for(D in J){J[D]+=I}}for(X=0,L=M.length;X<L;X++){E=M[X];d.doSetSurfaceRect(E.getSurface(),G);E.setMinimum(0);E.setLength(c);E.getSprites()}for(X=0,L=S.length;X<L;X++){N=S[X];if(N.type==="gauge"&&!C){C=N}else{N.setRadius(c)}d.doSetSurfaceRect(N.getSurface(),V)}d.doSetSurfaceRect(d.getSurface("overlay"),G);if(C){C.setRect(V);a=C.getRadius()-f;d.setRadius(a);d.setCenter(C.getCenter());C.setRadius(a);if(Y.length&&Y[0].getPosition()==="gauge"){E=Y[0];d.doSetSurfaceRect(E.getSurface(),G);E.setTotalAngle(C.getTotalAngle());E.setLength(a)}}else{d.setRadius(H);d.setCenter(b)}d.redraw()}catch(Z){Ext.log.error(d.$className+": Unhandled Exception: ",Z.description||Z.message);throw Z}finally{d.animationSuspendCount--;if(F){d.resumeThicknessChanged()}}},refloatAxes:function(){var I=this,G=I.getAxes(),H=I.getMainRect(),F,J,B,D,A,C,E;if(!H){return }E=0.5*Math.min(H[2],H[3]);for(D=0,A=G.length;D<A;D++){C=G[D];F=C.getFloating();J=F?F.value:null;if(J!==null){B=I.getAxis(F.alongAxis);if(C.getPosition()==="angular"){if(B){J=B.getLength()*J/B.getRange()[1]}else{J=0.01*J*E}C.sprites[0].setAttributes({length:J},true)}else{if(B){if(Ext.isString(J)){J=B.getCoordFor(J)}J=J/(B.getRange()[1]+1)*Math.PI*2-Math.PI*1.5+C.getRotation()}else{J=Ext.draw.Draw.rad(J)}C.sprites[0].setAttributes({baseRotation:J},true)}}}},redraw:function(){var F=this,G=F.getAxes(),D,C=F.getSeries(),B,A,E;for(A=0,E=G.length;A<E;A++){D=G[A];D.getSprites()}for(A=0,E=C.length;A<E;A++){B=C[A];B.getSprites()}F.renderFrame();F.callParent(arguments)},renderFrame:function(){this.refloatAxes();this.callParent()}})