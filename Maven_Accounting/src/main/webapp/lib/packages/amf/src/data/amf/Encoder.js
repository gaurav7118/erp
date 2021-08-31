Ext.define("Ext.data.amf.Encoder",{alias:"data.amf.Encoder",config:{format:3},bytes:[],constructor:function(A){this.initConfig(A);this.clear()},clear:function(){this.bytes=[]},applyFormat:function(B){var A={0:{writeUndefined:this.write0Undefined,writeNull:this.write0Null,writeBoolean:this.write0Boolean,writeNumber:this.write0Number,writeString:this.write0String,writeXml:this.write0Xml,writeDate:this.write0Date,writeArray:this.write0Array,writeGenericObject:this.write0GenericObject},3:{writeUndefined:this.write3Undefined,writeNull:this.write3Null,writeBoolean:this.write3Boolean,writeNumber:this.write3Number,writeString:this.write3String,writeXml:this.write3Xml,writeDate:this.write3Date,writeArray:this.write3Array,writeGenericObject:this.write3GenericObject}}[B];if(A){Ext.apply(this,A);return B}else{Ext.raise("Unsupported AMF format: "+B+". Only '3' (AMF3) is supported at this point.");return }},writeObject:function(B){var A=typeof (B);if(A==="undefined"){this.writeUndefined()}else{if(B===null){this.writeNull()}else{if(Ext.isBoolean(B)){this.writeBoolean(B)}else{if(Ext.isString(B)){this.writeString(B)}else{if(A==="number"||B instanceof Number){this.writeNumber(B)}else{if(A==="object"){if(B instanceof Date){this.writeDate(B)}else{if(Ext.isArray(B)){this.writeArray(B)}else{if(this.isXmlDocument(B)){this.writeXml(B)}else{this.writeGenericObject(B)}}}}else{Ext.log.warn("AMF Encoder: Unknown item type "+A+" can't be written to stream: "+B)}}}}}}},write3Undefined:function(){this.writeByte(0)},write0Undefined:function(){this.writeByte(6)},write3Null:function(){this.writeByte(1)},write0Null:function(){this.writeByte(5)},write3Boolean:function(A){if(typeof (A)!=="boolean"){Ext.log.warn("Encoder: writeBoolean argument is not a boolean. Coercing.")}if(A){this.writeByte(3)}else{this.writeByte(2)}},write0Boolean:function(A){if(typeof (A)!=="boolean"){Ext.log.warn("Encoder: writeBoolean argument is not a boolean. Coercing.")}this.writeByte(1);if(A){this.writeByte(1)}else{this.writeByte(0)}},encode29Int:function(D){var E=[],A=D,C,B;if(A==0){return[0]}if(A>2097151){C=A&255;E.unshift(C);A=A>>8}while(A>0){C=A&127;E.unshift(C);A=A>>7}for(B=0;B<E.length-1;B++){E[B]=E[B]|128}return E},write3Number:function(C){var D;var A=536870911,B=-268435455;if(typeof (C)!=="number"&&!(C instanceof Number)){Ext.log.warn("Encoder: writeNumber argument is not numeric. Can't coerce.")}if(C instanceof Number){C=C.valueOf()}if(C%1===0&&C>=B&&C<=A){C=C&A;D=this.encode29Int(C);D.unshift(4);this.writeBytes(D)}else{D=this.encodeDouble(C);D.unshift(5);this.writeBytes(D)}},write0Number:function(A){var B;if(typeof (A)!=="number"&&!(A instanceof Number)){Ext.log.warn("Encoder: writeNumber argument is not numeric. Can't coerce.")}if(A instanceof Number){A=A.valueOf()}B=this.encodeDouble(A);B.unshift(0);this.writeBytes(B)},encodeUtf8Char:function(F){var D=[],E,A,C,B;if(F>1114111){Ext.raise("UTF 8 char out of bounds")}if(F<=127){D.push(F)}else{if(F<=2047){A=2}else{if(F<=65535){A=3}else{A=4}}B=128;for(C=1;C<A;C++){E=(F&63)|128;D.unshift(E);F=F>>6;B=(B>>1)|128}E=F|B;D.unshift(E)}return D},encodeUtf8String:function(D){var A,C=[];for(A=0;A<D.length;A++){var B=this.encodeUtf8Char(D.charCodeAt(A));Ext.Array.push(C,B)}return C},encode3Utf8StringLen:function(C){var A=C.length,B=[];if(A<=268435455){A=A<<1;A=A|1;B=this.encode29Int(A)}else{Ext.raise("UTF8 encoded string too long to serialize to AMF: "+A)}return B},write3String:function(B){if(!Ext.isString(B)){Ext.log.warn("Encoder: writString argument is not a string.")}if(B==""){this.writeByte(6);this.writeByte(1)}else{var C=this.encodeUtf8String(B);var A=this.encode3Utf8StringLen(C);this.writeByte(6);this.writeBytes(A);this.writeBytes(C)}},encodeXInt:function(C,D){var B=[],A;for(A=0;A<D;A++){B.unshift(C&255);C=C>>8}return B},write0String:function(C){if(!Ext.isString(C)){Ext.log.warn("Encoder: writString argument is not a string.")}if(C==""){this.writeByte(2);this.writeBytes([0,0])}else{var D=this.encodeUtf8String(C);var B;var A;if(D.length<=65535){B=2;A=this.encodeXInt(D.length,2)}else{B=12;A=this.encodeXInt(D.length,4)}this.writeByte(B);this.writeBytes(A);this.writeBytes(D)}},write3XmlWithType:function(C,A){if(A!==7&&A!==11){Ext.raise("write XML with unknown AMF3 code: "+A)}if(!this.isXmlDocument(C)){Ext.log.warn("Encoder: write3XmlWithType argument is not an xml document.")}var E=this.convertXmlToString(C);if(E==""){this.writeByte(A);this.writeByte(1)}else{var D=this.encodeUtf8String(E);var B=this.encode3Utf8StringLen(D);this.writeByte(A);this.writeBytes(B);this.writeBytes(D)}},write3XmlDocument:function(A){this.write3XmlWithType(A,7)},write3Xml:function(A){this.write3XmlWithType(A,11)},write0Xml:function(B){if(!this.isXmlDocument(B)){Ext.log.warn("Encoder: write0Xml argument is not an xml document.")}var D=this.convertXmlToString(B);this.writeByte(15);var C=this.encodeUtf8String(D);var A=this.encodeXInt(C.length,4);this.writeBytes(A);this.writeBytes(C)},write3Date:function(A){if(!(A instanceof Date)){Ext.raise("Serializing a non-date object as date: "+A)}this.writeByte(8);this.writeBytes(this.encode29Int(1));this.writeBytes(this.encodeDouble(new Number(A)))},write0Date:function(A){if(!(A instanceof Date)){Ext.raise("Serializing a non-date object as date: "+A)}this.writeByte(11);this.writeBytes(this.encodeDouble(new Number(A)));this.writeBytes([0,0])},write3Array:function(B){if(!Ext.isArray(B)){Ext.raise("Serializing a non-array object as array: "+B)}if(B.length>268435455){Ext.raise("Array size too long to encode in AMF3: "+B.length)}this.writeByte(9);var A=B.length;A=A<<1;A=A|1;this.writeBytes(this.encode29Int(A));this.writeByte(1);Ext.each(B,function(C){this.writeObject(C)},this)},write0ObjectProperty:function(B,D){if(!(B instanceof String)&&(typeof (B)!=="string")){B=B+""}var C=this.encodeUtf8String(B);var A;A=this.encodeXInt(C.length,2);this.writeBytes(A);this.writeBytes(C);this.writeObject(D)},write0Array:function(A){var B;if(!Ext.isArray(A)){Ext.raise("Serializing a non-array object as array: "+A)}this.writeByte(8);var C=0;for(B in A){C++}this.writeBytes(this.encodeXInt(C,4));for(B in A){Ext.Array.push(this.write0ObjectProperty(B,A[B]))}this.writeBytes([0,0,9])},write0StrictArray:function(B){if(!Ext.isArray(B)){Ext.raise("Serializing a non-array object as array: "+B)}this.writeByte(10);var A=B.length;this.writeBytes(this.encodeXInt(A,4));Ext.each(B,function(C){this.writeObject(C)},this)},write3ByteArray:function(B){if(!Ext.isArray(B)){Ext.raise("Serializing a non-array object as array: "+B)}if(B.length>268435455){Ext.raise("Array size too long to encode in AMF3: "+B.length)}this.writeByte(12);var A=B.length;A=A<<1;A=A|1;this.writeBytes(this.encode29Int(A));this.writeBytes(B)},write3GenericObject:function(D){var B;if(!Ext.isObject(D)){Ext.raise("Serializing a non-object object: "+D)}this.writeByte(10);var C=11;this.writeByte(C);this.writeByte(1);for(B in D){var A=new String(B).valueOf();if(A==""){Ext.raise("Can't encode non-string field name: "+B)}var E=(this.encodeUtf8String(B));this.writeBytes(this.encode3Utf8StringLen(B));this.writeBytes(E);this.writeObject(D[B])}this.writeByte(1)},write0GenericObject:function(D){var C,A,B;if(!Ext.isObject(D)){Ext.raise("Serializing a non-object object: "+D)}C=!!D.$flexType;A=C?16:3;this.writeByte(A);if(C){this.write0ShortUtf8String(D.$flexType)}for(B in D){if(B!="$flexType"){Ext.Array.push(this.write0ObjectProperty(B,D[B]))}}this.writeBytes([0,0,9])},writeByte:function(A){if(A<0||A>255){Ex.Error.raise("ERROR: Value being written outside byte range: "+A)}Ext.Array.push(this.bytes,A)},writeBytes:function(A){var B;if(!Ext.isArray(A)){Ext.raise("Decoder: writeBytes parameter is not an array: "+A)}for(B=0;B<A.length;B++){if(A[B]<0||A[B]>255||!Ext.isNumber(A[B])){Ext.raise("ERROR: Value "+B+" being written outside byte range: "+A[B])}}Ext.Array.push(this.bytes,A)},convertXmlToString:function(A){var B;if(window.XMLSerializer){B=new window.XMLSerializer().serializeToString(A)}else{B=A.xml}return B},isXmlDocument:function(A){if(window.DOMParser){if(Ext.isDefined(A.doctype)){return true}}if(Ext.isString(A.xml)){return true}return false},encodeDouble:function(L){var B=11,A=52;var E=(1<<(B-1))-1,O,H,F,J,D,N,K,C=[];var G=[127,240,0,0,0,0,0,0],M=[255,240,0,0,0,0,0,0],I=[255,248,0,0,0,0,0,0];if(isNaN(L)){C=I}else{if(L===Infinity){C=G}else{if(L==-Infinity){C=M}else{if(L===0){H=0;F=0;O=(1/L===-Infinity)?1:0}else{O=L<0;L=Math.abs(L);if(L>=Math.pow(2,1-E)){J=Math.min(Math.floor(Math.log(L)/Math.LN2),E);H=J+E;F=Math.round(L*Math.pow(2,A-J)-Math.pow(2,A))}else{H=0;F=Math.round(L/Math.pow(2,1-E-A))}}N=[];for(D=A;D;D-=1){N.push(F%2?1:0);F=Math.floor(F/2)}for(D=B;D;D-=1){N.push(H%2?1:0);H=Math.floor(H/2)}N.push(O?1:0);N.reverse();K=N.join("");C=[];while(K.length){C.push(parseInt(K.substring(0,8),2));K=K.substring(8)}}}}return C},write0ShortUtf8String:function(C){var B=this.encodeUtf8String(C),A;A=this.encodeXInt(B.length,2);this.writeBytes(A);this.writeBytes(B)},writeAmfPacket:function(C,B){var A;if(this.config.format!=0){Ext.raise("Trying to write a packet on an AMF3 Encoder. Only AMF0 is supported!")}if(!Ext.isArray(C)){Ext.raise("headers is not an array: "+C)}if(!Ext.isArray(B)){Ext.raise("messages is not an array: "+B)}this.writeBytes([0,0]);this.writeBytes(this.encodeXInt(C.length,2));for(A in C){this.writeAmfHeader(C[A].name,C[A].mustUnderstand,C[A].value)}this.writeBytes(this.encodeXInt(B.length,2));for(A in B){this.writeAmfMessage(B[A].targetUri,B[A].responseUri,B[A].body)}},writeAmfHeader:function(D,B,C){if(this.config.format!=0){Ext.raise("Trying to write a header on an AMF3 Encoder. Only AMF0 is supported!")}if(!Ext.isString(D)){Ext.raise("targetURI is not a string: "+targetUri)}if((typeof (B)!=="boolean")&&!Ext.isBoolean(B)){Ext.raise("mustUnderstand is not a boolean value: "+B)}this.write0ShortUtf8String(D);var A=B?1:0;this.writeByte(A);this.writeBytes(this.encodeXInt(-1,4));this.writeObject(C)},writeAmfMessage:function(C,B,A){if(this.config.format!=0){Ext.raise("Trying to write a message on an AMF3 Encoder. Only AMF0 is supported!")}if(!Ext.isString(C)){Ext.raise("targetURI is not a string: "+C)}if(!Ext.isString(B)){Ext.raise("targetURI is not a string: "+B)}if(!Ext.isArray(A)){Ext.raise("body is not an array: "+typeof (A))}this.write0ShortUtf8String(C);this.write0ShortUtf8String(B);this.writeBytes(this.encodeXInt(-1,4));this.write0StrictArray(A)}})