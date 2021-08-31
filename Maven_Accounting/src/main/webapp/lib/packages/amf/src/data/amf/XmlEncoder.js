Ext.define("Ext.data.amf.XmlEncoder",{alias:"data.amf.xmlencoder",body:"",statics:{generateFlexUID:function(E){var D="",C,A,B;if(E===undefined){E=Ext.Number.randomInt(0,4294967295)}B=(E+4294967296).toString(16).toUpperCase();D=B.substr(B.length-8,8);for(A=0;A<3;A++){D+="-";for(C=0;C<4;C++){D+=Ext.Number.randomInt(0,15).toString(16).toUpperCase()}}D+="-";B=new Number(new Date()).valueOf().toString(16).toUpperCase();A=0;if(B.length<8){for(C=0;C<B.length-8;C++){A++;D+="0"}}D+=B.substr(-(8-A));for(C=0;C<4;C++){D+=Ext.Number.randomInt(0,15).toString(16).toUpperCase()}return D}},constructor:function(A){this.initConfig(A);this.clear()},clear:function(){this.body=""},encodeUndefined:function(){return this.encodeNull()},writeUndefined:function(){this.write(this.encodeUndefined())},encodeNull:function(){return"<null />"},writeNull:function(){this.write(this.encodeNull())},encodeBoolean:function(B){var A;if(B){A="<true />"}else{A="<false />"}return A},writeBoolean:function(A){this.write(this.encodeBoolean(A))},encodeString:function(B){var A;if(B===""){A="<string />"}else{A="<string>"+B+"</string>"}return A},writeString:function(A){this.write(this.encodeString(A))},encodeInt:function(A){return"<int>"+A.toString()+"</int>"},writeInt:function(A){this.write(this.encodeInt(A))},encodeDouble:function(A){return"<double>"+A.toString()+"</double>"},writeDouble:function(A){this.write(this.encodeDouble(A))},encodeNumber:function(B){var A=536870911,C=-268435455;if(typeof (B)!=="number"&&!(B instanceof Number)){Ext.log.warn("Encoder: writeNumber argument is not numeric. Can't coerce.")}if(B instanceof Number){B=B.valueOf()}if(B%1===0&&B>=C&&B<=A){return this.encodeInt(B)}else{return this.encodeDouble(B)}},writeNumber:function(A){this.write(this.encodeNumber(A))},encodeDate:function(A){return"<date>"+(new Number(A)).toString()+"</date>"},writeDate:function(A){this.write(this.encodeDate(A))},encodeEcmaElement:function(A,B){var C='<item name="'+A.toString()+'">'+this.encodeObject(B)+"</item>";return C},encodeArray:function(G){var E=[],A,D=[],C=G.length,B,F;for(B in G){if(Ext.isNumeric(B)&&(B%1==0)){E[B]=this.encodeObject(G[B])}else{D.push(this.encodeEcmaElement(B,G[B]))}}A=E.length;for(B=0;B<E.length;B++){if(E[B]===undefined){A=B;break}}if(A<E.length){for(B=firstNonOrdinals;B<E.length;B++){if(E[B]!==undefined){D.push(this.encodeEcmaElement(B,E[B]))}}E=E.slice(0,A)}F='<array length="'+E.length+'"';if(D.length>0){F+=' ecma="true"'}F+=">";for(B=0;B<E.length;B++){F+=E[B]}for(B in D){F+=D[B]}F+="</array>";return F},writeArray:function(A){this.write(this.encodeArray(A))},encodeXml:function(A){var B=this.convertXmlToString(A);return"<xml><![CDATA["+B+"]]></xml>"},writeXml:function(A){this.write(this.encodeXml(A))},encodeGenericObject:function(D){var C=[],A=[],F=null,B,E;for(B in D){if(B=="$flexType"){F=D[B]}else{C.push(this.encodeString(new String(B)));A.push(this.encodeObject(D[B]))}}if(F){E='<object type="'+F+'">'}else{E="<object>"}if(C.length>0){E+="<traits>";E+=C.join("");E+="</traits>"}else{E+="<traits />"}E+=A.join("");E+="</object>";return E},writeGenericObject:function(A){this.write(this.encodeGenericObject(A))},encodeByteArray:function(D){var C,A,B;if(D.length>0){C="<bytearray>";for(A=0;A<D.length;A++){if(!Ext.isNumber(D[A])){Ext.raise("Byte array contains a non-number: "+D[A]+" in index: "+A)}if(D[A]<0||D[A]>255){Ext.raise("Byte array value out of bounds: "+D[A])}B=D[A].toString(16).toUpperCase();if(D[A]<16){B="0"+B}C+=B}C+="</bytearray>"}else{C="<bytearray />"}return C},writeByteArray:function(A){this.write(this.encodeByteArray(A))},encodeObject:function(B){var A=typeof (B);if(A==="undefined"){return this.encodeUndefined()}else{if(B===null){return this.encodeNull()}else{if(Ext.isBoolean(B)){return this.encodeBoolean(B)}else{if(Ext.isString(B)){return this.encodeString(B)}else{if(A==="number"||B instanceof Number){return this.encodeNumber(B)}else{if(A==="object"){if(B instanceof Date){return this.encodeDate(B)}else{if(Ext.isArray(B)){return this.encodeArray(B)}else{if(this.isXmlDocument(B)){return this.encodeXml(B)}else{return this.encodeGenericObject(B)}}}}else{Ext.log.warn("AMFX Encoder: Unknown item type "+A+" can't be written to stream: "+B)}}}}}}return null},writeObject:function(A){this.write(this.encodeObject(A))},encodeAmfxRemotingPacket:function(A){var C,B;B='<amfx ver="3" xmlns="http://www.macromedia.com/2005/amfx"><body>';B+=A.encodeMessage();B+="</body></amfx>";return B},writeAmfxRemotingPacket:function(A){this.write(this.encodeAmfxRemotingPacket(A))},convertXmlToString:function(A){var B;if(window.XMLSerializer){B=new window.XMLSerializer().serializeToString(A)}else{B=A.xml}return B},isXmlDocument:function(A){if(window.DOMParser){if(Ext.isDefined(A.doctype)){return true}}if(Ext.isString(A.xml)){return true}return false},write:function(A){this.body+=A}})