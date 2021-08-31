Ext.define("Ext.fx.runner.CssTransition",{extend:"Ext.fx.runner.Css",requires:["Ext.AnimationQueue"],alternateClassName:"Ext.Animator",singleton:true,listenersAttached:false,constructor:function(){this.runningAnimationsData={};return this.callParent(arguments)},attachListeners:function(){this.listenersAttached=true;Ext.getWin().on("transitionend","onTransitionEnd",this)},onTransitionEnd:function(B){var A=B.target,C=A.id;if(C&&this.runningAnimationsData.hasOwnProperty(C)){this.refreshRunningAnimationsData(Ext.get(A),[B.browserEvent.propertyName])}},onAnimationEnd:function(G,F,D,I,M){var C=G.getId(),J=this.runningAnimationsData[C],N={},L={},B,H,E,K,A;D.un("stop","onAnimationStop",this);if(J){B=J.nameMap}N[C]=L;if(F.onBeforeEnd){F.onBeforeEnd.call(F.scope||this,G,I)}D.fireEvent("animationbeforeend",D,G,I);this.fireEvent("animationbeforeend",this,D,G,I);if(M||(!I&&!F.preserveEndState)){H=F.toPropertyNames;for(E=0,K=H.length;E<K;E++){A=H[E];if(B&&!B.hasOwnProperty(A)){L[A]=null}}}if(F.after){Ext.merge(L,F.after)}this.applyStyles(N);if(F.onEnd){F.onEnd.call(F.scope||this,G,I)}D.fireEvent("animationend",D,G,I);this.fireEvent("animationend",this,D,G,I);Ext.AnimationQueue.stop(Ext.emptyFn,D)},onAllAnimationsEnd:function(B){var C=B.getId(),A={};delete this.runningAnimationsData[C];A[C]={"transition-property":null,"transition-duration":null,"transition-timing-function":null,"transition-delay":null};this.applyStyles(A);this.fireEvent("animationallend",this,B)},hasRunningAnimations:function(A){var C=A.getId(),B=this.runningAnimationsData;return B.hasOwnProperty(C)&&B[C].sessions.length>0},refreshRunningAnimationsData:function(D,I,R,N){var G=D.getId(),O=this.runningAnimationsData,A=O[G];if(!A){return }var K=A.nameMap,Q=A.nameList,B=A.sessions,F,H,E,S,J,C,P,M,L=false;R=Boolean(R);N=Boolean(N);if(!B){return this}F=B.length;if(F===0){return this}if(N){A.nameMap={};Q.length=0;for(J=0;J<F;J++){C=B[J];this.onAnimationEnd(D,C.data,C.animation,R,N)}B.length=0}else{for(J=0;J<F;J++){C=B[J];P=C.map;M=C.list;for(H=0,E=I.length;H<E;H++){S=I[H];if(P[S]){delete P[S];Ext.Array.remove(M,S);C.length--;if(--K[S]==0){delete K[S];Ext.Array.remove(Q,S)}}}if(C.length==0){B.splice(J,1);J--;F--;L=true;this.onAnimationEnd(D,C.data,C.animation,R)}}}if(!N&&!R&&B.length==0&&L){this.onAllAnimationsEnd(D)}},getRunningData:function(B){var A=this.runningAnimationsData;if(!A.hasOwnProperty(B)){A[B]={nameMap:{},nameList:[],sessions:[]}}return A[B]},getTestElement:function(){var C=this.testElement,B,D,A;if(!C){B=document.createElement("iframe");B.setAttribute("data-sticky",true);B.setAttribute("tabIndex",-1);A=B.style;A.setProperty("visibility","hidden","important");A.setProperty("width","0px","important");A.setProperty("height","0px","important");A.setProperty("position","absolute","important");A.setProperty("border","0px","important");A.setProperty("zIndex","-1000","important");document.body.appendChild(B);D=B.contentDocument;D.open();D.writeln("</body>");D.close();this.testElement=C=D.createElement("div");C.style.setProperty("position","absolute","important");D.body.appendChild(C);this.testElementComputedStyle=window.getComputedStyle(C)}return C},getCssStyleValue:function(B,E){var D=this.getTestElement(),A=this.testElementComputedStyle,C=D.style;C.setProperty(B,E);if(Ext.browser.is.Firefox){D.offsetHeight}E=A.getPropertyValue(B);C.removeProperty(B);return E},run:function(O){var e=this,H=e.lengthProperties,W={},d={},f={},D,R,X,E,T,h,U,P,Q,A,L,Z,Y,N,a,J,S,G,b,g,I,F,V,M,C,c,B,K;if(!e.listenersAttached){e.attachListeners()}O=Ext.Array.from(O);for(Z=0,N=O.length;Z<N;Z++){a=O[Z];a=Ext.factory(a,Ext.fx.Animation);D=a.getElement();Ext.AnimationQueue.start(Ext.emptyFn,a);G=window.getComputedStyle(D.dom);R=D.getId();f=Ext.merge({},a.getData());if(a.onBeforeStart){a.onBeforeStart.call(a.scope||e,D)}a.fireEvent("animationstart",a);e.fireEvent("animationstart",e,a);f[R]=f;T=f.before;X=f.from;E=f.to;f.fromPropertyNames=h=[];f.toPropertyNames=U=[];for(g in E){if(E.hasOwnProperty(g)){E[g]=I=e.formatValue(E[g],g);b=e.formatName(g);M=H.hasOwnProperty(g);if(!M){I=e.getCssStyleValue(b,I)}if(X.hasOwnProperty(g)){X[g]=V=e.formatValue(X[g],g);if(!M){V=e.getCssStyleValue(b,V)}if(I!==V){h.push(b);U.push(b)}}else{F=G.getPropertyValue(b);if(I!==F){U.push(b)}}}}J=U.length;if(J===0){e.onAnimationEnd(D,f,a);continue}A=e.getRunningData(R);B=A.sessions;if(B.length>0){e.refreshRunningAnimationsData(D,Ext.Array.merge(h,U),true,f.replacePrevious)}C=A.nameMap;c=A.nameList;S={};for(Y=0;Y<J;Y++){g=U[Y];S[g]=true;if(!C.hasOwnProperty(g)){C[g]=1;c.push(g)}else{C[g]++}}K={element:D,map:S,list:U.slice(),length:J,data:f,animation:a};B.push(K);a.on("stop","onAnimationStop",e);L=Ext.apply({},T);Ext.apply(L,X);if(c.length>0){h=Ext.Array.difference(c,h);U=Ext.Array.merge(h,U);L["transition-property"]=h}W[R]=L;d[R]=Ext.apply({},E);d[R]["transition-property"]=U;d[R]["transition-duration"]=f.duration;d[R]["transition-timing-function"]=f.easing;d[R]["transition-delay"]=f.delay;a.startTime=Date.now()}Q=e.$className;e.applyStyles(W);P=function(i){if(i.data===Q&&i.source===window){window.removeEventListener("message",P,false);e.applyStyles(d)}};if(window.requestAnimationFrame){window.requestAnimationFrame(function(){window.addEventListener("message",P,false);window.postMessage(Q,"*")})}else{Ext.defer(function(){window.addEventListener("message",P,false);window.postMessage(Q,"*")},1)}},onAnimationStop:function(D){var F=this.runningAnimationsData,H,A,G,B,C,E;for(H in F){if(F.hasOwnProperty(H)){A=F[H];G=A.sessions;for(B=0,C=G.length;B<C;B++){E=G[B];if(E.animation===D){this.refreshRunningAnimationsData(E.element,E.list.slice(),false)}}}}}})