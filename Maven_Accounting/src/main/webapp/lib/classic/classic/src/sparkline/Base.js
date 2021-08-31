Ext.define("Ext.sparkline.Base",{extend:"Ext.Widget",requires:["Ext.XTemplate","Ext.sparkline.CanvasCanvas","Ext.sparkline.VmlCanvas"],cachedConfig:{baseCls:Ext.baseCSSPrefix+"sparkline",lineColor:"#157fcc",fillColor:"#def",defaultPixelsPerValue:3,tagValuesAttribute:"values",enableTagOptions:false,enableHighlight:true,highlightColor:null,highlightLighten:1.4,tooltipSkipNull:true,tooltipPrefix:"",tooltipSuffix:"",disableTooltips:false,disableInteraction:false,tipTpl:null},config:{values:null},element:{tag:"canvas",reference:"element",style:{display:"inline-block",verticalAlign:"top"},listeners:{mouseenter:"onMouseEnter",mouseleave:"onMouseLeave",mousemove:"onMouseMove"},width:0,height:0},defaultBindProperty:"values",redrawQueue:{},inheritableStatics:{sparkLineTipClass:Ext.baseCSSPrefix+"sparkline-tip-target",onClassCreated:function(A){var C=A.prototype,D=A.getConfigurator().configs,B;for(B in D){if(B!=="tipTpl"){C[Ext.Config.get(B).names.apply]=C.applyConfigChange}}}},constructor:function(A){var B=this;B.canvas=Ext.supports.Canvas?new Ext.sparkline.CanvasCanvas(B):new Ext.sparkline.VmlCanvas(B);if(!B.getDisableTooltips()){B.element.cls=Ext.sparkline.Base.sparkLineTipClass}Ext.apply(B,A);B.callParent([A]);B.el=B.element},all:function(D,B,A){var C;for(C=B.length;C--;){if(A&&B[C]===null){continue}if(B[C]!==D){return false}}return true},applyConfigChange:function(B){var A=this;A.redrawQueue[A.getId()]=A;if(!A.redrawTimer){Ext.sparkline.Base.prototype.redrawTimer=Ext.Function.requestAnimationFrame(A.processRedrawQueue)}return B},applyTipTpl:function(A){if(!A.isTemplate){A=new Ext.XTemplate(A)}return A},normalizeValue:function(B){var A;switch(B){case"undefined":B=undefined;break;case"null":B=null;break;case"true":B=true;break;case"false":B=false;break;default:A=parseFloat(B);if(B==A){B=A}}return B},normalizeValues:function(C){var B,A=[];for(B=C.length;B--;){A[B]=this.normalizeValue(C[B])}return A},updateWidth:function(B,A){var C=this,D=C.element.dom;C.callParent([B,A]);C.canvas.setWidth(B);C.width=B;if(C.height==null){C.setHeight(parseInt(C.measurer.getCachedStyle(D.parentNode,"line-height"),10))}else{C.redrawQueue[C.getId()]=C}},updateHeight:function(A,C){var B=this;B.callParent([A,C]);B.canvas.setHeight(A);B.height=A;B.redrawQueue[B.getId()]=B},updateValues:function(A){this.values=A},redraw:function(){var A=this;if(A.getValues()){A.onUpdate();A.canvas.onOwnerUpdate();A.renderGraph()}},onUpdate:Ext.emptyFn,renderGraph:function(){var A=true;if(this.disabled){this.canvas.reset();A=false}return A},onMouseEnter:function(A){this.onMouseMove(A)},onMouseMove:function(A){this.tooltip.triggerEvent=A;this.currentPageXY=A.getPoint();this.redraw()},onMouseLeave:function(){var A=this;A.currentPageXY=A.targetX=A.targetY=null;A.redraw();A.tooltip.target=null;A.tooltip.hide()},updateDisplay:function(){var C=this,B=C.getValues(),F,D=C.tooltip,A,E;if(B&&B.length&&C.currentPageXY&&C.el.getRegion().contains(C.currentPageXY)){F=C.canvas.el.getXY();E=C.getRegion(C.currentPageXY[0]-F[0],C.currentPageXY[1]-F[1]);if(E!=null&&E<B.length){if(!C.disableHighlight){C.renderHighlight(E)}A=C.getRegionTooltip(E)}C.fireEvent("sparklineregionchange",C);if(A){if(!C.lastTooltipHTML||A[0]!==C.lastTooltipHTML[0]||A[1]!==C.lastTooltipHTML[1]){D.setTitle(A[0]);D.update(A[1]);C.lastTooltipHTML=A}D.target=C.el;D.onTargetOver(D.triggerEvent)}}if(!A){D.target=null;D.hide()}},getRegion:Ext.emptyFn,getRegionTooltip:function(L){var K=this,E=K.tooltipChartTitle,F=[],N=K.getTipTpl(),G,H,D,I,B,M,J,C,A;G=K.getRegionFields(L);M=K.tooltipFormatter;if(M){return M(K,K,G)}if(!N){return""}if(!Ext.isArray(G)){G=[G]}H=K.tooltipFormatFieldlist;D=K.tooltipFormatFieldlistKey;if(H&&D){I=[];for(C=G.length;C--;){B=G[C][D];if((A=Ext.Array.indexOf(B,H))!==-1){I[A]=G[C]}}G=I}J=G.length;for(A=0;A<J;A++){if(!G[A].isNull||!K.getTooltipSkipNull()){Ext.apply(G[A],{prefix:K.getTooltipPrefix(),suffix:K.getTooltipSuffix()});F.push(N.apply(G[A]))}}if(E||F.length){return[E,F.join("<br>")]}return""},getRegionFields:Ext.emptyFn,calcHighlightColor:function(A){var D=this,H=D.getHighlightColor(),C=D.getHighlightLighten(),G,F,E,B;if(H){return H}if(C){G=/^#([0-9a-f])([0-9a-f])([0-9a-f])$/i.exec(A)||/^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i.exec(A);if(G){E=[];F=A.length===4?16:1;for(B=0;B<3;B++){E[B]=Ext.Number.constrain(Math.round(parseInt(G[B+1],16)*F*C),0,255)}return"rgb("+E.join(",")+")"}}return A},destroy:function(){delete this.redrawQueue[this.getId()];this.callParent()}},function(B){var A=B.prototype;Ext.onInternalReady(function(){A.tooltip=new Ext.tip.ToolTip({id:"sparklines-tooltip",showDelay:0,dismissDelay:0,hideDelay:400})});B.onClassCreated(B);A.processRedrawQueue=function(){var C=A.redrawQueue,D;for(D in C){C[D].redraw()}A.redrawQueue={};A.redrawTimer=0};if(!Ext.supports.Canvas){B.prototype.element={tag:"span",reference:"element",listeners:{mouseenter:"onMouseEnter",mouseleave:"onMouseLeave",mousemove:"onMouseMove"},style:{display:"inline-block",position:"relative",overflow:"hidden",margin:"0px",padding:"0px",verticalAlign:"top",cursor:"default"},children:[{tag:"svml:group",reference:"groupEl",coordorigin:"0 0",coordsize:"0 0",style:"position:absolute;width:0;height:0;pointer-events:none"}]}}})