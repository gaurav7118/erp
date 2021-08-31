describe("Ext.menu.KeyNav",function(){var B;function A(C){B=new Ext.menu.Menu(Ext.apply({text:"Main Menu",width:120,height:120,floating:false,renderTo:Ext.getBody(),items:[{text:"Menu One"},{text:"Menu Two",menuExpandDelay:0,menu:{items:[{text:"Next Level"},{text:"Next Level"},{text:"Next Level"}]}},{text:"Menu Three"},{text:"Menu Four"}]},C))}afterEach(function(){B.hide();Ext.destroy(B);B=null});describe("enter key nav",function(){describe("href property",function(){var C;afterEach(function(){C=null;location.hash=""});it("should follow the target",function(){A({items:[{text:"menu item one",href:"#ledzep"},{text:"menu item two"}]});C=B.items.first();B.activeItem=B.focusedItem=C;jasmine.fireKeyEvent(C.itemEl.dom,"keydown",13);waitsFor(function(){return location.hash==="#ledzep"},"timed out waiting for hash to change",1000);runs(function(){expect(location.hash).toBe("#ledzep")})});it("should not follow the target if the click listener stops the event",function(){var D=Ext.isIE?"#":"";A({items:[{text:"menu item one",href:"#motley",listeners:{click:function(E,F){F.preventDefault()}}},{text:"menu item two"}]});C=B.items.first();B.activeItem=B.focusedItem=C;jasmine.fireKeyEvent(C.itemEl.dom,"keydown",13);waitsFor(function(){return location.hash===D},"timed out waiting for hash to change",1000);runs(function(){expect(location.hash).toBe(D)})})})});describe("left key nav",function(){var C,D;beforeEach(function(){A()});afterEach(function(){C=D=null});it("should only hide child menus",function(){C=B.down('[text="Menu Two"]').el.dom;jasmine.fireMouseEvent(C,"mouseover");D=B.down("menu");C=D.el.down(".x-menu-item-link",true);jasmine.fireKeyEvent(C,"keydown",37);expect(D.hidden).toBe(true)});describe("parent menu",function(){it("should not hide",function(){C=B.el.down(".x-menu-item-link",true);jasmine.fireKeyEvent(C,"keydown",37);expect(B.hidden).toBe(false)});it("should not hide (tests hiding child menu first)",function(){C=B.down('[text="Menu Two"]').el.dom;jasmine.fireMouseEvent(C,"mouseover");D=B.down("menu");C=D.el.down(".x-menu-item-link",true);jasmine.fireKeyEvent(C,"keydown",37);C=B.el.down(".x-menu-item-link",true);jasmine.fireKeyEvent(C,"keydown",37);expect(B.hidden).toBe(false)})})})})