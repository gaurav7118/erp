Ext.define("Ext.ux.google.Api",{mixins:["Ext.mixin.Mashup"],requiredScripts:["//www.google.com/jsapi"],statics:{loadedModules:{}},onClassExtended:function(C,D,A){var E=A.onBeforeCreated,B=this;A.onBeforeCreated=function(P,I){var M=this,J=[],N=Ext.Array.from(I.requiresGoogle),L=B.loadedModules,G=0,O=function(){if(!--G){E.call(M,P,I,A)}Ext.env.Ready.unblock()},K,H,F;F=N.length;for(H=0;H<F;++H){if(Ext.isString(K=N[H])){J.push({api:K})}else{if(Ext.isObject(K)){J.push(Ext.apply({},K))}}}Ext.each(J,function(T){var R=T.api,Q=String(T.version||"1.x"),S=L[R];if(!S){++G;Ext.env.Ready.block();L[R]=S=[O].concat(T.callback||[]);delete T.api;delete T.version;google.load(R,Q,Ext.applyIf({callback:function(){L[R]=true;for(var U=S.length;U-->0;){S[U]()}}},T))}else{if(S!==true){S.push(O)}}});if(!G){E.call(M,P,I,A)}}}})