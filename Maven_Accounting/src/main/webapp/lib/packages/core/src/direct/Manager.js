Ext.define("Ext.direct.Manager",{singleton:true,requires:["Ext.util.MixedCollection"],mixins:["Ext.mixin.Observable"],exceptions:{TRANSPORT:"xhr",PARSE:"parse",DATA:"data",LOGIN:"login",SERVER:"exception"},providerClasses:{},remotingMethods:{},config:{varName:"Ext.REMOTING_API"},apiNotFoundError:"Ext Direct API was not found at {0}",constructor:function(){var A=this;A.mixins.observable.constructor.call(A);A.transactions=new Ext.util.MixedCollection();A.providers=new Ext.util.MixedCollection()},addProvider:function(F){var D=this,B=arguments,E=D.relayers||(D.relayers={}),C,A;if(B.length>1){for(C=0,A=B.length;C<A;++C){D.addProvider(B[C])}return }if(!F.isProvider){F=Ext.create("direct."+F.type+"provider",F)}D.providers.add(F);F.on("data",D.onProviderData,D);if(F.relayedEvents){E[F.id]=D.relayEvents(F,F.relayedEvents)}if(!F.isConnected()){F.connect()}return F},loadProvider:function(B,J,K){var G=this,C=G.providerClasses,H,A,D,I,E,F;if(Ext.isArray(B)){for(E=0,F=B.length;E<F;E++){G.loadProvider(B[E],J,K)}return }H=B.type;A=B.url;if(C[H]&&C[H].checkConfig(B)){I=G.addProvider(B);G.fireEventArgs("providerload",[A,I]);Ext.callback(J,K,[A,I]);return }D=B.varName||G.getVarName();delete B.varName;if(!A){Ext.raise("Need API discovery URL to load a Remoting provider!")}delete B.url;Ext.Loader.loadScript({url:A,scope:G,onLoad:function(){this.onApiLoadSuccess({url:A,varName:D,config:B,callback:J,scope:K})},onError:function(){this.onApiLoadFailure({url:A,callback:J,scope:K})}})},getProvider:function(A){return A.isProvider?A:this.providers.get(A)},removeProvider:function(D){var B=this,A=B.providers,C=B.relayers,E;D=D.isProvider?D:A.get(D);if(D){D.un("data",B.onProviderData,B);E=D.id;if(C[E]){C[E].destroy();delete C[E]}A.remove(D);return D}return null},addTransaction:function(A){this.transactions.add(A);return A},removeTransaction:function(B){var A=this;B=A.getTransaction(B);A.transactions.remove(B);return B},getTransaction:function(A){return typeof A==="object"?A:this.transactions.get(A)},onProviderData:function(E,D){var C=this,B,A;if(Ext.isArray(D)){for(B=0,A=D.length;B<A;++B){C.onProviderData(E,D[B])}return }if(D.name&&D.name!=="event"&&D.name!=="exception"){C.fireEvent(D.name,D)}else{if(D.status===false){C.fireEvent("exception",D)}}C.fireEvent("event",D,E)},parseMethod:function(D){var F=Ext.global,C=0,B,E,A;if(Ext.isFunction(D)){B=D}else{if(Ext.isString(D)){B=this.remotingMethods[D];if(!B){E=D.split(".");A=E.length;while(F&&C<A){F=F[E[C]];++C}B=Ext.isFunction(F)?F:null}}}return B||null},privates:{addProviderClass:function(B,A){this.providerClasses[B]=A},onApiLoadSuccess:function(options){var me=this,url=options.url,varName=options.varName,api,provider,error;try{api=Ext.apply(options.config,eval(varName));provider=me.addProvider(api)}catch(e){error=e+""}if(error){me.fireEventArgs("providerloaderror",[url,error]);Ext.callback(options.callback,options.scope,[url,error])}else{me.fireEventArgs("providerload",[url,provider]);Ext.callback(options.callback,options.scope,[url,provider])}},onApiLoadFailure:function(C){var B=C.url,A;A=Ext.String.format(this.apiNotFoundError,B);this.fireEventArgs("providerloaderror",[B,A]);Ext.callback(C.callback,C.scope,[B,A])},registerMethod:function(A,B){this.remotingMethods[A]=B},clearAllMethods:function(){this.remotingMethods={}}}},function(){Ext.Direct=Ext.direct.Manager})