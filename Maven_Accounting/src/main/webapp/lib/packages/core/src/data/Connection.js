Ext.define("Ext.data.Connection",{mixins:{observable:"Ext.mixin.Observable"},requires:["Ext.data.request.Ajax","Ext.data.request.Form","Ext.data.flash.BinaryXhr","Ext.Deferred"],statics:{requestId:0},enctypeRe:/multipart\/form-data/i,config:{url:null,async:true,username:"",password:"",disableCaching:true,withCredentials:false,binary:false,cors:false,isXdr:false,defaultXdrContentType:"text/plain",disableCachingParam:"_dc",timeout:30000,extraParams:null,autoAbort:false,method:null,defaultHeaders:null,defaultPostHeader:"application/x-www-form-urlencoded; charset=UTF-8",useDefaultXhrHeader:true,defaultXhrHeader:"XMLHttpRequest"},constructor:function(A){this.mixins.observable.constructor.call(this,A);this.requests={}},request:function(B){B=B||{};var D=this,A,C;if(D.fireEvent("beforerequest",D,B)!==false){A=D.setOptions(B,B.scope||Ext.global);C=D.createRequest(B,A);return C.start(A.data)}Ext.callback(B.callback,B.scope,[B,undefined,undefined]);return Ext.Deferred.rejected([B,undefined,undefined])},createRequest:function(B,A){var E=this,C=B.type||A.type,D;if(!C){C=E.isFormUpload(B)?"form":"ajax"}if(B.autoAbort||E.getAutoAbort()){E.abort()}D=Ext.Factory.request({type:C,owner:E,options:B,requestOptions:A,ownerConfig:E.getConfig()});E.requests[D.id]=D;E.latestId=D.id;return D},isFormUpload:function(A){var B=this.getForm(A);if(B){return A.isUpload||this.enctypeRe.test(B.getAttribute("enctype"))}return false},getForm:function(A){return Ext.getDom(A.form)},setOptions:function(L,K){var I=this,E=L.params||{},H=I.getExtraParams(),D=L.urlParams,C=L.url||I.getUrl(),G=L.cors,J=L.jsonData,B,A,F;if(G!==undefined){I.setCors(G)}if(Ext.isFunction(E)){E=E.call(K,L)}if(Ext.isFunction(C)){C=C.call(K,L)}C=this.setupUrl(L,C);if(!C){Ext.raise({options:L,msg:"No URL specified"})}F=L.rawData||L.binaryData||L.xmlData||J||null;if(J&&!Ext.isPrimitive(J)){F=Ext.encode(F)}if(L.binaryData){if(!Ext.isArray(L.binaryData)){Ext.log.warn("Binary submission data must be an array of byte values! Instead got "+typeof (L.binaryData))}if(I.nativeBinaryPostSupport()){F=(new Uint8Array(L.binaryData));if((Ext.isChrome&&Ext.chromeVersion<22)||Ext.isSafari||Ext.isGecko){F=F.buffer}}}if(Ext.isObject(E)){E=Ext.Object.toQueryString(E)}if(Ext.isObject(H)){H=Ext.Object.toQueryString(H)}E=E+((H)?((E)?"&":"")+H:"");D=Ext.isObject(D)?Ext.Object.toQueryString(D):D;E=this.setupParams(L,E);B=(L.method||I.getMethod()||((E||F)?"POST":"GET")).toUpperCase();this.setupMethod(L,B);A=L.disableCaching!==false?(L.disableCaching||I.getDisableCaching()):false;if(B==="GET"&&A){C=Ext.urlAppend(C,(L.disableCachingParam||I.getDisableCachingParam())+"="+(new Date().getTime()))}if((B=="GET"||F)&&E){C=Ext.urlAppend(C,E);E=null}if(D){C=Ext.urlAppend(C,D)}return{url:C,method:B,data:F||E||null}},setupUrl:function(B,A){var C=this.getForm(B);if(C){A=A||C.action}return A},setupParams:function(A,D){var C=this.getForm(A),B;if(C&&!this.isFormUpload(A)){B=Ext.Element.serializeForm(C);D=D?(D+"&"+B):B}return D},setupMethod:function(A,B){if(this.isFormUpload(A)){return"POST"}return B},isLoading:function(A){if(!A){A=this.getLatest()}return A?A.isLoading():false},abort:function(A){if(!A){A=this.getLatest()}if(A&&A.isLoading()){A.abort()}},abortAll:function(){var B=this.requests,A;for(A in B){this.abort(B[A])}},getLatest:function(){var B=this.latestId,A;if(B){A=this.requests[B]}return A||null},clearTimeout:function(A){if(!A){A=this.getLatest()}if(A){A.clearTimer()}},onRequestComplete:function(A){delete this.requests[A.id]},nativeBinaryPostSupport:function(){return Ext.isChrome||(Ext.isSafari&&Ext.isDefined(window.Uint8Array))||(Ext.isGecko&&Ext.isDefined(window.Uint8Array))}})