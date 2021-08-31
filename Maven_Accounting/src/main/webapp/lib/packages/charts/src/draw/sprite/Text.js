Ext.define("Ext.draw.sprite.Text",function(){var D={"xx-small":true,"x-small":true,"small":true,"medium":true,"large":true,"x-large":true,"xx-large":true};var B={normal:true,bold:true,bolder:true,lighter:true,100:true,200:true,300:true,400:true,500:true,600:true,700:true,800:true,900:true};var A={start:"start",left:"start",center:"center",middle:"center",end:"end",right:"end"};var C={top:"top",hanging:"hanging",middle:"middle",center:"middle",alphabetic:"alphabetic",ideographic:"ideographic",bottom:"bottom"};return{extend:"Ext.draw.sprite.Sprite",requires:["Ext.draw.TextMeasurer","Ext.draw.Color"],alias:"sprite.text",type:"text",lineBreakRe:/\r?\n/g,statics:{debug:false,fontSizes:D,fontWeights:B,textAlignments:A,textBaselines:C},inheritableStatics:{def:{animationProcessors:{text:"text"},processors:{x:"number",y:"number",text:"string",fontSize:function(E){if(Ext.isNumber(+E)){return E+"px"}else{if(E.match(Ext.dom.Element.unitRe)){return E}else{if(E in D){return E}}}},fontStyle:"enums(,italic,oblique)",fontVariant:"enums(,small-caps)",fontWeight:function(E){if(E in B){return String(E)}else{return""}},fontFamily:"string",textAlign:function(E){return A[E]||"center"},textBaseline:function(E){return C[E]||"alphabetic"},font:"string",debug:"default"},aliases:{"font-size":"fontSize","font-family":"fontFamily","font-weight":"fontWeight","font-variant":"fontVariant","text-anchor":"textAlign"},defaults:{fontStyle:"",fontVariant:"",fontWeight:"",fontSize:"10px",fontFamily:"sans-serif",font:"10px sans-serif",textBaseline:"alphabetic",textAlign:"start",strokeStyle:"rgba(0, 0, 0, 0)",fillStyle:"#000",x:0,y:0,text:""},triggers:{fontStyle:"fontX,bbox",fontVariant:"fontX,bbox",fontWeight:"fontX,bbox",fontSize:"fontX,bbox",fontFamily:"fontX,bbox",font:"font,bbox,canvas",textBaseline:"bbox",textAlign:"bbox",x:"bbox",y:"bbox",text:"bbox"},updaters:{fontX:"makeFontShorthand",font:"parseFontShorthand"}}},constructor:function(E){if(E&&E.font){E=Ext.clone(E);for(var F in E){if(F!=="font"&&F.indexOf("font")===0){delete E[F]}}}Ext.draw.sprite.Sprite.prototype.constructor.call(this,E)},fontValuesMap:{"italic":"fontStyle","oblique":"fontStyle","small-caps":"fontVariant","bold":"fontWeight","bolder":"fontWeight","lighter":"fontWeight","100":"fontWeight","200":"fontWeight","300":"fontWeight","400":"fontWeight","500":"fontWeight","600":"fontWeight","700":"fontWeight","800":"fontWeight","900":"fontWeight","xx-small":"fontSize","x-small":"fontSize","small":"fontSize","medium":"fontSize","large":"fontSize","x-large":"fontSize","xx-large":"fontSize"},makeFontShorthand:function(E){var F=[];if(E.fontStyle){F.push(E.fontStyle)}if(E.fontVariant){F.push(E.fontVariant)}if(E.fontWeight){F.push(E.fontWeight)}if(E.fontSize){F.push(E.fontSize)}if(E.fontFamily){F.push(E.fontFamily)}this.setAttributes({font:F.join(" ")},true)},parseFontShorthand:function(J){var M=J.font,K=M.length,L={},N=this.fontValuesMap,E=0,I,G,F,H;while(E<K&&I!==-1){I=M.indexOf(" ",E);if(I<0){F=M.substr(E)}else{if(I>E){F=M.substr(E,I-E)}else{continue}}G=F.indexOf("/");if(G>0){F=F.substr(0,G)}else{if(G===0){continue}}if(F!=="normal"&&F!=="inherit"){H=N[F];if(H){L[H]=F}else{if(F.match(Ext.dom.Element.unitRe)){L.fontSize=F}else{L.fontFamily=M.substr(E);break}}}E=I+1}if(!L.fontStyle){L.fontStyle=""}if(!L.fontVariant){L.fontVariant=""}if(!L.fontWeight){L.fontWeight=""}this.setAttributes(L,true)},fontProperties:{fontStyle:true,fontVariant:true,fontWeight:true,fontSize:true,fontFamily:true},setAttributes:function(G,I,E){var F,H;if(G&&G.font){H={};for(F in G){if(!(F in this.fontProperties)){H[F]=G[F]}}G=H}this.callParent([G,I,E])},getBBox:function(G){var H=this,F=H.attr.bbox.plain,E=H.getSurface();if(!E){Ext.raise("The sprite does not belong to a surface.")}if(F.dirty){H.updatePlainBBox(F);F.dirty=false}if(E.getInherited().rtl&&E.getFlipRtlText()){H.updatePlainBBox(F,true)}return H.callParent([G])},rtlAlignments:{start:"end",center:"center",end:"start"},updatePlainBBox:function(J,Y){var Z=this,V=Z.attr,N=V.x,M=V.y,P=[],S=V.font,Q=V.text,R=V.textBaseline,K=V.textAlign,T=(Y&&Z.oldSize)?Z.oldSize:(Z.oldSize=Ext.draw.TextMeasurer.measureText(Q,S)),W=Z.getSurface(),O=W.getInherited().rtl,U=O&&W.getFlipRtlText(),H=W.getRect(),F=T.sizes,G=T.height,I=T.width,L=F?F.length:0,E,X=0;switch(R){case"hanging":case"top":break;case"ideographic":case"bottom":M-=G;break;case"alphabetic":M-=G*0.8;break;case"middle":M-=G*0.5;break}if(U){N=H[2]-H[0]-N;K=Z.rtlAlignments[K]}switch(K){case"start":if(O){for(;X<L;X++){E=F[X].width;P.push(-(I-E))}}break;case"end":N-=I;if(O){break}for(;X<L;X++){E=F[X].width;P.push(I-E)}break;case"center":N-=I*0.5;for(;X<L;X++){E=F[X].width;P.push((O?-1:1)*(I-E)*0.5)}break}V.textAlignOffsets=P;J.x=N;J.y=M;J.width=I;J.height=G},setText:function(E){this.setAttributes({text:E},true)},render:function(F,Q,K){var I=this,H=I.attr,P=Ext.draw.Matrix.fly(H.matrix.elements.slice(0)),O=I.getBBox(true),S=H.textAlignOffsets,M=Ext.draw.Color.RGBA_NONE,L,J,G,R,N;if(H.text.length===0){return }R=H.text.split(I.lineBreakRe);N=O.height/R.length;L=H.bbox.plain.x;J=H.bbox.plain.y+N*0.78;P.toContext(Q);if(F.getInherited().rtl){L+=H.bbox.plain.width}for(G=0;G<R.length;G++){if(Q.fillStyle!==M){Q.fillText(R[G],L+(S[G]||0),J+N*G)}if(Q.strokeStyle!==M){Q.strokeText(R[G],L+(S[G]||0),J+N*G)}}var E=H.debug||this.statics().debug||Ext.draw.sprite.Sprite.debug;if(E){this.attr.inverseMatrix.toContext(Q);E.bbox&&I.renderBBox(F,Q)}}}})