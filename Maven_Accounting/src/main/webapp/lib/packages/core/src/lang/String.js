Ext.String=(function(){var H=/^[\x09\x0a\x0b\x0c\x0d\x20\xa0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000]+|[\x09\x0a\x0b\x0c\x0d\x20\xa0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000]+$/g,L=/('|\\)/g,B=/([-.*+?\^${}()|\[\]\/\\])/g,N=/^\s+|\s+$/g,I=/\s+/,K=/(^[^a-z]*|[^\w])/gi,E,A,G,D,F=function(P,O){return E[O]},J=function(P,O){return(O in A)?A[O]:String.fromCharCode(parseInt(O.substr(2),10))},C=function(P,O){if(P===null||P===undefined||O===null||O===undefined){return false}return O.length<=P.length},M;return M={insert:function(Q,R,P){if(!Q){return R}if(!R){return Q}var O=Q.length;if(!P&&P!==0){P=O}if(P<0){P*=-1;if(P>=O){P=0}else{P=O-P}}if(P===0){Q=R+Q}else{if(P>=Q.length){Q+=R}else{Q=Q.substr(0,P)+R+Q.substr(P)}}return Q},startsWith:function(Q,R,P){var O=C(Q,R);if(O){if(P){Q=Q.toLowerCase();R=R.toLowerCase()}O=Q.lastIndexOf(R,0)===0}return O},endsWith:function(R,P,Q){var O=C(R,P);if(O){if(Q){R=R.toLowerCase();P=P.toLowerCase()}O=R.indexOf(P,R.length-P.length)!==-1}return O},createVarName:function(O){return O.replace(K,"")},htmlEncode:function(O){return(!O)?O:String(O).replace(G,F)},htmlDecode:function(O){return(!O)?O:String(O).replace(D,J)},hasHtmlCharacters:function(O){return G.test(O)},addCharacterEntities:function(P){var O=[],S=[],Q,R;for(Q in P){R=P[Q];A[Q]=R;E[R]=Q;O.push(R);S.push(Q)}G=new RegExp("("+O.join("|")+")","g");D=new RegExp("("+S.join("|")+"|&#[0-9]{1,5};)","g")},resetCharacterEntities:function(){E={};A={};this.addCharacterEntities({"&amp;":"&","&gt;":">","&lt;":"<","&quot;":'"',"&#39;":"'"})},urlAppend:function(P,O){if(!Ext.isEmpty(O)){return P+(P.indexOf("?")===-1?"?":"&")+O}return P},trim:function(O){if(O){O=O.replace(H,"")}return O||""},capitalize:function(O){if(O){O=O.charAt(0).toUpperCase()+O.substr(1)}return O||""},uncapitalize:function(O){if(O){O=O.charAt(0).toLowerCase()+O.substr(1)}return O||""},ellipsis:function(Q,P,R){if(Q&&Q.length>P){if(R){var S=Q.substr(0,P-2),O=Math.max(S.lastIndexOf(" "),S.lastIndexOf("."),S.lastIndexOf("!"),S.lastIndexOf("?"));if(O!==-1&&O>=(P-15)){return S.substr(0,O)+"..."}}return Q.substr(0,P-3)+"..."}return Q},escapeRegex:function(O){return O.replace(B,"\\$1")},createRegex:function(S,R,P,O){var Q=S;if(S!=null&&!S.exec){Q=M.escapeRegex(String(S));if(R!==false){Q="^"+Q}if(P!==false){Q+="$"}Q=new RegExp(Q,(O!==false)?"i":"")}return Q},escape:function(O){return O.replace(L,"\\$1")},toggle:function(P,Q,O){return P===Q?O:Q},leftPad:function(P,Q,R){var O=String(P);R=R||" ";while(O.length<Q){O=R+O}return O},repeat:function(S,R,P){if(R<1){R=0}for(var O=[],Q=R;Q--;){O.push(S)}return O.join(P||"")},splitWords:function(O){if(O&&typeof O=="string"){return O.replace(N,"").split(I)}return O||[]}}}());Ext.String.resetCharacterEntities();Ext.htmlEncode=Ext.String.htmlEncode;Ext.htmlDecode=Ext.String.htmlDecode;Ext.urlAppend=Ext.String.urlAppend