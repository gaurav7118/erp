Ext.define("Ext.draw.Path",{requires:["Ext.draw.Draw"],statics:{pathRe:/,?([achlmqrstvxz]),?/gi,pathRe2:/-/gi,pathSplitRe:/\s|,/g},svgString:"",constructor:function(A){var B=this;B.commands=[];B.params=[];B.cursor=null;B.startX=0;B.startY=0;if(A){B.fromSvgString(A)}},clear:function(){var A=this;A.params.length=0;A.commands.length=0;A.cursor=null;A.startX=0;A.startY=0;A.dirt()},dirt:function(){this.svgString=""},moveTo:function(A,C){var B=this;if(!B.cursor){B.cursor=[A,C]}B.params.push(A,C);B.commands.push("M");B.startX=A;B.startY=C;B.cursor[0]=A;B.cursor[1]=C;B.dirt()},lineTo:function(A,C){var B=this;if(!B.cursor){B.cursor=[A,C];B.params.push(A,C);B.commands.push("M")}else{B.params.push(A,C);B.commands.push("L")}B.cursor[0]=A;B.cursor[1]=C;B.dirt()},bezierCurveTo:function(C,E,B,D,A,G){var F=this;if(!F.cursor){F.moveTo(C,E)}F.params.push(C,E,B,D,A,G);F.commands.push("C");F.cursor[0]=A;F.cursor[1]=G;F.dirt()},quadraticCurveTo:function(B,E,A,D){var C=this;if(!C.cursor){C.moveTo(B,E)}C.bezierCurveTo((2*B+C.cursor[0])/3,(2*E+C.cursor[1])/3,(2*B+A)/3,(2*E+D)/3,A,D)},closePath:function(){var A=this;if(A.cursor){A.cursor=null;A.commands.push("Z");A.dirt()}},arcTo:function(a,F,Z,D,J,I,V){var e=this;if(I===undefined){I=J}if(V===undefined){V=0}if(!e.cursor){e.moveTo(a,F);return }if(J===0||I===0){e.lineTo(a,F);return }Z-=a;D-=F;var b=e.cursor[0]-a,G=e.cursor[1]-F,c=Z*G-D*b,B,A,L,R,K,Q,X=Math.sqrt(b*b+G*G),U=Math.sqrt(Z*Z+D*D),T,E,C;if(c===0){e.lineTo(a,F);return }if(I!==J){B=Math.cos(V);A=Math.sin(V);L=B/J;R=A/I;K=-A/J;Q=B/I;var d=L*b+R*G;G=K*b+Q*G;b=d;d=L*Z+R*D;D=K*Z+Q*D;Z=d}else{b/=J;G/=I;Z/=J;D/=I}E=b*U+Z*X;C=G*U+D*X;T=1/(Math.sin(Math.asin(Math.abs(c)/(X*U))*0.5)*Math.sqrt(E*E+C*C));E*=T;C*=T;var O=(E*b+C*G)/(b*b+G*G),M=(E*Z+C*D)/(Z*Z+D*D);var N=b*O-E,P=G*O-C,H=Z*M-E,Y=D*M-C,W=Math.atan2(P,N),S=Math.atan2(Y,H);if(c>0){if(S<W){S+=Math.PI*2}}else{if(W<S){W+=Math.PI*2}}if(I!==J){E=B*E*J-A*C*I+a;C=A*C*I+B*C*I+F;e.lineTo(B*J*N-A*I*P+E,A*J*N+B*I*P+C);e.ellipse(E,C,J,I,V,W,S,c<0)}else{E=E*J+a;C=C*I+F;e.lineTo(J*N+E,I*P+C);e.ellipse(E,C,J,I,V,W,S,c<0)}},ellipse:function(H,F,C,A,O,L,D,E){var M=this,G=M.params,B=G.length,K,J,I;if(D-L>=Math.PI*2){M.ellipse(H,F,C,A,O,L,L+Math.PI,E);M.ellipse(H,F,C,A,O,L+Math.PI,D,E);return }if(!E){if(D<L){D+=Math.PI*2}K=M.approximateArc(G,H,F,C,A,O,L,D)}else{if(L<D){L+=Math.PI*2}K=M.approximateArc(G,H,F,C,A,O,D,L);for(J=B,I=G.length-2;J<I;J+=2,I-=2){var N=G[J];G[J]=G[I];G[I]=N;N=G[J+1];G[J+1]=G[I+1];G[I+1]=N}}if(!M.cursor){M.cursor=[G[G.length-2],G[G.length-1]];M.commands.push("M")}else{M.cursor[0]=G[G.length-2];M.cursor[1]=G[G.length-1];M.commands.push("L")}for(J=2;J<K;J+=6){M.commands.push("C")}M.dirt()},arc:function(B,F,A,D,C,E){this.ellipse(B,F,A,A,0,D,C,E)},rect:function(B,E,C,A){if(C==0||A==0){return }var D=this;D.moveTo(B,E);D.lineTo(B+C,E);D.lineTo(B+C,E+A);D.lineTo(B,E+A);D.closePath()},approximateArc:function(S,I,F,O,N,D,X,V){var E=Math.cos(D),Z=Math.sin(D),K=Math.cos(X),L=Math.sin(X),Q=E*K*O-Z*L*N,Y=-E*L*O-Z*K*N,P=Z*K*O+E*L*N,W=-Z*L*O+E*K*N,M=Math.PI/2,R=2,J=Q,U=Y,H=P,T=W,B=0.547443256150549,c,G,a,A,b,C;V-=X;if(V<0){V+=Math.PI*2}S.push(Q+I,P+F);while(V>=M){S.push(J+U*B+I,H+T*B+F,J*B+U+I,H*B+T+F,U+I,T+F);R+=6;V-=M;c=J;J=U;U=-c;c=H;H=T;T=-c}if(V){G=(0.3294738052815987+0.012120855841304373*V)*V;a=Math.cos(V);A=Math.sin(V);b=a+G*A;C=A-G*a;S.push(J+U*G+I,H+T*G+F,J*b+U*C+I,H*b+T*C+F,J*a+U*A+I,H*a+T*A+F);R+=6}return R},arcSvg:function(J,H,R,M,W,T,C){if(J<0){J=-J}if(H<0){H=-H}var X=this,U=X.cursor[0],F=X.cursor[1],A=(U-T)/2,Y=(F-C)/2,D=Math.cos(R),S=Math.sin(R),O=A*D+Y*S,V=-A*S+Y*D,I=O/J,G=V/H,P=I*I+G*G,E=(U+T)*0.5,B=(F+C)*0.5,L=0,K=0;if(P>=1){P=Math.sqrt(P);J*=P;H*=P}else{P=Math.sqrt(1/P-1);if(M===W){P=-P}L=P*J*G;K=-P*H*I;E+=D*L-S*K;B+=S*L+D*K}var Q=Math.atan2((V-K)/H,(O-L)/J),N=Math.atan2((-V-K)/H,(-O-L)/J)-Q;if(W){if(N<=0){N+=Math.PI*2}}else{if(N>=0){N-=Math.PI*2}}X.ellipse(E,B,J,H,R,Q,Q+N,1-W)},fromSvgString:function(E){if(!E){return }var L=this,H,K={a:7,c:6,h:1,l:2,m:2,q:4,s:4,t:2,v:1,z:0,A:7,C:6,H:1,L:2,M:2,Q:4,S:4,T:2,V:1,Z:0},J="",G,F,C=0,B=0,D=false,I,M,A;if(Ext.isString(E)){H=E.replace(Ext.draw.Path.pathRe," $1 ").replace(Ext.draw.Path.pathRe2," -").split(Ext.draw.Path.pathSplitRe)}else{if(Ext.isArray(E)){H=E.join(",").split(Ext.draw.Path.pathSplitRe)}}for(I=0,M=0;I<H.length;I++){if(H[I]!==""){H[M++]=H[I]}}H.length=M;L.clear();for(I=0;I<H.length;){J=D;D=H[I];A=(D.toUpperCase()!==D);I++;switch(D){case"M":L.moveTo(C=+H[I],B=+H[I+1]);I+=2;while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C=+H[I],B=+H[I+1]);I+=2}break;case"L":L.lineTo(C=+H[I],B=+H[I+1]);I+=2;while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C=+H[I],B=+H[I+1]);I+=2}break;case"A":while(I<M&&!K.hasOwnProperty(H[I])){L.arcSvg(+H[I],+H[I+1],+H[I+2]*Math.PI/180,+H[I+3],+H[I+4],C=+H[I+5],B=+H[I+6]);I+=7}break;case"C":while(I<M&&!K.hasOwnProperty(H[I])){L.bezierCurveTo(+H[I],+H[I+1],G=+H[I+2],F=+H[I+3],C=+H[I+4],B=+H[I+5]);I+=6}break;case"Z":L.closePath();break;case"m":L.moveTo(C+=+H[I],B+=+H[I+1]);I+=2;while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C+=+H[I],B+=+H[I+1]);I+=2}break;case"l":L.lineTo(C+=+H[I],B+=+H[I+1]);I+=2;while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C+=+H[I],B+=+H[I+1]);I+=2}break;case"a":while(I<M&&!K.hasOwnProperty(H[I])){L.arcSvg(+H[I],+H[I+1],+H[I+2]*Math.PI/180,+H[I+3],+H[I+4],C+=+H[I+5],B+=+H[I+6]);I+=7}break;case"c":while(I<M&&!K.hasOwnProperty(H[I])){L.bezierCurveTo(C+(+H[I]),B+(+H[I+1]),G=C+(+H[I+2]),F=B+(+H[I+3]),C+=+H[I+4],B+=+H[I+5]);I+=6}break;case"z":L.closePath();break;case"s":if(!(J==="c"||J==="C"||J==="s"||J==="S")){G=C;F=B}while(I<M&&!K.hasOwnProperty(H[I])){L.bezierCurveTo(C+C-G,B+B-F,G=C+(+H[I]),F=B+(+H[I+1]),C+=+H[I+2],B+=+H[I+3]);I+=4}break;case"S":if(!(J==="c"||J==="C"||J==="s"||J==="S")){G=C;F=B}while(I<M&&!K.hasOwnProperty(H[I])){L.bezierCurveTo(C+C-G,B+B-F,G=+H[I],F=+H[I+1],C=(+H[I+2]),B=(+H[I+3]));I+=4}break;case"q":while(I<M&&!K.hasOwnProperty(H[I])){L.quadraticCurveTo(G=C+(+H[I]),F=B+(+H[I+1]),C+=+H[I+2],B+=+H[I+3]);I+=4}break;case"Q":while(I<M&&!K.hasOwnProperty(H[I])){L.quadraticCurveTo(G=+H[I],F=+H[I+1],C=+H[I+2],B=+H[I+3]);I+=4}break;case"t":if(!(J==="q"||J==="Q"||J==="t"||J==="T")){G=C;F=B}while(I<M&&!K.hasOwnProperty(H[I])){L.quadraticCurveTo(G=C+C-G,F=B+B-F,C+=+H[I+1],B+=+H[I+2]);I+=2}break;case"T":if(!(J==="q"||J==="Q"||J==="t"||J==="T")){G=C;F=B}while(I<M&&!K.hasOwnProperty(H[I])){L.quadraticCurveTo(G=C+C-G,F=B+B-F,C=(+H[I+1]),B=(+H[I+2]));I+=2}break;case"h":while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C+=+H[I],B);I++}break;case"H":while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C=+H[I],B);I++}break;case"v":while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C,B+=+H[I]);I++}break;case"V":while(I<M&&!K.hasOwnProperty(H[I])){L.lineTo(C,B=+H[I]);I++}break}}},clone:function(){var A=this,B=new Ext.draw.Path();B.params=A.params.slice(0);B.commands=A.commands.slice(0);B.cursor=A.cursor?A.cursor.slice(0):null;B.startX=A.startX;B.startY=A.startY;B.svgString=A.svgString;return B},transform:function(I){if(I.isIdentity()){return }var A=I.getXX(),F=I.getYX(),L=I.getDX(),K=I.getXY(),E=I.getYY(),J=I.getDY(),B=this.params,C=0,D=B.length,H,G;for(;C<D;C+=2){H=B[C];G=B[C+1];B[C]=H*A+G*F+L;B[C+1]=H*K+G*E+J}this.dirt()},getDimension:function(F){if(!F){F={}}if(!this.commands||!this.commands.length){F.x=0;F.y=0;F.width=0;F.height=0;return F}F.left=Infinity;F.top=Infinity;F.right=-Infinity;F.bottom=-Infinity;var D=0,C=0,B=this.commands,G=this.params,E=B.length,A,H;for(;D<E;D++){switch(B[D]){case"M":case"L":A=G[C];H=G[C+1];F.left=Math.min(A,F.left);F.top=Math.min(H,F.top);F.right=Math.max(A,F.right);F.bottom=Math.max(H,F.bottom);C+=2;break;case"C":this.expandDimension(F,A,H,G[C],G[C+1],G[C+2],G[C+3],A=G[C+4],H=G[C+5]);C+=6;break}}F.x=F.left;F.y=F.top;F.width=F.right-F.left;F.height=F.bottom-F.top;return F},getDimensionWithTransform:function(L,F){if(!this.commands||!this.commands.length){if(!F){F={}}F.x=0;F.y=0;F.width=0;F.height=0;return F}F.left=Infinity;F.top=Infinity;F.right=-Infinity;F.bottom=-Infinity;var A=L.getXX(),I=L.getYX(),O=L.getDX(),N=L.getXY(),H=L.getYY(),M=L.getDY(),E=0,D=0,B=this.commands,C=this.params,G=B.length,K,J;for(;E<G;E++){switch(B[E]){case"M":case"L":K=C[D]*A+C[D+1]*I+O;J=C[D]*N+C[D+1]*H+M;F.left=Math.min(K,F.left);F.top=Math.min(J,F.top);F.right=Math.max(K,F.right);F.bottom=Math.max(J,F.bottom);D+=2;break;case"C":this.expandDimension(F,K,J,C[D]*A+C[D+1]*I+O,C[D]*N+C[D+1]*H+M,C[D+2]*A+C[D+3]*I+O,C[D+2]*N+C[D+3]*H+M,K=C[D+4]*A+C[D+5]*I+O,J=C[D+4]*N+C[D+5]*H+M);D+=6;break}}if(!F){F={}}F.x=F.left;F.y=F.top;F.width=F.right-F.left;F.height=F.bottom-F.top;return F},expandDimension:function(H,C,N,J,F,I,D,B,M){var K=this,E=H.left,A=H.right,O=H.top,L=H.bottom,G=K.dim||(K.dim=[]);K.curveDimension(C,J,I,B,G);E=Math.min(E,G[0]);A=Math.max(A,G[1]);K.curveDimension(N,F,D,M,G);O=Math.min(O,G[0]);L=Math.max(L,G[1]);H.left=E;H.right=A;H.top=O;H.bottom=L},curveDimension:function(L,J,G,F,D){var E=3*(-L+3*(J-G)+F),C=6*(L-2*J+G),B=-3*(L-J),K,I,A=Math.min(L,F),H=Math.max(L,F),M;if(E===0){if(C===0){D[0]=A;D[1]=H;return }else{K=-B/C;if(0<K&&K<1){I=this.interpolate(L,J,G,F,K);A=Math.min(A,I);H=Math.max(H,I)}}}else{M=C*C-4*E*B;if(M>=0){M=Math.sqrt(M);K=(M-C)/2/E;if(0<K&&K<1){I=this.interpolate(L,J,G,F,K);A=Math.min(A,I);H=Math.max(H,I)}if(M>0){K-=M/E;if(0<K&&K<1){I=this.interpolate(L,J,G,F,K);A=Math.min(A,I);H=Math.max(H,I)}}}}D[0]=A;D[1]=H},interpolate:function(B,A,F,E,C){if(C===0){return B}if(C===1){return E}var D=(1-C)/C;return C*C*C*(E+D*(3*F+D*(3*A+D*B)))},fromStripes:function(G){var E=this,C=0,D=G.length,B,A,F;E.clear();for(;C<D;C++){F=G[C];E.params.push.apply(E.params,F);E.commands.push("M");for(B=2,A=F.length;B<A;B+=6){E.commands.push("C")}}if(!E.cursor){E.cursor=[]}E.cursor[0]=E.params[E.params.length-2];E.cursor[1]=E.params[E.params.length-1];E.dirt()},toStripes:function(I){var M=I||[],N,L,K,B,A,H,G,F,E,C=this.commands,D=this.params,J=C.length;for(F=0,E=0;F<J;F++){switch(C[F]){case"M":N=[H=B=D[E++],G=A=D[E++]];M.push(N);break;case"L":L=D[E++];K=D[E++];N.push((B+B+L)/3,(A+A+K)/3,(B+L+L)/3,(A+K+K)/3,B=L,A=K);break;case"C":N.push(D[E++],D[E++],D[E++],D[E++],B=D[E++],A=D[E++]);break;case"Z":L=H;K=G;N.push((B+B+L)/3,(A+A+K)/3,(B+L+L)/3,(A+K+K)/3,B=L,A=K);break}}return M},updateSvgString:function(){var B=[],A=this.commands,F=this.params,E=A.length,D=0,C=0;for(;D<E;D++){switch(A[D]){case"M":B.push("M"+F[C]+","+F[C+1]);C+=2;break;case"L":B.push("L"+F[C]+","+F[C+1]);C+=2;break;case"C":B.push("C"+F[C]+","+F[C+1]+" "+F[C+2]+","+F[C+3]+" "+F[C+4]+","+F[C+5]);C+=6;break;case"Z":B.push("Z");break}}this.svgString=B.join("")},toString:function(){if(!this.svgString){this.updateSvgString()}return this.svgString}})