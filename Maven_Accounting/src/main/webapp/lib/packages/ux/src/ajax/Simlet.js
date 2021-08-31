Ext.define("Ext.ux.ajax.Simlet",function(){var D=/([^?#]*)(#.*)?$/,A=/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/,B=/^[+-]?\d+$/,C=/^[+-]?\d+\.\d+$/;function E(G){var F;if(Ext.isDefined(G)){G=decodeURIComponent(G);if(B.test(G)){G=parseInt(G,10)}else{if(C.test(G)){G=parseFloat(G)}else{if(!!(F=A.test(G))){G=new Date(Date.UTC(+F[1],+F[2]-1,+F[3],+F[4],+F[5],+F[6]))}}}}return G}return{alias:"simlet.basic",isSimlet:true,responseProps:["responseText","responseXML","status","statusText"],status:200,statusText:"OK",constructor:function(F){Ext.apply(this,F)},doGet:function(F){var H=this,G={};Ext.Array.forEach(H.responseProps,function(I){if(I in H){G[I]=H[I]}});return G},doPost:function(F){var H=this,G={};Ext.Array.forEach(H.responseProps,function(I){if(I in H){G[I]=H[I]}});return G},doRedirect:function(F){return false},doDelete:function(F){var H=this,I=F.xhr,G=I.options.records;H.removeFromData(F,G)},exec:function(I){var H=this,F={},J="do"+Ext.String.capitalize(I.method.toLowerCase()),G=H[J];if(G){F=G.call(H,H.getCtx(I.method,I.url,I))}else{F={status:405,statusText:"Method Not Allowed"}}return F},getCtx:function(H,F,G){return{method:H,params:this.parseQueryString(F),url:F,xhr:G}},openRequest:function(L,H,G,I){var F=this.getCtx(L,H),K=this.doRedirect(F),J;if(G.action==="destroy"){L="delete"}if(K){J=K}else{J=new Ext.ux.ajax.SimXhr({mgr:this.manager,simlet:this,options:G});J.open(L,H,I)}return J},parseQueryString:function(L){var G=D.exec(L),K={},N,M,J,F;if(G&&G[1]){var I,H=G[1].split("&");for(J=0,F=H.length;J<F;++J){if((I=H[J].split("="))[0]){N=decodeURIComponent(I.shift());M=E((I.length>1)?I.join("="):I[0]);if(!(N in K)){K[N]=M}else{if(Ext.isArray(K[N])){K[N].push(M)}else{K[N]=[K[N],M]}}}}}return K},redirect:function(H,F,G){switch(arguments.length){case 2:if(typeof F=="string"){break}G=F;case 1:F=H;H="GET";break}if(G){F=Ext.urlAppend(F,Ext.Object.toQueryString(G))}return this.manager.openRequest(H,F)},removeFromData:function(F,G){var J=this,K=J.getData(F),I=(F.xhr.options.proxy&&F.xhr.options.proxy.getModel())||{},H=I.idProperty||"id";Ext.each(G,function(L){var N=L.get(H);for(var M=K.length;M-->0;){if(K[M][H]===N){J.deleteRecord(M);break}}})}}}())