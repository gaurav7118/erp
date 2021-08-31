xdescribe("Ext.Element.anim",function(){var B,A=Ext.isSafari4?xit:it;beforeEach(function(){B=Ext.getBody().createChild({id:"testElement"})});afterEach(function(){B.destroy()});describe("callbacks",function(){var F,D,C,E;beforeEach(function(){D=false;C={};F=jasmine.createSpy("callback").andCallFake(function(){D=true;E=this})});afterEach(function(){E=undefined});describe("slideIn()",function(){beforeEach(function(){B.slideIn("t",{duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("slideOut()",function(){beforeEach(function(){B.slideOut("t",{duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("puff()",function(){beforeEach(function(){B.slideIn("t",{duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("switchOff()",function(){beforeEach(function(){B.switchOff({duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("frame()",function(){beforeEach(function(){B.frame("#ff0000",1,{duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("ghost()",function(){beforeEach(function(){B.ghost("b",{duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("highlight()",function(){beforeEach(function(){B.highlight("#0000ff",{duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("fadeIn()",function(){beforeEach(function(){B.fadeIn({duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("fadeOut()",function(){beforeEach(function(){B.fadeOut({duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("scale()",function(){beforeEach(function(){B.scale(100,100,{duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})});describe("shift()",function(){beforeEach(function(){B.shift({x:200,y:200,duration:10,callback:F,scope:C})});A("should run callback",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(D).toBeTruthy()})});A("should run callback in correct scope",function(){waitsFor(function(){return D},1000,"Callback to fire");runs(function(){expect(E).toBe(C)})})})})})