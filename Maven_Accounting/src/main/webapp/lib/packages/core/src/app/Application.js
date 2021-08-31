Ext.define("Ext.app.Application",{extend:"Ext.app.Controller",requires:["Ext.util.History","Ext.util.MixedCollection"],isApplication:true,scope:undefined,namespaces:[],paths:null,config:{name:"",appProperty:"app",profiles:[],currentProfile:null,mainView:{$value:null,lazy:true},defaultToken:null,glyphFontFamily:null},onClassExtended:function(H,C,G){var B=Ext.app.Controller,D=H.prototype,J=[],E,I,A,F;A=C.name||H.superclass.name;if(A){C.$namespace=A;Ext.app.addNamespaces(A)}if(C.namespaces){Ext.app.addNamespaces(C.namespaces)}if(C["paths processed"]){delete C["paths processed"]}else{Ext.app.setupPaths(A,("appFolder" in C)?C.appFolder:H.superclass.appFolder,C.paths)}B.processDependencies(D,J,A,"profile",C.profiles);D.getDependencies(H,C,J);if(J.length){E=G.onBeforeCreated;G.onBeforeCreated=function(K,M){var L=Ext.Array.clone(arguments);Ext.require(J,function(){return E.apply(this,L)})}}},getDependencies:Ext.emptyFn,constructor:function(A){var B=this;Ext.app.route.Router.application=B;B.callParent(arguments);if(Ext.isEmpty(B.getName())){Ext.raise("[Ext.app.Application] Name property is required")}B.doInit(B);B.initNamespace();Ext.on("appupdate",B.onAppUpdate,B,{single:true});Ext.Loader.setConfig({enabled:true});this.onProfilesReady()},onAppUpdate:Ext.emptyFn,onProfilesReady:function(){var E=this,B=E.getProfiles(),D=B.length,F,C,A;for(C=0;C<D;C++){A=Ext.create(B[C],{application:E});if(A.isActive()&&!F){F=A;E.setCurrentProfile(F)}}if(F){F.init()}E.initControllers();E.onBeforeLaunch();E.finishInitControllers()},initNamespace:function(){var C=this,A=C.getAppProperty(),B;B=Ext.namespace(C.getName());if(B){B.getApplication=function(){return C};if(A){if(!B[A]){B[A]=C}else{if(B[A]!==C){Ext.log.warn("An existing reference is being overwritten for "+name+"."+A+". See the appProperty config.")}}}}},initControllers:function(){var D=this,E=Ext.Array.from(D.controllers),B=D.getCurrentProfile(),A,C;D.controllers=new Ext.util.MixedCollection();for(A=0,C=E.length;A<C;A++){D.getController(E[A])}if(B){E=B.getControllers();for(A=0,C=E.length;A<C;A++){D.getController(E[A])}}},finishInitControllers:function(){var C=this,D,B,A;D=C.controllers.getRange();for(B=0,A=D.length;B<A;B++){D[B].finishInit(C)}},launch:Ext.emptyFn,onBeforeLaunch:function(){var H=this,D=Ext.util.History,A=H.getDefaultToken(),F=H.getCurrentProfile(),B,G,I,E,C;H.initMainView();if(F){F.launch()}H.launch.call(H.scope||H);H.launched=true;H.fireEvent("launch",H);B=H.controllers.items;I=B.length;for(G=0;G<I;G++){E=B[G];E.onLaunch(H)}if(!D.ready){D.init()}C=D.getToken();if(C||C===A){Ext.app.route.Router.onStateChange(C)}else{if(A){D.add(A)}}if(Ext.Microloader&&Ext.Microloader.appUpdate&&Ext.Microloader.appUpdate.updated){Ext.Microloader.fireAppUpdate()}Ext.defer(Ext.ClassManager.clearNamespaceCache,2000,Ext.ClassManager)},getModuleClassName:function(A,B){return Ext.app.Controller.getFullName(A,B,this.getName()).absoluteName},initMainView:function(){var C=this,B=C.getCurrentProfile(),A;if(B){A=B.getMainView()}if(A){C.setMainView(A)}else{C.getMainView()}},applyMainView:function(B){var A=this.getView(B);return A.create()},createController:function(A){return this.getController(A)},destroyController:function(A){if(typeof A==="string"){A=this.getController(A,true)}Ext.destroy(A)},getController:function(B,A){var I=this,C=I.controllers,G,E,F,D,H,J;E=C.get(B);if(!E){J=C.items;for(D=0,F=J.length;D<F;++D){H=J[D];G=H.getModuleClassName();if(G&&G===B){E=H;break}}}if(!E&&!A){G=I.getModuleClassName(B,"controller");E=Ext.create(G,{application:I,moduleClassName:B});C.add(E);if(I._initialized){E.doInit(I)}}return E},unregister:function(A){this.controllers.remove(A)},getApplication:function(){return this},destroy:function(A){var C=this,E=C.controllers,B=Ext.namespace(C.getName()),D=C.getAppProperty();Ext.destroy(C.viewport);if(E){E.each(function(F){F.destroy(A,true)})}C.controllers=null;C.callParent([A,true]);if(B&&B[D]===C){delete B[D]}},updateGlyphFontFamily:function(A){Ext.setGlyphFontFamily(A)},applyProfiles:function(A){var B=this;return Ext.Array.map(A,function(C){return B.getModuleClassName(C,"profile")})}})