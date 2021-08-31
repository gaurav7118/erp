Ext.define("Ext.resizer.BorderSplitterTracker",{extend:"Ext.resizer.SplitterTracker",requires:["Ext.util.Region"],getPrevCmp:null,getNextCmp:null,calculateConstrainRegion:function(){var X=this,A=X.splitter,M=A.collapseTarget,D=A.defaultSplitMin,G=A.vertical?"Width":"Height",C="min"+G,T="max"+G,H="get"+G,S=A.neighbors,E=S.length,L=M.el.getBox(),F=L.x,N=L.y,W=L.right,I=L.bottom,P=A.vertical?(W-F):(I-N),V,J,Q,K,U,R,O,B;K=(M[C]||Math.min(P,D))-P;U=M[T];if(!U){U=1000000000}else{U-=P}B=P;for(V=0;V<E;++V){J=S[V];P=J[H]();Q=J[T];if(Q===null){Q=undefined}R=P-Q;O=P-(J[C]||Math.min(P,D));if(!isNaN(R)){if(K<R){K=R}}if(U>O){U=O}}if(U-K<2){return null}L=new Ext.util.Region(N,W,I,F);X.constraintAdjusters[X.getCollapseDirection()](L,K,U,A);X.dragInfo={minRange:K,maxRange:U,targetSize:B};return L},constraintAdjusters:{left:function(C,A,B,D){C[0]=C.x=C.left=C.right+A;C.right+=B+D.getWidth()},top:function(C,A,B,D){C[1]=C.y=C.top=C.bottom+A;C.bottom+=B+D.getHeight()},bottom:function(C,A,B,D){C.bottom=C.top-A;C.top-=B+D.getHeight()},right:function(C,A,B,D){C.right=C.left-A;C[0]=C.x=C.left=C.x-B+D.getWidth()}},onBeforeStart:function(H){var E=this,F=E.splitter,D=F.collapseTarget,B=F.neighbors,C=B.length,A,G;if(D.collapsed){return false}for(A=0;A<C;++A){G=B[A];if(G.collapsed&&G.isHorz===D.isHorz){return false}}if(!(E.constrainTo=E.calculateConstrainRegion())){return false}return true},performResize:function(H,G){var I=this,B=I.splitter,F=B.getCollapseDirection(),A=B.collapseTarget,E=I.splitAdjusters[B.vertical?"horz":"vert"],J=G[E.index],D=I.dragInfo,C;if(F==="right"||F==="bottom"){J=-J}J=Math.min(Math.max(D.minRange,J),D.maxRange);if(J){(C=B.ownerCt).suspendLayouts();E.adjustTarget(A,D.targetSize,J);C.resumeLayouts(true)}},splitAdjusters:{horz:{index:0,adjustTarget:function(B,A,C){B.flex=null;B.setSize(A+C)}},vert:{index:1,adjustTarget:function(B,A,C){B.flex=null;B.setSize(undefined,A+C)}}},getCollapseDirection:function(){return this.splitter.getCollapseDirection()}})