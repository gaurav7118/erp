Ext.ux.DataViewTransition=Ext.extend(Object,{defaults:{duration:750,idProperty:"id"},constructor:function(A){Ext.apply(this,A||{},this.defaults)},init:function(A){this.dataview=A;var B=this.idProperty;A.blockRefresh=true;A.updateIndexes=Ext.Function.createSequence(A.updateIndexes,function(){this.getTargetEl().select(this.itemSelector).each(function(D,E,C){D.id=D.dom.id=Ext.util.Format.format("{0}-{1}",A.id,A.store.getAt(C).get(B))},this)},A);this.dataviewID=A.id;this.cachedStoreData={};this.cacheStoreData(A.store.snapshot);A.store.on("datachanged",function(L){var J=A.getTargetEl(),F=L.getAt(0),O=this.getAdded(L),X=this.getRemoved(L),G=this.getRemaining(L),T=Ext.apply({},G,O);Ext.each(X,function(c){Ext.fly(this.dataviewID+"-"+c.get(this.idProperty)).animate({remove:false,duration:C,opacity:0,useDisplay:true})},this);if(F==undefined){this.cacheStoreData(L);return }var E=Ext.get(this.dataviewID+"-"+F.get(this.idProperty));var Z=L.getCount(),I=E.getMargin("lr")+E.getWidth(),U=E.getMargin("bt")+E.getHeight(),Q=J.getWidth(),D=Math.floor(Q/I),P=Math.ceil(Z/D),a=Math.ceil(this.getExistingCount()/D);J.applyStyles({display:"block",position:"relative"});var H={},b={},R={};Ext.iterate(G,function(e,d){var e=d.get(this.idProperty),c=R[e]=Ext.get(this.dataviewID+"-"+e);H[e]={top:c.getY()-J.getY()-c.getMargin("t")-J.getPadding("t"),left:c.getX()-J.getX()-c.getMargin("l")-J.getPadding("l")}},this);Ext.iterate(G,function(f,e){var c=H[f],d=R[f];if(d.getStyle("position")!="absolute"){R[f].applyStyles({position:"absolute",left:c.left+"px",top:c.top+"px",width:d.getWidth(!Ext.isIE||Ext.isStrict),height:d.getHeight(!Ext.isIE||Ext.isStrict)})}});var N=0;Ext.iterate(L.data.items,function(e){var i=e.get(B),d=R[i];var c=N%D,h=Math.floor(N/D),g=h*U,f=c*I;b[i]={top:g,left:f};N++},this);var S=new Date(),C=this.duration,K=this.dataviewID;var Y=function(){var l=new Date()-S,n=l/C;if(n>=1){for(var c in b){Ext.fly(K+"-"+c).applyStyles({top:b[c].top+"px",left:b[c].left+"px"})}Ext.TaskManager.stop(W)}else{for(var c in b){if(!G[c]){continue}var f=H[c],i=b[c],g=f.top,j=i.top,e=f.left,k=i.left,h=n*Math.abs(g-j),m=n*Math.abs(e-k),o=g>j?g-h:g+h,d=e>k?e-m:e+m;Ext.fly(K+"-"+c).applyStyles({top:o+"px",left:d+"px"})}}};var W={run:Y,interval:20,scope:this};Ext.TaskManager.start(W);var M=0;for(var V in O){M++}if(Ext.global.console&&Ext.global.console.log){Ext.global.console.log("added:",M)}Ext.iterate(O,function(d,c){Ext.fly(this.dataviewID+"-"+c.get(this.idProperty)).applyStyles({top:b[c.get(this.idProperty)].top+"px",left:b[c.get(this.idProperty)].left+"px"});Ext.fly(this.dataviewID+"-"+c.get(this.idProperty)).animate({remove:false,duration:C,opacity:1})},this);this.cacheStoreData(L)},this)},cacheStoreData:function(A){this.cachedStoreData={};A.each(function(B){this.cachedStoreData[B.get(this.idProperty)]=B},this)},getExisting:function(){return this.cachedStoreData},getExistingCount:function(){var C=0,B=this.getExisting();for(var A in B){C++}return C},getAdded:function(A){var B={};A.each(function(C){if(this.cachedStoreData[C.get(this.idProperty)]==undefined){B[C.get(this.idProperty)]=C}},this);return B},getRemoved:function(A){var B=[];for(var C in this.cachedStoreData){if(A.findExact(this.idProperty,Number(C))==-1){B.push(this.cachedStoreData[C])}}return B},getRemaining:function(A){var B={};A.each(function(C){if(this.cachedStoreData[C.get(this.idProperty)]!=undefined){B[C.get(this.idProperty)]=C}},this);return B}})