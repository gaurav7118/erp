describe("Ext.ComponentLoader",function(){var A,D,B,C,E,H,F,I,G;beforeEach(function(){MockAjaxManager.addMethods();F=function(J){J=J||{};G=new Ext.Component(J)};H=function(J){J=J||{};G=new Ext.container.Container(J)};E=function(J){J=J||{};Ext.applyIf(J,{url:"url",target:G});I=new Ext.ComponentLoader(J)};C=function(K,J){Ext.Ajax.mockComplete({status:J||200,responseText:K||"response"})};D=function(K,J){I.load(J);C(K)};B=function(K,J){I.load(J);C(K,500)};A=function(){return Ext.Ajax.mockGetRequestXHR().options}});afterEach(function(){MockAjaxManager.removeMethods();if(G){G.destroy()}if(I){I.destroy()}A=B=D=C=E=H=F=I=G=null});describe("defaults",function(){beforeEach(function(){I=new Ext.ComponentLoader()});it("should default removeAll to false",function(){expect(I.removeAll).toBeFalsy()});it("should default the renderer to html",function(){expect(I.renderer).toEqual("html")})});describe("loadOnRender",function(){describe("when not rendered",function(){it("should load when the component renders",function(){F();E({target:G,loadOnRender:true});expect(Ext.Ajax.mockGetAllRequests()).toEqual([]);G.render(Ext.getBody());C("New Content");expect(G.getEl().dom).hasHTML("New Content")});it("should pass any options in loadOnRender",function(){F();E({target:G,loadOnRender:{url:"bar"}});G.render(Ext.getBody());expect(A().url).toBe("bar")})});describe("already rendered",function(){it("should load immediately",function(){F({renderTo:Ext.getBody()});E({target:G,loadOnRender:true});C("New Content");expect(G.getEl().dom).hasHTML("New Content")});it("should pass any options in loadOnRender",function(){F({renderTo:Ext.getBody()});E({target:G,loadOnRender:{url:"bar"}});expect(A().url).toBe("bar")})})});describe("masking",function(){beforeEach(function(){F({renderTo:document.body})});afterEach(function(){if(Ext.WindowManager.mask){Ext.WindowManager.mask.remove();Ext.WindowManager.mask=null}});it("should not mask by default",function(){E();I.load();expect(G.loadMask).toBeFalsy()});it("should unmask after the request completes",function(){E({loadMask:true});I.load();expect(G.loadMask!=null).toBe(true);C();expect(G.loadMask.isVisible()).toBeFalsy()});it("should accept a masking config",function(){E({loadMask:{msg:"Waiting"}});I.load();expect(G.loadMask.msg).toEqual("Waiting");C()});it("should use the masking load option",function(){E();I.load({loadMask:true});expect(G.loadMask!=null).toBe(true);C()});it("should give precedence to the load option",function(){E({loadMask:{msg:"Waiting"}});I.load({loadMask:{msg:"Other"}});expect(G.loadMask.msg).toEqual("Other");C()})});describe("target",function(){var J;beforeEach(function(){J=Ext.ComponentLoader});afterEach(function(){J=null});it("should take the target from the config object",function(){F();E();expect(I.getTarget()).toEqual(G)});it("should take a string config",function(){F({id:"id"});I=new J({target:"id"});expect(I.getTarget()).toEqual(G)});it("should assign the target",function(){F();I=new J();I.setTarget(G);expect(I.getTarget()).toEqual(G)});it("should assign a new target",function(){var K=new Ext.Component();F();E();I.setTarget(K);expect(I.getTarget()).toEqual(K);K.destroy()});it("should assign a new target via id",function(){F({id:"id"});I=new J();I.setTarget("id");expect(I.getTarget()).toEqual(G)})});describe("renderers",function(){describe("html",function(){it("should use html as the default renderer",function(){F({renderTo:document.body});E();D("New content");expect(G.getEl().dom).hasHTML("New content")});it("should use html if it's specified",function(){F({renderTo:document.body});E({renderer:"html"});D("New content");expect(G.getEl().dom).hasHTML("New content")})});describe("data",function(){it("should work with array data - data renderer",function(){F({renderTo:document.body,tpl:'<tpl for=".">{name}</tpl>'});E({renderer:"data"});D('[{"name": "foo"}, {"name": "bar"}, {"name": "baz"}]');expect(G.getEl().dom).hasHTML("foobarbaz")});it("should work with an object",function(){F({renderTo:document.body,tpl:"{name} - {age}"});E({renderer:"data"});D('{"name": "foo", "age": 21}');expect(G.getEl().dom).hasHTML("foo - 21")});it("should fail if the data could not be decoded",function(){var L={fn:function(M,N){J=N}},J;spyOn(L,"fn").andCallThrough();F({renderTo:document.body,tpl:"{name}"});E({renderer:"data",callback:L.fn});var K=Ext.global;Ext.global={};D("not data");Ext.global=K;expect(J).toBeFalsy();expect(G.getEl().dom).hasHTML("")})});describe("component",function(){beforeEach(function(){H({renderTo:document.body});E({renderer:"component"})});it("should exception if using a non-container",function(){G.destroy();F({renderTo:document.body});I.setTarget(G);I.load();expect(function(){C('{"html": "foo"}')}).toRaiseExtError("Components can only be loaded into a container")});it("should add a single item",function(){I.load();C('{"xtype": "component", "html": "new item"}');expect(G.items.first().getEl().dom).hasHTML("new item")});it("should add multiple items",function(){I.load();C('[{"xtype": "component", "html": "new item1"}, {"xtype": "component", "html": "new item2"}]');expect(G.items.first().getEl().dom).hasHTML("new item1");expect(G.items.last().getEl().dom).hasHTML("new item2")});it("should respect the removeAll option",function(){I.removeAll=true;I.load();G.add({xtype:"component"});C('[{"xtype": "component", "html": "new item1"}, {"xtype": "component", "html": "new item2"}]');expect(G.items.getCount()).toEqual(2)});it("should give precedence to removeAll in the config options",function(){I.load({removeAll:true});G.add({xtype:"component"});C('[{"xtype": "component", "html": "new item1"}, {"xtype": "component", "html": "new item2"}]');expect(G.items.getCount()).toEqual(2)});it("should fail if items could not be decoded",function(){var L={fn:function(M,N){J=N}},J;spyOn(L,"fn").andCallThrough();I.callback=L.fn;var K=Ext.global;Ext.global={};D("not items");Ext.global=K;expect(J).toBeFalsy();expect(G.items.getCount()).toEqual(0)})});describe("panel",function(){beforeEach(function(){G=new Ext.panel.Panel({title:"Panel",height:400,width:600,renderTo:document.body});E({renderer:"html"})});it("should use the component as the scope for inline scripts",function(){var J={};I.load({scripts:true,success:function(){this.foo="bar"},scope:J});C('<script>this.setTitle("New title");<\/script>New content');waitsFor(function(){return G.getTitle()==="New title"},"the inline script to be executed");runs(function(){expect(G.body.dom.textContent||G.body.dom.innerText).toBe("New content");expect(J.foo).toBe("bar")})});it("should use the rendererScope as the scope for inline scripts",function(){var J={};I.load({scripts:true,rendererScope:J});C('<script>this.foo = "bar";<\/script>New content');waitsFor(function(){return J.foo==="bar"},"callback to be executed with the correct scope");runs(function(){expect(G.body.dom.textContent||G.body.dom.innerText).toBe("New content")})})});describe("custom renderer",function(){it("should use a custom renderer if one is specified",function(){var J={fn:function(K,L,M){K.getTarget().update("This is the "+L.responseText)}};spyOn(J,"fn").andCallThrough();F({renderTo:document.body});E({renderer:J.fn});D("response");expect(J.fn).toHaveBeenCalled();expect(G.getEl().dom).hasHTML("This is the response")});it("should fail if the renderer returns false",function(){var J;F({renderTo:document.body});E({renderer:function(){return false},callback:function(K,L){J=L}});D();expect(J).toBeFalsy();expect(G.getEl().dom).hasHTML("")})})})})