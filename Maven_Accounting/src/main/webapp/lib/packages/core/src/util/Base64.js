Ext.define("Ext.util.Base64",{singleton:true,_str:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(E){var D=this;var A="",L,J,H,K,I,G,F,B=0;E=D._utf8_encode(E);var C=E.length;while(B<C){L=E.charCodeAt(B++);J=E.charCodeAt(B++);H=E.charCodeAt(B++);K=L>>2;I=((L&3)<<4)|(J>>4);G=((J&15)<<2)|(H>>6);F=H&63;if(isNaN(J)){G=F=64}else{if(isNaN(H)){F=64}}A=A+D._str.charAt(K)+D._str.charAt(I)+D._str.charAt(G)+D._str.charAt(F)}return A},decode:function(E){var D=this;var A="",L,J,H,K,I,G,F,B=0;E=E.replace(/[^A-Za-z0-9\+\/\=]/g,"");var C=E.length;while(B<C){K=D._str.indexOf(E.charAt(B++));I=D._str.indexOf(E.charAt(B++));G=D._str.indexOf(E.charAt(B++));F=D._str.indexOf(E.charAt(B++));L=(K<<2)|(I>>4);J=((I&15)<<4)|(G>>2);H=((G&3)<<6)|F;A=A+String.fromCharCode(L);if(G!==64){A=A+String.fromCharCode(J)}if(F!==64){A=A+String.fromCharCode(H)}}A=D._utf8_decode(A);return A},_utf8_encode:function(C){C=C.replace(/\r\n/g,"\n");var B="",E=0,A=C.length;for(;E<A;E++){var D=C.charCodeAt(E);if(D<128){B+=String.fromCharCode(D)}else{if((D>127)&&(D<2048)){B+=String.fromCharCode((D>>6)|192);B+=String.fromCharCode((D&63)|128)}else{B+=String.fromCharCode((D>>12)|224);B+=String.fromCharCode(((D>>6)&63)|128);B+=String.fromCharCode((D&63)|128)}}}return B},_utf8_decode:function(B){var D="",F=0,G=0,C=0,E=0,A=B.length;while(F<A){G=B.charCodeAt(F);if(G<128){D+=String.fromCharCode(G);F++}else{if((G>191)&&(G<224)){E=B.charCodeAt(F+1);D+=String.fromCharCode(((G&31)<<6)|(E&63));F+=2}else{E=B.charCodeAt(F+1);C=B.charCodeAt(F+2);D+=String.fromCharCode(((G&15)<<12)|((E&63)<<6)|(C&63));F+=3}}}return D}})