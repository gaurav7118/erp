describe("Ext.fx.Anim",function(){var B,C,A;beforeEach(function(){C=Ext.getBody().createChild({cls:"fxtarget"});spyOn(Ext.fx.Anim.prototype,"end").andCallThrough()});afterEach(function(){C.destroy()});describe("instantiation",function(){beforeEach(function(){spyOn(Ext.fx.Manager,"addAnim");B=new Ext.fx.Anim({target:C})});it("should mix in Ext.util.Observable",function(){expect(B.mixins.observable).toEqual(Ext.util.Observable.prototype)});it("should have a default duration configuration option equal to 250",function(){expect(B.duration).toEqual(250)});it("should have a default delay configuration option equal to 0",function(){expect(B.delay).toEqual(0)});it("should have a default easing configuration option equal to ease",function(){expect(B.easing).toEqual("ease")});it("should have a default reverse configuration option equal to false",function(){expect(B.reverse).toBe(false)});it("should have a default running configuration option equal to false",function(){expect(B.running).toBe(false)});it("should have a default paused configuration option equal to false",function(){expect(B.paused).toBe(false)});it("should have a default iterations configuration option equal to 1",function(){expect(B.iterations).toEqual(1)});it("should have a default currentIteration configuration option equal to 0",function(){expect(B.currentIteration).toEqual(0)});it("should have a default startTime configuration option equal to 0",function(){expect(B.startTime).toEqual(0)})});describe("events",function(){beforeEach(function(){spyOn(Ext.fx.Anim.prototype,"fireEvent").andCallThrough();B=new Ext.fx.Anim({target:C,duration:1,from:{opacity:0},to:{opacity:1}})});it("should fire beforeanimate and afteranimate",function(){waitsFor(function(){return Ext.fx.Anim.prototype.end.calls.length===1},"event firing was never completed");runs(function(){expect(Ext.fx.Anim.prototype.fireEvent).toHaveBeenCalledWith("beforeanimate",B);expect(Ext.fx.Anim.prototype.fireEvent.calls[1].args[0]).toEqual("afteranimate");expect(Ext.fx.Anim.prototype.fireEvent.calls[1].args[1]).toEqual(B)})})});describe("opacity",function(){beforeEach(function(){B=new Ext.fx.Anim({target:C,duration:1,from:{opacity:0},to:{opacity:1}});waitsFor(function(){return Ext.fx.Anim.prototype.end.calls.length===1},"event firing was never completed")});it("should change opacity",function(){if(Ext.isIE||Ext.isOpera){expect(C.dom.style.filter).toEqual("")}else{expect(C.dom.style.opacity).toEqual("1")}})});describe("color",function(){describe("hexadecimal colors",function(){beforeEach(function(){B=new Ext.fx.Anim({target:C,duration:1,from:{color:"#000000"},to:{color:"#f1c101"}});waitsFor(function(){return Ext.fx.Anim.prototype.end.calls.length===1},"event firing was never completed")});it("should change color",function(){var D=C.dom.style.color.replace(/ /g,"");if(D.charAt(0)==="#"){expect(D).toEqual("#f1c101")}else{expect(D).toEqual("rgb(241,193,1)")}})});xdescribe("shorthand hexadecimal colors",function(){beforeEach(function(){B=new Ext.fx.Anim({target:C,duration:1,from:{color:"#000000"},to:{color:"#fc0"}})});it("should change color",function(){waitsFor(function(){var D=C.dom.style;return D.color==="rgb(255, 204, 0)"},"color wasn't changed")})})})})