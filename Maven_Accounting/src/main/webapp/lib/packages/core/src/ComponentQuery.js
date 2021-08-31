Ext.define("Ext.ComponentQuery",{singleton:true,requires:["Ext.ComponentManager","Ext.util.Operators","Ext.util.LruCache"]},function(){var F=this,R=Ext.util.Operators,J=/(\d*)n\+?(\d*)/,E=/\D/,L=/^(\s)+/,K=/\\(.)/g,M=new Ext.util.LruCache({maxSize:100}),N=["var r = [],","i = 0,","it = items,","l = it.length,","c;","for (; i < l; i++) {","c = it[i];","if (c.{0}) {","r.push(c);","}","}","return r;"].join(""),O=function(T,S){return S.method.apply(this,[T].concat(S.args))},A=function(U,Y){var S=[],V=0,X=U.length,W,T=Y!==">";for(;V<X;V++){W=U[V];if(W.getRefItems){S=S.concat(W.getRefItems(T))}}return S},G=function(T){var S=[],U=0,W=T.length,V;for(;U<W;U++){V=T[U];while(!!(V=V.getRefOwner())){S.push(V)}}return S},D=function(T,Y,X){if(Y==="*"){return T.slice()}else{var S=[],U=0,W=T.length,V;for(;U<W;U++){V=T[U];if(V.isXType(Y,X)){S.push(V)}}return S}},B=function(b,c,V,U){var f=[],a=0,T=b.length,e,X,d,S,Z,Y,W;if(c.charAt(0)==="@"){e=true;c=c.substr(1)}if(c.charAt(0)==="?"){e=true;X=true;c=c.substr(1)}for(;a<T;a++){d=b[a];W=d.getConfigurator&&d.self.$config.configs[c];if(W){S=d[W.names.get]()}else{if(e&&!d.hasOwnProperty(c)){continue}else{S=d[c]}}if(X){f.push(d)}else{if(V==="~="){if(S){if(!Ext.isArray(S)){S=S.split(" ")}for(Z=0,Y=S.length;Z<Y;Z++){if(R[V](Ext.coerce(S[Z],U),U)){f.push(d);break}}}}else{if(V==="/="){if(S!=null&&U.test(S)){f.push(d)}}else{if(!U?!!d[c]:R[V](Ext.coerce(S,U),U)){f.push(d)}}}}}return f},H=function(T,X){var S=[],U=0,W=T.length,V;for(;U<W;U++){V=T[U];if(V.getItemId()===X){S.push(V)}}return S},Q=function(S,T,U){return F.pseudos[T](S,U)},I=/^(\s?([>\^])\s?|\s|$)/,P=/^(#)?((?:\\\.|[\w\-])+|\*)(?:\((true|false)\))?/,C=[{re:/^\.((?:\\\.|[\w\-])+)(?:\((true|false)\))?/,method:D,argTransform:function(T){var S=T[0];Ext.log.warn('"'+S+'" ComponentQuery selector style is deprecated, use "'+S.replace(/^\./,"")+'" without the leading dot instead');if(T[1]!==undefined){T[1]=T[1].replace(K,"$1")}return T.slice(1)}},{re:/^(?:\[((?:[@?$])?[\w\-]*)\s*(?:([\^$*~%!\/]?=)\s*(['"])?((?:\\\]|.)*?)\3)?(?!\\)\])/,method:B,argTransform:function(Y){var W=Y[0],a=Y[1],T=Y[2],S=Y[4],V;if(S!==undefined){S=S.replace(K,"$1");var Z=Ext.String.format,U="ComponentQuery selector '{0}' has an unescaped ({1}) character at the {2} of the attribute value pattern. Usually that indicates an error where the opening quote is not followed by the closing quote. If you need to match a ({1}) character at the {2} of the attribute value, escape the quote character in your pattern: (\\{1})",X;if(X=/^(['"]).*?[^'"]$/.exec(S)){Ext.log.warn(Z(U,W,X[1],"beginning"))}else{if(X=/^[^'"].*?(['"])$/.exec(S)){Ext.log.warn(Z(U,W,X[1],"end"))}}}if(T==="/="){V=M.get(S);if(V){S=V}else{S=M.add(S,new RegExp(S))}}return[a,T,S]}},{re:/^#((?:\\\.|[\w\-])+)/,method:H},{re:/^\:([\w\-]+)(?:\(((?:\{[^\}]+\})|(?:(?!\{)[^\s>\/]*?(?!\})))\))?/,method:Q,argTransform:function(S){if(S[2]!==undefined){S[2]=S[2].replace(K,"$1")}return S.slice(1)}},{re:/^(?:\{([^\}]+)\})/,method:N}];F.Query=Ext.extend(Object,{constructor:function(S){S=S||{};Ext.apply(this,S)},execute:function(U){var V=this.operations,T=[],X,W,S;for(W=0,S=V.length;W<S;W++){X=V[W];T=T.concat(this._execute(U,X))}return T},_execute:function(T,V){var W=0,X=V.length,U,S;if(!T){S=Ext.ComponentManager.getAll()}else{if(Ext.isIterable(T)){S=T}else{if(T.isMixedCollection){S=T.items}}}for(;W<X;W++){U=V[W];if(U.mode==="^"){S=G(S||[T])}else{if(U.mode){S=A(S||[T],U.mode)}else{S=O(S||A([T]),U)}}if(W===X-1){return S}}return[]},is:function(V){var U=this.operations,T=false,S=U.length,X,W;if(S===0){return true}for(W=0;W<S;W++){X=U[W];T=this._is(V,X);if(T){return T}}return false},_is:function(a,S){var Y=S.length,T=[a],U,W,V,X,Z,b;for(W=Y-1;W>=0;--W){U=S[W];X=U.mode;if(X){if(X==="^"){T=A(T," ")}else{if(X===">"){Z=[];for(V=0,Y=T.length;V<Y;++V){b=T[V].getRefOwner();if(b){Z.push(b)}}T=Z}else{T=G(T)}}if(T.length===0){return false}}else{T=O(T,U);if(T.length===0){return false}}}return true},getMatches:function(V,T){var S=T.length,U;for(U=0;U<S;++U){V=O(V,T[U]);if(V.length===0){break}}return V},isMultiMatch:function(){return this.operations.length>1}});Ext.apply(F,{cache:new Ext.util.LruCache({maxSize:100}),pseudos:{not:function(Y,S){var W=0,X=Y.length,V=[],U=-1,T;for(;W<X;++W){T=Y[W];if(!F.is(T,S)){V[++U]=T}}return V},first:function(T){var S=[];if(T.length>0){S.push(T[0])}return S},last:function(U){var S=U.length,T=[];if(S>0){T.push(U[S-1])}return T},focusable:function(T){var S=T.length,V=[],U=0,W;for(;U<S;U++){W=T[U];if(W.isFocusable&&W.isFocusable()){V.push(W)}}return V},"nth-child":function(Y,Z){var b=[],T=J.exec(Z==="even"&&"2n"||Z==="odd"&&"2n+1"||!E.test(Z)&&"n+"+Z||Z),W=(T[1]||1)-0,X=T[2]-0,V,S,U;for(V=0;S=Y[V];V++){U=V+1;if(W===1){if(X===0||U===X){b.push(S)}}else{if((U+X)%W===0){b.push(S)}}}return b},scrollable:function(T){var S=T.length,V=[],U=0,W;for(;U<S;U++){W=T[U];if(W.scrollable||W._scrollable){V.push(W)}}return V}},query:function(S,Z){if(!S){return Ext.ComponentManager.all.getArray()}var U=[],a=[],X={},W=F.cache.get(S),V,Y,T;if(!W){W=F.cache.add(S,F.parse(S))}U=W.execute(Z);if(W.isMultiMatch()){V=U.length;for(T=0;T<V;T++){Y=U[T];if(!X[Y.id]){a.push(Y);X[Y.id]=true}}U=a}return U},visitPreOrder:function(S,U,W,V,T){F._visit(true,S,U,W,V,T)},visitPostOrder:function(S,U,W,V,T){F._visit(false,S,U,W,V,T)},_visit:function(c,T,b,a,d,X){var Z=F.cache.get(T),W=[b],S,Y=0,V,U;if(!Z){Z=F.cache.add(T,F.parse(T))}U=Z.is(b);if(b.getRefItems){S=b.getRefItems();Y=S.length}if(X){Ext.Array.push(W,X)}if(c){if(U){if(a.apply(d||b,W)===false){return false}}}for(V=0;V<Y;V++){if(F._visit.call(F,c,T,S[V],a,d,X)===false){return false}}if(!c){if(U){if(a.apply(d||b,W)===false){return false}}}},is:function(T,S){if(!S){return true}var U=F.cache.get(S);if(!U){U=F.cache.add(S,F.parse(S))}return U.is(T)},parse:function(T){var U=[],W,X,V,S;W=Ext.splitAndUnescape(T,",");for(V=0,S=W.length;V<S;V++){X=Ext.String.trim(W[V]);if(X===""){Ext.raise('Invalid ComponentQuery selector: ""')}U.push(F._parse(X))}return new F.Query({operations:U})},_parse:function(Y){var T=[],W=Ext.String.trim,U=C.length,d,Z,X,e,f,g,V,a,b,S,c;while(Y&&d!==Y){d=Y;Z=Y.match(P);if(Z){e=Z[1];X=W(Z[2]).replace(K,"$1");if(e==="#"){T.push({method:H,args:[X]})}else{T.push({method:D,args:[X,Boolean(Z[3])]})}Y=Y.replace(Z[0],"").replace(L,"$1")}while(!(f=Y.match(I))){for(a=0;Y&&a<U;a++){b=C[a];g=Y.match(b.re);S=b.method;V=b.argTransform;if(g){if(V){c=V(g)}else{c=g.slice(1)}T.push({method:Ext.isString(b.method)?Ext.functionFactory("items",Ext.String.format.apply(Ext.String,[S].concat(g.slice(1)))):b.method,args:c});Y=Y.replace(g[0],"").replace(L,"$1");break}if(a===(U-1)){Ext.raise('Invalid ComponentQuery selector: "'+arguments[0]+'"')}}}if(f[1]){T.push({mode:f[2]||f[1]});Y=Y.replace(f[0],"").replace(L,"")}}return T}});Ext.all=function(){return F.query.apply(F,arguments)};Ext.first=function(){var S=F.query.apply(F,arguments);return(S&&S[0])||null}})