(function(){var B=Ext.Config,C=B.map,A=Ext.Object;Ext.Configurator=function(D){var F=this,E=D.prototype,G=D.superclass?D.superclass.self.$config:null;F.cls=D;F.superCfg=G;if(G){F.configs=A.chain(G.configs);F.cachedConfigs=A.chain(G.cachedConfigs);F.initMap=A.chain(G.initMap);F.values=A.chain(G.values);F.needsFork=G.needsFork;F.deprecations=A.chain(G.deprecations)}else{F.configs={};F.cachedConfigs={};F.initMap={};F.values={};F.deprecations={}}E.config=E.defaultConfig=F.values;D.$config=F};Ext.Configurator.prototype={self:Ext.Configurator,needsFork:false,initList:null,add:function(S,D){var T=this,H=T.cls,K=T.configs,U=T.cachedConfigs,M=T.initMap,P=H.prototype,V=D&&D.$config.configs,E=T.values,J,L,R,F,G,I,W,O,N,Q;for(W in S){Q=S[W];J=Q&&Q.constructor===Object;L=J&&"$value" in Q?Q:null;if(L){R=!!L.cached;Q=L.$value;J=Q&&Q.constructor===Object}F=L&&L.merge;G=K[W];if(G){if(D){F=G.merge;if(!F){continue}L=null}else{F=F||G.merge}if(!D&&R&&!U[W]){Ext.raise("Redefining config as cached: "+W+" in class: "+H.$className)}I=E[W];if(F){Q=F.call(G,Q,I,H,D)}else{if(J){if(I&&I.constructor===Object){Q=A.merge({},I,Q)}}}}else{if(V){G=V[W];L=null}else{G=B.get(W)}K[W]=G;if(G.cached||R){U[W]=true}O=G.names;if(!P[N=O.get]){P[N]=G.getter||G.getGetter()}if(!P[N=O.set]){P[N]=(L&&L.evented)?(G.eventedSetter||G.getEventedSetter()):(G.setter||G.getSetter())}}if(L){if(G.owner!==H){K[W]=G=Ext.Object.chain(G);G.owner=H}Ext.apply(G,L);delete G.$value}if(!T.needsFork&&Q&&(Q.constructor===Object||Q instanceof Array)){T.needsFork=true}if(Q!==null){M[W]=true}else{if(P.$configPrefixed){P[K[W].names.internal]=null}else{P[K[W].name]=null}if(W in M){M[W]=false}}E[W]=Q}},addDeprecations:function(J){var I=this,E=I.deprecations,G=(I.cls.$className||"")+"#",H,D,F;for(F in J){D=J[F];if(!D){H="This config has been removed."}else{if(!(H=D.message)){H='This config has been renamed to "'+D+'"'}}E[F]=G+F+": "+H}},configure:function(X,L){var Z=this,K=Z.configs,I=Z.deprecations,M=Z.initMap,O=Z.initListMap,V=Z.initList,P=Z.cls.prototype,E=Z.values,Q=0,S=!V,F,G,H,b,U,T,J,N,a,R,Y,W,D;E=Z.needsFork?A.fork(E):A.chain(E);X.isConfiguring=true;if(S){Z.initList=V=[];Z.initListMap=O={};X.isFirstInstance=true;for(a in M){b=M[a];G=K[a];Y=G.cached;if(b){N=G.names;R=E[a];if(!P[N.set].$isDefault||P[N.apply]||P[N.update]||typeof R==="object"){if(Y){(F||(F=[])).push(G)}else{V.push(G);O[a]=true}X[N.get]=G.initGetter||G.getInitGetter()}else{P[G.getInternalName(P)]=R}}else{if(Y){P[G.getInternalName(P)]=undefined}}}}J=F&&F.length;if(J){for(U=0;U<J;++U){T=F[U].getInternalName(P);X[T]=null}for(U=0;U<J;++U){N=(G=F[U]).names;H=N.get;if(X.hasOwnProperty(H)){X[N.set](E[G.name]);delete X[H]}}for(U=0;U<J;++U){T=F[U].getInternalName(P);P[T]=X[T];delete X[T]}}if(L&&L.platformConfig){L=Z.resolvePlatformConfig(X,L)}if(S){if(X.afterCachedConfig&&!X.afterCachedConfig.$nullFn){X.afterCachedConfig(L)}}X.config=E;for(U=0,J=V.length;U<J;++U){G=V[U];X[G.names.get]=G.initGetter||G.getInitGetter()}if(X.transformInstanceConfig){L=X.transformInstanceConfig(L)}if(L){for(a in L){R=L[a];G=K[a];if(I[a]){Ext.log.warn(I[a]);if(!G){continue}}if(!G){D=X.self.prototype[a];if(X.$configStrict&&(typeof D==="function")&&!D.$nullFn){Ext.raise("Cannot override method "+a+" on "+X.$className+" instance.")}X[a]=R}else{if(!G.lazy){++Q}if(!O[a]){X[G.names.get]=G.initGetter||G.getInitGetter()}if(G.merge){R=G.merge(R,E[a],X)}else{if(R&&R.constructor===Object){W=E[a];if(W&&W.constructor===Object){R=A.merge(E[a],R)}else{R=Ext.clone(R)}}}}E[a]=R}}if(X.beforeInitConfig&&!X.beforeInitConfig.$nullFn){if(X.beforeInitConfig(L)===false){return }}if(L){for(a in L){if(!Q){break}G=K[a];if(G&&!G.lazy){--Q;N=G.names;H=N.get;if(X.hasOwnProperty(H)){X[N.set](E[a]);delete X[N.get]}}}}for(U=0,J=V.length;U<J;++U){G=V[U];N=G.names;H=N.get;if(!G.lazy&&X.hasOwnProperty(H)){X[N.set](E[G.name]);delete X[H]}}delete X.isConfiguring},getCurrentConfig:function(E){var D=E.defaultConfig,G={},F;for(F in D){G[F]=E[C[F].names.get]()}return G},merge:function(D,I,G){var K=this.configs,F,J,H,E;for(F in G){J=G[F];E=K[F];if(E){if(E.merge){J=E.merge(J,I[F],D)}else{if(J&&J.constructor===Object){H=I[F];if(H&&H.constructor===Object){J=Ext.Object.merge(H,J)}else{J=Ext.clone(J)}}}}I[F]=J}return I},reconfigure:function(Q,L,R){var H=Q.config,I=[],P=Q.$configStrict&&!(R&&R.strict===false),M=this.configs,F=R&&R.defaults,K,O,G,J,E,N,D;for(E in L){if(F&&Q.hasOwnProperty(E)){continue}H[E]=L[E];K=M[E];if(this.deprecations[E]){Ext.log.warn(this.deprecations[E]);if(!K){continue}}if(K){Q[K.names.get]=K.initGetter||K.getInitGetter()}else{D=Q.self.prototype[E];if(P){if((typeof D==="function")&&!D.$nullFn){Ext.Error.raise("Cannot override method "+E+" on "+Q.$className+" instance.");continue}else{if(E!=="type"){Ext.log.warn('No such config "'+E+'" for class '+Q.$className)}}}}I.push(E)}for(G=0,J=I.length;G<J;G++){E=I[G];K=M[E];if(K){N=K.names;O=N.get;if(Q.hasOwnProperty(O)){Q[N.set](L[E]);delete Q[O]}}else{K=C[E]||Ext.Config.get(E);N=K.names;if(Q[N.set]){Q[N.set](L[E])}else{Q[E]=L[E]}}}},resolvePlatformConfig:function(D,J){var H=J&&J.platformConfig,E=J,F,G,I;if(H){G=Ext.getPlatformConfigKeys(H);I=G.length;if(I){E=Ext.merge({},E);for(F=0,I=G.length;F<I;++F){this.merge(D,E,H[G[F]])}}}return E}}}())