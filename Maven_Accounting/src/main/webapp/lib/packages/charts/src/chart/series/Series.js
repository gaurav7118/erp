Ext.define("Ext.chart.series.Series",{requires:["Ext.chart.Markers","Ext.chart.label.Label","Ext.tip.ToolTip"],mixins:["Ext.mixin.Observable","Ext.mixin.Bindable"],isSeries:true,defaultBindProperty:"store",type:null,seriesType:"sprite",identifiablePrefix:"ext-line-",observableType:"series",darkerStrokeRatio:0.15,config:{chart:null,title:null,renderer:null,showInLegend:true,triggerAfterDraw:false,style:{},subStyle:{},themeStyle:{},colors:null,useDarkerStrokeColor:true,store:null,label:{},labelOverflowPadding:null,showMarkers:true,marker:null,markerSubStyle:null,itemInstancing:null,background:null,highlightItem:null,surface:null,overlaySurface:null,hidden:false,highlight:false,highlightCfg:{merge:function(A){return A},$value:{fillStyle:"yellow",strokeStyle:"red"}},animation:null,tooltip:null},directions:[],sprites:null,themeColorCount:function(){return 1},isStoreDependantColorCount:false,themeMarkerCount:function(){return 0},getFields:function(F){var E=this,A=[],C,B,D;for(B=0,D=F.length;B<D;B++){C=E["get"+F[B]+"Field"]();if(Ext.isArray(C)){A.push.apply(A,C)}else{A.push(C)}}return A},applyAnimation:function(A,B){if(!A){A={duration:0}}else{if(A===true){A={easing:"easeInOut",duration:500}}}return B?Ext.apply({},A,B):A},getAnimation:function(){var A=this.getChart();if(A&&A.animationSuspendCount){return{duration:0}}else{return this.callParent()}},updateTitle:function(A){var I=this,G=I.getChart();if(!G||G.isInitializing){return }A=Ext.Array.from(A);var C=G.getSeries(),B=Ext.Array.indexOf(C,I),E=G.getLegendStore(),H=I.getYField(),D,K,J,F;if(E.getCount()&&B!==-1){F=H?Math.min(A.length,H.length):A.length;for(D=0;D<F;D++){J=A[D];K=E.getAt(B+D);if(J&&K){K.set("name",J)}}}},applyHighlight:function(A,B){if(Ext.isObject(A)){A=Ext.merge({},this.config.highlightCfg,A)}else{if(A===true){A=this.config.highlightCfg}}return Ext.apply(B||{},A)},updateHighlight:function(A){this.getStyle();if(!Ext.Object.isEmpty(A)){this.addItemHighlight()}},updateHighlightCfg:function(A){if(!Ext.Object.equals(A,this.defaultConfig.highlightCfg)){this.addItemHighlight()}},applyItemInstancing:function(A,B){return Ext.merge(B||{},A)},setAttributesForItem:function(C,D){var B=C&&C.sprite,A;if(B){if(B.itemsMarker&&C.category==="items"){B.putMarker(C.category,D,C.index,false,true)}if(B.isMarkerHolder&&C.category==="markers"){B.putMarker(C.category,D,C.index,false,true)}else{if(B.isInstancing){B.setAttributesFor(C.index,D)}else{if(Ext.isArray(B)){for(A=0;A<B.length;A++){B[A].setAttributes(D)}}else{B.setAttributes(D)}}}}},getBBoxForItem:function(A){if(A&&A.sprite){if(A.sprite.itemsMarker&&A.category==="items"){return A.sprite.getMarkerBBox(A.category,A.index)}else{if(A.sprite instanceof Ext.draw.sprite.Instancing){return A.sprite.getBBoxFor(A.index)}else{return A.sprite.getBBox()}}}return null},applyHighlightItem:function(D,A){if(D===A){return }if(Ext.isObject(D)&&Ext.isObject(A)){var C=D.sprite===A.sprite,B=D.index===A.index;if(C&&B){return }}return D},updateHighlightItem:function(B,A){this.setAttributesForItem(A,{highlighted:false});this.setAttributesForItem(B,{highlighted:true})},constructor:function(A){var B=this,C;A=A||{};if(A.tips){A=Ext.apply({tooltip:A.tips},A)}if(A.highlightCfg){A=Ext.apply({highlight:A.highlightCfg},A)}if("id" in A){C=A.id}else{if("id" in B.config){C=B.config.id}else{C=B.getId()}}B.setId(C);B.sprites=[];B.dataRange=[];B.mixins.observable.constructor.call(B,A);B.initBindable()},lookupViewModel:function(A){var B=this.getChart();return B?B.lookupViewModel(A):null},applyTooltip:function(C,B){var A=Ext.apply({xtype:"tooltip",renderer:Ext.emptyFn,constrainPosition:true,shrinkWrapDock:true,autoHide:true,offsetX:10,offsetY:10},C);return Ext.create(A)},updateTooltip:function(){this.addItemHighlight()},addItemHighlight:function(){var D=this.getChart();if(!D){return }var E=D.getInteractions(),C,A,B;for(C=0;C<E.length;C++){A=E[C];if(A.isItemHighlight||A.isItemEdit){B=true;break}}if(!B){E.push("itemhighlight");D.setInteractions(E)}},showTooltip:function(L,M){var D=this,N=D.getTooltip(),J,A,I,F,H,K,G,E,B,C;if(!N){return }clearTimeout(D.tooltipTimeout);B=N.config;if(N.trackMouse){M[0]+=B.offsetX;M[1]+=B.offsetY}else{J=L.sprite;A=J.getSurface();I=Ext.get(A.getId());if(I){K=L.series.getBBoxForItem(L);G=K.x+K.width/2;E=K.y+K.height/2;H=A.matrix.transformPoint([G,E]);F=I.getXY();C=A.getInherited().rtl;G=C?F[0]+I.getWidth()-H[0]:F[0]+H[0];E=F[1]+H[1];M=[G,E]}}Ext.callback(N.renderer,N.scope,[N,L.record,L],0,D);N.show(M)},hideTooltip:function(B){var A=this,C=A.getTooltip();if(!C){return }clearTimeout(A.tooltipTimeout);A.tooltipTimeout=Ext.defer(function(){C.hide()},1)},applyStore:function(A){return A&&Ext.StoreManager.lookup(A)},getStore:function(){return this._store||this.getChart()&&this.getChart().getStore()},updateStore:function(B,A){var H=this,G=H.getChart(),C=G&&G.getStore(),F,I,E,D;A=A||C;if(A&&A!==B){A.un({datachanged:"onDataChanged",update:"onDataChanged",scope:H})}if(B){B.on({datachanged:"onDataChanged",update:"onDataChanged",scope:H});F=H.getSprites();for(D=0,E=F.length;D<E;D++){I=F[D];if(I.setStore){I.setStore(B)}}H.onDataChanged()}H.fireEvent("storechange",H,B,A)},onStoreChange:function(B,A,C){if(!this._store){this.updateStore(A,C)}},coordinate:function(N,L,E){var K=this,O=K.getStore(),H=K.getHidden(),J=O.getData().items,B=K["get"+N+"Axis"](),F={min:Infinity,max:-Infinity},P=K["fieldCategory"+N]||[N],G=K.getFields(P),D,M,C,A={},I=K.getSprites();if(I.length>0){if(!Ext.isBoolean(H)||!H){for(D=0;D<P.length;D++){M=G[D];C=K.coordinateData(J,M,B);K.getRangeOfData(C,F);A["data"+P[D]]=C}}K.dataRange[L]=F.min;K.dataRange[L+E]=F.max;A["dataMin"+N]=F.min;A["dataMax"+N]=F.max;if(B){B.range=null;A["range"+N]=B.getRange()}for(D=0;D<I.length;D++){I[D].setAttributes(A)}}},coordinateData:function(B,H,D){var G=[],F=B.length,E=D&&D.getLayout(),C,A;for(C=0;C<F;C++){A=B[C].data[H];if(!Ext.isEmpty(A,true)){if(E){G[C]=E.getCoordFor(A,H,C,B)}else{G[C]=+A}}else{G[C]=A}}return G},getRangeOfData:function(G,B){var E=G.length,D=B.min,A=B.max,C,F;for(C=0;C<E;C++){F=G[C];if(F<D){D=F}if(F>A){A=F}}B.min=D;B.max=A},updateLabelData:function(){var H=this,J=H.getStore(),G=J.getData().items,F=H.getSprites(),A=H.getLabel().getTemplate(),L=Ext.Array.from(A.getField()),C,B,E,D,K,I;if(!F.length||!L.length){return }for(C=0;C<F.length;C++){D=[];K=F[C];I=K.getField();if(Ext.Array.indexOf(L,I)<0){I=L[C]}for(B=0,E=G.length;B<E;B++){D.push(G[B].get(I))}K.setAttributes({labels:D})}},processData:function(){if(!this.getStore()){return }var D=this,F=this.directions,A,C=F.length,E,B;for(A=0;A<C;A++){E=F[A];B=D["get"+E+"Axis"]();if(B){B.processData(D);continue}if(D["coordinate"+E]){D["coordinate"+E]()}}D.updateLabelData()},applyBackground:function(A){if(this.getChart()){this.getSurface().setBackground(A);return this.getSurface().getBackground()}else{return A}},updateChart:function(D,A){var C=this,B=C._store;if(A){A.un("axeschange","onAxesChange",C);C.clearSprites();C.setSurface(null);C.setOverlaySurface(null);A.unregister(C);C.onChartDetached(A);if(!B){C.updateStore(null)}}if(D){C.setSurface(D.getSurface("series"));C.setOverlaySurface(D.getSurface("overlay"));D.on("axeschange","onAxesChange",C);if(D.getAxes()){C.onAxesChange(D)}C.onChartAttached(D);D.register(C);if(!B){C.updateStore(D.getStore())}}},onAxesChange:function(H){var J=this,G=H.getAxes(),C,A={},B={},E=false,I=this.directions,K,D,F;for(D=0,F=I.length;D<F;D++){K=I[D];B[K]=J.getFields(J["fieldCategory"+K])}for(D=0,F=G.length;D<F;D++){C=G[D];if(!A[C.getDirection()]){A[C.getDirection()]=[C]}else{A[C.getDirection()].push(C)}}for(D=0,F=I.length;D<F;D++){K=I[D];if(J["get"+K+"Axis"]()){continue}if(A[K]){C=J.findMatchingAxis(A[K],B[K]);if(C){J["set"+K+"Axis"](C);if(C.getNeedHighPrecision()){E=true}}}}this.getSurface().setHighPrecision(E)},findMatchingAxis:function(F,E){var D,C,B,A;for(B=0;B<F.length;B++){D=F[B];C=D.getFields();if(!C.length){return D}else{if(E){for(A=0;A<E.length;A++){if(Ext.Array.indexOf(C,E[A])>=0){return D}}}}}},onChartDetached:function(A){var B=this;B.fireEvent("chartdetached",A,B);A.un("storechange","onStoreChange",B)},onChartAttached:function(A){var B=this;B.setBackground(B.getBackground());B.fireEvent("chartattached",A,B);A.on("storechange","onStoreChange",B);B.processData()},updateOverlaySurface:function(A){var B=this;if(A){if(B.getLabel()){B.getOverlaySurface().add(B.getLabel())}}},applyLabel:function(A,B){if(!B){B=new Ext.chart.Markers({zIndex:10});B.setTemplate(new Ext.chart.label.Label(A))}else{B.getTemplate().setAttributes(A)}return B},createItemInstancingSprite:function(C,B){var E=this,F=new Ext.chart.Markers(),A,D;F.setAttributes({zIndex:Number.MAX_VALUE});A=Ext.apply({},B);if(E.getHighlight()){A.highlight=E.getHighlight();A.modifiers=["highlight"]}F.setTemplate(A);D=F.getTemplate();D.setAttributes(E.getStyle());D.fx.on("animationstart","onSpriteAnimationStart",this);D.fx.on("animationend","onSpriteAnimationEnd",this);C.bindMarker("items",F);E.getSurface().add(F);return F},getDefaultSpriteConfig:function(){return{type:this.seriesType,renderer:this.getRenderer()}},updateRenderer:function(C){var B=this,A=B.getChart(),D;if(A&&A.isInitializing){return }D=B.getSprites();if(D.length){D[0].setAttributes({renderer:C||null});if(A&&!A.isInitializing){A.redraw()}}},updateShowMarkers:function(A){var D=this.getSprites(),B=D&&D[0],C=B&&B.getMarker("markers");if(C){C.getTemplate().setAttributes({hidden:!A})}},createSprite:function(){var F=this,A=F.getSurface(),E=F.getItemInstancing(),D=A.add(F.getDefaultSpriteConfig()),B=F.getMarker(),G,C;D.setAttributes(F.getStyle());D.setSeries(F);if(E){D.itemsMarker=F.createItemInstancingSprite(D,E)}if(D.bindMarker){if(B){G=new Ext.chart.Markers();C=Ext.Object.merge({},B);if(F.getHighlight()){C.highlight=F.getHighlight();C.modifiers=["highlight"]}G.setTemplate(C);G.getTemplate().fx.setCustomDurations({translationX:0,translationY:0});D.dataMarker=G;D.bindMarker("markers",G);F.getOverlaySurface().add(G)}if(F.getLabel().getTemplate().getField()){D.bindMarker("labels",F.getLabel())}}if(D.setStore){D.setStore(F.getStore())}D.fx.on("animationstart","onSpriteAnimationStart",F);D.fx.on("animationend","onSpriteAnimationEnd",F);F.sprites.push(D);return D},getSprites:Ext.emptyFn,onDataChanged:function(){var D=this,C=D.getChart(),B=C&&C.getStore(),A=D.getStore();if(A!==B){D.processData()}},isXType:function(A){return A==="series"},getItemId:function(){return this.getId()},applyThemeStyle:function(E,A){var B=this,D,C;D=E&&E.subStyle&&E.subStyle.fillStyle;C=D&&E.subStyle.strokeStyle;if(D&&!C){E.subStyle.strokeStyle=B.getStrokeColorsFromFillColors(D)}D=E&&E.markerSubStyle&&E.markerSubStyle.fillStyle;C=D&&E.markerSubStyle.strokeStyle;if(D&&!C){E.markerSubStyle.strokeStyle=B.getStrokeColorsFromFillColors(D)}return Ext.apply(A||{},E)},applyStyle:function(C,B){var A=Ext.ClassManager.get(Ext.ClassManager.getNameByAlias("sprite."+this.seriesType));if(A&&A.def){C=A.def.normalize(C)}return Ext.apply({},C,B)},applySubStyle:function(B,C){var A=Ext.ClassManager.get(Ext.ClassManager.getNameByAlias("sprite."+this.seriesType));if(A&&A.def){B=A.def.batchedNormalize(B,true)}return Ext.merge({},C,B)},applyMarker:function(C,A){var D=(C&&C.type)||(A&&A.type)||"circle",B=Ext.ClassManager.get(Ext.ClassManager.getNameByAlias("sprite."+D));if(B&&B.def){C=B.def.normalize(Ext.isObject(C)?C:{},true);C.type=D}return Ext.merge(A||{},C)},applyMarkerSubStyle:function(C,A){var D=(C&&C.type)||(A&&A.type)||"circle",B=Ext.ClassManager.get(Ext.ClassManager.getNameByAlias("sprite."+D));if(B&&B.def){C=B.def.batchedNormalize(C,true)}return Ext.merge(A||{},C)},updateHidden:function(B){var A=this;A.getColors();A.getSubStyle();A.setSubStyle({hidden:B});A.processData();A.doUpdateStyles();if(!Ext.isArray(B)){A.updateLegendStore(B)}},updateLegendStore:function(F,B){var E=this,D=E.getChart(),C=D.getLegendStore(),G=E.getId(),A;if(C){if(arguments.length>1){A=C.findBy(function(H){return H.get("series")===G&&H.get("index")===B});if(A!==-1){A=C.getAt(A)}}else{A=C.findRecord("series",G)}if(A&&A.get("disabled")!==F){A.set("disabled",F)}}},setHiddenByIndex:function(A,C){var B=this;if(Ext.isArray(B.getHidden())){B.getHidden()[A]=C;B.updateHidden(B.getHidden());B.updateLegendStore(C,A)}else{B.setHidden(C)}},getStrokeColorsFromFillColors:function(A){var C=this,E=C.getUseDarkerStrokeColor(),B=(Ext.isNumber(E)?E:C.darkerStrokeRatio),D;if(E){D=Ext.Array.map(A,function(F){F=Ext.isString(F)?F:F.stops[0].color;F=Ext.draw.Color.fromString(F);return F.createDarker(B).toString()})}else{D=Ext.Array.clone(A)}return D},updateThemeColors:function(B){var C=this,D=C.getThemeStyle(),A=Ext.Array.clone(B),F=C.getStrokeColorsFromFillColors(B),E={fillStyle:A,strokeStyle:F};D.subStyle=Ext.apply(D.subStyle||{},E);D.markerSubStyle=Ext.apply(D.markerSubStyle||{},E);C.doUpdateStyles()},themeOnlyIfConfigured:{},updateTheme:function(D){var H=this,A=D.getSeries(),N=H.getInitialConfig(),C=H.defaultConfig,F=H.getConfigurator().configs,J=A.defaults,K=A[H.type],G=H.themeOnlyIfConfigured,L,I,O,B,M,E;A=Ext.merge({},J,K);for(L in A){I=A[L];E=F[L];if(I!==null&&I!==undefined&&E){M=N[L];O=Ext.isObject(I);B=M===C[L];if(O){if(B&&G[L]){continue}I=Ext.merge({},I,M)}if(B||O){H[E.names.set](I)}}}},updateChartColors:function(A){var B=this;if(!B.getColors()){B.updateThemeColors(A)}},updateColors:function(A){this.updateThemeColors(A)},updateStyle:function(){this.doUpdateStyles()},updateSubStyle:function(){this.doUpdateStyles()},updateThemeStyle:function(){this.doUpdateStyles()},doUpdateStyles:function(){var G=this,H=G.sprites,D=G.getItemInstancing(),C=0,F=H&&H.length,A=G.getConfig("showMarkers",true),B=G.getMarker(),E;for(;C<F;C++){E=G.getStyleByIndex(C);if(D){H[C].itemsMarker.getTemplate().setAttributes(E)}H[C].setAttributes(E);if(B&&H[C].dataMarker){H[C].dataMarker.getTemplate().setAttributes(G.getMarkerStyleByIndex(C))}}},getStyleWithTheme:function(){var B=this,C=B.getThemeStyle(),D=(C&&C.style)||{},A=Ext.applyIf(Ext.apply({},B.getStyle()),D);return A},getSubStyleWithTheme:function(){var C=this,D=C.getThemeStyle(),A=(D&&D.subStyle)||{},B=Ext.applyIf(Ext.apply({},C.getSubStyle()),A);return B},getStyleByIndex:function(B){var E=this,H=E.getThemeStyle(),D,G,C,F,A={};D=E.getStyle();G=(H&&H.style)||{};C=E.styleDataForIndex(E.getSubStyle(),B);F=E.styleDataForIndex((H&&H.subStyle),B);Ext.apply(A,G);Ext.apply(A,F);Ext.apply(A,D);Ext.apply(A,C);return A},getMarkerStyleByIndex:function(D){var G=this,C=G.getThemeStyle(),A,E,J,I,B,K,H,F,L={};A=G.getStyle();E=(C&&C.style)||{};J=G.styleDataForIndex(G.getSubStyle(),D);if(J.hasOwnProperty("hidden")){J.hidden=J.hidden||!this.getConfig("showMarkers",true)}I=G.styleDataForIndex((C&&C.subStyle),D);B=G.getMarker();K=(C&&C.marker)||{};H=G.getMarkerSubStyle();F=G.styleDataForIndex((C&&C.markerSubStyle),D);Ext.apply(L,E);Ext.apply(L,I);Ext.apply(L,K);Ext.apply(L,F);Ext.apply(L,A);Ext.apply(L,J);Ext.apply(L,B);Ext.apply(L,H);return L},styleDataForIndex:function(D,C){var E,B,A={};if(D){for(B in D){E=D[B];if(Ext.isArray(E)){A[B]=E[C%E.length]}else{A[B]=E}}}return A},getItemForPoint:Ext.emptyFn,getItemByIndex:function(A,E){var D=this,F=D.getSprites(),B=F&&F[0],C;if(!B){return }if(E===undefined&&B.isMarkerHolder){E=D.getItemInstancing()?"items":"markers"}else{if(!E||E===""||E==="sprites"){B=F[A]}}if(B){C={series:D,category:E,index:A,record:D.getStore().getData().items[A],field:D.getYField(),sprite:B};return C}},onSpriteAnimationStart:function(A){this.fireEvent("animationstart",this,A)},onSpriteAnimationEnd:function(A){this.fireEvent("animationend",this,A)},resolveListenerScope:function(E){var D=this,A=Ext._namedScopes[E],C=D.getChart(),B;if(!A){B=C?C.resolveListenerScope(E,false):(E||D)}else{if(A.isThis){B=D}else{if(A.isController){B=C?C.resolveListenerScope(E,false):D}else{if(A.isSelf){B=C?C.resolveListenerScope(E,false):D;if(B===C&&!C.getInheritedConfig("defaultListenerScope")){B=D}}}}}return B},provideLegendInfo:function(A){A.push({name:this.getTitle()||this.getId(),mark:"black",disabled:this.getHidden(),series:this.getId(),index:0})},clearSprites:function(){var D=this.sprites,B,A,C;for(A=0,C=D.length;A<C;A++){B=D[A];if(B&&B.isSprite){B.destroy()}}this.sprites=[]},destroy:function(){var B=this,A=B._store,C=B.getConfig("tooltip",true);if(A&&A.getAutoDestroy()){Ext.destroy(A)}B.setChart(null);B.clearListeners();if(C){Ext.destroy(C);clearTimeout(B.tooltipTimeout)}B.callParent()}})