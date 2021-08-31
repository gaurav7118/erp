Ext.define("Ext.draw.PathUtil",function(){var A=Math.abs,C=Math.pow,E=Math.cos,B=Math.acos,D=Math.sqrt,F=Math.PI;return{singleton:true,requires:["Ext.draw.overrides.Path","Ext.draw.overrides.sprite.Path","Ext.draw.overrides.sprite.Instancing","Ext.draw.overrides.Surface"],cubicRoots:function(L){var g=L[0],e=L[1],Z=L[2],Y=L[3];if(g===0){return this.quadraticRoots(e,Z,Y)}var W=e/g,V=Z/g,U=Y/g,J=(3*V-C(W,2))/9,I=(9*W*V-27*U-2*C(W,3))/54,O=C(J,3)+C(I,2),M=[],H,G,N,K,X,f=Ext.Number.sign;if(O>=0){H=f(I+D(O))*C(A(I+D(O)),1/3);G=f(I-D(O))*C(A(I-D(O)),1/3);M[0]=-W/3+(H+G);M[1]=-W/3-(H+G)/2;M[2]=M[1];N=A(D(3)*(H-G)/2);if(N!==0){M[1]=-1;M[2]=-1}}else{K=B(I/D(-C(J,3)));M[0]=2*D(-J)*E(K/3)-W/3;M[1]=2*D(-J)*E((K+2*F)/3)-W/3;M[2]=2*D(-J)*E((K+4*F)/3)-W/3}for(X=0;X<3;X++){if(M[X]<0||M[X]>1){M[X]=-1}}return M},quadraticRoots:function(H,G,M){var L,K,J,I;if(H===0){return this.linearRoot(G,M)}L=G*G-4*H*M;if(L===0){J=[-G/(2*H)]}else{if(L>0){K=D(L);J=[(-G-K)/(2*H),(-G+K)/(2*H)]}else{return[]}}for(I=0;I<J.length;I++){if(J[I]<0||J[I]>1){J[I]=-1}}return J},linearRoot:function(H,G){var I=-G/H;if(H===0||I<0||I>1){return[]}return[I]},bezierCoeffs:function(H,G,K,J){var I=[];I[0]=-H+3*G-3*K+J;I[1]=3*H-6*G+3*K;I[2]=-3*H+3*G;I[3]=H;return I},cubicLineIntersections:function(c,a,Z,Y,K,J,I,H,g,O,e,M){var R=[],h=[],X=O-M,W=e-g,V=g*(M-O)-O*(e-g),f=this.bezierCoeffs(c,a,Z,Y),d=this.bezierCoeffs(K,J,I,H),b,U,T,S,G,Q,N,L;R[0]=X*f[0]+W*d[0];R[1]=X*f[1]+W*d[1];R[2]=X*f[2]+W*d[2];R[3]=X*f[3]+W*d[3]+V;U=this.cubicRoots(R);for(b=0;b<U.length;b++){S=U[b];if(S<0||S>1){continue}G=S*S;Q=G*S;N=f[0]*Q+f[1]*G+f[2]*S+f[3];L=d[0]*Q+d[1]*G+d[2]*S+d[3];if((e-g)!==0){T=(N-g)/(e-g)}else{T=(L-O)/(M-O)}if(!(T<0||T>1)){h.push([N,L])}}return h},splitCubic:function(G,R,Q,O,M){var J=M*M,N=M*J,I=M-1,H=I*I,K=I*H,L=N*O-3*J*I*Q+3*M*H*R-K*G;return[[G,M*R-I*G,J*Q-2*M*I*R+H*G,L],[L,J*O-2*M*I*Q+H*R,M*O-I*Q,O]]},cubicDimension:function(P,O,L,K){var J=3*(-P+3*(O-L)+K),I=6*(P-2*O+L),H=-3*(P-O),Q,N,G=Math.min(P,K),M=Math.max(P,K),R;if(J===0){if(I===0){return[G,M]}else{Q=-H/I;if(0<Q&&Q<1){N=this.interpolateCubic(P,O,L,K,Q);G=Math.min(G,N);M=Math.max(M,N)}}}else{R=I*I-4*J*H;if(R>=0){R=D(R);Q=(R-I)/2/J;if(0<Q&&Q<1){N=this.interpolateCubic(P,O,L,K,Q);G=Math.min(G,N);M=Math.max(M,N)}if(R>0){Q-=R/J;if(0<Q&&Q<1){N=this.interpolateCubic(P,O,L,K,Q);G=Math.min(G,N);M=Math.max(M,N)}}}}return[G,M]},interpolateCubic:function(H,G,L,K,I){if(I===0){return H}if(I===1){return K}var J=(1-I)/I;return I*I*I*(K+J*(3*L+J*(3*G+J*H)))},cubicsIntersections:function(R,Q,P,O,a,Z,Y,V,G,f,e,d,M,L,K,I){var c=this,X=c.cubicDimension(R,Q,P,O),b=c.cubicDimension(a,Z,Y,V),N=c.cubicDimension(G,f,e,d),S=c.cubicDimension(M,L,K,I),J,H,U,T,W=[];if(X[0]>N[1]||X[1]<N[0]||b[0]>S[1]||b[1]<S[0]){return[]}if(A(a-Z)<1&&A(Y-V)<1&&A(R-O)<1&&A(Q-P)<1&&A(M-L)<1&&A(K-I)<1&&A(G-d)<1&&A(f-e)<1){return[[(R+O)*0.5,(a+Z)*0.5]]}J=c.splitCubic(R,Q,P,O,0.5);H=c.splitCubic(a,Z,Y,V,0.5);U=c.splitCubic(G,f,e,d,0.5);T=c.splitCubic(M,L,K,I,0.5);W.push.apply(W,c.cubicsIntersections.apply(c,J[0].concat(H[0],U[0],T[0])));W.push.apply(W,c.cubicsIntersections.apply(c,J[0].concat(H[0],U[1],T[1])));W.push.apply(W,c.cubicsIntersections.apply(c,J[1].concat(H[1],U[0],T[0])));W.push.apply(W,c.cubicsIntersections.apply(c,J[1].concat(H[1],U[1],T[1])));return W},linesIntersection:function(K,P,J,O,H,N,Q,M){var L=(J-K)*(M-N)-(O-P)*(Q-H),I,G;if(L===0){return null}I=((Q-H)*(P-N)-(K-H)*(M-N))/L;G=((J-K)*(P-N)-(O-P)*(K-H))/L;if(I>=0&&I<=1&&G>=0&&G<=1){return[K+I*(J-K),P+I*(O-P)]}return null},pointOnLine:function(J,M,H,L,G,N){var K,I;if(A(H-J)<A(L-M)){I=J;J=M;M=I;I=H;H=L;L=I;I=G;G=N;N=I}K=(G-J)/(H-J);if(K<0||K>1){return false}return A(M+K*(L-M)-N)<4},pointOnCubic:function(T,R,Q,P,J,I,H,G,N,M){var X=this,W=X.bezierCoeffs(T,R,Q,P),V=X.bezierCoeffs(J,I,H,G),U,S,L,K,O;W[3]-=N;V[3]-=M;L=X.cubicRoots(W);K=X.cubicRoots(V);for(U=0;U<L.length;U++){O=L[U];for(S=0;S<K.length;S++){if(O>=0&&O<=1&&A(O-K[S])<0.05){return true}}}return false}}})