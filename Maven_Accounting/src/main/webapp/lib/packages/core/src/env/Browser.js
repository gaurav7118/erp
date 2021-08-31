(Ext.env||(Ext.env={})).Browser=function(S,M){var T=this,A=Ext.Boot.browserPrefixes,C=Ext.Boot.browserNames,B=T.enginePrefixes,I=T.engineNames,P=S.match(new RegExp("((?:"+Ext.Object.getValues(A).join(")|(?:")+"))([\\w\\._]+)")),G=S.match(new RegExp("((?:"+Ext.Object.getValues(B).join(")|(?:")+"))([\\w\\._]+)")),O=C.other,R=I.other,K="",F="",D="",H=false,N,L,J,U,Q;T.userAgent=S;if(/Edge\//.test(S)){P=S.match(/(Edge\/)([\w.]+)/)}if(P){O=C[Ext.Object.getKey(A,P[1])];if(O==="Safari"&&/^Opera/.test(S)){O="Opera"}K=new Ext.Version(P[2])}if(G){R=I[Ext.Object.getKey(B,G[1])];F=new Ext.Version(G[2])}if(R==="Trident"&&O!=="IE"){O="IE";var E=S.match(/.*rv:(\d+.\d+)/);if(E&&E.length){E=E[1];K=new Ext.Version(E)}}if(O&&K){Ext.setVersion(O,K)}if(S.match(/FB/)&&O==="Other"){O=C.safari;R=I.webkit}if(S.match(/Android.*Chrome/g)){O="ChromeMobile"}if(S.match(/OPR/)){O="Opera";P=S.match(/OPR\/(\d+.\d+)/);K=new Ext.Version(P[1])}Ext.apply(this,{engineName:R,engineVersion:F,name:O,version:K});this.setFlag(O,true,M);if(K){D=K.getMajor()||"";if(T.is.IE){D=parseInt(D,10);J=document.documentMode;if(J===7||(D===7&&J!==8&&J!==9&&J!==10)){D=7}else{if(J===8||(D===8&&J!==8&&J!==9&&J!==10)){D=8}else{if(J===9||(D===9&&J!==7&&J!==8&&J!==10)){D=9}else{if(J===10||(D===10&&J!==7&&J!==8&&J!==9)){D=10}else{if(J===11||(D===11&&J!==7&&J!==8&&J!==9&&J!==10)){D=11}}}}}Q=Math.max(D,Ext.Boot.maxIEVersion);for(N=7;N<=Q;++N){L="isIE"+N;if(D<=N){Ext[L+"m"]=true}if(D===N){Ext[L]=true}if(D>=N){Ext[L+"p"]=true}}}if(T.is.Opera&&parseInt(D,10)<=12){Ext.isOpera12m=true}Ext.chromeVersion=Ext.isChrome?D:0;Ext.firefoxVersion=Ext.isFirefox?D:0;Ext.ieVersion=Ext.isIE?D:0;Ext.operaVersion=Ext.isOpera?D:0;Ext.safariVersion=Ext.isSafari?D:0;Ext.webKitVersion=Ext.isWebKit?D:0;this.setFlag(O+D,true,M);this.setFlag(O+K.getShortVersion())}for(N in C){if(C.hasOwnProperty(N)){U=C[N];this.setFlag(U,O===U)}}this.setFlag(U);if(F){this.setFlag(R+(F.getMajor()||""));this.setFlag(R+F.getShortVersion())}for(N in I){if(I.hasOwnProperty(N)){U=I[N];this.setFlag(U,R===U,M)}}this.setFlag("Standalone",!!navigator.standalone);this.setFlag("Ripple",!!document.getElementById("tinyhippos-injected")&&!Ext.isEmpty(window.top.ripple));this.setFlag("WebWorks",!!window.blackberry);if(window.PhoneGap!==undefined||window.Cordova!==undefined||window.cordova!==undefined){H=true;this.setFlag("PhoneGap");this.setFlag("Cordova")}if(/(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)(?!.*FBAN)/i.test(S)){H=true}this.setFlag("WebView",H);this.isStrict=Ext.isStrict=document.compatMode==="CSS1Compat";this.isSecure=Ext.isSecure;this.identity=O+D+(this.isStrict?"Strict":"Quirks")};Ext.env.Browser.prototype={constructor:Ext.env.Browser,engineNames:{webkit:"WebKit",gecko:"Gecko",presto:"Presto",trident:"Trident",other:"Other"},enginePrefixes:{webkit:"AppleWebKit/",gecko:"Gecko/",presto:"Presto/",trident:"Trident/"},styleDashPrefixes:{WebKit:"-webkit-",Gecko:"-moz-",Trident:"-ms-",Presto:"-o-",Other:""},stylePrefixes:{WebKit:"Webkit",Gecko:"Moz",Trident:"ms",Presto:"O",Other:""},propertyPrefixes:{WebKit:"webkit",Gecko:"moz",Trident:"ms",Presto:"o",Other:""},is:function(A){return !!this.is[A]},name:null,version:null,engineName:null,engineVersion:null,setFlag:function(A,C,B){if(C===undefined){C=true}this.is[A]=C;this.is[A.toLowerCase()]=C;if(B){Ext["is"+A]=C}return this},getStyleDashPrefix:function(){return this.styleDashPrefixes[this.engineName]},getStylePrefix:function(){return this.stylePrefixes[this.engineName]},getVendorProperyName:function(A){var B=this.propertyPrefixes[this.engineName];if(B.length>0){return B+Ext.String.capitalize(A)}return A},getPreferredTranslationMethod:function(A){if(typeof A==="object"&&"translationMethod" in A&&A.translationMethod!=="auto"){return A.translationMethod}else{return"csstransform"}}};(function(A){Ext.browser=new Ext.env.Browser(A,true);Ext.userAgent=A.toLowerCase();Ext.SSL_SECURE_URL=Ext.isSecure&&Ext.isIE?"javascript:''":"about:blank"}(Ext.global.navigator.userAgent))