Ext.define("Ext.ux.ajax.DataSimlet",function(){function B(F,E){var C=F.direction,D=(C&&C.toUpperCase()==="DESC")?-1:1;return function(H,I){var G=H[F.property],K=I[F.property],J=(G<K)?-1:((K<G)?1:0);if(J||!E){return J*D}return E(H,I)}}function A(C,E){for(var F=E,D=C&&C.length;D;){F=B(C[--D],F)}return F}return{extend:"Ext.ux.ajax.Simlet",buildNodes:function(G,K){var J=this,D={data:[]},I=G.length,F,H,E,C;J.nodes[K]=D;for(H=0;H<I;++H){D.data.push(E=G[H]);C=E.text||E.title;E.id=K?K+"/"+C:C;F=E.children;if(!(E.leaf=!F)){delete E.children;J.buildNodes(F,E.id)}}},deleteRecord:function(C){if(this.data&&typeof this.data!=="function"){Ext.Array.removeAt(this.data,C)}},fixTree:function(D,C){var G=this,F=D.params.node,E;if(!(E=G.nodes)){G.nodes=E={};G.buildNodes(C,"")}F=E[F];if(F){if(G.node){G.node.sortedData=G.sortedData;G.node.currentOrder=G.currentOrder}G.node=F;G.data=F.data;G.sortedData=F.sortedData;G.currentOrder=F.currentOrder}else{G.data=null}},getData:function(K){var I=this,F=K.params,E=(F.filter||"")+(F.group||"")+"-"+(F.sort||"")+"-"+(F.dir||""),L=I.tree,C,G,H,J;if(L){I.fixTree(K,L)}G=I.data;if(typeof G==="function"){C=true;G=G.call(this,K)}if(!G||E==="--"){return G||[]}if(!C&&E==I.currentOrder){return I.sortedData}K.filterSpec=F.filter&&Ext.decode(F.filter);K.groupSpec=F.group&&Ext.decode(F.group);H=F.sort;if(F.dir){H=[{direction:F.dir,property:H}]}else{H=Ext.decode(F.sort)}if(K.filterSpec){var D=new Ext.util.FilterCollection();D.add(this.processFilters(K.filterSpec));G=Ext.Array.filter(G,D.getFilterFn())}J=A((K.sortSpec=H));if(K.groupSpec){J=A([K.groupSpec],J)}G=Ext.isArray(G)?G.slice(0):G;if(J){Ext.Array.sort(G,J)}I.sortedData=G;I.currentOrder=E;return G},processFilters:Ext.identityFn,getPage:function(D,G){var E=G,F=G.length,H=D.params.start||0,C=D.params.limit?Math.min(F,H+D.params.limit):F;if(H||C<F){E=E.slice(H,C)}return E},getGroupSummary:function(D,E,C){return E[0]},getSummary:function(M,G,H){var J=this,C=M.groupSpec.property,K,F={},I=[],D,E;Ext.each(H,function(N){D=N[C];F[D]=true});function L(){if(K){I.push(J.getGroupSummary(C,K,M));K=null}}Ext.each(G,function(N){D=N[C];if(E!==D){L();E=D}if(!F[D]){return !I.length}if(K){K.push(N)}else{K=[N]}return true});L();return I}}}())