Ext.define("Ext.ux.dashboard.GoogleRssView",{extend:"Ext.Component",requires:["Ext.tip.ToolTip","Ext.ux.google.Feeds"],feedCls:Ext.baseCSSPrefix+"dashboard-googlerss",previewCls:Ext.baseCSSPrefix+"dashboard-googlerss-preview",closeDetailsCls:Ext.baseCSSPrefix+"dashboard-googlerss-close",nextCls:Ext.baseCSSPrefix+"dashboard-googlerss-next",prevCls:Ext.baseCSSPrefix+"dashboard-googlerss-prev",feedUrl:null,scrollable:true,maxFeedEntries:10,previewTips:false,mode:"detail",closeDetailsGlyph:"8657@",prevGlyph:"9664@",nextGlyph:"9654@",detailTpl:'<tpl for="entries[currentEntry]"><div class="'+Ext.baseCSSPrefix+'dashboard-googlerss-detail-header"><div class="'+Ext.baseCSSPrefix+'dashboard-googlerss-detail-nav"><tpl if="parent.hasPrev"><span class="'+Ext.baseCSSPrefix+"dashboard-googlerss-prev "+Ext.baseCSSPrefix+'dashboard-googlerss-glyph">{parent.prevGlyph}</span> </tpl> {[parent.currentEntry+1]}/{parent.numEntries} <span class="'+Ext.baseCSSPrefix+"dashboard-googlerss-next "+Ext.baseCSSPrefix+'dashboard-googlerss-glyph"<tpl if="!parent.hasNext"> style="visibility:hidden"</tpl>> {parent.nextGlyph}</span> <span class="'+Ext.baseCSSPrefix+"dashboard-googlerss-close "+Ext.baseCSSPrefix+'dashboard-googlerss-glyph"> {parent.closeGlyph}</span> </div><div class="'+Ext.baseCSSPrefix+'dashboard-googlerss-title"><a href="{link}" target=_blank>{title}</a></div><div class="'+Ext.baseCSSPrefix+'dashboard-googlerss-author">By {author} - {publishedDate:this.date}</div></div><div class="'+Ext.baseCSSPrefix+'dashboard-googlerss-detail">{content}</div></tpl>',summaryTpl:'<tpl for="entries"><div class="'+Ext.baseCSSPrefix+'dashboard-googlerss"><span class="'+Ext.baseCSSPrefix+'dashboard-googlerss-title"><a href="{link}" target=_blank>{title}</a></span> <img src="'+Ext.BLANK_IMAGE_URL+'" data-index="{#}" class="'+Ext.baseCSSPrefix+'dashboard-googlerss-preview"><br><span class="'+Ext.baseCSSPrefix+'dashboard-googlerss-author">By {author} - {publishedDate:this.date}</span><br><span class="'+Ext.baseCSSPrefix+'dashboard-googlerss-snippet">{contentSnippet}</span><br></div></tpl>',initComponent:function(){var A=this;A.feedMgr=new google.feeds.Feed(A.feedUrl);A.callParent()},afterRender:function(){var A=this;A.callParent();if(A.feedMgr){A.refresh()}A.el.on({click:A.onClick,scope:A});if(A.previewTips){A.tip=new Ext.tip.ToolTip({target:A.el,delegate:"."+A.previewCls,maxWidth:800,showDelay:750,autoHide:false,scrollable:true,anchor:"top",listeners:{beforeshow:"onBeforeShowTip",scope:A}})}},formatDate:function(B){if(!B){return""}B=new Date(B);var A=new Date(),D=Ext.Date.clearTime(A,true),C=Ext.Date.clearTime(B,true).getTime();if(C===D.getTime()){return"Today "+Ext.Date.format(B,"g:i a")}D=Ext.Date.add(D,"d",-6);if(D.getTime()<=C){return Ext.Date.format(B,"D g:i a")}if(D.getYear()===A.getYear()){return Ext.Date.format(B,"D M d \\a\\t g:i a")}return Ext.Date.format(B,"D M d, Y \\a\\t g:i a")},getTitle:function(){var A=this.data;return A&&A.title},onBeforeShowTip:function(C){if(this.mode!=="summary"){return false}var B=C.triggerElement,A=parseInt(B.getAttribute("data-index"),10);C.maxHeight=Ext.Element.getViewportHeight()/2;C.update(this.data.entries[A-1].content)},onClick:function(D){var B=this,A=B.data.currentEntry,C=Ext.fly(D.getTarget());if(C.hasCls(B.nextCls)){B.setCurrentEntry(A+1)}else{if(C.hasCls(B.prevCls)){B.setCurrentEntry(A-1)}else{if(C.hasCls(B.closeDetailsCls)){B.setMode("summary")}else{if(C.hasCls(B.previewCls)){B.setMode("detail",parseInt(C.getAttribute("data-index"),10))}}}}},refresh:function(){var A=this;if(!A.feedMgr){return }A.fireEvent("beforeload",A);A.feedMgr.setNumEntries(A.maxFeedEntries);A.feedMgr.load(function(B){A.setFeedData(B.feed);A.fireEvent("load",A)})},setCurrentEntry:function(A){this.setMode(this.mode,A)},setFeedData:function(B){var D=this,A=B.entries,C=A&&A.length||0,E=Ext.apply({numEntries:C,closeGlyph:D.wrapGlyph(D.closeDetailsGlyph),prevGlyph:D.wrapGlyph(D.prevGlyph),nextGlyph:D.wrapGlyph(D.nextGlyph),currentEntry:0},B);D.data=E;D.setMode(D.mode)},setMode:function(E,A){var B=this,C=B.data,D=(A===undefined)?C.currentEntry:A;B.tpl=B.getTpl(E+"Tpl");B.tpl.date=B.formatDate;B.mode=E;C.currentEntry=D;C.hasNext=D+1<C.numEntries;C.hasPrev=D>0;B.update(C);B.el.dom.scrollTop=0},wrapGlyph:function(C){var D=Ext._glyphFontFamily,B,A;if(typeof C==="string"){B=C.split("@");C=B[0];D=B[1]}A="&#"+C+";";if(D){A='<span style="font-family:'+D+'">'+A+"</span>"}return A},beforeDestroy:function(){Ext.destroy(this.tip);this.callParent()}})