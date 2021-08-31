describe("Ext.util.Floating",function(){var C;function B(D){C=new Ext.Component(Ext.apply({floating:true},D))}function A(E,D,G){var H={fn:G||Ext.emptyFn},F=spyOn(H,"fn");E.addListener(D,H.fn);return F}afterEach(function(){if(C){C.destroy();C=null}});it("should fire the deactivate event once on hide",function(){B();var E=A(C,"activate"),D=A(C,"deactivate");C.show();expect(E.callCount).toBe(1);expect(D.callCount).toBe(0);C.hide();expect(E.callCount).toBe(1);expect(D.callCount).toBe(1)});it("should call the floating constructor on first show",function(){B();spyOn(C.mixins.floating,"constructor").andCallThrough();C.show();expect(C.mixins.floating.constructor).toHaveBeenCalled()});it("should have the x-layer CSS class on its element",function(){B();C.show();expect(C.el).toHaveCls("x-layer")});it("should have the x-fixed-layer CSS class if fixed is true",function(){B({fixed:true});C.show();expect(C.el).toHaveCls("x-fixed-layer")});it("should wait until first show to render the component",function(){B();expect(C.rendered).toBe(false);expect(C.el).toBeUndefined();C.show();expect(C.rendered).toBe(true);expect(C.el instanceof Ext.dom.Element).toBe(true)});it("should render the component to the renderTo element",function(){var D=Ext.getBody().createChild();B({renderTo:D});expect(C.rendered).toBe(true);expect(C.el.parent()).toBe(D);expect(C.el.isVisible()).toBe(true);D.destroy()});it("should render the component as hidden to the renderTo el if hidden is true",function(){var D=Ext.getBody().createChild();B({renderTo:D,hidden:true});expect(C.rendered).toBe(true);expect(C.el.parent()).toBe(D);expect(C.el.isVisible()).toBe(false);D.destroy()});it("it should show the element when the component is shown",function(){B();C.show();expect(C.el.isVisible()).toBe(true)});it("it should hide the element when the component is hidden",function(){B();C.show();C.hide();expect(C.el.isVisible()).toBe(false)});describe("shim",function(){it("should not have a shim by default",function(){B();C.show();expect(C.el.shim).toBeUndefined()});it("should create a shim if shim is true",function(){B({shim:true});C.show();expect(C.el.shim instanceof Ext.dom.Shim).toBe(true)});it("should create a shim if Ext.useShims is true",function(){Ext.useShims=true;B({shim:true});C.show();expect(C.el.shim instanceof Ext.dom.Shim).toBe(true);Ext.useShims=false});it("should set position:fixed on the shim if fixed is true",function(){B({fixed:true,shim:true});C.show();expect(C.el.shim.el.getStyle("position")).toBe("fixed")})});describe("shadow",function(){it("should have a shadow by default",function(){B();C.show();expect(C.el.shadow instanceof Ext.dom.Shadow).toBe(true)});it("should not have a shadow if shadow is false",function(){B({shadow:false});C.show();expect(C.el.shadow).toBeUndefined()});it("should pass shadowOffset along to the shadow",function(){B({shadowOffset:15});C.show();expect(C.el.shadow.offset).toBe(15)});it("should use 'sides' as the default mode",function(){B();C.show();expect(C.el.shadow.mode).toBe("sides")});it("should pass a string shadow config along as the 'mode' config of the shadow",function(){B({shadow:"drop"});C.show();expect(C.el.shadow.mode).toBe("drop")});it("should set position:fixed on the shadow if fixed is true",function(){B({fixed:true});C.show();expect(C.el.shadow.el.getStyle("position")).toBe("fixed")});it("should hide the shadow during animations",function(){var D=false,F,E;B({width:200,height:100,x:100,y:100});C.show();F=C.el.shadow;E=F.el;expect(E.isVisible()).toBe(true);C.el.setXY([350,400],{duration:200,listeners:{afteranimate:function(){D=true}}});waitsFor(function(){return !F.el&&!E.isVisible()},"Shadow was never hidden",150);waitsFor(function(){return D},"Animation never completed",300);runs(function(){expect(F.el.isVisible()).toBe(true);expect(F.el.getX()).toBe(Ext.isIE8?345:350);expect(F.el.getY()).toBe(Ext.isIE8?397:404);expect(F.el.getWidth()).toBe(Ext.isIE8?209:200);expect(F.el.getHeight()).toBe(Ext.isIE8?107:96)})});it("should not hide the shadow during animations if animateShadow is true",function(){var D=false,E;B({animateShadow:true,width:200,height:100,x:100,y:100});C.show();E=C.el.shadow;spyOn(E,"hide").andCallThrough();expect(E.el.isVisible()).toBe(true);C.el.setXY([350,400],{duration:50,listeners:{afteranimate:function(){D=true}}});waitsFor(function(){return D},"Animation never completed",300);runs(function(){expect(E.hide).not.toHaveBeenCalled();expect(E.el.isVisible()).toBe(true);expect(E.el.getX()).toBe(Ext.isIE8?345:350);expect(E.el.getY()).toBe(Ext.isIE8?397:404);expect(E.el.getWidth()).toBe(Ext.isIE8?209:200);expect(E.el.getHeight()).toBe(Ext.isIE8?107:96)})})});describe("setActive",function(){describe("focus",function(){it("should not focus the floater if a descandant component contains focus",function(){C=new Ext.window.Window({autoShow:true,floating:true,items:[{xtype:"textfield",itemId:"text"}]});var D=C.down("#text");jasmine.focusAndWait(D);runs(function(){C.setActive(true,true)});jasmine.waitAWhile();runs(function(){expect(Ext.ComponentManager.getActiveComponent()).toBe(D)})});it("should not focus the floater if a descandant component contains focus and it is not in the same DOM hierarchy",function(){C=new Ext.window.Window({autoShow:true,floating:true});var D=new Ext.form.field.Text({renderTo:Ext.getBody(),getRefOwner:function(){return C}});jasmine.focusAndWait(D);runs(function(){C.setActive(true,true)});jasmine.waitAWhile();runs(function(){expect(Ext.ComponentManager.getActiveComponent()).toBe(D);D.destroy()})})})});describe("scroll alignment when rendered to body",function(){var F,H,E,G;function D(I){F=jasmine.createSpy();G=Ext.GlobalEvents.hasListeners.scroll;H={renderTo:Ext.getBody(),width:400,height:400,scrollable:true};if(I){H.items=[{xtype:"component",autoEl:{html:"A",style:"float:left;width:100px;height:500px"}},{xtype:"component",id:"align",autoEl:{html:"B",style:"float:left;width:100px;height:200px"}}]}else{H.autoEl={children:[{html:"A",style:{"float":"left",width:"100px",height:"500px"}},{html:"B",cls:"align",style:{"float":"left",width:"100px",height:"200px"}}]}}H=new (I?Ext.Container:Ext.Component)(H);E=new Ext.Component({autoShow:true,floating:true,shadow:false,width:50,height:50,style:"border: 1px solid black"})}afterEach(function(){Ext.un("scroll",F);G=H=E=F=Ext.destroy(E,H)});describe("aligning to element",function(){beforeEach(function(){D(false)});it("should keep the floater aligned on scroll",function(){var I=spyOn(E,"alignTo").andCallThrough();E.alignTo(H.getEl().down(".align"),"tl-bl");expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(200);Ext.on("scroll",F);H.getScrollable().getElement().dom.scrollTop=50;waitsFor(function(){return F.callCount===1});runs(function(){expect(I.callCount).toBe(2);expect(E.getEl().getTop()).toBe(150);H.getScrollable().getElement().dom.scrollTop=100});waitsFor(function(){return F.callCount===2});runs(function(){expect(I.callCount).toBe(3);expect(E.getEl().getTop()).toBe(100)})});it("should unbind the scroll listener on destroy",function(){E.alignTo(H.getEl().down(".align"),"tl-bl");E.destroy();expect(Ext.GlobalEvents.hasListeners.scroll).toBe(G)});it("should not move the element if the alignTo element is destroyed",function(){E.alignTo(H.getEl().down(".align"),"tl-bl");expect(E.getEl().getTop()).toBe(200);H.getEl().down(".align").destroy();Ext.on("scroll",F);runs(function(){H.getScrollable().getElement().dom.scrollTop=100});waitsFor(function(){return F.callCount===1});runs(function(){expect(E.getEl().getTop()).toBe(200)})})});describe("aligning to component",function(){beforeEach(function(){D(true)});it("should keep the floater aligned on scroll",function(){var I=spyOn(E,"alignTo").andCallThrough();E.alignTo(H.down("#align"),"tl-bl");expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(200);Ext.on("scroll",F);H.getScrollable().getElement().dom.scrollTop=50;waitsFor(function(){return F.callCount===1});runs(function(){expect(I.callCount).toBe(2);expect(E.getEl().getTop()).toBe(150);H.getScrollable().getElement().dom.scrollTop=100});waitsFor(function(){return F.callCount===2});runs(function(){expect(I.callCount).toBe(3);expect(E.getEl().getTop()).toBe(100)})});it("should unbind the scroll listener on destroy",function(){E.alignTo(H.down("#align"),"tl-bl");E.destroy();expect(Ext.GlobalEvents.hasListeners.scroll).toBe(G)});it("should not move the element if the alignTo element is destroyed",function(){E.alignTo(H.down("#align"),"tl-bl");expect(E.getEl().getTop()).toBe(200);H.down("#align").destroy();Ext.on("scroll",F);runs(function(){H.getScrollable().getElement().dom.scrollTop=100});waitsFor(function(){return F.callCount===1});runs(function(){expect(E.getEl().getTop()).toBe(200)})})})});describe("scroll alignment when rendered into the scrolling element",function(){var F,H,E,G;function D(I){F=jasmine.createSpy();G=Ext.GlobalEvents.hasListeners.scroll;H={renderTo:Ext.getBody(),width:400,height:400,scrollable:true};if(I){H.items=[{xtype:"component",autoEl:{html:"A",style:"float:left;width:100px;height:500px"}},{xtype:"component",id:"align",autoEl:{html:"B",style:"float:left;width:100px;height:200px"}}]}else{H.autoEl={children:[{html:"A",style:{"float":"left",width:"100px",height:"500px"}},{html:"B",cls:"align",style:{"float":"left",width:"100px",height:"200px"}}]}}H=new (I?Ext.Container:Ext.Component)(H);E=new Ext.Component({autoShow:true,floating:true,shadow:false,width:50,height:50,style:"border: 1px solid black",renderTo:H.getContentTarget()})}afterEach(function(){Ext.un("scroll",F);G=H=E=F=Ext.destroy(E,H)});describe("aligning to Element",function(){beforeEach(function(){D(false)});it("should keep the floater aligned on scroll",function(){var I=spyOn(E,"alignTo").andCallThrough();E.alignTo(H.getEl().down(".align"),"tl-bl");expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(200);Ext.on("scroll",F);H.getScrollable().getElement().dom.scrollTop=50;waits(100);runs(function(){expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(150);H.getScrollable().getElement().dom.scrollTop=100});waits(100);runs(function(){expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(100)})})});describe("aligning to Component",function(){beforeEach(function(){D(true)});it("should keep the floater aligned on scroll",function(){var I=spyOn(E,"alignTo").andCallThrough();E.alignTo(H.down("#align"),"tl-bl");expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(200);Ext.on("scroll",F);H.getScrollable().getElement().dom.scrollTop=50;waits(100);runs(function(){expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(150);H.getScrollable().getElement().dom.scrollTop=100});waits(100);runs(function(){expect(I.callCount).toBe(1);expect(E.getEl().getTop()).toBe(100)})})})})})