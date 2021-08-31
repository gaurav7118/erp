Ext.define("Ext.overrides.dom.Element",(function(){var I,K=window,d=document,l="hidden",P="isClipped",j="overflow",T="overflow-x",S="overflow-y",W="originalClip",b="height",E="width",F="visibility",A="display",f="none",n="offsets",C="clip",L="originalDisplay",g="visibilityMode",X="isVisible",J=Ext.baseCSSPrefix+"hidden-offsets",O=Ext.baseCSSPrefix+"hidden-clip",o=['<div class="{0}-tl" role="presentation">','<div class="{0}-tr" role="presentation">','<div class="{0}-tc" role="presentation"></div>',"</div>","</div>",'<div class="{0}-ml" role="presentation">','<div class="{0}-mr" role="presentation">','<div class="{0}-mc" role="presentation"></div>',"</div>","</div>",'<div class="{0}-bl" role="presentation">','<div class="{0}-br" role="presentation">','<div class="{0}-bc" role="presentation"></div>',"</div>","</div>"].join(""),i=/(?:<script([^>]*)?>)((\n|\r|.)*?)(?:<\/script>)/ig,c=/(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)/ig,B=/\ssrc=([\'\"])(.*?)\1/i,N=/\S/,H=/\stype=([\'\"])(.*?)\1/i,Z=/^-ms-/,a=/(-[a-z])/gi,h=function(p,q){return q.charAt(1).toUpperCase()},M=Ext.baseCSSPrefix+"masked",Y=Ext.baseCSSPrefix+"masked-relative",U=Ext.baseCSSPrefix+"mask-msg",R=/^body/i,Q={},V=function(q){var r=q.getData(),p=r[g];if(p===undefined){r[g]=p=I.VISIBILITY}return p},m=d.createRange?d.createRange():null,e={INPUT:true,TEXTAREA:true};if(Ext.isIE8){var G=d.createElement("div"),k=[],D=Ext.Function.createBuffered(function(){var p=k.length,q;for(q=0;q<p;q++){G.appendChild(k[q])}G.innerHTML="";k.length=0},10)}return{override:"Ext.dom.Element",mixins:["Ext.util.Animate"],uses:["Ext.dom.GarbageCollector","Ext.dom.Fly","Ext.event.publisher.MouseEnterLeave","Ext.fx.Manager","Ext.fx.Anim"],skipGarbageCollection:false,_init:function(p){I=p;if(K.__UNIT_TESTING__){p.destroyQueue=k}p.tabbableSelector+=",["+p.tabbableSavedCounterAttribute+"]"},statics:{selectableCls:Ext.baseCSSPrefix+"selectable",unselectableCls:Ext.baseCSSPrefix+"unselectable",tabbableSelector:Ext.supports.CSS3NegationSelector?'a[href],button,iframe,input,select,textarea,[tabindex]:not([tabindex="-1"]),[contenteditable="true"]':'a[href],button,iframe,input,select,textarea,[tabindex],[contenteditable="true"]',naturallyFocusableTags:{BUTTON:true,IFRAME:true,EMBED:true,INPUT:true,OBJECT:true,SELECT:true,TEXTAREA:true,HTML:Ext.isIE?true:false},naturallyTabbableTags:{BUTTON:true,IFRAME:true,INPUT:true,SELECT:true,TEXTAREA:true,OBJECT:Ext.isIE8m?true:false},tabbableSavedCounterAttribute:"data-tabindex-counter",tabbableSavedValueAttribute:"data-tabindex-value",normalize:function(p){if(p==="float"){p=Ext.supports.Float?"cssFloat":"styleFloat"}return Q[p]||(Q[p]=p.replace(Z,"ms-").replace(a,h))}},addClsOnClick:function(r,u,q){var s=this,t=s.dom,p=Ext.isFunction(u);s.on("mousedown",function(){if(p&&u.call(q||s,s)===false){return false}Ext.fly(t).addCls(r);var w=Ext.getDoc(),v=function(){Ext.fly(t).removeCls(r);w.removeListener("mouseup",v)};w.on("mouseup",v)});return s},addClsOnFocus:function(r,u,q){var s=this,t=s.dom,p=Ext.isFunction(u);s.on("focus",function(){if(p&&u.call(q||s,s)===false){return false}Ext.fly(t).addCls(r)});s.on("blur",function(){Ext.fly(t).removeCls(r)});return s},addClsOnOver:function(r,u,q){var s=this,t=s.dom,p=Ext.isFunction(u);s.hover(function(){if(p&&u.call(q||s,s)===false){return }Ext.fly(t).addCls(r)},function(){Ext.fly(t).removeCls(r)});return s},addKeyListener:function(q,s,r){var p;if(typeof q!=="object"||Ext.isArray(q)){p={target:this,key:q,fn:s,scope:r}}else{p={target:this,key:q.key,shift:q.shift,ctrl:q.ctrl,alt:q.alt,fn:s,scope:r}}return new Ext.util.KeyMap(p)},addKeyMap:function(p){return new Ext.util.KeyMap(Ext.apply({target:this},p))},afterAnimate:function(){var p=this.shadow;if(p&&!p.disabled&&!p.animate){p.show()}},anchorAnimX:function(p){var q=(p==="l")?"right":"left";this.dom.style[q]="0px"},anim:function(p){if(!Ext.isObject(p)){return(p)?{}:false}var q=this,r=p.duration||Ext.fx.Anim.prototype.duration,t=p.easing||"ease",s;if(p.stopAnimation){q.stopAnimation()}Ext.applyIf(p,Ext.fx.Manager.getFxDefaults(q.id));Ext.fx.Manager.setFxDefaults(q.id,{delay:0});s={target:q.dom,remove:p.remove,alternate:p.alternate||false,duration:r,easing:t,callback:p.callback,listeners:p.listeners,iterations:p.iterations||1,scope:p.scope,block:p.block,concurrent:p.concurrent,delay:p.delay||0,paused:true,keyframes:p.keyframes,from:p.from||{},to:Ext.apply({},p),userConfig:p};Ext.apply(s.to,p.to);delete s.to.to;delete s.to.from;delete s.to.remove;delete s.to.alternate;delete s.to.keyframes;delete s.to.iterations;delete s.to.listeners;delete s.to.target;delete s.to.paused;delete s.to.callback;delete s.to.scope;delete s.to.duration;delete s.to.easing;delete s.to.concurrent;delete s.to.block;delete s.to.stopAnimation;delete s.to.delay;return s},animate:function(p){this.addAnimation(p);return this},addAnimation:function(r){var t=this,q=t.dom.id||Ext.id(t.dom),s,u,p;if(!Ext.fx.Manager.hasFxBlock(q)){if(r.listeners){s=r.listeners;delete r.listeners}if(r.internalListeners){r.listeners=r.internalListeners;delete r.internalListeners}p=r.autoEnd;delete r.autoEnd;u=new Ext.fx.Anim(t.anim(r));u.on({afteranimate:"afterAnimate",beforeanimate:"beforeAnimate",scope:t,single:true});if(s){u.on(s)}Ext.fx.Manager.queueFx(u);if(p){u.jumpToEnd()}}return u},beforeAnimate:function(){var p=this.shadow;if(p&&!p.disabled&&!p.animate){p.hide()}},boxWrap:function(p){p=p||Ext.baseCSSPrefix+"box";var q=Ext.get(this.insertHtml("beforeBegin","<div class='"+p+"' role='presentation'>"+Ext.String.format(o,p)+"</div>"));q.selectNode("."+p+"-mc").appendChild(this.dom);return q},clean:function(q){var s=this,u=s.dom,t=s.getData(),v=u.firstChild,r=-1,p;if(t.isCleaned&&q!==true){return s}while(v){p=v.nextSibling;if(v.nodeType===3){if(!(N.test(v.nodeValue))){u.removeChild(v)}else{if(p&&p.nodeType===3){v.appendData(Ext.String.trim(p.data));u.removeChild(p);p=v.nextSibling;v.nodeIndex=++r}}}else{Ext.fly(v,"_clean").clean();v.nodeIndex=++r}v=p}t.isCleaned=true;return s},empty:m?function(){var p=this.dom;if(p.firstChild){m.setStartBefore(p.firstChild);m.setEndAfter(p.lastChild);m.deleteContents()}}:function(){var p=this.dom;while(p.lastChild){p.removeChild(p.lastChild)}},clearListeners:function(){this.removeAnchor();this.callParent()},clearPositioning:function(p){p=p||"";return this.setStyle({left:p,right:p,top:p,bottom:p,"z-index":"",position:"static"})},createProxy:function(p,t,s){p=(typeof p==="object")?p:{tag:"div",role:"presentation",cls:p};var r=this,q=t?Ext.DomHelper.append(t,p,true):Ext.DomHelper.insertBefore(r.dom,p,true);q.setVisibilityMode(I.DISPLAY);q.hide();if(s&&r.setBox&&r.getBox){q.setBox(r.getBox())}return q},clearOpacity:function(){return this.setOpacity("")},clip:function(){var q=this,r=q.getData(),p;if(!r[P]){r[P]=true;p=q.getStyle([j,T,S]);r[W]={o:p[j],x:p[T],y:p[S]};q.setStyle(j,l);q.setStyle(T,l);q.setStyle(S,l)}return q},destroy:function(){var r=this,t=r.dom,s=r.getData(),q,p;if(t&&r.isAnimate){r.stopAnimation()}r.callParent();if(t&&Ext.isIE8&&(t.window!=t)&&(t.nodeType!==9)&&(t.tagName!=="BODY")&&(t.tagName!=="HTML")){k[k.length]=t;D()}if(s){q=s.maskEl;p=s.maskMsg;if(q){q.destroy()}if(p){p.destroy()}}},enableDisplayMode:function(q){var p=this;p.setVisibilityMode(I.DISPLAY);if(q!==undefined){p.getData()[L]=q}return p},fadeIn:function(r){var p=this,q=p.dom;p.animate(Ext.apply({},r,{opacity:1,internalListeners:{beforeanimate:function(t){var s=Ext.fly(q,"_anim");if(s.isStyle("display","none")){s.setDisplayed("")}else{s.show()}}}}));return this},fadeOut:function(r){var p=this,q=p.dom;r=Ext.apply({opacity:0,internalListeners:{afteranimate:function(t){if(q&&t.to.opacity===0){var s=Ext.fly(q,"_anim");if(r.useDisplay){s.setDisplayed(false)}else{s.hide()}}}}},r);p.animate(r);return p},fixDisplay:function(){var p=this;if(p.isStyle(A,f)){p.setStyle(F,l);p.setStyle(A,p._getDisplay());if(p.isStyle(A,f)){p.setStyle(A,"block")}}},frame:function(p,s,t){var r=this,u=r.dom,q;p=p||"#C3DAF9";s=s||1;t=t||{};q=function(){var y=Ext.fly(u,"_anim"),x=this,z,w,v;y.show();z=y.getBox();w=Ext.getBody().createChild({role:"presentation",id:y.dom.id+"-anim-proxy",style:{position:"absolute","pointer-events":"none","z-index":35000,border:"0px solid "+p}});v=new Ext.fx.Anim({target:w,duration:t.duration||1000,iterations:s,from:{top:z.y,left:z.x,borderWidth:0,opacity:1,height:z.height,width:z.width},to:{top:z.y-20,left:z.x-20,borderWidth:10,opacity:0,height:z.height+40,width:z.width+40}});v.on("afteranimate",function(){w.destroy();x.end()})};r.animate({duration:(Math.max(t.duration,500)*2)||2000,listeners:{beforeanimate:{fn:q}},callback:t.callback,scope:t.scope});return r},getColor:function(q,r,x){var t=this.getStyle(q),s=x||x===""?x:"#",w,p,u=0;if(!t||(/transparent|inherit/.test(t))){return r}if(/^r/.test(t)){t=t.slice(4,t.length-1).split(",");p=t.length;for(;u<p;u++){w=parseInt(t[u],10);s+=(w<16?"0":"")+w.toString(16)}}else{t=t.replace("#","");s+=t.length===3?t.replace(/^(\w)(\w)(\w)$/,"$1$1$2$2$3$3"):t}return(s.length>5?s.toLowerCase():r)},getLoader:function(){var q=this,r=q.getData(),p=r.loader;if(!p){r.loader=p=new Ext.ElementLoader({target:q})}return p},getPositioning:function(q){var p=this.getStyle(["left","top","position","z-index"]),r=this.dom;if(q){if(p.left==="auto"){p.left=r.offsetLeft+"px"}if(p.top==="auto"){p.top=r.offsetTop+"px"}}return p},ghost:function(p,s){var r=this,t=r.dom,q;p=p||"b";q=function(){var x=Ext.fly(t,"_anim"),w=x.getWidth(),v=x.getHeight(),y=x.getXY(),u=x.getPositioning(),z={opacity:0};switch(p){case"t":z.y=y[1]-v;break;case"l":z.x=y[0]-w;break;case"r":z.x=y[0]+w;break;case"b":z.y=y[1]+v;break;case"tl":z.x=y[0]-w;z.y=y[1]-v;break;case"bl":z.x=y[0]-w;z.y=y[1]+v;break;case"br":z.x=y[0]+w;z.y=y[1]+v;break;case"tr":z.x=y[0]+w;z.y=y[1]-v;break}this.to=z;this.on("afteranimate",function(){var AA=Ext.fly(t,"_anim");if(AA){AA.hide();AA.clearOpacity();AA.setPositioning(u)}})};r.animate(Ext.applyIf(s||{},{duration:500,easing:"ease-out",listeners:{beforeanimate:q}}));return r},hide:function(p){if(typeof p==="string"){this.setVisible(false,p);return this}this.setVisible(false,this.anim(p));return this},highlight:function(s,q){var w=this,t=w.dom,y={},v,z,u,r,p,x;q=q||{};r=q.listeners||{};u=q.attr||"backgroundColor";y[u]=s||"ffff9c";if(!q.to){z={};z[u]=q.endColor||w.getColor(u,"ffffff","")}else{z=q.to}q.listeners=Ext.apply(Ext.apply({},r),{beforeanimate:function(){v=t.style[u];var AA=Ext.fly(t,"_anim");AA.clearOpacity();AA.show();p=r.beforeanimate;if(p){x=p.fn||p;return x.apply(p.scope||r.scope||K,arguments)}},afteranimate:function(){if(t){t.style[u]=v}p=r.afteranimate;if(p){x=p.fn||p;x.apply(p.scope||r.scope||K,arguments)}}});w.animate(Ext.apply({},q,{duration:1000,easing:"ease-in",from:y,to:z}));return w},hover:function(q,p,s,r){var t=this;t.on("mouseenter",q,s||t.dom,r);t.on("mouseleave",p,s||t.dom,r);return t},initDD:function(r,q,s){var p=new Ext.dd.DD(Ext.id(this.dom),r,q);return Ext.apply(p,s)},initDDProxy:function(r,q,s){var p=new Ext.dd.DDProxy(Ext.id(this.dom),r,q);return Ext.apply(p,s)},initDDTarget:function(r,q,s){var p=new Ext.dd.DDTarget(Ext.id(this.dom),r,q);return Ext.apply(p,s)},isFocusable:function(){var q=this.dom,p=false,r;if(q&&!q.disabled){r=q.nodeName;p=!!Ext.Element.naturallyFocusableTags[r]||((r==="A"||r==="LINK")&&!!q.href)||q.getAttribute("tabIndex")!=null||q.contentEditable==="true";if(Ext.isIE8&&r==="INPUT"&&q.type==="hidden"){p=false}p=p&&this.isVisible(true)}return p},isInputField:function(){var q=this.dom,p=q.contentEditable;if((e[q.tagName]&&q.type!=="button")||(p===""||p==="true")){return true}return false},isTabbable:function(r){var s=this.dom,u=false,t,q,p;if(s&&!s.disabled){t=s.nodeName;p=s.getAttribute("tabIndex");q=p!=null;p-=0;if(t==="A"||t==="LINK"){if(s.href){u=q&&p<0?false:true}else{if(s.contentEditable==="true"){u=!q||(q&&p>=0)?true:false}else{u=q&&p>=0?true:false}}}else{if(s.contentEditable==="true"||Ext.Element.naturallyTabbableTags[t]){u=q&&p<0?false:true}else{if(q&&p>=0){u=true}}}if(Ext.isIE8&&t==="INPUT"&&s.type==="hidden"){u=false}u=u&&(r||((!this.component||this.component.isVisible(true))&&this.isVisible(true)))}return u},isMasked:function(p){var t=this,v=t.getData(),s=v.maskEl,q=v.maskMsg,u=false,r;if(s&&s.isVisible()){if(q){q.center(t)}u=true}else{if(p){r=t.findParentNode();if(r){return Ext.fly(r).isMasked(p)}}}return u},load:function(p){this.getLoader().load(p);return this},mask:function(w,u,p){var s=this,v=s.dom,t=s.getData(),r=t.maskEl,q;if(!(R.test(v.tagName)&&s.getStyle("position")==="static")){s.addCls(Y)}if(r){r.destroy()}r=Ext.DomHelper.append(v,{role:"presentation",cls:Ext.baseCSSPrefix+"mask "+Ext.baseCSSPrefix+"border-box",children:{role:"presentation",cls:u?U+" "+u:U,cn:{tag:"div",role:"presentation",cls:Ext.baseCSSPrefix+"mask-msg-inner",cn:{tag:"div",role:"presentation",cls:Ext.baseCSSPrefix+"mask-msg-text",html:w||""}}}},true);q=Ext.get(r.dom.firstChild);t.maskEl=r;s.addCls(M);r.setDisplayed(true);if(typeof w==="string"){q.setDisplayed(true);q.center(s)}else{q.setDisplayed(false)}if(v===d.body){r.addCls(Ext.baseCSSPrefix+"mask-fixed")}s.saveTabbableState({skipSelf:v===d.body});if(Ext.isIE9m&&v!==d.body&&s.isStyle("height","auto")){r.setSize(undefined,p||s.getHeight())}return r},monitorMouseLeave:function(p,s,r){var t=this,u,q={mouseleave:function(v){if(Ext.isIE9m){v.enableIEAsync()}u=Ext.defer(s,p,r||t,[v])},mouseenter:function(){clearTimeout(u)}};t.on(q);return q},puff:function(t){var s=this,u=s.dom,q,r=s.getBox(),p=s.getStyle(["width","height","left","right","top","bottom","position","z-index","font-size","opacity"],true);t=Ext.applyIf(t||{},{easing:"ease-out",duration:500,useDisplay:false});q=function(){var v=Ext.fly(u,"_anim");v.clearOpacity();v.show();this.to={width:r.width*2,height:r.height*2,x:r.x-(r.width/2),y:r.y-(r.height/2),opacity:0,fontSize:"200%"};this.on("afteranimate",function(){var w=Ext.fly(u,"_anim");if(w){if(t.useDisplay){w.setDisplayed(false)}else{w.hide()}w.setStyle(p);Ext.callback(t.callback,t.scope)}})};s.animate({duration:t.duration,easing:t.easing,listeners:{beforeanimate:{fn:q}}});return s},selectable:function(){var p=this;p.dom.unselectable="";p.removeCls(I.unselectableCls);p.addCls(I.selectableCls);return p},setCapture:function(){var p=this.dom;if(Ext.isIE9m&&p.setCapture){p.setCapture()}},setHeight:function(p,q){var r=this;if(!q||!r.anim){r.callParent(arguments)}else{if(!Ext.isObject(q)){q={}}r.animate(Ext.applyIf({to:{height:p}},q))}return r},setHorizontal:function(){var q=this,p=q.verticalCls;delete q.vertical;if(p){delete q.verticalCls;q.removeCls(p)}delete q.setWidth;delete q.setHeight;if(!Ext.isIE8){delete q.getWidth;delete q.getHeight}delete q.styleHooks},updateText:function(s){var p=this,r,q;if(r){q=r.firstChild;if(!q||(q.nodeType!==3||q.nextSibling)){q=d.createTextNode();p.empty();r.appendChild(q)}if(s){q.data=s}}},setHtml:function(r,q,w,s){var t=this,v,u,p;if(!t.dom){return t}r=r||"";u=t.dom;if(q!==true){u.innerHTML=r;Ext.callback(w,t);return t}v=Ext.id();r+='<span id="'+v+'" role="presentation"></span>';p=Ext.interval(function(){var AD,AA,z,y,x,AC,AB;if(!(AC=d.getElementById(v))){return false}clearInterval(p);Ext.removeNode(AC);AD=Ext.getHead().dom;while((AA=i.exec(r))){z=AA[1];y=z?z.match(B):false;if(y&&y[2]){AB=d.createElement("script");AB.src=y[2];x=z.match(H);if(x&&x[2]){AB.type=x[2]}AD.appendChild(AB)}else{if(AA[2]&&AA[2].length>0){if(s){Ext.functionFactory(AA[2]).call(s)}else{Ext.globalEval(AA[2])}}}}Ext.callback(w,s||t)},20);u.innerHTML=r.replace(c,"");return t},setOpacity:function(q,p){var r=this;if(!r.dom){return r}if(!p||!r.anim){r.setStyle("opacity",q)}else{if(typeof p!="object"){p={duration:350,easing:"ease-in"}}r.animate(Ext.applyIf({to:{opacity:q}},p))}return r},setPositioning:function(p){return this.setStyle(p)},setVertical:function(s,p){var r=this,q=I.prototype;r.vertical=true;if(p){r.addCls(r.verticalCls=p)}r.setWidth=q.setHeight;r.setHeight=q.setWidth;if(!Ext.isIE8){r.getWidth=q.getHeight;r.getHeight=q.getWidth}r.styleHooks=(s===270)?q.verticalStyleHooks270:q.verticalStyleHooks90},setSize:function(r,p,q){var s=this;if(Ext.isObject(r)){q=p;p=r.height;r=r.width}if(!q||!s.anim){s.dom.style.width=I.addUnits(r);s.dom.style.height=I.addUnits(p);if(s.shadow||s.shim){s.syncUnderlays()}}else{if(q===true){q={}}s.animate(Ext.applyIf({to:{width:r,height:p}},q))}return s},setVisible:function(t,p){var r=this,s=r.dom,q=V(r);if(typeof p==="string"){switch(p){case A:q=I.DISPLAY;break;case F:q=I.VISIBILITY;break;case n:q=I.OFFSETS;break;case C:q=I.CLIP;break}r.setVisibilityMode(q);p=false}if(!p||!r.anim){if(q===I.DISPLAY){return r.setDisplayed(t)}else{if(q===I.OFFSETS){r[t?"removeCls":"addCls"](J)}else{if(q===I.CLIP){r[t?"removeCls":"addCls"](O)}else{if(q===I.VISIBILITY){r.fixDisplay();s.style.visibility=t?"":l}}}}}else{if(t){r.setOpacity(0.01);r.setVisible(true)}if(!Ext.isObject(p)){p={duration:350,easing:"ease-in"}}r.animate(Ext.applyIf({callback:function(){if(!t){Ext.fly(s).setVisible(false).setOpacity(1)}},to:{opacity:(t)?1:0}},p))}r.getData()[X]=t;if(r.shadow||r.shim){r.setUnderlaysVisible(t)}return r},setWidth:function(q,p){var r=this;if(!p||!r.anim){r.callParent(arguments)}else{if(!Ext.isObject(p)){p={}}r.animate(Ext.applyIf({to:{width:q}},p))}return r},setX:function(p,q){return this.setXY([p,this.getY()],q)},setXY:function(r,p){var q=this;if(!p||!q.anim){q.callParent([r])}else{if(!Ext.isObject(p)){p={}}q.animate(Ext.applyIf({to:{x:r[0],y:r[1]}},p))}return this},setY:function(q,p){return this.setXY([this.getX(),q],p)},show:function(p){if(typeof p==="string"){this.setVisible(true,p);return this}this.setVisible(true,this.anim(p));return this},slideIn:function(s,r,t){var v=this,q=v.dom,y=q.style,x,p,u,w;s=s||"t";r=r||{};x=function(){var AD=this,AC=r.listeners,AB=Ext.fly(q,"_anim"),AE,z,AF,AA;if(!t){AB.fixDisplay()}AE=AB.getBox();if((s=="t"||s=="b")&&AE.height===0){AE.height=q.scrollHeight}else{if((s=="l"||s=="r")&&AE.width===0){AE.width=q.scrollWidth}}z=AB.getStyle(["width","height","left","right","top","bottom","position","z-index"],true);AB.setSize(AE.width,AE.height);if(r.preserveScroll){u=AB.cacheScrollValues()}AA=AB.wrap({role:"presentation",id:Ext.id()+"-anim-wrap-for-"+AB.dom.id,style:{visibility:t?"visible":"hidden"}});w=AA.dom.parentNode;AA.setPositioning(AB.getPositioning());if(AA.isStyle("position","static")){AA.position("relative")}AB.clearPositioning("auto");AA.clip();if(u){u()}AB.setStyle({visibility:"",position:"absolute"});if(t){AA.setSize(AE.width,AE.height)}switch(s){case"t":AF={from:{width:AE.width+"px",height:"0px"},to:{width:AE.width+"px",height:AE.height+"px"}};y.bottom="0px";break;case"l":AF={from:{width:"0px",height:AE.height+"px"},to:{width:AE.width+"px",height:AE.height+"px"}};v.anchorAnimX(s);break;case"r":AF={from:{x:AE.x+AE.width,width:"0px",height:AE.height+"px"},to:{x:AE.x,width:AE.width+"px",height:AE.height+"px"}};v.anchorAnimX(s);break;case"b":AF={from:{y:AE.y+AE.height,width:AE.width+"px",height:"0px"},to:{y:AE.y,width:AE.width+"px",height:AE.height+"px"}};break;case"tl":AF={from:{x:AE.x,y:AE.y,width:"0px",height:"0px"},to:{width:AE.width+"px",height:AE.height+"px"}};y.bottom="0px";v.anchorAnimX("l");break;case"bl":AF={from:{y:AE.y+AE.height,width:"0px",height:"0px"},to:{y:AE.y,width:AE.width+"px",height:AE.height+"px"}};v.anchorAnimX("l");break;case"br":AF={from:{x:AE.x+AE.width,y:AE.y+AE.height,width:"0px",height:"0px"},to:{x:AE.x,y:AE.y,width:AE.width+"px",height:AE.height+"px"}};v.anchorAnimX("r");break;case"tr":AF={from:{x:AE.x+AE.width,width:"0px",height:"0px"},to:{x:AE.x,width:AE.width+"px",height:AE.height+"px"}};y.bottom="0px";v.anchorAnimX("r");break}AA.show();p=Ext.apply({},r);delete p.listeners;p=new Ext.fx.Anim(Ext.applyIf(p,{target:AA,duration:500,easing:"ease-out",from:t?AF.to:AF.from,to:t?AF.from:AF.to}));p.on("afteranimate",function(){var AG=Ext.fly(q,"_anim");AG.setStyle(z);if(t){if(r.useDisplay){AG.setDisplayed(false)}else{AG.hide()}}if(AA.dom){if(AA.dom.parentNode){AA.dom.parentNode.insertBefore(AG.dom,AA.dom)}else{w.appendChild(AG.dom)}AA.destroy()}if(u){u()}AD.end()});if(AC){p.on(AC)}};v.animate({duration:r.duration?Math.max(r.duration,500)*2:1000,listeners:{beforeanimate:x}});return v},slideOut:function(p,q){return this.slideIn(p,q,true)},swallowEvent:function(q,r){var t=this,u,p,s=function(v){v.stopPropagation();if(r){v.preventDefault()}};if(Ext.isArray(q)){p=q.length;for(u=0;u<p;u++){t.on(q[u],s)}return t}t.on(q,s);return t},switchOff:function(r){var q=this,s=q.dom,p;r=Ext.applyIf(r||{},{easing:"ease-in",duration:500,remove:false,useDisplay:false});p=function(){var x=Ext.fly(s,"_anim"),w=this,v=x.getSize(),y=x.getXY(),u,t;x.clearOpacity();x.clip();t=x.getPositioning();u=new Ext.fx.Animator({target:s,duration:r.duration,easing:r.easing,keyframes:{33:{opacity:0.3},66:{height:1,y:y[1]+v.height/2},100:{width:1,x:y[0]+v.width/2}}});u.on("afteranimate",function(){var z=Ext.fly(s,"_anim");if(r.useDisplay){z.setDisplayed(false)}else{z.hide()}z.clearOpacity();z.setPositioning(t);z.setSize(v);w.end()})};q.animate({duration:(Math.max(r.duration,500)*2),listeners:{beforeanimate:{fn:p}},callback:r.callback,scope:r.scope});return q},syncContent:function(q){q=Ext.getDom(q);var r=q.childNodes,AC=r.length,AA=this.dom,AB=AA.childNodes,y=AB.length,w,z,t,v,s,p,x,u=AA._extData;if(Ext.isIE9m&&AA.mergeAttributes){AA.mergeAttributes(q,true);AA.src=q.src}else{s=q.attributes;p=s.length;for(w=0;w<p;w++){x=s[w].name;if(x!=="id"){AA.setAttribute(x,s[w].value)}}}if(u){u.isSynchronized=false}if(AC!==y){AA.innerHTML=q.innerHTML;return }for(w=0;w<AC;w++){t=r[w];z=AB[w];v=t.nodeType;if(v!==z.nodeType||(v===1&&t.tagName!==z.tagName)){AA.innerHTML=q.innerHTML;return }if(v===3){z.data=t.data}else{if(t.id&&z.id!==t.id){z.id=t.id}z.style.cssText=t.style.cssText;z.className=t.className;Ext.fly(z,"_syncContent").syncContent(t)}}},toggle:function(p){var q=this;q.setVisible(!q.isVisible(),q.anim(p));return q},unmask:function(){var r=this,s=r.getData(),q=s.maskEl,p;if(q){p=q.dom.style;if(p.clearExpression){p.clearExpression("width");p.clearExpression("height")}if(q){q.destroy();delete s.maskEl}r.removeCls([M,Y])}r.restoreTabbableState(r.dom===d.body)},unclip:function(){var q=this,r=q.getData(),p;if(r[P]){r[P]=false;p=r[W];if(p.o){q.setStyle(j,p.o)}if(p.x){q.setStyle(T,p.x)}if(p.y){q.setStyle(S,p.y)}}return q},translate:function(p,r,q){if(Ext.supports.CssTransforms&&!Ext.isIE9m){this.callParent(arguments)}else{if(p!=null){this.dom.style.left=p+"px"}if(r!=null){this.dom.style.top=r+"px"}}},unselectable:function(){var p=this;if(Ext.isOpera){p.dom.unselectable="on"}p.removeCls(I.selectableCls);p.addCls(I.unselectableCls);return p},privates:{findTabbableElements:function(AE){var p,z,AB,t,u,x=this.dom,w=Ext.Element.tabbableSavedCounterAttribute,AD=[],AC=0,q,s,v,y,AA,r;if(!x){return AD}if(AE){p=AE.skipSelf;z=AE.skipChildren;AB=AE.excludeRoot;t=AE.includeSaved;u=AE.includeHidden}AB=AB&&Ext.getDom(AB);if(AB&&AB.contains(x)){return AD}if(!p&&((t&&x.hasAttribute(w))||this.isTabbable(u))){AD[AC++]=x}if(z){return AD}q=x.querySelectorAll(Ext.Element.tabbableSelector);AA=q.length;if(!AA){return AD}v=new Ext.dom.Fly();for(y=0;y<AA;y++){s=q[y];r=+s.getAttribute("tabIndex");if(((t&&s.hasAttribute(w))||(!(r<0)&&v.attach(s).isTabbable(u)))&&!(AB&&(AB===s||AB.contains(s)))){AD[AC++]=s}}return AD},saveTabbableState:function(t){var w=Ext.Element.tabbableSavedCounterAttribute,s=Ext.Element.tabbableSavedValueAttribute,q,r,v,u,p;if(!t||t.includeSaved==null){t=Ext.Object.chain(t||null);t.includeSaved=true}r=this.findTabbableElements(t);for(u=0,p=r.length;u<p;u++){v=r[u];q=+v.getAttribute(w);if(q>0){v.setAttribute(w,++q)}else{if(v.hasAttribute("tabIndex")){v.setAttribute(s,v.getAttribute("tabIndex"))}else{v.setAttribute(s,"none")}v.setAttribute("tabIndex","-1");v.setAttribute(w,"1")}}return r},restoreTabbableState:function(p,v){var t=this.dom,x=Ext.Element.tabbableSavedCounterAttribute,y=Ext.Element.tabbableSavedValueAttribute,r=[],z,q,r,s,u,w;if(!t){return this}if(!v){r=Ext.Array.from(t.querySelectorAll("["+x+"]"))}if(!p){r.unshift(t)}for(u=0,w=r.length;u<w;u++){s=r[u];if(!s.hasAttribute(x)||!s.hasAttribute(y)){continue}q=+s.getAttribute(x);if(q>1){s.setAttribute(x,--q);continue}z=s.getAttribute(y);if(z==="none"){s.removeAttribute("tabIndex")}else{s.setAttribute("tabIndex",z)}s.removeAttribute(y);s.removeAttribute(x)}return r}},deprecated:{"4.0":{methods:{pause:function(p){var q=this;Ext.fx.Manager.setFxDefaults(q.id,{delay:p});return q},scale:function(p,q,r){this.animate(Ext.apply({},r,{width:p,height:q}));return this},shift:function(p){this.animate(p);return this}}},"4.2":{methods:{moveTo:function(p,r,q){return this.setXY([p,r],q)},setBounds:function(q,t,s,p,r){return this.setBox({x:q,y:t,width:s,height:p},r)},setLeftTop:function(s,r){var q=this,p=q.dom.style;p.left=I.addUnits(s);p.top=I.addUnits(r);if(q.shadow||q.shim){q.syncUnderlays()}return q},setLocation:function(p,r,q){return this.setXY([p,r],q)}}},"5.0":{methods:{getAttributeNS:function(q,p){return this.getAttribute(p,q)},getCenterXY:function(){return this.getAlignToXY(d,"c-c")},getComputedHeight:function(){return Math.max(this.dom.offsetHeight,this.dom.clientHeight)||parseFloat(this.getStyle(b))||0},getComputedWidth:function(){return Math.max(this.dom.offsetWidth,this.dom.clientWidth)||parseFloat(this.getStyle(E))||0},getStyleSize:function(){var u=this,v=this.dom,q=(v===d||v===d.body),t,p,r;if(q){return{width:I.getViewportWidth(),height:I.getViewportHeight()}}t=u.getStyle(["height","width"],true);if(t.width&&t.width!=="auto"){p=parseFloat(t.width)}if(t.height&&t.height!=="auto"){r=parseFloat(t.height)}return{width:p||u.getWidth(true),height:r||u.getHeight(true)}},isBorderBox:function(){return true},isDisplayed:function(){return !this.isStyle("display","none")},focusable:"isFocusable"}}}}})(),function(){var O=Ext.dom.Element,N=O.prototype,U=!Ext.isIE8,B=document,K=B.defaultView,T=/alpha\(opacity=(.*)\)/i,G=/^\s+|\s+$/g,V=N.styleHooks,R=Ext.supports,E,M,D,Q,F,W,C;N._init(O);delete N._init;Ext.plainTableCls=Ext.baseCSSPrefix+"table-plain";Ext.plainListCls=Ext.baseCSSPrefix+"list-plain";if(Ext.CompositeElementLite){Ext.CompositeElementLite.importElementMethods()}if(!R.Opacity&&Ext.isIE){Ext.apply(V.opacity,{get:function(a){var Z=a.style.filter,Y,X;if(Z.match){Y=Z.match(T);if(Y){X=parseFloat(Y[1]);if(!isNaN(X)){return X?X/100:0}}}return 1},set:function(a,Y){var X=a.style,Z=X.filter.replace(T,"").replace(G,"");X.zoom=1;if(typeof (Y)==="number"&&Y>=0&&Y<1){Y*=100;X.filter=Z+(Z.length?" ":"")+"alpha(opacity="+Y+")"}else{X.filter=Z}}})}if(!R.matchesSelector){var I=/^([a-z]+|\*)?(?:\.([a-z][a-z\-_0-9]*))?$/i,J=/\-/g,A,S=function(X,Y){var Z=new RegExp("(?:^|\\s+)"+Y.replace(J,"\\-")+"(?:\\s+|$)");if(X&&X!=="*"){X=X.toUpperCase();return function(a){return a.tagName===X&&Z.test(a.className)}}return function(a){return Z.test(a.className)}},P=function(X){X=X.toUpperCase();return function(Y){return Y.tagName===X}},L={};N.matcherCache=L;N.is=function(X){if(!X){return true}var Y=this.dom,e,a,d,c,b,Z,f;if(Y.nodeType!==1){return false}if(!(d=Ext.isFunction(X)?X:L[X])){if(!(a=X.match(I))){c=Y.parentNode;if(!c){b=true;c=A||(A=B.createDocumentFragment());A.appendChild(Y)}Z=Ext.Array.indexOf(Ext.fly(c,"_is").query(X),Y)!==-1;if(b){A.removeChild(Y)}return Z}f=a[1];e=a[2];L[X]=d=e?S(f,e):P(f)}return d(Y)}}if(!K||!K.getComputedStyle){N.getStyle=function(m,g){var h=this,c=h.dom,o=typeof m!=="string",Z=m,j=Z,f=1,a=g,Y=h.styleHooks,n,e,l,k,b,X,d;if(o){l={};Z=j[0];d=0;if(!(f=j.length)){return l}}if(!c||c.documentElement){return l||""}e=c.style;if(g){X=e}else{X=c.currentStyle;if(!X){a=true;X=e}}do{k=Y[Z];if(!k){Y[Z]=k={name:O.normalize(Z)}}if(k.get){b=k.get(c,h,a,X)}else{n=k.name;b=X[n]}if(!o){return b}l[Z]=b;Z=j[++d]}while(d<f);return l}}if(Ext.isIE8){C=function(a,Y,Z,X){if(X[this.styleName]==="none"){return"0px"}return X[this.name]};D=["Top","Right","Bottom","Left"];Q=D.length;while(Q--){F=D[Q];W="border"+F+"Width";V["border-"+F.toLowerCase()+"-width"]=V[W]={name:W,styleName:"border"+F+"Style",get:C}}var H=Ext.baseCSSPrefix+"sync-repaint";N.syncRepaint=function(){this.addCls(H);this.getWidth();this.removeCls(H)}}if(Ext.isIE10m){Ext.override(O,{focus:function(b,a){var Z=this,Y;a=a||Z.dom;if(Number(b)){Ext.defer(Z.focus,b,Z,[null,a])}else{Ext.GlobalEvents.fireEvent("beforefocus",a);if(a&&(a.tagName==="INPUT"||a.tagname==="TEXTAREA")){Ext.synchronouslyFocusing=document.activeElement}try{a.focus()}catch(X){Y=X}if(Ext.synchronouslyFocusing&&document.activeElement!==a&&!Y){a.focus()}Ext.synchronouslyFocusing=null}return Z}})}Ext.apply(Ext,{enableGarbageCollector:true,isBorderBox:true,useShims:false,getDetachedBody:function(){var X=Ext.detachedBodyEl;if(!X){X=B.createElement("div");Ext.detachedBodyEl=X=new Ext.dom.Fly(X);X.isDetachedBody=true}return X},getElementById:function(Z){var Y=B.getElementById(Z),X;if(!Y&&(X=Ext.detachedBodyEl)){Y=X.dom.querySelector(Ext.makeIdSelector(Z))}return Y},addBehaviors:function(c){if(!Ext.isReady){Ext.onInternalReady(function(){Ext.addBehaviors(c)})}else{var Y={},a,X,Z;for(X in c){if((a=X.split("@"))[1]){Z=a[0];if(!Y[Z]){Y[Z]=Ext.fly(document).select(Z,true)}Y[Z].on(a[1],c[X])}}Y=null}}});if(Ext.isIE9m){Ext.getElementById=function(Z){var Y=B.getElementById(Z),X;if(!Y&&(X=Ext.detachedBodyEl)){Y=X.dom.all[Z]}return Y};N.getById=function(c,X){var b=this.dom,Y=null,a,Z;if(b){Z=(U&&B.getElementById(c))||b.all[c];if(Z){if(X){Y=Z}else{a=Ext.cache[c];if(a){if(a.skipGarbageCollection||!Ext.isGarbage(a.dom)){Y=a}else{Ext.raise("Stale Element with id '"+Z.id+"' found in Element cache. Make sure to clean up Element instances using destroy()");a.destroy()}}Y=Y||new Ext.Element(Z)}}}return Y}}else{if(!B.querySelector){Ext.getDetachedBody=Ext.getBody;Ext.getElementById=function(X){return B.getElementById(X)};N.getById=function(Z,X){var Y=B.getElementById(Z);return X?Y:(Y?Ext.get(Y):null)}}}if(Ext.isIE&&!(Ext.isIE9p&&B.documentMode>=9)){N.getAttribute=function(X,Z){var a=this.dom,Y;if(Z){Y=typeof a[Z+":"+X];if(Y!=="undefined"&&Y!=="unknown"){return a[Z+":"+X]||null}return null}if(X==="for"){X="htmlFor"}return a[X]||null}}Ext.onInternalReady(function(){var a=/^(?:transparent|(?:rgba[(](?:\s*\d+\s*[,]){3}\s*0\s*[)]))$/i,Y=[],f=N.setWidth,g=N.setHeight,l=N.setSize,m=/^\d+(?:\.\d*)?px$/i,e,c,X,k;if(R.FixedTableWidthBug){V.width={name:"width",set:function(r,q,o){var n=r.style,i=o._needsTableWidthFix,p=n.display;if(i){n.display="none"}n.width=q;if(i){r.scrollWidth;n.display=p}}};N.setWidth=function(p,n){var r=this,s=r.dom,o=s.style,i=r._needsTableWidthFix,q=o.display;if(i&&!n){o.display="none"}f.call(r,p,n);if(i&&!n){s.scrollWidth;o.display=q}return r};N.setSize=function(q,n,o){var s=this,t=s.dom,p=t.style,i=s._needsTableWidthFix,r=p.display;if(i&&!o){p.display="none"}l.call(s,q,n,o);if(i&&!o){t.scrollWidth;p.display=r}return s}}if(Ext.isIE8){V.height={name:"height",set:function(r,q,o){var n=o.component,p,i;if(n&&n._syncFrameHeight&&o===n.el){i=n.frameBody.dom.style;if(m.test(q)){p=n.getFrameInfo();if(p){i.height=(parseInt(q,10)-p.height)+"px"}}else{if(!q||q==="auto"){i.height=""}}}r.style.height=q}};N.setHeight=function(i,o){var p=this.component,q,n;if(p&&p._syncFrameHeight&&this===p.el){n=p.frameBody.dom.style;if(!i||i==="auto"){n.height=""}else{q=p.getFrameInfo();if(q){n.height=(i-q.height)+"px"}}}return g.call(this,i,o)};N.setSize=function(q,i,o){var p=this.component,r,n;if(p&&p._syncFrameHeight&&this===p.el){n=p.frameBody.dom.style;if(!i||i==="auto"){n.height=""}else{r=p.getFrameInfo();if(r){n.height=(i-r.height)+"px"}}}return l.call(this,q,i,o)}}Ext.getDoc().on("selectstart",function(q,r){var p=O.selectableCls,o=O.unselectableCls,i=r&&r.tagName;i=i&&i.toLowerCase();if(i==="input"||i==="textarea"){return }while(r&&r.nodeType===1&&r!==B.documentElement){var n=Ext.fly(r);if(n.hasCls(p)){return }if(n.hasCls(o)){q.stopEvent();return }r=r.parentNode}});function d(q,n,p,i){var o=i[this.name]||"";return a.test(o)?"transparent":o}function j(n,o,i){return function(){n.selectionStart=o;n.selectionEnd=i}}function h(q){var o=R.DisplayChangeInputSelectionBug,p=R.DisplayChangeTextAreaSelectionBug,r,i,s,n;if(o||p){r=O.getActiveElement();i=r&&r.tagName;if((p&&i==="TEXTAREA")||(o&&i==="INPUT"&&r.type==="text")){if(Ext.fly(q).isAncestor(r)){s=r.selectionStart;n=r.selectionEnd;if(Ext.isNumber(s)&&Ext.isNumber(n)){return j(r,s,n)}}}}return Ext.emptyFn}function b(s,p,r,o){var i=o.marginRight,n,q;if(i!=="0px"){n=s.style;q=n.display;n.display="inline-block";i=(r?o:s.ownerDocument.defaultView.getComputedStyle(s,null)).marginRight;n.display=q}return i}function Z(t,q,s,p){var i=p.marginRight,o,n,r;if(i!=="0px"){o=t.style;n=h(t);r=o.display;o.display="inline-block";i=(s?p:t.ownerDocument.defaultView.getComputedStyle(t,"")).marginRight;o.display=r;n()}return i}if(!R.RightMargin){V.marginRight=V["margin-right"]={name:"marginRight",get:(R.DisplayChangeInputSelectionBug||R.DisplayChangeTextAreaSelectionBug)?Z:b}}if(!R.TransparentColor){e=["background-color","border-color","color","outline-color"];for(c=e.length;c--;){X=e[c];k=O.normalize(X);V[X]=V[k]={name:k,get:d}}}N.verticalStyleHooks90=E=Ext.Object.chain(V);N.verticalStyleHooks270=M=Ext.Object.chain(V);E.width=V.height||{name:"height"};E.height=V.width||{name:"width"};E["margin-top"]={name:"marginLeft"};E["margin-right"]={name:"marginTop"};E["margin-bottom"]={name:"marginRight"};E["margin-left"]={name:"marginBottom"};E["padding-top"]={name:"paddingLeft"};E["padding-right"]={name:"paddingTop"};E["padding-bottom"]={name:"paddingRight"};E["padding-left"]={name:"paddingBottom"};E["border-top"]={name:"borderLeft"};E["border-right"]={name:"borderTop"};E["border-bottom"]={name:"borderRight"};E["border-left"]={name:"borderBottom"};M.width=V.height||{name:"height"};M.height=V.width||{name:"width"};M["margin-top"]={name:"marginRight"};M["margin-right"]={name:"marginBottom"};M["margin-bottom"]={name:"marginLeft"};M["margin-left"]={name:"marginTop"};M["padding-top"]={name:"paddingRight"};M["padding-right"]={name:"paddingBottom"};M["padding-bottom"]={name:"paddingLeft"};M["padding-left"]={name:"paddingTop"};M["border-top"]={name:"borderRight"};M["border-right"]={name:"borderBottom"};M["border-bottom"]={name:"borderLeft"};M["border-left"]={name:"borderTop"};if(!Ext.scopeCss){Y.push(Ext.baseCSSPrefix+"body")}if(R.Touch){Y.push(Ext.baseCSSPrefix+"touch")}if(Ext.isIE&&Ext.isIE9m){Y.push(Ext.baseCSSPrefix+"ie",Ext.baseCSSPrefix+"ie9m");Y.push(Ext.baseCSSPrefix+"ie8p");if(Ext.isIE8){Y.push(Ext.baseCSSPrefix+"ie8")}else{Y.push(Ext.baseCSSPrefix+"ie9",Ext.baseCSSPrefix+"ie9p")}if(Ext.isIE8m){Y.push(Ext.baseCSSPrefix+"ie8m")}}if(Ext.isIE10){Y.push(Ext.baseCSSPrefix+"ie10")}if(Ext.isIE10p){Y.push(Ext.baseCSSPrefix+"ie10p")}if(Ext.isIE11){Y.push(Ext.baseCSSPrefix+"ie11")}if(Ext.isGecko){Y.push(Ext.baseCSSPrefix+"gecko")}if(Ext.isOpera){Y.push(Ext.baseCSSPrefix+"opera")}if(Ext.isOpera12m){Y.push(Ext.baseCSSPrefix+"opera12m")}if(Ext.isWebKit){Y.push(Ext.baseCSSPrefix+"webkit")}if(Ext.isSafari){Y.push(Ext.baseCSSPrefix+"safari")}if(Ext.isChrome){Y.push(Ext.baseCSSPrefix+"chrome")}if(Ext.isMac){Y.push(Ext.baseCSSPrefix+"mac")}if(Ext.isLinux){Y.push(Ext.baseCSSPrefix+"linux")}if(!R.CSS3BorderRadius){Y.push(Ext.baseCSSPrefix+"nbr")}if(!R.CSS3LinearGradient){Y.push(Ext.baseCSSPrefix+"nlg")}if(R.Touch){Y.push(Ext.baseCSSPrefix+"touch")}Ext.getBody().addCls(Y)},null,{priority:1500})})