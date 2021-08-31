describe("Ext.layout.container.Fit",function(){var A;afterEach(function(){Ext.destroy(A);A=null});function B(D,C){var E=Ext.failedLayouts;A=Ext.widget(Ext.apply({renderTo:Ext.getBody(),width:100,height:100,defaultType:"component",xtype:"container",layout:Ext.apply({type:"fit"},C)},D));if(E!=Ext.failedLayouts){expect("failedLayout=true").toBe("false")}}describe("should handle minWidth and/or minHeight",function(){it("should stretch the configured size child",function(){B({width:undefined,height:undefined,floating:true,minWidth:100,minHeight:100,items:{xtype:"component",width:50,height:50}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 100 100"}}}})})});describe("Fixed dimensions",function(){it("should size the child item to the parent",function(){B({items:{}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 100 100"}}}})});it("should account for padding on the owner",function(){B({padding:10,items:{}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 10 80 80"}}}})});it("should account for top padding on the owner",function(){B({padding:"10 0 0 0",items:{}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 10 100 90"}}}})});it("should account for right padding on the owner",function(){B({padding:"0 10 0 0",items:{}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 90 100"}}}})});it("should account for bottom padding on the owner",function(){B({padding:"0 0 10 0",items:{}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 100 90"}}}})});it("should account for left padding on the owner",function(){B({padding:"0 0 0 10",items:{}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 0 90 100"}}}})});it("should account for margin on the child",function(){B({items:{margin:10}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 10 80 80"}}}})});it("should account for a top margin on the child",function(){B({items:{margin:"10 0 0 0"}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 10 100 90"}}}})});it("should account for a right margin on the child",function(){B({items:{margin:"0 10 0 0"}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 90 100"}}}})});it("should account for a bottom margin on the child",function(){B({items:{margin:"0 0 10"}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 100 90"}}}})});it("should account for a left margin on the child",function(){B({items:{margin:"0 0 0 10"}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 0 90 100"}}}})});it("should account for both padding & margin",function(){B({padding:10,items:{margin:10}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"20 20 60 60"}}}})});it("should account for margin and bodyPadding in a panel",function(){B({items:{margin:10},bodyPadding:"5 15",border:false,xtype:"panel"});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"25 15 50 70"}}}})});it("should support margin and a style margin",function(){B({items:{style:{margin:"10px"},margin:15}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"15 15 70 70"}}}})});it("should support multiple items",function(){B({style:"position: relative",items:[{},{style:{position:"absolute"},itemId:"second"}]});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 100 100"}},1:{el:{xywh:"0 0 100 100"}}}})});it("should support multiple items with margin & padding",function(){B({style:"position: relative",padding:10,items:[{margin:true},{style:{position:"absolute"},itemId:"second",margin:20}]});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"15 15 70 70"}},1:{el:{xywh:"20 20 40 40"}}}})});it("should prioritize fitting the child over a configured size",function(){B({items:{height:50,margin:10,width:50}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 10 80 80"}}}})})});describe("Shrink-wrapping",function(){it("should force the parent to the child size",function(){B({floating:true,items:{width:100,height:100}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"0 0 100 100"}}}})});it("should take into account owner padding",function(){B({floating:true,padding:10,items:{width:80,height:80}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 10 80 80"}}}})});it("should take into account child margin",function(){B({floating:true,items:{margin:10,width:80,height:80}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 10 80 80"}}}})});it("should account for both padding/margin",function(){B({floating:true,padding:10,items:{margin:10,width:60,height:60}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"20 20 60 60"}}}})});it("should account for left padding & a top margin",function(){B({floating:true,padding:"0 0 0 10",items:{margin:"10 0 0",width:90,height:90}});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"10 10 90 90"}}}})});it("should account for margin in a panel",function(){B({floating:true,items:{margin:"10 5 20 15",width:80,height:70},border:false,xtype:"panel"});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"15 10 80 70"}}}})});it("should account for margin and bodyPadding in a panel",function(){B({floating:true,items:{margin:10,width:70,height:70},bodyPadding:5,border:false,xtype:"panel"});expect(A).toHaveLayout({el:{w:100,h:100},items:{0:{el:{xywh:"15 15 70 70"}}}})});it("should account for hscrollbar if overflowing",function(){B({floating:true,width:100,height:undefined,autoScroll:true,items:{minWidth:200,height:50}});expect(A).toHaveLayout({el:{w:100,h:50+Ext.getScrollbarSize().height},items:{0:{el:{xywh:"0 0 200 50"}}}})});it("should account for vscrollbar if overflowing",function(){B({floating:true,xtype:"panel",border:false,width:undefined,height:100,autoScroll:true,items:{minHeight:200,width:50}});expect(A).toHaveLayout({el:{w:50+Ext.getScrollbarSize().width,h:100},items:{0:{el:{xywh:"0 0 50 200"}}}})})});it("should not fail when the item is hidden & the container is shrink wrapping",function(){expect(function(){A=new Ext.container.Container({shrinkWrap:3,renderTo:Ext.getBody(),layout:"fit",items:{hidden:true,xtype:"component"}})}).not.toThrow()})})