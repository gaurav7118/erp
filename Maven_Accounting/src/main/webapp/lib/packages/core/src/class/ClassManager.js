Ext.ClassManager=(function(Q,L,T,E,O){var B=Ext.Class.makeCtor,I=typeof window==="undefined",G=[],H={Ext:{name:"Ext",value:Ext}},C=Ext.apply(new Ext.Inventory(),{classes:{},classState:{},existCache:{},instantiators:[],isCreated:function(U){if(typeof U!=="string"||U.length<1){throw new Error("[Ext.ClassManager] Invalid classname, must be a string and must not be empty")}if(C.classes[U]||C.existCache[U]){return true}if(!C.lookupName(U,false)){return false}C.triggerCreated(U);return true},createdListeners:[],nameCreatedListeners:{},existsListeners:[],nameExistsListeners:{},overrideMap:{},triggerCreated:function(U,V){C.existCache[U]=V||1;C.classState[U]+=40;C.notify(U,C.createdListeners,C.nameCreatedListeners)},onCreated:function(W,V,U){C.addListener(W,V,U,C.createdListeners,C.nameCreatedListeners)},notify:function(c,e,W){var X=C.getAlternatesByName(c),d=[c],Z,b,Y,a,V,U;for(Z=0,b=e.length;Z<b;Z++){V=e[Z];V.fn.call(V.scope,c)}while(d){for(Z=0,b=d.length;Z<b;Z++){U=d[Z];e=W[U];if(e){for(Y=0,a=e.length;Y<a;Y++){V=e[Y];V.fn.call(V.scope,U)}delete W[U]}}d=X;X=null}},addListener:function(Z,Y,X,W,V){if(Ext.isArray(X)){Z=Ext.Function.createBarrier(X.length,Z,Y);for(U=0;U<X.length;U++){this.addListener(Z,null,X[U],W,V)}return }var U,a={fn:Z,scope:Y};if(X){if(this.isCreated(X)){Z.call(Y,X);return }if(!V[X]){V[X]=[]}V[X].push(a)}else{W.push(a)}},$namespaceCache:H,addRootNamespaces:function(V){for(var U in V){H[U]={name:U,value:V[U]}}},clearNamespaceCache:function(){G.length=0;for(var U in H){if(!H[U].value){delete H[U]}}},getNamespaceEntry:function(V){if(typeof V!=="string"){return V}var W=H[V],U;if(!W){U=V.lastIndexOf(".");if(U<0){W={name:V}}else{W={name:V.substring(U+1),parent:C.getNamespaceEntry(V.substring(0,U))}}H[V]=W}return W},lookupName:function(X,Z){var Y=C.getNamespaceEntry(X),W=Ext.global,U=0,a,V;for(a=Y;a;a=a.parent){G[U++]=a}while(W&&U-->0){a=G[U];V=W;W=a.value||W[a.name];if(!W&&Z){V[a.name]=W={}}}return W},setNamespace:function(V,X){var W=C.getNamespaceEntry(V),U=Ext.global;if(W.parent){U=C.lookupName(W.parent,true)}U[W.name]=X;return X},setXType:function(U,b){var W=U.$className,a=W?U:C.get(W=U),X=a.prototype,Y=X.xtypes,V=X.xtypesChain,Z=X.xtypesMap;if(!X.hasOwnProperty("xtypes")){X.xtypes=Y=[];X.xtypesChain=V=V?V.slice(0):[];X.xtypesMap=Z=Ext.apply({},Z)}C.addAlias(W,"widget."+b,true);Y.push(b);V.push(b);Z[b]=true},set:function(U,W){var V=C.getName(W);C.classes[U]=C.setNamespace(U,W);if(V&&V!==U){C.addAlternate(V,U)}return C},get:function(U){return C.classes[U]||C.lookupName(U,false)},addNameAliasMappings:function(U){C.addAlias(U)},addNameAlternateMappings:function(U){C.addAlternate(U)},getByAlias:function(U){return C.get(C.getNameByAlias(U))},getByConfig:function(V,W){var X=V.xclass,U;if(X){U=X}else{U=V.xtype;if(U){W="widget."}else{U=V.type}U=C.getNameByAlias(W+U)}return C.get(U)},getName:function(U){return U&&U.$className||""},getClass:function(U){return U&&U.self||null},create:function(V,X,U){if(V!=null&&typeof V!=="string"){throw new Error("[Ext.define] Invalid class name '"+V+"' specified, must be a non-empty string")}var W=B(V);if(typeof X==="function"){X=X(W)}if(V){if(C.classes[V]){Ext.log.warn("[Ext.define] Duplicate class name '"+V+"' specified, must be a non-empty string")}W.name=V}X.$className=V;return new Q(W,X,function(){var Y=X.postprocessors||C.defaultPostprocessors,f=C.postprocessors,g=[],e,a,d,Z,c,b,h;delete X.postprocessors;for(a=0,d=Y.length;a<d;a++){e=Y[a];if(typeof e==="string"){e=f[e];b=e.properties;if(b===true){g.push(e.fn)}else{if(b){for(Z=0,c=b.length;Z<c;Z++){h=b[Z];if(X.hasOwnProperty(h)){g.push(e.fn);break}}}}}else{g.push(e)}}X.postprocessors=g;X.createdFn=U;C.processCreate(V,this,X)})},processCreate:function(X,V,Z){var Y=this,U=Z.postprocessors.shift(),W=Z.createdFn;if(!U){Ext.classSystemMonitor&&Ext.classSystemMonitor(X,"Ext.ClassManager#classCreated",arguments);if(X){Y.set(X,V)}delete V._classHooks;if(W){W.call(V,V)}if(X){Y.triggerCreated(X)}return }if(U.call(Y,X,V,Z,Y.processCreate)!==false){Y.processCreate(X,V,Z)}},createOverride:function(a,Y,W){var c=this,d=Y.override,f=Y.requires,X=Y.uses,Z=Y.mixins,U,e=1,b,V=function(){var g,l,k,j,h;if(!b){l=f?f.slice(0):[];if(Z){if(!(U=Z instanceof Array)){for(j in Z){if(Ext.isString(g=Z[j])){l.push(g)}}}else{for(k=0,h=Z.length;k<h;++k){if(Ext.isString(g=Z[k])){l.push(g)}}}}b=true;if(l.length){Ext.require(l,V);return }}if(U){for(k=0,h=Z.length;k<h;++k){if(Ext.isString(g=Z[k])){Z[k]=Ext.ClassManager.get(g)}}}else{if(Z){for(j in Z){if(Ext.isString(g=Z[j])){Z[j]=Ext.ClassManager.get(g)}}}}g=c.get(d);delete Y.override;delete Y.compatibility;delete Y.requires;delete Y.uses;Ext.override(g,Y);Ext.Loader.history.push(a);if(X){Ext["Loader"].addUsedClasses(X)}if(W){W.call(g,g)}};C.overrideMap[a]=true;if("compatibility" in Y&&Ext.isString(e=Y.compatibility)){e=Ext.checkVersion(e)}if(e){c.onCreated(V,c,d)}c.triggerCreated(a,2);return c},instantiateByAlias:function(){var V=arguments[0],U=T.call(arguments),W=this.getNameByAlias(V);if(!W){throw new Error("[Ext.createByAlias] Unrecognized alias: "+V)}U[0]=W;return Ext.create.apply(Ext,U)},instantiate:function(){Ext.log.warn("Ext.ClassManager.instantiate() is deprecated.  Use Ext.create() instead.");return Ext.create.apply(Ext,arguments)},dynInstantiate:function(V,U){U=E(U,true);U.unshift(V);return Ext.create.apply(Ext,U)},getInstantiator:function(X){var W=this.instantiators,Y,V,U;Y=W[X];if(!Y){V=X;U=[];for(V=0;V<X;V++){U.push("a["+V+"]")}Y=W[X]=new Function("c","a","return new c("+U.join(",")+")");Y.name="Ext.create"+X}return Y},postprocessors:{},defaultPostprocessors:[],registerPostprocessor:function(V,Y,W,U,X){if(!U){U="last"}if(!W){W=[V]}this.postprocessors[V]={name:V,properties:W||false,fn:Y};this.setDefaultPostprocessorPosition(V,U,X);return this},setDefaultPostprocessors:function(U){this.defaultPostprocessors=E(U);return this},setDefaultPostprocessorPosition:function(V,Y,X){var W=this.defaultPostprocessors,U;if(typeof Y==="string"){if(Y==="first"){W.unshift(V);return this}else{if(Y==="last"){W.push(V);return this}}Y=(Y==="after")?1:-1}U=Ext.Array.indexOf(W,X);if(U!==-1){Ext.Array.splice(W,Math.max(0,U+Y),0,V)}return this}});C.registerPostprocessor("alias",function(W,V,Z){Ext.classSystemMonitor&&Ext.classSystemMonitor(W,"Ext.ClassManager#aliasPostProcessor",arguments);var U=Ext.Array.from(Z.alias),X,Y;for(X=0,Y=U.length;X<Y;X++){L=U[X];this.addAlias(V,L)}},["xtype","alias"]);C.registerPostprocessor("singleton",function(V,U,X,W){Ext.classSystemMonitor&&Ext.classSystemMonitor(V,"Ext.ClassManager#singletonPostProcessor",arguments);if(X.singleton){W.call(this,V,new U(),X)}else{return true}return false});C.registerPostprocessor("alternateClassName",function(V,U,Z){Ext.classSystemMonitor&&Ext.classSystemMonitor(V,"Ext.ClassManager#alternateClassNamePostprocessor",arguments);var X=Z.alternateClassName,W,Y,a;if(!(X instanceof Array)){X=[X]}for(W=0,Y=X.length;W<Y;W++){a=X[W];if(typeof a!=="string"){throw new Error("[Ext.define] Invalid alternate of: '"+a+"' for class: '"+V+"'; must be a valid string")}this.set(a,U)}});C.registerPostprocessor("debugHooks",function(V,U,W){Ext.classSystemMonitor&&Ext.classSystemMonitor(U,"Ext.Class#debugHooks",arguments);if(Ext.isDebugEnabled(U.$className,W.debugHooks.$enabled)){delete W.debugHooks.$enabled;Ext.override(U,W.debugHooks)}var X=U.isInstance?U.self:U;delete X.prototype.debugHooks});C.registerPostprocessor("deprecated",function(V,U,W){Ext.classSystemMonitor&&Ext.classSystemMonitor(U,"Ext.Class#deprecated",arguments);var X=U.isInstance?U.self:U;X.addDeprecations(W.deprecated);delete X.prototype.deprecated});Ext.apply(Ext,{create:function(){var W=arguments[0],X=typeof W,V=T.call(arguments,1),U;if(X==="function"){U=W}else{if(X!=="string"&&V.length===0){V=[W];if(!(W=W.xclass)){W=V[0].xtype;if(W){W="widget."+W}}}if(typeof W!=="string"||W.length<1){throw new Error("[Ext.create] Invalid class name or alias '"+W+"' specified, must be a non-empty string")}W=C.resolveName(W);U=C.get(W)}if(!U){!I&&Ext.log.warn("[Ext.Loader] Synchronously loading '"+W+"'; consider adding Ext.require('"+W+"') above Ext.onReady");Ext.syncRequire(W);U=C.get(W)}if(!U){throw new Error("[Ext.create] Unrecognized class name / alias: "+W)}if(typeof U!=="function"){throw new Error("[Ext.create] Singleton '"+W+"' cannot be instantiated.")}return C.getInstantiator(V.length)(U,V)},widget:function(W,V){var Z=W,X,Y,U;if(typeof Z!=="string"){V=W;Z=V.xtype;Y=V.xclass}else{V=V||{}}if(V.isComponent){return V}if(!Y){X="widget."+Z;Y=C.getNameByAlias(X)}if(Y){U=C.get(Y)}if(!U){return Ext.create(Y||X,V)}return new U(V)},createByAlias:L(C,"instantiateByAlias"),define:function(V,W,U){Ext.classSystemMonitor&&Ext.classSystemMonitor(V,"ClassManager#define",arguments);if(W.override){C.classState[V]=20;return C.createOverride.apply(C,arguments)}C.classState[V]=10;return C.create.apply(C,arguments)},undefine:function(W){Ext.classSystemMonitor&&Ext.classSystemMonitor(W,"Ext.ClassManager#undefine",arguments);var U=C.classes;delete U[W];delete C.existCache[W];delete C.classState[W];C.removeName(W);var X=C.getNamespaceEntry(W),V=X.parent?C.lookupName(X.parent,false):Ext.global;if(V){try{delete V[X.name]}catch(Y){V[X.name]=undefined}}},getClassName:L(C,"getName"),getDisplayName:function(U){if(U){if(U.displayName){return U.displayName}if(U.$name&&U.$class){return Ext.getClassName(U.$class)+"#"+U.$name}if(U.$className){return U.$className}}return"Anonymous"},getClass:L(C,"getClass"),namespace:function(){var U=O,V;for(V=arguments.length;V-->0;){U=C.lookupName(arguments[V],true)}return U}});Ext.addRootNamespaces=C.addRootNamespaces;Ext.createWidget=Ext.widget;Ext.ns=Ext.namespace;Q.registerPreprocessor("className",function(U,V){if("$className" in V){U.$className=V.$className;U.displayName=U.$className}Ext.classSystemMonitor&&Ext.classSystemMonitor(U,"Ext.ClassManager#classNamePreprocessor",arguments)},true,"first");Q.registerPreprocessor("alias",function(f,Z){Ext.classSystemMonitor&&Ext.classSystemMonitor(f,"Ext.ClassManager#aliasPreprocessor",arguments);var d=f.prototype,W=E(Z.xtype),U=E(Z.alias),g="widget.",e=g.length,a=Array.prototype.slice.call(d.xtypesChain||[]),X=Ext.merge({},d.xtypesMap||{}),Y,c,b,V;for(Y=0,c=U.length;Y<c;Y++){b=U[Y];if(typeof b!=="string"||b.length<1){throw new Error("[Ext.define] Invalid alias of: '"+b+"' for class: '"+S+"'; must be a valid string")}if(b.substring(0,e)===g){V=b.substring(e);Ext.Array.include(W,V)}}f.xtype=Z.xtype=W[0];Z.xtypes=W;for(Y=0,c=W.length;Y<c;Y++){V=W[Y];if(!X[V]){X[V]=true;a.push(V)}}Z.xtypesChain=a;Z.xtypesMap=X;Ext.Function.interceptAfter(Z,"onClassCreated",function(){Ext.classSystemMonitor&&Ext.classSystemMonitor(f,"Ext.ClassManager#aliasPreprocessor#afterClassCreated",arguments);var h=d.mixins,j,i;for(j in h){if(h.hasOwnProperty(j)){i=h[j];W=i.xtypes;if(W){for(Y=0,c=W.length;Y<c;Y++){V=W[Y];if(!X[V]){X[V]=true;a.push(V)}}}}}});for(Y=0,c=W.length;Y<c;Y++){V=W[Y];if(typeof V!=="string"||V.length<1){throw new Error("[Ext.define] Invalid xtype of: '"+V+"' for class: '"+S+"'; must be a valid non-empty string")}Ext.Array.include(U,g+V)}Z.alias=U},["xtype","alias"]);if(Ext.manifest){var F=Ext.manifest,R=F.classes,N=F.paths,P={},J={},D,K,S,M,A;if(N){if(F.bootRelative){A=Ext.Boot.baseUrl;for(M in N){if(N.hasOwnProperty(M)){N[M]=A+N[M]}}}C.setPath(N)}if(R){for(D in R){J[D]=[];P[D]=[];K=R[D];if(K.alias){P[D]=K.alias}if(K.alternates){J[D]=K.alternates}}}C.addAlias(P);C.addAlternate(J)}return C}(Ext.Class,Ext.Function.alias,Array.prototype.slice,Ext.Array.from,Ext.global))