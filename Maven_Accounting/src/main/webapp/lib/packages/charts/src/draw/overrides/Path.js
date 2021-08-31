Ext.define("Ext.draw.overrides.Path",{override:"Ext.draw.Path",rayOrigin:{x:-10000,y:-10000},isPointInPath:function(M,L){var K=this,C=K.commands,O=Ext.draw.PathUtil,N=K.rayOrigin,F=K.params,J=C.length,E=null,D=null,B=0,A=0,I=0,H,G;for(H=0,G=0;H<J;H++){switch(C[H]){case"M":if(E!==null){if(O.linesIntersection(E,D,B,A,N.x,N.y,M,L)){I+=1}}E=B=F[G];D=A=F[G+1];G+=2;break;case"L":if(O.linesIntersection(B,A,F[G],F[G+1],N.x,N.y,M,L)){I+=1}B=F[G];A=F[G+1];G+=2;break;case"C":I+=O.cubicLineIntersections(B,F[G],F[G+2],F[G+4],A,F[G+1],F[G+3],F[G+5],N.x,N.y,M,L).length;B=F[G+4];A=F[G+5];G+=6;break;case"Z":if(E!==null){if(O.linesIntersection(E,D,B,A,N.x,N.y,M,L)){I+=1}}break}}return I%2===1},isPointOnPath:function(L,K){var J=this,C=J.commands,M=Ext.draw.PathUtil,F=J.params,I=C.length,E=null,D=null,B=0,A=0,H,G;for(H=0,G=0;H<I;H++){switch(C[H]){case"M":if(E!==null){if(M.pointOnLine(E,D,B,A,L,K)){return true}}E=B=F[G];D=A=F[G+1];G+=2;break;case"L":if(M.pointOnLine(B,A,F[G],F[G+1],L,K)){return true}B=F[G];A=F[G+1];G+=2;break;case"C":if(M.pointOnCubic(B,F[G],F[G+2],F[G+4],A,F[G+1],F[G+3],F[G+5],L,K)){return true}B=F[G+4];A=F[G+5];G+=6;break;case"Z":if(E!==null){if(M.pointOnLine(E,D,B,A,L,K)){return true}}break}}return false},getSegmentIntersections:function(R,D,Q,C,P,B,M,A){var U=this,G=arguments.length,T=Ext.draw.PathUtil,F=U.commands,S=U.params,I=F.length,K=null,J=null,H=0,E=0,V=[],O,L,N;for(O=0,L=0;O<I;O++){switch(F[O]){case"M":if(K!==null){switch(G){case 4:N=T.linesIntersection(K,J,H,E,R,D,Q,C);if(N){V.push(N)}break;case 8:N=T.cubicLineIntersections(R,Q,P,M,D,C,B,A,K,J,H,E);V.push.apply(V,N);break}}K=H=S[L];J=E=S[L+1];L+=2;break;case"L":switch(G){case 4:N=T.linesIntersection(H,E,S[L],S[L+1],R,D,Q,C);if(N){V.push(N)}break;case 8:N=T.cubicLineIntersections(R,Q,P,M,D,C,B,A,H,E,S[L],S[L+1]);V.push.apply(V,N);break}H=S[L];E=S[L+1];L+=2;break;case"C":switch(G){case 4:N=T.cubicLineIntersections(H,S[L],S[L+2],S[L+4],E,S[L+1],S[L+3],S[L+5],R,D,Q,C);V.push.apply(V,N);break;case 8:N=T.cubicsIntersections(H,S[L],S[L+2],S[L+4],E,S[L+1],S[L+3],S[L+5],R,Q,P,M,D,C,B,A);V.push.apply(V,N);break}H=S[L+4];E=S[L+5];L+=6;break;case"Z":if(K!==null){switch(G){case 4:N=T.linesIntersection(K,J,H,E,R,D,Q,C);if(N){V.push(N)}break;case 8:N=T.cubicLineIntersections(R,Q,P,M,D,C,B,A,K,J,H,E);V.push.apply(V,N);break}}break}}return V},getIntersections:function(M){var K=this,C=K.commands,G=K.params,J=C.length,F=null,E=null,B=0,A=0,D=[],I,H,L;for(I=0,H=0;I<J;I++){switch(C[I]){case"M":if(F!==null){L=M.getSegmentIntersections.call(M,F,E,B,A);D.push.apply(D,L)}F=B=G[H];E=A=G[H+1];H+=2;break;case"L":L=M.getSegmentIntersections.call(M,B,A,G[H],G[H+1]);D.push.apply(D,L);B=G[H];A=G[H+1];H+=2;break;case"C":L=M.getSegmentIntersections.call(M,B,A,G[H],G[H+1],G[H+2],G[H+3],G[H+4],G[H+5]);D.push.apply(D,L);B=G[H+4];A=G[H+5];H+=6;break;case"Z":if(F!==null){L=M.getSegmentIntersections.call(M,F,E,B,A);D.push.apply(D,L)}break}}return D}})