Ext.define("Ext.data.amf.Packet",function(){var F=Math.pow(2,-52),B=Math.pow(2,8),G=0,C,A,D,E;return{typeMap:{0:{0:"readDouble",1:"readBoolean",2:"readAmf0String",3:"readAmf0Object",5:"readNull",6:"readUndefined",7:"readReference",8:"readEcmaArray",10:"readStrictArray",11:"readAmf0Date",12:"readLongString",13:"readUnsupported",15:"readAmf0Xml",16:"readTypedObject"},3:{0:"readUndefined",1:"readNull",2:"readFalse",3:"readTrue",4:"readUInt29",5:"readDouble",6:"readAmf3String",7:"readAmf3Xml",8:"readAmf3Date",9:"readAmf3Array",10:"readAmf3Object",11:"readAmf3Xml",12:"readByteArray"}},decode:function(I){var L=this,M=L.headers=[],K=L.messages=[],J,H;G=0;C=L.bytes=I;A=L.strings=[];D=L.objects=[];E=L.traits=[];L.version=L.readUInt(2);for(J=L.readUInt(2);J--;){M.push({name:L.readAmf0String(),mustUnderstand:L.readBoolean(),byteLength:L.readUInt(4),value:L.readValue()});A=L.strings=[];D=L.objects=[];E=L.traits=[]}for(H=L.readUInt(2);H--;){K.push({targetURI:L.readAmf0String(),responseURI:L.readAmf0String(),byteLength:L.readUInt(4),body:L.readValue()});A=L.strings=[];D=L.objects=[];E=L.traits=[]}G=0;C=A=D=E=L.bytes=L.strings=L.objects=L.traits=null;return L},decodeValue:function(H){var I=this;C=I.bytes=H;G=0;I.version=3;A=I.strings=[];D=I.objects=[];E=I.traits=[];return I.readValue()},parseXml:function(H){var I;if(window.DOMParser){I=(new DOMParser()).parseFromString(H,"text/xml")}else{I=new ActiveXObject("Microsoft.XMLDOM");I.loadXML(H)}return I},readAmf0Date:function(){var H=new Date(this.readDouble());G+=2;return H},readAmf0Object:function(J){var I=this,H;J=J||{};D.push(J);while((H=I.readAmf0String())||C[G]!==9){J[H]=I.readValue()}G++;return J},readAmf0String:function(){return this.readUtf8(this.readUInt(2))},readAmf0Xml:function(){return this.parseXml(this.readLongString())},readAmf3Array:function(){var K=this,M=K.readUInt29(),J,I,L,H;if(M&1){J=(M>>1);I=K.readAmf3String();if(I){L={};D.push(L);do{L[I]=K.readValue()}while((I=K.readAmf3String()));for(H=0;H<J;H++){L[H]=K.readValue()}}else{L=[];D.push(L);for(H=0;H<J;H++){L.push(K.readValue())}}}else{L=D[M>>1]}return L},readAmf3Date:function(){var I=this,J=I.readUInt29(),H;if(J&1){H=new Date(I.readDouble());D.push(H)}else{H=D[J>>1]}return H},readAmf3Object:function(){var Q=this,M=Q.readUInt29(),J=[],S,O,P,I,H,L,R,N,K;if(M&1){S=(M&7);if(S===3){P=Q.readAmf3String();I=!!(M&8);O=(M>>4);for(K=0;K<O;K++){J.push(Q.readAmf3String())}H={className:P,dynamic:I,members:J};E.push(H)}else{if((M&3)===1){H=E[M>>2];P=H.className;I=H.dynamic;J=H.members;O=J.length}else{if(S===7){}}}if(P){N=Ext.ClassManager.getByAlias("amf."+P);L=N?new N():{$className:P}}else{L={}}D.push(L);for(K=0;K<O;K++){L[J[K]]=Q.readValue()}if(I){while((R=Q.readAmf3String())){L[R]=Q.readValue()}}if((!N)&&this.converters[P]){L=this.converters[P](L)}}else{L=D[M>>1]}return L},readAmf3String:function(){var H=this,J=H.readUInt29(),I;if(J&1){I=H.readUtf8(J>>1);if(I){A.push(I)}return I}else{return A[J>>1]}},readAmf3Xml:function(){var H=this,J=H.readUInt29(),I;if(J&1){I=H.parseXml(H.readUtf8(J>>1));D.push(I)}else{I=D[J>>1]}return I},readBoolean:function(){return !!C[G++]},readByteArray:function(){var J=this.readUInt29(),I,H;if(J&1){H=G+(J>>1);I=Array.prototype.slice.call(C,G,H);D.push(I);G=H}else{I=D[J>>1]}return I},readDouble:function(){var K=C[G++],H=C[G++],J=(K>>7)?-1:1,N=(((K&127)<<4)|(H>>4)),I=(H&15),L=N?1:0,M=6;while(M--){I=(I*B)+C[G++]}if(!N){if(!I){return 0}N=1}if(N===2047){return I?NaN:(Infinity*J)}return J*Math.pow(2,N-1023)*(L+F*I)},readEcmaArray:function(){G+=4;return this.readAmf0Object()},readFalse:function(){return false},readLongString:function(){return this.readUtf8(this.readUInt(4))},readNull:function(){return null},readReference:function(){return D[this.readUInt(2)]},readStrictArray:function(){var J=this,I=J.readUInt(4),H=[];D.push(H);while(I--){H.push(J.readValue())}return H},readTrue:Ext.returnTrue,readTypedObject:function(){var L=this,K=L.readAmf0String(),I,H,J;I=Ext.ClassManager.getByAlias("amf."+K);H=I?new I():{$className:K};J=L.readAmf0Object(H);if((!I)&&this.converters[K]){J=this.converters[K](H)}return J},readUInt:function(J){var I=1,H;H=C[G++];for(;I<J;++I){H=(H<<8)|C[G++]}return H},readUInt29:function(){var I=C[G++],H;if(I&128){H=C[G++];I=((I&127)<<7)|(H&127);if(H&128){H=C[G++];I=(I<<7)|(H&127);if(H&128){H=C[G++];I=(I<<8)|H}}}return I},readUndefined:Ext.emptyFn,readUnsupported:Ext.emptyFn,readUtf8:function(Q){var K=G+Q,N=[],I=0,O=65535,J=1,R=[],M=0,L,H,P;L=[N];while(G<K){P=C[G++];if(P>127){if(P>239){H=4;P=(P&7)}else{if(P>223){H=3;P=(P&15)}else{H=2;P=(P&31)}}while(--H){P=((P<<6)|(C[G++]&63))}}N.push(P);if(++I===O){L.push(N=[]);I=0;J++}}for(;M<J;M++){R.push(String.fromCharCode.apply(String,L[M]))}return R.join("")},readValue:function(){var I=this,H=C[G++];if(H===17){I.version=3;H=C[G++]}return I[I.typeMap[I.version][H]]()},converters:{"flex.messaging.io.ArrayCollection":function(H){return H.source||[]}}}})