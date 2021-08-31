describe("Ext.LoadMask",function(){var B,E,D;beforeEach(function(){MockAjaxManager.addMethods()});function A(F){E=Ext.widget(F&&F.xtype||"component",Ext.apply({width:100,height:100,renderTo:Ext.getBody()},F))}function C(F,G){if(!E){A(G)}B=new Ext.LoadMask(Ext.apply({target:E},F));return B}afterEach(function(){Ext.destroy(E,B);B=E=null;MockAjaxManager.removeMethods()});describe("mask options",function(){describe("msg",function(){it("should default the message to Loading...",function(){C().show();expect(B.msgTextEl.dom.innerHTML).toEqual("Loading...")});it("should accept a custom message",function(){C({msg:"Foo"}).show();expect(B.msgTextEl.dom.innerHTML).toEqual("Foo")})});describe("msgCls",function(){it("should default to x-mask-loading",function(){C().show();expect(B.msgEl.hasCls("x-mask-loading")).toBe(true)});it("should accept a custom class",function(){C({msgCls:"foo"}).show();expect(B.msgEl.hasCls("foo")).toBe(true)})});describe("msgWrapCls",function(){it("should default to x-mask-msg",function(){C().show();expect(B.msgWrapEl.hasCls("x-mask-msg")).toBe(true)});it("should accept a custom class",function(){C({msgWrapCls:"foo"}).show();expect(B.msgWrapEl.hasCls("foo")).toBe(true)});it("should accept legacy maskCls config",function(){spyOn(Ext.log,"warn");C({maskCls:"foo"}).show();expect(B.msgWrapEl.hasCls("foo")).toBe(true)});it("should favor msgWrapCls over maskCls if both are present",function(){spyOn(Ext.log,"warn");C({maskCls:"foo",msgWrapCls:"bar"}).show();expect(B.msgWrapEl.hasCls("bar")).toBe(true);expect(B.msgWrapEl.hasCls("foo")).toBe(false)})});describe("useMsg",function(){it("should default to true",function(){C().show();expect(B.el.isVisible()).toBe(true)});it("should respect the useMsg: false",function(){C({useMsg:false}).show();expect(B.msgEl.isVisible()).toBe(false)});it("should should still show the mask even when useMsg: false",function(){C({useMsg:false}).show();expect(B.el.isVisible()).toBe(true)})});describe("useTargetEl",function(){it("should size to the targetEl & should default to false",function(){C().show();var F=B.el.getSize();expect(F.width).toBe(100);expect(F.height).toBe(100)});it("should size to the targetEl when useTargetEl: true",function(){C({useTargetEl:true},{xtype:"panel",width:100,height:100,renderTo:Ext.getBody(),title:"Title"}).show();var G=B.el.getSize(),F=E.body.getViewSize();expect(G.width).toBe(F.width);expect(G.height).toBe(F.height)})})});describe("z-index on show",function(){describe("with floating",function(){it("should have a higher z-index than the floater when used directly on a floater",function(){C(null,{floating:true});E.show();B.show();expect(B.getEl().getZIndex()).toBeGreaterThan(E.getEl().getZIndex())});it("should have a higher z-index than the floater when used on a direct child of a floater",function(){var F=new Ext.container.Container({floating:true,width:100,height:100,items:{xtype:"component"}});F.show();E=F.items.first();C();B.show();expect(B.getEl().getZIndex()).toBeGreaterThan(F.getEl().getZIndex());F.destroy()});it("should have a higher z-index than the floater when used on a deep child of a floater",function(){var F=new Ext.container.Container({floating:true,width:100,height:100,items:{xtype:"container",items:{xtype:"container",items:{xtype:"container",items:{xtype:"container",items:{xtype:"component",itemId:"foo"}}}}}});F.show();E=F.down("#foo");C();B.show();expect(B.getEl().getZIndex()).toBeGreaterThan(F.getEl().getZIndex());F.destroy()})})});describe("updating to target changes",function(){describe("root level component",function(){describe("floating target",function(){beforeEach(function(){E=new Ext.Component({floating:true,width:100,height:100,x:100,y:100});E.show()});it("should set the position of the mask to match the floater",function(){C().show();var F=B.el.getXY();expect(F[0]).toBe(100);expect(F[1]).toBe(100)});it("should change the position when the component moves",function(){C().show();E.setPosition(200,200);var F=B.el.getXY();expect(F[0]).toBe(200);expect(F[1]).toBe(200)})});describe("sizing",function(){it("should update the mask size when the component resizes",function(){C().show();E.setSize(150,200);var F=B.el.getSize();expect(F.width).toBe(150);expect(F.height).toBe(200)});it("should update the mask size to the targetEl when the component resizes",function(){C({useTargetEl:true},{xtype:"panel",renderTo:Ext.getBody(),width:100,height:100,title:"Title"}).show();E.setSize(150,200);var G=B.el.getSize(),F=E.body.getViewSize();expect(G.width).toBe(F.width);expect(G.height).toBe(F.height)})});describe("hide/show",function(){it("should hide the mask when the component is hidden",function(){C(null,{getMaskTarget:function(){return null}}).show();E.hide();expect(B.isVisible()).toBe(false)});it("should re-show the mask when toggling the hidden state",function(){C(null,{getMaskTarget:function(){return null}}).show();E.hide();E.show();expect(B.isVisible()).toBe(true)});it("should not show the mask if it's hidden during a toggle",function(){C(null,{getMaskTarget:function(){return null}}).show();E.hide();B.hide();E.show();expect(B.isVisible()).toBe(false)})});describe("disable/enable",function(){it("should not show the loadMask when loading a store if the mask is disabled",function(){var J,H,I,G,F=new Ext.grid.Panel({renderTo:document.body,title:"Test focus",height:300,width:600,store:{asynchronousLoad:false,proxy:{type:"ajax",url:"foo"}},loadMask:true,columns:[{text:"Columns one",width:200},{text:"Column two",flex:1}]});G=F.store;G.load();waitsFor(function(){I=F.view.loadMask;return I.isLoadMask},"Store not loaded");runs(function(){spyOn(I,"show").andCallThrough();expect(I.show.callCount).toBe(0);I.setDisabled(true);G.load();I.setDisabled(false);G.load();expect(I.show.callCount).toBe(1);F.destroy()})})});describe("expand/collapse",function(){beforeEach(function(){E=new Ext.panel.Panel({width:100,height:100,renderTo:Ext.getBody(),collapsible:true,animCollapse:false,getMaskTarget:function(){return null}})});it("should hide the mask when the component is collapsed",function(){C().show();E.collapse();expect(B.isVisible()).toBe(false)});it("should re-show the mask after expanding",function(){C().show();E.collapse();E.expand();expect(B.isVisible()).toBe(true)});it("should not show the mask if it's hidden during a toggle",function(){C().show();E.collapse();B.hide();E.expand();expect(B.isVisible()).toBe(false)})});describe("focus handling",function(){var F=jasmine.waitForFocus,J=jasmine.expectFocused,I,H,G;beforeEach(function(){E=new Ext.panel.Panel({width:100,height:100,renderTo:Ext.getBody(),items:[{xtype:"button",text:"foo"}]});C();I=E.down("button");I.focus();F(I)});afterEach(function(){Ext.destroy(H,G)});it("should not cause onFocusLeave consequences on show",function(){var N,K,M,L;G=new Ext.grid.Panel({renderTo:document.body,title:"Test focus",height:300,width:600,store:{proxy:{type:"ajax",url:"foo"}},loadMask:true,columns:[{text:"Columns one",width:200,locked:true},{text:"Column two",flex:1}]});L=G.store;jasmine.fireMouseEvent(G.getVisibleColumnManager().getColumns()[0].el,"mouseover");jasmine.fireMouseEvent(G.getVisibleColumnManager().getColumns()[0].triggerEl,"click");N=G.down("menu");K=N.child(":first");K.focus();F(K,"menuItemOne to recieve focus");runs(function(){L.fireEvent("beforeload",L)});waitsFor(function(){M=G.view.loadMask;return M&&M.isVisible()},"LoadMask to receive show");runs(function(){expect(N.isVisible()).toBe(true);expect(K.hasFocus).toBe(true);L.fireEvent("load",L)});waitsFor(function(){return !M.isVisible()},"LoadMask to hide");runs(function(){expect(K.hasFocus).toBe(true)})});it("should steal focus from within target on show",function(){runs(function(){B.show()});F(B);J(B)});describe("restoring focus on hide",function(){beforeEach(function(){runs(function(){B.show()});F(B)});it("should go to previously focused element",function(){runs(function(){B.hide()});F(I);J(I)});it("should not restore focus if mask el is not focused",function(){H=new Ext.button.Button({renderTo:Ext.getBody(),text:"bar"});runs(function(){H.focus()});F(H);runs(function(){B.hide()});F(H);J(H)})})})});describe("in a container",function(){var G,F,H;beforeEach(function(){H=function(I){E=new Ext.Component({getMaskTarget:function(){return null}});F=new Ext.panel.Panel({animCollapse:false,title:"Title2",collapsible:true,layout:"fit",items:E});G=new Ext.panel.Panel({animCollapse:false,width:200,height:200,floating:I,title:"Title1",collapsible:true,renderTo:Ext.getBody(),layout:"fit",items:F,x:I?100:undefined,y:I?100:undefined});G.show()}});afterEach(function(){Ext.destroy(G);H=G=F=null});describe("floating target",function(){it("should set the position of the mask to match the floater",function(){H(true);C().show();var J=B.el.getXY(),I=E.getPosition();expect(J[0]).toBe(I[0]);expect(J[1]).toBe(I[1])});it("should change the position when the component moves",function(){H(true);C().show();G.setPosition(200,200);var J=B.el.getXY(),I=E.getPosition();expect(J[0]).toBe(I[0]);expect(J[1]).toBe(I[1])})});describe("sizing",function(){it("should update the mask size when the component resizes",function(){H(null,{getMaskTarget:function(){return null}});C().show();G.setSize(250,300);var I=B.el.getSize(),J=E.getSize();expect(I.width).toBe(J.width);expect(I.height).toBe(J.height)})});describe("hide/show",function(){it("should hide the mask when the top-most container is hidden",function(){H(null,{getMaskTarget:function(){return null}});C().show();G.hide();expect(B.isVisible()).toBe(false)});it("should hide the mask when the direct parent container is hidden",function(){H(null,{getMaskTarget:function(){return null}});C().show();F.hide();expect(B.isVisible()).toBe(false)});it("should re-show the mask when the top-most container is shown",function(){H(null,{getMaskTarget:function(){return null}});C().show();G.hide();G.show();expect(B.isVisible()).toBe(true)});it("should re-show the mask when the direct parent container is shown",function(){H(null,{getMaskTarget:function(){return null}});C().show();F.hide();F.show();expect(B.isVisible()).toBe(true)});it("should not re-show the mask when the mask is hidden during the top-most toggle",function(){H(null,{getMaskTarget:function(){return null}});C().show();G.hide();B.hide();G.show();expect(B.isVisible()).toBe(false)});it("should not re-show the mask when the mask is hidden during the parent container toggle",function(){H(null,{getMaskTarget:function(){return null}});C().show();F.hide();B.hide();F.show();expect(B.isVisible()).toBe(false)})});describe("expand/collapse",function(){it("should hide the mask when the top-most container is collapsed",function(){H(null,{getMaskTarget:function(){return null}});C().show();G.collapse();expect(B.isVisible()).toBe(false)});it("should hide the mask when the direct parent container is collapsed",function(){H(null,{getMaskTarget:function(){return null}});C().show();F.collapse();expect(B.isVisible()).toBe(false)});it("should re-show the mask after the top-most container expands",function(){H(null,{getMaskTarget:function(){return null}});C().show();G.collapse();G.expand();expect(B.isVisible()).toBe(true)});it("should re-show the mask after the direct parent container expands",function(){H(null,{getMaskTarget:function(){return null}});C().show();F.collapse();F.expand();expect(B.isVisible()).toBe(true)});it("should not re-show the mask when the mask is hidden during the top-most toggle",function(){H(null,{getMaskTarget:function(){return null}});C().show();G.collapse();B.hide();G.expand();expect(B.isVisible()).toBe(false)});it("should not re-show the mask when the mask is hidden during the parent container toggle",function(){H(null,{getMaskTarget:function(){return null}});C().show();F.collapse();B.hide();F.expand();expect(B.isVisible()).toBe(false)})})})});describe("shim",function(){it("should not have a shim by default",function(){C().show();expect(B.el.shim).toBeUndefined()});it("should have a shim if configured with shim: true",function(){C({shim:true}).show();expect(B.el.shim instanceof Ext.dom.Shim).toBe(true);expect(B.el.shim.el.isVisible()).toBe(true)});it("should have a shim if Ext.useShims is true",function(){Ext.useShims=true;C().show();expect(B.el.shim instanceof Ext.dom.Shim).toBe(true);expect(B.el.shim.el.isVisible()).toBe(true);Ext.useShims=false});it("should hide the shim when the loadmask is hidden",function(){C({shim:true}).show();B.hide();expect(B.el.shim.el).toBeNull()});it("should show the shim when the loadmask is shown",function(){C({shim:true}).show();B.hide();B.show();expect(B.el.shim instanceof Ext.dom.Shim).toBe(true);expect(B.el.shim.el.isVisible()).toBe(true)});it("should allow shim to be enabled after first show",function(){C().show();expect(B.el.shim).toBeUndefined();B.hide();B.shim=true;B.show();expect(B.el.shim instanceof Ext.dom.Shim).toBe(true);expect(B.el.shim.el.isVisible()).toBe(true)});it("should allow shim to be disabled after first show",function(){C({shim:true}).show();expect(B.el.shim instanceof Ext.dom.Shim).toBe(true);expect(B.el.shim.el.isVisible()).toBe(true);B.hide();B.shim=false;B.show();expect(B.el.shim.el).toBeNull()})})})