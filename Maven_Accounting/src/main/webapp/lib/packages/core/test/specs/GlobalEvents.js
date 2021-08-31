describe("Ext.GlobalEvents",function(){describe("idle event",function(){var D=Ext.isIE?50:10,A,B;function C(){A=true}beforeEach(function(){A=false;B=false;Ext.on("idle",C)});afterEach(function(){Ext.un("idle",C)});it("should fire after DOM event handler are invoked, but before control is returned to the browser",function(){var F=Ext.getBody().createChild(),G=0;function E(){expect(A).toBe(false);G++}F.on("mousedown",E);F.on("mousedown",function(){E()});jasmine.fireMouseEvent(F,"mousedown");expect(G).toBe(2);expect(A).toBe(true);F.destroy()});it("should fire after a JsonPProxy processes a return packet",function(){var E=Ext.create("Ext.data.Store",{proxy:{type:"jsonp",reader:{rootProperty:"topics",totalProperty:"totalCount"},url:"http://www.sencha.com/forum/remote_topics/index.php"},fields:["title"],listeners:{load:function(){B=true}}});E.loadPage(1);waitsFor(function(){return B===true});runs(function(){waits(D);runs(function(){expect(A).toBe(true);E.destroy()})})});it("should fire after a JsonP request is processed",function(){Ext.data.JsonP.request({url:"http://www.sencha.com/forum/remote_topics/index.php?page=1&start=0&limit=100",callback:function(){B=true}});waitsFor(function(){return B===true});runs(function(){waits(D);runs(function(){expect(A).toBe(true)})})});it("should fire after an Ajax request is processed",function(){Ext.Ajax.request({url:"resources/foo.json",callback:function(){B=true}});waitsFor(function(){return B===true});runs(function(){waits(D);runs(function(){expect(A).toBe(true)})})});it("should fire after a scheduled Task is run",function(){Ext.TaskManager.newTask({run:function(){B=true},repeat:1,interval:1}).start();waitsFor(function(){return B===true});runs(function(){waits(D);runs(function(){expect(A).toBe(true)})})})});describe("scroll event",function(){var D,B,C=[],E=Ext.supports.touchScroll?xit:it;afterEach(function(){D.destroy();B.destroy()});function A(F){C.push(F.getElement())}E("should fire the global scroll event whenever anything scrolls",function(){D=Ext.getBody().createChild({style:"height:10000px"});B=new Ext.Panel({renderTo:document.body,floating:true,x:0,y:0,width:300,height:300,layout:"auto",scrollable:true,items:{xtype:"component",style:"height:1000px"}});Ext.on({scroll:A});Ext.scroll.DomScroller.document.scrollBy(null,100);waitsFor(function(){return C.length===1&&C[0]===Ext.scroll.DomScroller.document.getElement()});runs(function(){B.getScrollable().scrollBy(null,100)});waitsFor(function(){return C.length===2&&C[1]===B.getScrollable().getElement()})})})})