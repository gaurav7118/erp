Ext.define("Ext.util.XTemplateCompiler",{extend:"Ext.util.XTemplateParser",useEval:Ext.isGecko,useIndex:Ext.isIE8m,useFormat:true,propNameRe:/^[\w\d\$]*$/,compile:function(A){var C=this,B=C.generate(A);return C.useEval?C.evalTpl(B):(new Function("Ext",B))(Ext)},generate:function(A){var D=this,B="var fm=Ext.util.Format,ts=Object.prototype.toString;",C;D.maxLevel=0;D.body=["var c0=values, a0="+D.createArrayTest(0)+", p0=parent, n0=xcount, i0=xindex, k0, v;\n"];if(D.definitions){if(typeof D.definitions==="string"){D.definitions=[D.definitions,B]}else{D.definitions.push(B)}}else{D.definitions=[B]}D.switches=[];D.parse(A);D.definitions.push((D.useEval?"$=":"return")+" function ("+D.fnArgs+") {",D.body.join(""),"}");C=D.definitions.join("\n");D.definitions.length=D.body.length=D.switches.length=0;delete D.definitions;delete D.body;delete D.switches;return C},doText:function(C){var B=this,A=B.body;C=C.replace(B.aposRe,"\\'").replace(B.newLineRe,"\\n");if(B.useIndex){A.push("out[out.length]='",C,"'\n")}else{A.push("out.push('",C,"')\n")}},doExpr:function(B){var A=this.body;A.push("if ((v="+B+") != null) out");if(this.useIndex){A.push("[out.length]=v+''\n")}else{A.push(".push(v+'')\n")}},doTag:function(A){var B=this.parseTag(A);if(B){this.doExpr(B)}else{this.doText("{"+A+"}")}},doElse:function(){this.body.push("} else {\n")},doEval:function(A){this.body.push(A,"\n")},doIf:function(B,C){var A=this;if(B==="."){A.body.push("if (values) {\n")}else{if(A.propNameRe.test(B)){A.body.push("if (",A.parseTag(B),") {\n")}else{A.body.push("if (",A.addFn(B),A.callFn,") {\n")}}if(C.exec){A.doExec(C.exec)}},doElseIf:function(B,C){var A=this;if(B==="."){A.body.push("else if (values) {\n")}else{if(A.propNameRe.test(B)){A.body.push("} else if (",A.parseTag(B),") {\n")}else{A.body.push("} else if (",A.addFn(B),A.callFn,") {\n")}}if(C.exec){A.doExec(C.exec)}},doSwitch:function(C){var B=this,A;if(C==="."||C==="#"){A=C==="."?"values":"xindex";B.body.push("switch (",A,") {\n")}else{if(B.propNameRe.test(C)){B.body.push("switch (",B.parseTag(C),") {\n")}else{B.body.push("switch (",B.addFn(C),B.callFn,") {\n")}}B.switches.push(0)},doCase:function(E){var D=this,C=Ext.isArray(E)?E:[E],F=D.switches.length-1,A,B;if(D.switches[F]){D.body.push("break;\n")}else{D.switches[F]++}for(B=0,F=C.length;B<F;++B){A=D.intRe.exec(C[B]);C[B]=A?A[1]:("'"+C[B].replace(D.aposRe,"\\'")+"'")}D.body.push("case ",C.join(": case "),":\n")},doDefault:function(){var A=this,B=A.switches.length-1;if(A.switches[B]){A.body.push("break;\n")}else{A.switches[B]++}A.body.push("default:\n")},doEnd:function(B,D){var C=this,A=C.level-1;if(B=="for"||B=="foreach"){if(D.exec){C.doExec(D.exec)}C.body.push("}\n");C.body.push("parent=p",A,";values=r",A+1,";xcount=n"+A+";xindex=i",A,"+1;xkey=k",A,";\n")}else{if(B=="if"||B=="switch"){C.body.push("}\n")}}},doFor:function(E,G){var D=this,C,B=D.level,A=B-1,F;if(E==="."){C="values"}else{if(D.propNameRe.test(E)){C=D.parseTag(E)}else{C=D.addFn(E)+D.callFn}}if(D.maxLevel<B){D.maxLevel=B;D.body.push("var ")}if(E=="."){F="c"+B}else{F="a"+A+"?c"+A+"[i"+A+"]:c"+A}D.body.push("i",B,"=0,n",B,"=0,c",B,"=",C,",a",B,"=",D.createArrayTest(B),",r",B,"=values,p",B,",k",B,";\n","p",B,"=parent=",F,"\n","if (c",B,"){if(a",B,"){n",B,"=c",B,".length;}else if (c",B,".isMixedCollection){c",B,"=c",B,".items;n",B,"=c",B,".length;}else if(c",B,".isStore){c",B,"=c",B,".data.items;n",B,"=c",B,".length;}else{c",B,"=[c",B,"];n",B,"=1;}}\n","for (xcount=n",B,";i",B,"<n"+B+";++i",B,"){\n","values=c",B,"[i",B,"]");if(G.propName){D.body.push(".",G.propName)}D.body.push("\n","xindex=i",B,"+1\n");if(G.between){D.body.push('if(xindex>1){ out.push("',G.between,'"); } \n')}},doForEach:function(E,G){var D=this,C,B=D.level,A=B-1,F;if(E==="."){C="values"}else{if(D.propNameRe.test(E)){C=D.parseTag(E)}else{C=D.addFn(E)+D.callFn}}if(D.maxLevel<B){D.maxLevel=B;D.body.push("var ")}if(E=="."){F="c"+B}else{F="a"+A+"?c"+A+"[i"+A+"]:c"+A}D.body.push("i",B,"=-1,n",B,"=0,c",B,"=",C,",a",B,"=",D.createArrayTest(B),",r",B,"=values,p",B,",k",B,";\n","p",B,"=parent=",F,"\n","for(k",B," in c",B,"){\n","xindex=++i",B,"+1;\n","xkey=k",B,";\n","values=c",B,"[k",B,"];");if(G.propName){D.body.push(".",G.propName)}if(G.between){D.body.push('if(xindex>1){ out.push("',G.between,'"); } \n')}},createArrayTest:("isArray" in Array)?function(A){return"Array.isArray(c"+A+")"}:function(A){return"ts.call(c"+A+')==="[object Array]"'},doExec:function(D,E){var C=this,A="f"+C.definitions.length,B=C.guards[C.strict?0:1];C.definitions.push("function "+A+"("+C.fnArgs+") {",B.doTry," var $v = values; with($v) {","  "+D," }",B.doCatch,"}");C.body.push(A+C.callFn+"\n")},guards:[{doTry:"",doCatch:""},{doTry:"try { ",doCatch:' } catch(e) {\nExt.log.warn("XTemplate evaluation exception: " + e.message);\n}'}],addFn:function(A){var D=this,B="f"+D.definitions.length,C=D.guards[D.strict?0:1];if(A==="."){D.definitions.push("function "+B+"("+D.fnArgs+") {"," return values","}")}else{if(A===".."){D.definitions.push("function "+B+"("+D.fnArgs+") {"," return parent","}")}else{D.definitions.push("function "+B+"("+D.fnArgs+") {",C.doTry," var $v = values; with($v) {","  return("+A+")"," }",C.doCatch,"}")}}return B},parseTag:function(B){var G=this,A=G.tagRe.exec(B),E,H,D,F,C;if(!A){return null}E=A[1];H=A[2];D=A[3];F=A[4];if(E=="."){if(!G.validTypes){G.definitions.push("var validTypes={string:1,number:1,boolean:1};");G.validTypes=true}C='validTypes[typeof values] || ts.call(values) === "[object Date]" ? values : ""'}else{if(E=="#"){C="xindex"}else{if(E=="$"){C="xkey"}else{if(E.substr(0,7)=="parent."){C=E}else{if(isNaN(E)&&E.indexOf("-")==-1&&E.indexOf(".")!=-1){C="values."+E}else{C="values['"+E+"']"}}}}}if(F){C="("+C+F+")"}if(H&&G.useFormat){D=D?","+D:"";if(H.substr(0,5)!="this."){H="fm."+H+"("}else{H+="("}}else{return C}return H+C+D+")"},evalTpl:function($){eval($);return $},newLineRe:/\r\n|\r|\n/g,aposRe:/[']/g,intRe:/^\s*(\d+)\s*$/,tagRe:/^([\w-\.\#\$]+)(?:\:([\w\.]*)(?:\((.*?)?\))?)?(\s?[\+\-\*\/]\s?[\d\.\+\-\*\/\(\)]+)?$/},function(){var A=this.prototype;A.fnArgs="out,values,parent,xindex,xcount,xkey";A.callFn=".call(this,"+A.fnArgs+")"})