Ext.draw||(Ext.draw={});Ext.draw.engine||(Ext.draw.engine={});Ext.draw.engine.excanvas=true;if(!document.createElement("canvas").getContext){(function(){var AB=Math;var K=AB.round;var J=AB.sin;var W=AB.cos;var e=AB.abs;var n=AB.sqrt;var D=10;var F=D/2;var V=+navigator.userAgent.match(/MSIE ([\d.]+)?/)[1];function U(){return this.context_||(this.context_=new a(this))}var P=Array.prototype.slice;function G(i,j,m){var Z=P.call(arguments,2);return function(){return i.apply(j,Z.concat(P.call(arguments)))}}function AF(Z){return String(Z).replace(/&/g,"&amp;").replace(/"/g,"&quot;")}function z(j,i,Z){Ext.onReady(function(){if(!j.namespaces[i]){j.namespaces.add(i,Z,"#default#VML")}})}function s(i){z(i,"g_vml_","urn:schemas-microsoft-com:vml");z(i,"g_o_","urn:schemas-microsoft-com:office:office");if(!i.styleSheets["ex_canvas_"]){var Z=i.createStyleSheet();Z.owningElement.id="ex_canvas_";Z.cssText="canvas{display:inline-block;overflow:hidden;text-align:left;width:300px;height:150px}"}}s(document);var E={init:function(Z){var i=Z||document;i.createElement("canvas");i.attachEvent("onreadystatechange",G(this.init_,this,i))},init_:function(m){var j=m.getElementsByTagName("canvas");for(var Z=0;Z<j.length;Z++){this.initElement(j[Z])}},initElement:function(i){if(!i.getContext){i.getContext=U;s(i.ownerDocument);i.innerHTML="";i.attachEvent("onpropertychange",T);i.attachEvent("onresize",x);var Z=i.attributes;if(Z.width&&Z.width.specified){i.style.width=Z.width.nodeValue+"px"}else{i.width=i.clientWidth}if(Z.height&&Z.height.specified){i.style.height=Z.height.nodeValue+"px"}else{i.height=i.clientHeight}}return i}};function T(i){var Z=i.srcElement;switch(i.propertyName){case"width":Z.getContext().clearRect();Z.style.width=Z.attributes.width.nodeValue+"px";Z.firstChild.style.width=Z.clientWidth+"px";break;case"height":Z.getContext().clearRect();Z.style.height=Z.attributes.height.nodeValue+"px";Z.firstChild.style.height=Z.clientHeight+"px";break}}function x(i){var Z=i.srcElement;if(Z.firstChild){Z.firstChild.style.width=Z.clientWidth+"px";Z.firstChild.style.height=Z.clientHeight+"px"}}E.init();var I=[];for(var AE=0;AE<16;AE++){for(var AD=0;AD<16;AD++){I[AE*16+AD]=AE.toString(16)+AD.toString(16)}}function X(){return[[1,0,0],[0,1,0],[0,0,1]]}function g(m,j){var i=X();for(var Z=0;Z<3;Z++){for(var AH=0;AH<3;AH++){var p=0;for(var AG=0;AG<3;AG++){p+=m[Z][AG]*j[AG][AH]}i[Z][AH]=p}}return i}function R(i,Z){Z.fillStyle=i.fillStyle;Z.lineCap=i.lineCap;Z.lineJoin=i.lineJoin;Z.lineDash=i.lineDash;Z.lineWidth=i.lineWidth;Z.miterLimit=i.miterLimit;Z.shadowBlur=i.shadowBlur;Z.shadowColor=i.shadowColor;Z.shadowOffsetX=i.shadowOffsetX;Z.shadowOffsetY=i.shadowOffsetY;Z.strokeStyle=i.strokeStyle;Z.globalAlpha=i.globalAlpha;Z.font=i.font;Z.textAlign=i.textAlign;Z.textBaseline=i.textBaseline;Z.arcScaleX_=i.arcScaleX_;Z.arcScaleY_=i.arcScaleY_;Z.lineScale_=i.lineScale_}var B={aliceblue:"#F0F8FF",antiquewhite:"#FAEBD7",aquamarine:"#7FFFD4",azure:"#F0FFFF",beige:"#F5F5DC",bisque:"#FFE4C4",black:"#000000",blanchedalmond:"#FFEBCD",blueviolet:"#8A2BE2",brown:"#A52A2A",burlywood:"#DEB887",cadetblue:"#5F9EA0",chartreuse:"#7FFF00",chocolate:"#D2691E",coral:"#FF7F50",cornflowerblue:"#6495ED",cornsilk:"#FFF8DC",crimson:"#DC143C",cyan:"#00FFFF",darkblue:"#00008B",darkcyan:"#008B8B",darkgoldenrod:"#B8860B",darkgray:"#A9A9A9",darkgreen:"#006400",darkgrey:"#A9A9A9",darkkhaki:"#BDB76B",darkmagenta:"#8B008B",darkolivegreen:"#556B2F",darkorange:"#FF8C00",darkorchid:"#9932CC",darkred:"#8B0000",darksalmon:"#E9967A",darkseagreen:"#8FBC8F",darkslateblue:"#483D8B",darkslategray:"#2F4F4F",darkslategrey:"#2F4F4F",darkturquoise:"#00CED1",darkviolet:"#9400D3",deeppink:"#FF1493",deepskyblue:"#00BFFF",dimgray:"#696969",dimgrey:"#696969",dodgerblue:"#1E90FF",firebrick:"#B22222",floralwhite:"#FFFAF0",forestgreen:"#228B22",gainsboro:"#DCDCDC",ghostwhite:"#F8F8FF",gold:"#FFD700",goldenrod:"#DAA520",grey:"#808080",greenyellow:"#ADFF2F",honeydew:"#F0FFF0",hotpink:"#FF69B4",indianred:"#CD5C5C",indigo:"#4B0082",ivory:"#FFFFF0",khaki:"#F0E68C",lavender:"#E6E6FA",lavenderblush:"#FFF0F5",lawngreen:"#7CFC00",lemonchiffon:"#FFFACD",lightblue:"#ADD8E6",lightcoral:"#F08080",lightcyan:"#E0FFFF",lightgoldenrodyellow:"#FAFAD2",lightgreen:"#90EE90",lightgrey:"#D3D3D3",lightpink:"#FFB6C1",lightsalmon:"#FFA07A",lightseagreen:"#20B2AA",lightskyblue:"#87CEFA",lightslategray:"#778899",lightslategrey:"#778899",lightsteelblue:"#B0C4DE",lightyellow:"#FFFFE0",limegreen:"#32CD32",linen:"#FAF0E6",magenta:"#FF00FF",mediumaquamarine:"#66CDAA",mediumblue:"#0000CD",mediumorchid:"#BA55D3",mediumpurple:"#9370DB",mediumseagreen:"#3CB371",mediumslateblue:"#7B68EE",mediumspringgreen:"#00FA9A",mediumturquoise:"#48D1CC",mediumvioletred:"#C71585",midnightblue:"#191970",mintcream:"#F5FFFA",mistyrose:"#FFE4E1",moccasin:"#FFE4B5",navajowhite:"#FFDEAD",oldlace:"#FDF5E6",olivedrab:"#6B8E23",orange:"#FFA500",orangered:"#FF4500",orchid:"#DA70D6",palegoldenrod:"#EEE8AA",palegreen:"#98FB98",paleturquoise:"#AFEEEE",palevioletred:"#DB7093",papayawhip:"#FFEFD5",peachpuff:"#FFDAB9",peru:"#CD853F",pink:"#FFC0CB",plum:"#DDA0DD",powderblue:"#B0E0E6",rosybrown:"#BC8F8F",royalblue:"#4169E1",saddlebrown:"#8B4513",salmon:"#FA8072",sandybrown:"#F4A460",seagreen:"#2E8B57",seashell:"#FFF5EE",sienna:"#A0522D",skyblue:"#87CEEB",slateblue:"#6A5ACD",slategray:"#708090",slategrey:"#708090",snow:"#FFFAFA",springgreen:"#00FF7F",steelblue:"#4682B4",tan:"#D2B48C",thistle:"#D8BFD8",tomato:"#FF6347",turquoise:"#40E0D0",violet:"#EE82EE",wheat:"#F5DEB3",whitesmoke:"#F5F5F5",yellowgreen:"#9ACD32"};function l(i){var m=i.indexOf("(",3);var Z=i.indexOf(")",m+1);var j=i.substring(m+1,Z).split(",");if(j.length!=4||i.charAt(3)!="a"){j[3]=1}return j}function C(Z){return parseFloat(Z)/100}function N(i,j,Z){return Math.min(Z,Math.max(j,i))}function f(AG){var Z,AI,AJ,AH,AK,m;AH=parseFloat(AG[0])/360%360;if(AH<0){AH++}AK=N(C(AG[1]),0,1);m=N(C(AG[2]),0,1);if(AK==0){Z=AI=AJ=m}else{var i=m<0.5?m*(1+AK):m+AK-m*AK;var j=2*m-i;Z=A(j,i,AH+1/3);AI=A(j,i,AH);AJ=A(j,i,AH-1/3)}return"#"+I[Math.floor(Z*255)]+I[Math.floor(AI*255)]+I[Math.floor(AJ*255)]}function A(i,Z,j){if(j<0){j++}if(j>1){j--}if(6*j<1){return i+(Z-i)*6*j}else{if(2*j<1){return Z}else{if(3*j<2){return i+(Z-i)*(2/3-j)*6}else{return i}}}}var Y={};function c(Z){if(Z in Y){return Y[Z]}var AG,p=1;Z=String(Z);if(Z.charAt(0)=="#"){AG=Z}else{if(/^rgb/.test(Z)){var m=l(Z);var AG="#",AH;for(var j=0;j<3;j++){if(m[j].indexOf("%")!=-1){AH=Math.floor(C(m[j])*255)}else{AH=+m[j]}AG+=I[N(AH,0,255)]}p=+m[3]}else{if(/^hsl/.test(Z)){var m=l(Z);AG=f(m);p=m[3]}else{AG=B[Z]||Z}}}return Y[Z]={color:AG,alpha:p}}var L={style:"normal",variant:"normal",weight:"normal",size:10,family:"sans-serif"};var k={};function b(Z){if(k[Z]){return k[Z]}var m=document.createElement("div");var j=m.style;try{j.font=Z}catch(i){}return k[Z]={style:j.fontStyle||L.style,variant:j.fontVariant||L.variant,weight:j.fontWeight||L.weight,size:j.fontSize||L.size,family:j.fontFamily||L.family}}function Q(j,i){var Z={};for(var AH in j){Z[AH]=j[AH]}var AG=parseFloat(i.currentStyle.fontSize),m=parseFloat(j.size);if(typeof j.size=="number"){Z.size=j.size}else{if(j.size.indexOf("px")!=-1){Z.size=m}else{if(j.size.indexOf("em")!=-1){Z.size=AG*m}else{if(j.size.indexOf("%")!=-1){Z.size=(AG/100)*m}else{if(j.size.indexOf("pt")!=-1){Z.size=m/0.75}else{Z.size=AG}}}}}Z.size*=0.981;return Z}function AC(Z){return Z.style+" "+Z.variant+" "+Z.weight+" "+Z.size+"px "+Z.family}var O={"butt":"flat","round":"round"};function t(Z){return O[Z]||"square"}function a(Z){this.m_=X();this.mStack_=[];this.aStack_=[];this.currentPath_=[];this.strokeStyle="#000";this.fillStyle="#000";this.lineWidth=1;this.lineJoin="miter";this.lineDash=[];this.lineCap="butt";this.miterLimit=D*1;this.globalAlpha=1;this.font="10px sans-serif";this.textAlign="left";this.textBaseline="alphabetic";this.canvas=Z;var j="width:"+Z.clientWidth+"px;height:"+Z.clientHeight+"px;overflow:hidden;position:absolute";var i=Z.ownerDocument.createElement("div");i.style.cssText=j;Z.appendChild(i);var m=i.cloneNode(false);m.style.backgroundColor="red";m.style.filter="alpha(opacity=0)";Z.appendChild(m);this.element_=i;this.arcScaleX_=1;this.arcScaleY_=1;this.lineScale_=1}var M=a.prototype;M.clearRect=function(){if(this.textMeasureEl_){this.textMeasureEl_.removeNode(true);this.textMeasureEl_=null}this.element_.innerHTML=""};M.beginPath=function(){this.currentPath_=[]};M.moveTo=function(i,Z){var j=w(this,i,Z);this.currentPath_.push({type:"moveTo",x:j.x,y:j.y});this.currentX_=j.x;this.currentY_=j.y};M.lineTo=function(i,Z){var j=w(this,i,Z);this.currentPath_.push({type:"lineTo",x:j.x,y:j.y});this.currentX_=j.x;this.currentY_=j.y};M.bezierCurveTo=function(j,i,AK,AJ,AI,AG){var Z=w(this,AI,AG);var AH=w(this,j,i);var m=w(this,AK,AJ);h(this,AH,m,Z)};function h(Z,m,j,i){Z.currentPath_.push({type:"bezierCurveTo",cp1x:m.x,cp1y:m.y,cp2x:j.x,cp2y:j.y,x:i.x,y:i.y});Z.currentX_=i.x;Z.currentY_=i.y}M.quadraticCurveTo=function(AI,j,i,Z){var AH=w(this,AI,j);var AG=w(this,i,Z);var AJ={x:this.currentX_+2/3*(AH.x-this.currentX_),y:this.currentY_+2/3*(AH.y-this.currentY_)};var m={x:AJ.x+(AG.x-this.currentX_)/3,y:AJ.y+(AG.y-this.currentY_)/3};h(this,AJ,m,AG)};M.arc=function(AL,AJ,AK,AG,i,j){AK*=D;var AP=j?"at":"wa";var AM=AL+W(AG)*AK-F;var AO=AJ+J(AG)*AK-F;var Z=AL+W(i)*AK-F;var AN=AJ+J(i)*AK-F;if(AM==Z&&!j){AM+=0.125}var m=w(this,AL,AJ);var AI=w(this,AM,AO);var AH=w(this,Z,AN);this.currentPath_.push({type:AP,x:m.x,y:m.y,radius:AK,xStart:AI.x,yStart:AI.y,xEnd:AH.x,yEnd:AH.y})};M.rect=function(j,i,Z,m){this.moveTo(j,i);this.lineTo(j+Z,i);this.lineTo(j+Z,i+m);this.lineTo(j,i+m);this.closePath()};M.strokeRect=function(j,i,Z,m){var p=this.currentPath_;this.beginPath();this.moveTo(j,i);this.lineTo(j+Z,i);this.lineTo(j+Z,i+m);this.lineTo(j,i+m);this.closePath();this.stroke();this.currentPath_=p};M.fillRect=function(j,i,Z,m){var p=this.currentPath_;this.beginPath();this.moveTo(j,i);this.lineTo(j+Z,i);this.lineTo(j+Z,i+m);this.lineTo(j,i+m);this.closePath();this.fill();this.currentPath_=p};M.createLinearGradient=function(i,m,Z,j){var p=new v("gradient");p.x0_=i;p.y0_=m;p.x1_=Z;p.y1_=j;return p};M.createRadialGradient=function(m,AG,j,i,p,Z){var AH=new v("gradientradial");AH.x0_=m;AH.y0_=AG;AH.r0_=j;AH.x1_=i;AH.y1_=p;AH.r1_=Z;return AH};M.drawImage=function(AN,i){var AH,p,AJ,AR,AL,AK,AO,AU;var AI=AN.runtimeStyle.width;var AM=AN.runtimeStyle.height;AN.runtimeStyle.width="auto";AN.runtimeStyle.height="auto";var AG=AN.width;var AQ=AN.height;AN.runtimeStyle.width=AI;AN.runtimeStyle.height=AM;if(arguments.length==3){AH=arguments[1];p=arguments[2];AL=AK=0;AO=AJ=AG;AU=AR=AQ}else{if(arguments.length==5){AH=arguments[1];p=arguments[2];AJ=arguments[3];AR=arguments[4];AL=AK=0;AO=AG;AU=AQ}else{if(arguments.length==9){AL=arguments[1];AK=arguments[2];AO=arguments[3];AU=arguments[4];AH=arguments[5];p=arguments[6];AJ=arguments[7];AR=arguments[8]}else{throw Error("Invalid number of arguments")}}}var AT=w(this,AH,p);var AS=[];var Z=10;var j=10;var AP=this.m_;AS.push(" <g_vml_:group",' coordsize="',D*Z,",",D*j,'"',' coordorigin="0,0"',' style="width:',K(Z*AP[0][0]),"px;height:",K(j*AP[1][1]),"px;position:absolute;","top:",K(AT.y/D),"px;left:",K(AT.x/D),"px; rotation:",K(Math.atan(AP[0][1]/AP[1][1])*180/Math.PI),";");AS.push('" >','<g_vml_:image src="',AN.src,'"',' style="width:',D*AJ,"px;"," height:",D*AR,'px"',' cropleft="',AL/AG,'"',' croptop="',AK/AQ,'"',' cropright="',(AG-AL-AO)/AG,'"',' cropbottom="',(AQ-AK-AU)/AQ,'"'," />","</g_vml_:group>");this.element_.insertAdjacentHTML("BeforeEnd",AS.join(""))};M.setLineDash=function(Z){if(Z.length===1){Z=Z.slice();Z[1]=Z[0]}this.lineDash=Z};M.getLineDash=function(){return this.lineDash};M.stroke=function(AK){var AI=[];var j=10;var AL=10;AI.push("<g_vml_:shape",' filled="',!!AK,'"',' style="position:absolute;width:',j,"px;height:",AL,'px;left:0px;top:0px;"',' coordorigin="0,0"',' coordsize="',D*j,",",D*AL,'"',' stroked="',!AK,'"',' path="');var m={x:null,y:null};var AJ={x:null,y:null};for(var AG=0;AG<this.currentPath_.length;AG++){var Z=this.currentPath_[AG];var AH;switch(Z.type){case"moveTo":AH=Z;AI.push(" m ",K(Z.x),",",K(Z.y));break;case"lineTo":AI.push(" l ",K(Z.x),",",K(Z.y));break;case"close":AI.push(" x ");Z=null;break;case"bezierCurveTo":AI.push(" c ",K(Z.cp1x),",",K(Z.cp1y),",",K(Z.cp2x),",",K(Z.cp2y),",",K(Z.x),",",K(Z.y));break;case"at":case"wa":AI.push(" ",Z.type," ",K(Z.x-this.arcScaleX_*Z.radius),",",K(Z.y-this.arcScaleY_*Z.radius)," ",K(Z.x+this.arcScaleX_*Z.radius),",",K(Z.y+this.arcScaleY_*Z.radius)," ",K(Z.xStart),",",K(Z.yStart)," ",K(Z.xEnd),",",K(Z.yEnd));break}if(Z){if(m.x==null||Z.x<m.x){m.x=Z.x}if(AJ.x==null||Z.x>AJ.x){AJ.x=Z.x}if(m.y==null||Z.y<m.y){m.y=Z.y}if(AJ.y==null||Z.y>AJ.y){AJ.y=Z.y}}}AI.push(' ">');if(!AK){S(this,AI)}else{d(this,AI,m,AJ)}AI.push("</g_vml_:shape>");this.element_.insertAdjacentHTML("beforeEnd",AI.join(""))};function S(j,AG){var i=c(j.strokeStyle);var m=i.color;var p=i.alpha*j.globalAlpha;var Z=j.lineScale_*j.lineWidth;if(Z<1){p*=Z}AG.push("<g_vml_:stroke",' opacity="',p,'"',' joinstyle="',j.lineJoin,'"',' dashstyle="',j.lineDash.join(" "),'"',' miterlimit="',j.miterLimit,'"',' endcap="',t(j.lineCap),'"',' weight="',Z,'px"',' color="',m,'" />')}function d(AQ,AI,Aj,AR){var AJ=AQ.fillStyle;var Aa=AQ.arcScaleX_;var AZ=AQ.arcScaleY_;var Z=AR.x-Aj.x;var m=AR.y-Aj.y;if(AJ instanceof v){var AN=0;var Ae={x:0,y:0};var AW=0;var AM=1;if(AJ.type_=="gradient"){var AL=AJ.x0_/Aa;var j=AJ.y0_/AZ;var AK=AJ.x1_/Aa;var Al=AJ.y1_/AZ;var Ai=w(AQ,AL,j);var Ah=w(AQ,AK,Al);var AG=Ah.x-Ai.x;var p=Ah.y-Ai.y;AN=Math.atan2(AG,p)*180/Math.PI;if(AN<0){AN+=360}if(AN<0.000001){AN=0}}else{var Ai=w(AQ,AJ.x0_,AJ.y0_);Ae={x:(Ai.x-Aj.x)/Z,y:(Ai.y-Aj.y)/m};Z/=Aa*D;m/=AZ*D;var Ac=AB.max(Z,m);AW=2*AJ.r0_/Ac;AM=2*AJ.r1_/Ac-AW}var AU=AJ.colors_;AU.sort(function(Am,i){return Am.offset-i.offset});var AP=AU.length;var AT=AU[0].color;var AS=AU[AP-1].color;var AY=AU[0].alpha*AQ.globalAlpha;var AX=AU[AP-1].alpha*AQ.globalAlpha;var Ad=[];for(var Ag=0;Ag<AP;Ag++){var AO=AU[Ag];Ad.push(AO.offset*AM+AW+" "+AO.color)}AI.push('<g_vml_:fill type="',AJ.type_,'"',' method="none" focus="100%"',' color="',AT,'"',' color2="',AS,'"',' colors="',Ad.join(","),'"',' opacity="',AX,'"',' g_o_:opacity2="',AY,'"',' angle="',AN,'"',' focusposition="',Ae.x,",",Ae.y,'" />')}else{if(AJ instanceof u){if(Z&&m){var AH=-Aj.x;var Ab=-Aj.y;AI.push("<g_vml_:fill",' position="',AH/Z*Aa*Aa,",",Ab/m*AZ*AZ,'"',' type="tile"',' src="',AJ.src_,'" />')}}else{var Ak=c(AQ.fillStyle);var AV=Ak.color;var Af=Ak.alpha*AQ.globalAlpha;AI.push('<g_vml_:fill color="',AV,'" opacity="',Af,'" />')}}}M.fill=function(){this.$stroke(true)};M.closePath=function(){this.currentPath_.push({type:"close"})};function w(i,p,j){var Z=i.m_;return{x:D*(p*Z[0][0]+j*Z[1][0]+Z[2][0])-F,y:D*(p*Z[0][1]+j*Z[1][1]+Z[2][1])-F}}M.save=function(){var Z={};R(this,Z);this.aStack_.push(Z);this.mStack_.push(this.m_);this.m_=g(X(),this.m_)};M.restore=function(){if(this.aStack_.length){R(this.aStack_.pop(),this);this.m_=this.mStack_.pop()}};function H(Z){return isFinite(Z[0][0])&&isFinite(Z[0][1])&&isFinite(Z[1][0])&&isFinite(Z[1][1])&&isFinite(Z[2][0])&&isFinite(Z[2][1])}function AA(i,Z,j){if(!H(Z)){return }i.m_=Z;if(j){var p=Z[0][0]*Z[1][1]-Z[0][1]*Z[1][0];i.lineScale_=n(e(p))}}M.translate=function(j,i){var Z=[[1,0,0],[0,1,0],[j,i,1]];AA(this,g(Z,this.m_),false)};M.rotate=function(i){var m=W(i);var j=J(i);var Z=[[m,j,0],[-j,m,0],[0,0,1]];AA(this,g(Z,this.m_),false)};M.scale=function(j,i){this.arcScaleX_*=j;this.arcScaleY_*=i;var Z=[[j,0,0],[0,i,0],[0,0,1]];AA(this,g(Z,this.m_),true)};M.transform=function(p,m,AH,AG,i,Z){var j=[[p,m,0],[AH,AG,0],[i,Z,1]];AA(this,g(j,this.m_),true)};M.setTransform=function(AG,p,AI,AH,j,i){var Z=[[AG,p,0],[AI,AH,0],[j,i,1]];AA(this,Z,true)};M.drawText_=function(AM,AK,AJ,AP,AI){var AO=this.m_,AS=1000,i=0,AR=AS,AH={x:0,y:0},AG=[];var Z=Q(b(this.font),this.element_);var j=AC(Z);var AT=this.element_.currentStyle;var p=this.textAlign.toLowerCase();switch(p){case"left":case"center":case"right":break;case"end":p=AT.direction=="ltr"?"right":"left";break;case"start":p=AT.direction=="rtl"?"right":"left";break;default:p="left"}switch(this.textBaseline){case"hanging":case"top":AH.y=Z.size/1.75;break;case"middle":break;default:case null:case"alphabetic":case"ideographic":case"bottom":AH.y=-Z.size/3;break}switch(p){case"right":i=AS;AR=0.05;break;case"center":i=AR=AS/2;break}var AQ=w(this,AK+AH.x,AJ+AH.y);AG.push('<g_vml_:line from="',-i,' 0" to="',AR,' 0.05" ',' coordsize="100 100" coordorigin="0 0"',' filled="',!AI,'" stroked="',!!AI,'" style="position:absolute;width:1px;height:1px;left:0px;top:0px;">');if(AI){S(this,AG)}else{d(this,AG,{x:-i,y:0},{x:AR,y:Z.size})}var AN=AO[0][0].toFixed(3)+","+AO[1][0].toFixed(3)+","+AO[0][1].toFixed(3)+","+AO[1][1].toFixed(3)+",0,0";var AL=K(AQ.x/D)+","+K(AQ.y/D);AG.push('<g_vml_:skew on="t" matrix="',AN,'" ',' offset="',AL,'" origin="',i,' 0" />','<g_vml_:path textpathok="true" />','<g_vml_:textpath on="true" string="',AF(AM),'" style="v-text-align:',p,";font:",AF(j),'" /></g_vml_:line>');this.element_.insertAdjacentHTML("beforeEnd",AG.join(""))};M.fillText=function(j,Z,m,i){this.drawText_(j,Z,m,i,false)};M.strokeText=function(j,Z,m,i){this.drawText_(j,Z,m,i,true)};M.measureText=function(j){if(!this.textMeasureEl_){var Z='<span style="position:absolute;top:-20000px;left:0;padding:0;margin:0;border:none;white-space:pre;"></span>';this.element_.insertAdjacentHTML("beforeEnd",Z);this.textMeasureEl_=this.element_.lastChild}var i=this.element_.ownerDocument;this.textMeasureEl_.innerHTML="";this.textMeasureEl_.style.font=this.font;this.textMeasureEl_.appendChild(i.createTextNode(j));return{width:this.textMeasureEl_.offsetWidth}};M.clip=function(){};M.arcTo=function(){};M.createPattern=function(i,Z){return new u(i,Z)};function v(Z){this.type_=Z;this.x0_=0;this.y0_=0;this.r0_=0;this.x1_=0;this.y1_=0;this.r1_=0;this.colors_=[]}v.prototype.addColorStop=function(i,Z){Z=c(Z);this.colors_.push({offset:i,color:Z.color,alpha:Z.alpha})};function u(i,Z){r(i);switch(Z){case"repeat":case null:case"":this.repetition_="repeat";break;case"repeat-x":case"repeat-y":case"no-repeat":this.repetition_=Z;break;default:o("SYNTAX_ERR")}this.src_=i.src;this.width_=i.width;this.height_=i.height}function o(Z){throw new q(Z)}function r(Z){if(!Z||Z.nodeType!=1||Z.tagName!="IMG"){o("TYPE_MISMATCH_ERR")}if(Z.readyState!="complete"){o("INVALID_STATE_ERR")}}function q(Z){this.code=this[Z];this.message=Z+": DOM Exception "+this.code}var y=q.prototype=new Error;y.INDEX_SIZE_ERR=1;y.DOMSTRING_SIZE_ERR=2;y.HIERARCHY_REQUEST_ERR=3;y.WRONG_DOCUMENT_ERR=4;y.INVALID_CHARACTER_ERR=5;y.NO_DATA_ALLOWED_ERR=6;y.NO_MODIFICATION_ALLOWED_ERR=7;y.NOT_FOUND_ERR=8;y.NOT_SUPPORTED_ERR=9;y.INUSE_ATTRIBUTE_ERR=10;y.INVALID_STATE_ERR=11;y.SYNTAX_ERR=12;y.INVALID_MODIFICATION_ERR=13;y.NAMESPACE_ERR=14;y.INVALID_ACCESS_ERR=15;y.VALIDATION_ERR=16;y.TYPE_MISMATCH_ERR=17;G_vmlCanvasManager=E;CanvasRenderingContext2D=a;CanvasGradient=v;CanvasPattern=u;DOMException=q})()}