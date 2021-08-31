Ext.define("Ext.direct.RemotingMethod",{constructor:function(C){var H=this,E=C.params,G=C.len,J=C.metadata,I={},A,F,B,D;H.name=C.name;H.disableBatching=C.batched!=null?!C.batched:false;if(C.formHandler){H.formHandler=C.formHandler}else{if(Ext.isNumeric(G)){H.len=G;H.ordered=true}else{H.named=true;H.strict=C.strict!==undefined?C.strict:true;H.params={};F=E&&E.length;for(B=0;B<F;B++){D=E[B];A=Ext.isObject(D)?D.name:D;H.params[A]=true}}}if(J){E=J.params;G=J.len;if(Ext.isNumeric(G)){if(G===0){Ext.raise("metadata.len cannot be 0 for Ext Direct method "+H.name)}I.ordered=true;I.len=G}else{if(Ext.isArray(E)){I.named=true;I.params={};for(B=0,F=E.length;B<F;B++){D=E[B];I.params[D]=true}I.strict=J.strict!==undefined?J.strict:true}else{Ext.raise("metadata is neither named nor ordered for Ext Direct method "+H.name)}}H.metadata=I}},getArgs:function(B){var G=this,C=B.params,H=B.paramOrder,A=B.paramsAsHash,I=B.metadata,J=B.options,F=[],D,E;if(G.ordered){if(G.len>0){if(H){for(D=0,E=H.length;D<E;D++){F.push(C[H[D]])}}else{if(A){F.push(C)}}}}else{F.push(C)}F.push(B.callback,B.scope||window);if(J||I){J=Ext.apply({},J);if(I){J.metadata=I}F.push(J)}return F},getCallData:function(F){var G=this,D=null,E=G.len,C=G.params,I=G.strict,B,J,K,A,L,H;if(G.ordered){J=F[E];K=F[E+1];L=F[E+2];if(E!==0){D=F.slice(0,E)}}else{if(G.formHandler){B=F[0];J=F[1];K=F[2];L=F[3]}else{D=Ext.apply({},F[0]);J=F[1];K=F[2];L=F[3];if(I){for(A in D){if(D.hasOwnProperty(A)&&!C[A]){delete D[A]}}}}}if(G.metadata&&L&&L.metadata){if(G.metadata.ordered){if(!Ext.isArray(L.metadata)){Ext.raise("options.metadata is not an Array for Ext Direct method "+G.name)}else{if(L.metadata.length<G.metadata.len){Ext.raise("Not enough parameters in options.metadata for Ext Direct method "+G.name)}}H=L.metadata.slice(0,G.metadata.len)}else{if(!Ext.isObject(L.metadata)){Ext.raise("options.metadata is not an Object for Ext Direct method "+G.name)}H=Ext.apply({},L.metadata);if(G.metadata.strict){for(A in H){if(H.hasOwnProperty(A)&&!G.metadata.params[A]){delete H[A]}}}for(A in G.metadata.params){if(!H.hasOwnProperty(A)){Ext.raise("Named parameter "+A+" is missing in options.metadata for Ext Direct method "+G.name)}}}delete L.metadata}return{form:B,data:D,metadata:H,callback:J,scope:K,options:L}}})