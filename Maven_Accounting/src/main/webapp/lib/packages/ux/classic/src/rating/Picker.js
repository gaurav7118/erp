Ext.define("Ext.ux.rating.Picker",{extend:"Ext.Widget",xtype:"rating",focusable:true,cachedConfig:{family:"monospace",glyphs:"☆★",minimum:1,limit:5,overStyle:null,rounding:1,scale:"125%",selectedStyle:null,tooltip:null,trackOver:true,value:null,tooltipText:null,trackingValue:null},config:{animate:null},element:{cls:"u"+Ext.baseCSSPrefix+"rating-picker",reference:"element",children:[{reference:"innerEl",cls:"u"+Ext.baseCSSPrefix+"rating-picker-inner",listeners:{click:"onClick",mousemove:"onMouseMove",mouseenter:"onMouseEnter",mouseleave:"onMouseLeave"},children:[{reference:"valueEl",cls:"u"+Ext.baseCSSPrefix+"rating-picker-value"},{reference:"trackerEl",cls:"u"+Ext.baseCSSPrefix+"rating-picker-tracker"}]}]},defaultBindProperty:"value",twoWayBindable:"value",overCls:"u"+Ext.baseCSSPrefix+"rating-picker-over",trackOverCls:"u"+Ext.baseCSSPrefix+"rating-picker-track-over",applyGlyphs:function(A){if(typeof A==="string"){if(A.length!==2){Ext.raise('Expected 2 characters for "glyphs" not "'+A+'".')}A=[A.charAt(0),A.charAt(1)]}else{if(typeof A[0]==="number"){A=[String.fromCharCode(A[0]),String.fromCharCode(A[1])]}}return A},applyOverStyle:function(A){this.trackerEl.applyStyles(A)},applySelectedStyle:function(A){this.valueEl.applyStyles(A)},applyTooltip:function(A){if(A&&typeof A!=="function"){if(!A.isTemplate){A=new Ext.XTemplate(A)}A=A.apply.bind(A)}return A},applyTrackingValue:function(A){return this.applyValue(A)},applyValue:function(C){if(C!==null){var B=this.getRounding(),A=this.getLimit(),D=this.getMinimum();C=Math.round(Math.round(C/B)*B*1000)/1000;C=(C<D)?D:(C>A?A:C)}return C},onClick:function(A){var B=this.valueFromEvent(A);this.setValue(B)},onMouseEnter:function(){this.element.addCls(this.overCls)},onMouseLeave:function(){this.element.removeCls(this.overCls)},onMouseMove:function(A){var B=this.valueFromEvent(A);this.setTrackingValue(B)},updateFamily:function(A){this.element.setStyle("fontFamily","'"+A+"'")},updateGlyphs:function(){this.refreshGlyphs()},updateLimit:function(){this.refreshGlyphs()},updateScale:function(A){this.element.setStyle("fontSize",A)},updateTooltip:function(){this.refreshTooltip()},updateTooltipText:function(E){var D=this.innerEl,A=Ext.tip&&Ext.tip.QuickTipManager,B=A&&A.tip,C;if(A){D.dom.setAttribute("data-qtip",E);this.trackerEl.dom.setAttribute("data-qtip",E);C=B&&B.activeTarget;C=C&&C.el;if(C&&D.contains(C)){B.update(E)}}},updateTrackingValue:function(D){var C=this,A=C.trackerEl,B=C.valueToPercent(D);A.setStyle("width",B);C.refreshTooltip()},updateTrackOver:function(A){this.element[A?"addCls":"removeCls"](this.trackOverCls)},updateValue:function(H,C){var F=this,B=F.getAnimate(),G=F.valueEl,E=F.valueToPercent(H),D,A;if(F.isConfiguring||!B){G.setStyle("width",E)}else{G.stopAnimation();G.animate(Ext.merge({from:{width:F.valueToPercent(C)},to:{width:E}},B))}F.refreshTooltip();if(!F.isConfiguring){if(F.hasListeners.change){F.fireEvent("change",F,H,C)}D=F.getWidgetColumn&&F.getWidgetColumn();A=D&&F.getWidgetRecord&&F.getWidgetRecord();if(A&&D.dataIndex){A.set(D.dataIndex,H)}}},afterCachedConfig:function(){this.refresh();return this.callParent(arguments)},initConfig:function(A){this.isConfiguring=true;this.callParent([A]);this.refresh()},setConfig:function(){var A=this;A.isReconfiguring=true;A.callParent(arguments);A.isReconfiguring=false;A.refresh();return A},destroy:function(){this.tip=Ext.destroy(this.tip);this.callParent()},privates:{getGlyphTextNode:function(B){var A=B.lastChild;if(!A||A.nodeType!==3){A=B.ownerDocument.createTextNode("");B.appendChild(A)}return A},getTooltipData:function(){var A=this;return{component:A,tracking:A.getTrackingValue(),trackOver:A.getTrackOver(),value:A.getValue()}},refresh:function(){var A=this;if(A.invalidGlyphs){A.refreshGlyphs(true)}if(A.invalidTooltip){A.refreshTooltip(true)}},refreshGlyphs:function(A){var I=this,G=!A&&(I.isConfiguring||I.isReconfiguring),C,J,F,H,E,B,D;if(!G){C=I.getGlyphTextNode(I.innerEl.dom);D=I.getGlyphTextNode(I.valueEl.dom);B=I.getGlyphTextNode(I.trackerEl.dom);J=I.getGlyphs();F=I.getLimit();for(H=E="";F--;){E+=J[0];H+=J[1]}C.nodeValue=E;D.nodeValue=H;B.nodeValue=H}I.invalidGlyphs=G},refreshTooltip:function(B){var C=this,A=!B&&(C.isConfiguring||C.isReconfiguring),E=C.getTooltip(),D,F;if(!A){E=C.getTooltip();if(E){D=C.getTooltipData();F=E(D);C.setTooltipText(F)}}C.invalidTooltip=A},valueFromEvent:function(A){var F=this,B=F.innerEl,E=A.getX(),J=F.getRounding(),D=B.getX(),G=E-D,H=B.getWidth(),C=F.getLimit(),I;if(F.getInherited().rtl){G=H-G}I=G/H*C;I=Math.ceil(I/J)*J;return I},valueToPercent:function(A){A=(A/this.getLimit())*100;return A+"%"}}})