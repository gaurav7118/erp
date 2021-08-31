describe("Ext.Template",function(){describe("instantiation",function(){var B;it("it should extend Ext.Base",function(){B=new Ext.Template("");expect(B.superclass).toEqual(Ext.Base.prototype)});describe("configuration options",function(){it("should disableFormats by default",function(){B=new Ext.Template("");expect(B.disableFormats).toBe(false)})});it("should alias apply with applyTemplate",function(){B=new Ext.Template("");spyOn(B,"apply");B.applyTemplate();expect(B.apply).toHaveBeenCalled()});it("should be able to compile immediately",function(){spyOn(Ext.Template.prototype,"compile").andCallThrough();B=new Ext.Template("Hello {foo}",{compiled:true});var C=B.apply({foo:42});expect(C).toBe("Hello 42");expect(Ext.Template.prototype.compile).toHaveBeenCalled()});describe("constructor arguments",function(){describe("objects",function(){it("should apply all object passed after first arguments to configuration",function(){var E={a:1},D={a:2},C={a:3};spyOn(Ext,"apply");B=new Ext.Template("",E,D,C);expect(Ext.apply.calls[1].args).toEqual([B,E]);expect(Ext.apply.calls[3].args).toEqual([B,D]);expect(Ext.apply.calls[5].args).toEqual([B,C])})});describe("strings",function(){it("should concat all strings passed as arguments",function(){var E="a",D="b",C="c";B=new Ext.Template(E,D,C);expect(B.html).toEqual(E+D+C)})});describe("array",function(){it("should concat all array strings",function(){var C=new Ext.Template(["foo","bar","baz"]);expect(C.html).toBe("foobarbaz")});it("should apply an objects after the first argument to the template",function(){var E={a:function(){}},D={b:function(){}};var C=new Ext.Template(["foo","bar",E,D]);expect(C.html).toBe("foobar");expect(C.a).toBe(E.a);expect(C.b).toBe(D.b)})})})});describe("methods",function(){var C,I,G,B,F,D,E,H;beforeEach(function(){F=Ext.fly(document.body).createChild({cls:"foo",children:[{cls:"bar"}]});D=F.first();G=new Ext.Template('<div class="template">Hello {0}.</div>');C=["world"];B=new Ext.Template(['<div name="{id}">','<span class="{cls}">{name} {value:ellipsis(10)}</span>',"</div>"]);I={id:"myid",cls:"myclass",name:"foo",value:"bar"};spyOn(Ext,"getDom").andCallThrough()});afterEach(function(){F.destroy();D.destroy()});describe("append",function(){describe("with a simple template",function(){beforeEach(function(){E=G.append(F,["world"],true)});afterEach(function(){E.destroy()});it("should append the new node to the end of the specified element",function(){expect(E).toEqual(F.last())});it("should apply the supplied value to the template",function(){expect(E.dom).hasHTML("Hello world.")});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(F)})});describe("with a complex template",function(){beforeEach(function(){H=B.append(F,I,true)});afterEach(function(){H.destroy()});it("should append the new node to the end of the specified element",function(){expect(H).toEqual(F.last())});it("should apply the supplied value to the template",function(){expect(H.dom).hasHTML('<span class="myclass">foo bar</span>')});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(F)})})});describe("apply",function(){describe("with a simple template",function(){it("should apply the supplied value and return an HTML fragments",function(){expect(G.apply(C)).toEqual('<div class="template">Hello world.</div>')})});describe("with a complex template",function(){it("should apply the supplied value and return an HTML fragments",function(){expect(B.apply(I)).toEqual('<div name="myid"><span class="myclass">foo bar</span></div>')})})});describe("insertAfter",function(){describe("with a simple template",function(){beforeEach(function(){E=G.insertAfter(D,["world"],true)});afterEach(function(){E.destroy()});it("should insert the new node after the specified element",function(){expect(E).toEqual(D.next())});it("should apply the supplied value to the template",function(){expect(E.dom).hasHTML("Hello world.")});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(D)})});describe("with a complex template",function(){beforeEach(function(){H=B.insertAfter(D,I,true)});afterEach(function(){H.destroy()});it("should insert the new node after the specified element",function(){expect(H).toEqual(D.next())});it("should apply the supplied value to the template",function(){expect(H.dom).hasHTML('<span class="myclass">foo bar</span>')});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(D)})})});describe("insertBefore",function(){describe("with a simple template",function(){beforeEach(function(){E=G.insertBefore(D,["world"],true)});afterEach(function(){E.destroy()});it("should insert the new node before the specified element",function(){expect(E).toEqual(D.prev())});it("should apply the supplied value to the template",function(){expect(E.dom).hasHTML("Hello world.")});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(D)})});describe("with a complex template",function(){beforeEach(function(){H=B.insertBefore(D,I,true)});afterEach(function(){H.destroy()});it("should insert the new node before the specified element",function(){expect(H).toEqual(D.prev())});it("should apply the supplied value to the template",function(){expect(H.dom).hasHTML('<span class="myclass">foo bar</span>')});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(D)})})});describe("insertFirst",function(){describe("with a simple template",function(){beforeEach(function(){E=G.insertFirst(F,["world"],true)});afterEach(function(){E.destroy()});it("should insert the new node as first child of the specified element",function(){expect(E).toEqual(F.first())});it("should apply the supplied value to the template",function(){expect(E.dom).hasHTML("Hello world.")});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(F)})});describe("with a complex template",function(){beforeEach(function(){H=B.insertFirst(F,I,true)});afterEach(function(){H.destroy()});it("should insert the new node as first child of the specified element",function(){expect(H).toEqual(F.first())});it("should apply the supplied value to the template",function(){expect(H.dom).hasHTML('<span class="myclass">foo bar</span>')});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(F)})})});describe("overwrite",function(){describe("with a simple template",function(){beforeEach(function(){E=G.overwrite(F,["world"],true)});afterEach(function(){E.destroy()});it("should overrride the content of the specified element",function(){expect(E).toEqual(F.first());expect(E).toEqual(F.last())});it("should apply the supplied value to the template",function(){expect(E.dom).hasHTML("Hello world.")});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(F)})});describe("with a complex template",function(){beforeEach(function(){H=B.overwrite(F,I,true)});afterEach(function(){H.destroy()});it("should overrride the content of the specified element",function(){expect(H).toEqual(F.first());expect(H).toEqual(F.last())});it("should apply the supplied value to the template",function(){expect(H.dom).hasHTML('<span class="myclass">foo bar</span>')});it("should call Ext.getDom",function(){expect(Ext.getDom).toHaveBeenCalledWith(F)})})});describe("overwrite a table element",function(){var J;beforeEach(function(){J=Ext.fly(document.body).createChild({tag:"table"})});afterEach(function(){J.destroy()});it("should insert table structure into a table",function(){new Ext.Template("<tr><td>text</td></tr>").overwrite(J);var K=J.dom.innerHTML;if(Ext.isSafari4){expect(K).toEqual("<tr><td>text</td></tr>")}else{expect(K.toLowerCase().replace(/\s/g,"")).toEqual("<tbody><tr><td>text</td></tr></tbody>")}})});describe("set",function(){var J='<div class="template">Good bye {0}.</div>';it("should set the HTML used as the template",function(){G.set(J);expect(G.apply(["world"])).toEqual('<div class="template">Good bye world.</div>')});it("should be able to compile the template",function(){G.set(J,true);var K=G.apply([42]);expect(K).toBe('<div class="template">Good bye 42.</div>');expect(typeof G.fn==="function").toBe(true)})});describe("compile",function(){it("should call compiled function",function(){B.compile();spyOn(B,"fn").andCallThrough();B.apply(I);expect(B.fn).toHaveBeenCalledWith(I)});it("should return the same value as if it wasn't compiled with a complex template",function(){var K,J;K=B.apply(I);B.compile();J=B.apply(I);expect(J).toEqual(K)});it("should return the same value as if it wasn't compiled with a simple template",function(){var K,J;K=G.apply(C);G.compile();J=G.apply(C);expect(J).toEqual(K)});it("should return the template itself",function(){expect(G.compile()).toEqual(G)})})});describe("formats",function(){var B,D,C,E;beforeEach(function(){E={a:"123",b:"456789"};D=spyOn(Ext.util.Format,"ellipsis");C=spyOn(Ext.util.Format,"htmlEncode")});describe("enabled",function(){beforeEach(function(){B=new Ext.Template("{a:ellipsis(2)}","{b:htmlEncode}")});it("should call Ext.util.Format.ellipsis with a non compiled template",function(){B.apply(E);expect(D).toHaveBeenCalledWith(E.a,2);expect(C).toHaveBeenCalledWith(E.b)});it("should call Ext.util.Format.ellipsis with compiled template",function(){B.compile();B.apply(E);expect(D).toHaveBeenCalledWith(E.a,2);expect(C).toHaveBeenCalledWith(E.b)})});describe("disabled",function(){beforeEach(function(){B=new Ext.Template("{a:ellipsis(2)}",{disableFormats:true})});it("should not call Ext.util.Format.ellipsis with a non compiled template",function(){B.apply(E);expect(D).not.toHaveBeenCalled()});it("should not call Ext.util.Format.ellipsis with compiled template",function(){B.compile();B.apply(E);expect(D).not.toHaveBeenCalled()})})});describe("members functions",function(){var B,C,D;beforeEach(function(){C=jasmine.createSpy().andCallFake(function(F,G){return F+G});var E={referenceHolder:true,controller:"foo",fmt:function(){},promote:function(){},items:[{items:[{xtype:"button",reference:"btn",listeners:{click:"promote"},bind:{text:"Promote {user.name:this.fmt}"}}]}]};B=new Ext.Template("{a:this.increment(7)}",{increment:C});D={a:1}});it("should call members functions with a non compiled template",function(){B.apply(D);expect(C).toHaveBeenCalledWith(1,7)});it("should call members functions with a compiled template",function(){B.compile();B.apply(D);expect(C).toHaveBeenCalledWith(1,7)});it("should add member function in initialConfig",function(){expect(B.initialConfig).toEqual({increment:C})})});describe("Ext.Template.from",function(){var C,B;beforeEach(function(){C=Ext.fly(document.body).createChild({tag:"div",html:"FOO {0}."});B=Ext.fly(document.body).createChild({tag:"input"});B.dom.value="BAR {0}."});afterEach(function(){C.remove();B.remove()});it("should create a template with dom element innerHTML",function(){var D=Ext.Template.from(C);expect(D.apply(["BAR"])).toEqual("FOO BAR.")});it("should create a template with dom element value",function(){var D=Ext.Template.from(B);expect(D.apply(["FOO"])).toEqual("BAR FOO.")})});function A(B){describe("Using numeric tokens and a values array",function(){it("should use Ext.util.Format formatting functions by default",function(){expect(new Ext.Template('Value: {0:number("0.00")}',{compiled:B}).apply([3.257])).toBe("Value: 3.26")});it('should use member formatting functions when prepended with "this."',function(){var C=["Warning: {0:this.bold}",{bold:function(D){return"<b>"+D+"</b>"},compiled:B}];expect(new Ext.Template(C).apply(["Warning message"])).toBe("Warning: <b>Warning message</b>")});it('should not see "{margin:0} as a token',function(){expect(new Ext.Template("p{margin:0}body{direction:{0}}",{compiled:B}).apply(["rtl"])).toBe("p{margin:0}body{direction:rtl}")});it('should not see "{1:someText} as a token',function(){expect(new Ext.Template("{0}{1:sometext}{1}",{compiled:B}).apply(["foo","bar"])).toBe("foo{1:sometext}bar")})});describe("Using alphanumeric tokens and a values object",function(){it("should use Ext.util.Format formatting functions by default",function(){expect(new Ext.Template('Value: {prop0:number("0.00")}',{compiled:B}).apply({prop0:3.257})).toBe("Value: 3.26")});it('should use member formatting functions when prepended with "this."',function(){var C=["Warning: {prop0:this.bold}",{bold:function(D){return"<b>"+D+"</b>"},compiled:B}];expect(new Ext.Template(C).apply({prop0:"Warning message"})).toBe("Warning: <b>Warning message</b>")});it('should not see "{margin:0} as a token',function(){expect(new Ext.Template("p{margin:0}body{direction:{prop0}}",{compiled:B}).apply({prop0:"rtl"})).toBe("p{margin:0}body{direction:rtl}")});it('should not see "{1:someText} as a token',function(){expect(new Ext.Template("{prop0}{1:sometext}{prop1}",{compiled:B}).apply({prop0:"foo",prop1:"bar"})).toBe("foo{1:sometext}bar")})})}describe("Non-compiled",function(){A(false)});describe("Compiled",function(){A(true)})})