Ext.define("Ext.toolbar.Paging",{extend:"Ext.toolbar.Toolbar",xtype:"pagingtoolbar",alternateClassName:"Ext.PagingToolbar",requires:["Ext.toolbar.TextItem","Ext.form.field.Number"],mixins:["Ext.util.StoreHolder"],displayInfo:false,prependButtons:false,displayMsg:"Displaying {0} - {1} of {2}",emptyMsg:"No data to display",beforePageText:"Page",afterPageText:"of {0}",firstText:"First Page",prevText:"Previous Page",nextText:"Next Page",lastText:"Last Page",refreshText:"Refresh",inputItemWidth:30,emptyPageData:{total:0,currentPage:0,pageCount:0,toRecord:0,fromRecord:0},defaultBindProperty:"store",getPagingItems:function(){var B=this,A={scope:B,blur:B.onPagingBlur};A[Ext.supports.SpecialKeyDownRepeat?"keydown":"keypress"]=B.onPagingKeyDown;return[{itemId:"first",tooltip:B.firstText,overflowText:B.firstText,iconCls:Ext.baseCSSPrefix+"tbar-page-first",disabled:true,handler:B.moveFirst,scope:B},{itemId:"prev",tooltip:B.prevText,overflowText:B.prevText,iconCls:Ext.baseCSSPrefix+"tbar-page-prev",disabled:true,handler:B.movePrevious,scope:B},"-",B.beforePageText,{xtype:"numberfield",itemId:"inputItem",name:"inputItem",cls:Ext.baseCSSPrefix+"tbar-page-number",allowDecimals:false,minValue:1,hideTrigger:true,enableKeyEvents:true,keyNavEnabled:false,selectOnFocus:true,submitValue:false,isFormField:false,width:B.inputItemWidth,margin:"-1 2 3 2",listeners:A},{xtype:"tbtext",itemId:"afterTextItem",html:Ext.String.format(B.afterPageText,1)},"-",{itemId:"next",tooltip:B.nextText,overflowText:B.nextText,iconCls:Ext.baseCSSPrefix+"tbar-page-next",disabled:true,handler:B.moveNext,scope:B},{itemId:"last",tooltip:B.lastText,overflowText:B.lastText,iconCls:Ext.baseCSSPrefix+"tbar-page-last",disabled:true,handler:B.moveLast,scope:B},"-",{itemId:"refresh",tooltip:B.refreshText,overflowText:B.refreshText,iconCls:Ext.baseCSSPrefix+"tbar-loading",disabled:B.store.isLoading(),handler:B.doRefresh,scope:B}]},initComponent:function(){var B=this,A=B.items||B.buttons||[],C;B.bindStore(B.store||"ext-empty-store",true);C=B.getPagingItems();if(B.prependButtons){B.items=A.concat(C)}else{B.items=C.concat(A)}delete B.buttons;if(B.displayInfo){B.items.push("->");B.items.push({xtype:"tbtext",itemId:"displayItem"})}B.callParent()},beforeRender:function(){this.callParent(arguments);this.updateBarInfo()},updateBarInfo:function(){var A=this;if(!A.store.isLoading()){A.calledInternal=true;A.onLoad();A.calledInternal=false}},updateInfo:function(){var E=this,C=E.child("#displayItem"),A=E.store,B=E.getPageData(),D,F;if(C){D=A.getCount();if(D===0){F=E.emptyMsg}else{F=Ext.String.format(E.displayMsg,B.fromRecord,B.toRecord,B.total)}C.setText(F)}},onLoad:function(){var G=this,D,B,C,A,F,H,E;F=G.store.getCount();H=F===0;if(!H){D=G.getPageData();B=D.currentPage;C=D.pageCount;if(B>C){if(C>0){G.store.loadPage(C)}else{G.getInputItem().reset()}return }A=Ext.String.format(G.afterPageText,isNaN(C)?1:C)}else{B=0;C=0;A=Ext.String.format(G.afterPageText,0)}Ext.suspendLayouts();E=G.child("#afterTextItem");if(E){E.update(A)}E=G.getInputItem();if(E){E.setDisabled(H).setValue(B)}G.setChildDisabled("#first",B===1||H);G.setChildDisabled("#prev",B===1||H);G.setChildDisabled("#next",B===C||H);G.setChildDisabled("#last",B===C||H);G.setChildDisabled("#refresh",false);G.updateInfo();Ext.resumeLayouts(true);if(!G.calledInternal){G.fireEvent("change",G,D||G.emptyPageData)}},setChildDisabled:function(A,B){var C=this.child(A);if(C){C.setDisabled(B)}},getPageData:function(){var B=this.store,A=B.getTotalCount();return{total:A,currentPage:B.currentPage,pageCount:Math.ceil(A/B.pageSize),fromRecord:((B.currentPage-1)*B.pageSize)+1,toRecord:Math.min(B.currentPage*B.pageSize,A)}},onLoadError:function(){this.setChildDisabled("#refresh",false)},getInputItem:function(){return this.child("#inputItem")},readPageFromInput:function(B){var C=this.getInputItem(),D=false,A;if(C){A=C.getValue();D=parseInt(A,10);if(!A||isNaN(D)){C.setValue(B.currentPage);return false}}return D},onPagingBlur:function(C){var B=this.getInputItem(),A;if(B){A=this.getPageData().currentPage;B.setValue(A)}},onPagingKeyDown:function(B,A){this.processKeyEvent(B,A)},processKeyEvent:function(G,F){var D=this,C=F.getKey(),B=D.getPageData(),A=F.shiftKey?10:1,E;if(C===F.RETURN){F.stopEvent();E=D.readPageFromInput(B);if(E!==false){E=Math.min(Math.max(1,E),B.pageCount);if(E!==B.currentPage&&D.fireEvent("beforechange",D,E)!==false){D.store.loadPage(E)}}}else{if(C===F.HOME||C===F.END){F.stopEvent();E=C===F.HOME?1:B.pageCount;G.setValue(E)}else{if(C===F.UP||C===F.PAGE_UP||C===F.DOWN||C===F.PAGE_DOWN){F.stopEvent();E=D.readPageFromInput(B);if(E){if(C===F.DOWN||C===F.PAGE_DOWN){A*=-1}E+=A;if(E>=1&&E<=B.pageCount){G.setValue(E)}}}}}},beforeLoad:function(){this.setChildDisabled("#refresh",true)},moveFirst:function(){if(this.fireEvent("beforechange",this,1)!==false){this.store.loadPage(1);return true}return false},movePrevious:function(){var C=this,A=C.store,B=A.currentPage-1;if(B>0){if(C.fireEvent("beforechange",C,B)!==false){A.previousPage();return true}}return false},moveNext:function(){var D=this,A=D.store,C=D.getPageData().pageCount,B=A.currentPage+1;if(B<=C){if(D.fireEvent("beforechange",D,B)!==false){A.nextPage();return true}}return false},moveLast:function(){var B=this,A=B.getPageData().pageCount;if(B.fireEvent("beforechange",B,A)!==false){B.store.loadPage(A);return true}return false},doRefresh:function(){var B=this,A=B.store,C=A.currentPage;if(B.fireEvent("beforechange",B,C)!==false){A.loadPage(C);return true}return false},getStoreListeners:function(){return{beforeload:this.beforeLoad,load:this.onLoad,exception:this.onLoadError}},onBindStore:function(){if(this.rendered){this.updateBarInfo()}},onDestroy:function(){this.bindStore(null);this.callParent()}})