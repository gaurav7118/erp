Ext.define("Ext.fx.target.Sprite",{extend:"Ext.fx.target.Target",type:"draw",getFromPrim:function(B,A){var C;switch(A){case"rotate":case"rotation":C=B.attr.rotation;return{x:C.x||0,y:C.y||0,degrees:C.degrees||0};case"scale":case"scaling":C=B.attr.scaling;return{x:C.x||1,y:C.y||1,cx:C.cx||0,cy:C.cy||0};case"translate":case"translation":C=B.attr.translation;return{x:C.x||0,y:C.y||0};default:return B.attr[A]}},getAttr:function(A,B){return[[this.target,B!==undefined?B:this.getFromPrim(this.target,A)]]},setAttr:function(J){var F=J.length,H=[],B,E,M,O,N,L,K,D,C,I,G,A;for(D=0;D<F;D++){B=J[D].attrs;for(E in B){M=B[E];A=M.length;for(C=0;C<A;C++){N=M[C][0];O=M[C][1];if(E==="translate"||E==="translation"){K={x:O.x,y:O.y}}else{if(E==="rotate"||E==="rotation"){I=O.x;if(isNaN(I)){I=null}G=O.y;if(isNaN(G)){G=null}K={degrees:O.degrees,x:I,y:G}}else{if(E==="scale"||E==="scaling"){I=O.x;if(isNaN(I)){I=null}G=O.y;if(isNaN(G)){G=null}K={x:I,y:G,cx:O.cx,cy:O.cy}}else{if(E==="width"||E==="height"||E==="x"||E==="y"){K=parseFloat(O)}else{K=O}}}}L=Ext.Array.indexOf(H,N);if(L===-1){H.push([N,{}]);L=H.length-1}H[L][1][E]=K}}}F=H.length;for(D=0;D<F;D++){H[D][0].setAttributes(H[D][1])}this.target.redraw()}})