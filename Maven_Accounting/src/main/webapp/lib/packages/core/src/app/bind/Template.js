Ext.define("Ext.app.bind.Template",{requires:["Ext.util.Format"],numberRe:/^(?:\d+(?:\.\d*)?)$/,stringRe:/^(?:["][^"]*["])$/,tokenRe:/\{[!]?(?:(?:(\d+)|([a-z_][\w\-\.]*))(?::([a-z_\.]+)(?:\(([^\)]*?)?\))?)?)\}/gi,formatRe:/^([a-z_]+)(?:\(([^\)]*?)?\))?$/i,buffer:null,slots:null,tokens:null,constructor:function(D){var C=this,B=C._initters,A;C.text=D;for(A in B){C[A]=B[A]}},_initters:{apply:function(A,B){return this.parse().apply(A,B)},getTokens:function(){return this.parse().getTokens()}},apply:function(H,I){var E=this,D=E.slots,B=E.buffer,A=D.length,C,G,F;for(C=0;C<A;++C){G=D[C];if(G){if((F=H[G.pos])==null){F=""}if(G.not){F=!F}if(G.format){F=G.format(F,I)}B[C]=F}}return B.join("")},getTokens:function(){return this.tokens},parse:function(){var J=this,O=J.text,E=[],H=[],I=[],N={},M=0,D=J.tokenRe,K=0,B,F,A,G,P,L,C;for(F in J._initters){delete J[F]}J.buffer=E;J.slots=H;J.tokens=I;while((G=D.exec(O))){A=G.index-M;if(A){E[K++]=O.substring(M,M+A);M+=A}M+=(P=G[0]).length;L={fmt:(B=G[3]||null),index:G[1]?parseInt(G[1],10):null,not:P.charAt(1)==="!",token:G[2]||null};C=L.token||String(L.index);if(C in N){L.pos=N[C]}else{N[C]=L.pos=I.length;I.push(C)}if(B){if(B.substring(0,5)==="this."){L.fmt=B.substring(5)}else{if(!(B in Ext.util.Format)){Ext.raise('Invalid format specified: "'+B+'"')}L.scope=Ext.util.Format}J.parseArgs(G[4],L)}H[K++]=L}if(M<O.length){E[K++]=O.substring(M)}return J},parseArgs:function(F,G){var E=this,B=E.numberRe,H=E.stringRe,I,D,C,A;if(!F){D=[]}else{if(F.indexOf(",")<0){D=[F]}else{D=F.split(",")}}G=G||{};A=D.length;G.args=D;for(C=0;C<A;++C){I=D[C];if(I==="true"){D[C]=true}else{if(I==="false"){D[C]=false}else{if(I==="null"){D[C]=null}else{if(B.test(I)){D[C]=parseFloat(I)}else{if(H.test(I)){D[C]=I.substring(1,I.length-1)}else{G.fn=Ext.functionFactory("return ["+F+"];");G.format=E._formatEval;break}}}}}}if(!G.format){D.unshift(0);G.format=E._formatArgs}return G},parseFormat:function(A){var D=this,C=D.formatRe.exec(A),E={fmt:A,scope:Ext.util.Format},B;if(!C){Ext.raise('Invalid format syntax: "'+E+'"')}B=C[2];if(B){E.fmt=C[1];D.parseArgs(B,E)}else{E.args=[0];E.format=D._formatArgs}return E},_formatArgs:function(B,A){A=this.scope||A;this.args[0]=B;return A[this.fmt].apply(A,this.args)},_formatEval:function(C,B){var A=this.fn();A.unshift(C);B=this.scope||B;return B[this.fmt].apply(B,A)}})