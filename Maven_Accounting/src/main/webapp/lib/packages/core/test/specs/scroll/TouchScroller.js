(Ext.isIE9m?xdescribe:describe)("Ext.scroll.TouchScroller",function(){var C,B,D;function A(E){B=new Ext.scroll.TouchScroller(Ext.apply({element:C,autoRefresh:false},E))}beforeEach(function(){C=Ext.getBody().createChild({style:"height:100px;width:100px;"})});afterEach(function(){if(B){B.destroy()}if(C){C.destroy()}});describe("innerElement",function(){it("should automatically wrap the content in a scroller element",function(){C.appendChild({id:"foo"},true);C.appendChild({id:"bar"},true);A();D=B.getInnerElement();expect(C.dom.childNodes.length).toBe(1);expect(C.first()).toBe(D);expect(Ext.fly("foo").parent()).toBe(D);expect(Ext.fly("bar").parent()).toBe(D)});it("should wrap the content in a scroller element when the first child is a text node",function(){C.setHtml("foo");A();D=B.getInnerElement();expect(D.dom.innerHTML).toBe("foo")});it("should use the first child of the element as the innerElement if it has the scrollerCls",function(){D=C.appendChild({cls:"x-scroll-scroller"});A();expect(B.getInnerElement()).toBe(D);D.destroy()});describe("configuring",function(){afterEach(function(){var E=B.getInnerElement();if(E){B.getInnerElement().destroy()}});it("should accept an HTMLElement",function(){D=document.createElement("div");C.dom.appendChild(D);A({innerElement:D});expect(B.getInnerElement().isElement).toBe(true);expect(B.getInnerElement().dom).toBe(D)});it("should accept an Element ID",function(){D=document.createElement("div");D.id="theScrollerEl";C.dom.appendChild(D);A({innerElement:"theScrollerEl"});expect(B.getInnerElement().isElement).toBe(true);expect(B.getInnerElement().dom).toBe(D)});it("should accept an Ext.dom.Element",function(){D=C.createChild();A({innerElement:D});expect(B.getInnerElement()).toBe(D)});it("should throw an error if element with given id not found",function(){expect(function(){A({innerElement:"foobarelement"})}).toThrow("Cannot create Ext.scroll.TouchScroller instance with null innerElement")})})});describe("css classes",function(){it("should add the 'x-scroll-container' class to the element",function(){A();expect(C).toHaveCls("x-scroll-container")});it("should add the 'x-scroll-scroller' class to a generated innerElement",function(){A();expect(B.getInnerElement()).toHaveCls("x-scroll-scroller")});it("should add the 'x-scroll-scroller' class to a configured innerElement",function(){D=C.createChild();A({innerElement:D});expect(B.getInnerElement()).toHaveCls("x-scroll-scroller");D.destroy()})});describe("x",function(){function F(G){C.appendChild({style:"height:100px;width:200px;"},true);A(G)}function E(G){C.appendChild({style:"height:100px;width:100px;"},true);A(G)}it("should enable the x axis by default if content overflows horizontally",function(){F();expect(B.isAxisEnabled("x")).toBe(true)});it("should not enable the x axis by default if content does not overflow horizontally",function(){E();expect(B.isAxisEnabled("x")).toBe(false)});it("should enable the x axis when x is true if content overflows horizontally",function(){F({x:true});expect(B.isAxisEnabled("x")).toBe(true)});it("should not enable the x axis when x is true if content does not overflow horizontally",function(){E({x:true});expect(B.isAxisEnabled("x")).toBe(false)});it("should enable the x axis when x is 'auto' if content overflows horizontally",function(){F({x:"auto"});expect(B.isAxisEnabled("x")).toBe(true)});it("should not enable the x axis when x is 'auto' if content does not overflow horizontally",function(){E({x:"auto"});expect(B.isAxisEnabled("x")).toBe(false)});it("should enable the x axis when x is 'scroll' if content overflows horizontally",function(){F({x:"scroll"});expect(B.isAxisEnabled("x")).toBe(true)});it("should enable the x axis when x is 'scroll' if content does not overflow horizontally",function(){E({x:"scroll"});expect(B.isAxisEnabled("x")).toBe(true)});it("should not enable the x axis when x is false if content overflows horizontally",function(){F({x:false});expect(B.isAxisEnabled("x")).toBe(false)});it("should not enable the x axis when x is false if content does not overflow horizontally",function(){E({x:false});expect(B.isAxisEnabled("x")).toBe(false)});it("should disable the x axis when moving from true to false",function(){F({x:true});B.setX(false);expect(B.isAxisEnabled("x")).toBe(false)});it("should enable the x axis when moving from false to true",function(){F({x:false});B.setX(true);expect(B.isAxisEnabled("x")).toBe(true)})});describe("y",function(){function F(G){C.appendChild({style:"height:200px;width:100px;"},true);A(G)}function E(G){C.appendChild({style:"height:100px;width:100px;"},true);A(G)}it("should enable the y axis by default if content overflows vertically",function(){F();expect(B.isAxisEnabled("y")).toBe(true)});it("should not enable the y axis by default if content does not overflow vertically",function(){E();expect(B.isAxisEnabled("y")).toBe(false)});it("should enable the y axis when y is true if content overflows vertically",function(){F({y:true});expect(B.isAxisEnabled("y")).toBe(true)});it("should not enable the y axis when y is true if content does not overflow vertically",function(){E({y:true});expect(B.isAxisEnabled("y")).toBe(false)});it("should enable the y axis when y is 'auto' if content overflows vertically",function(){F({y:"auto"});expect(B.isAxisEnabled("y")).toBe(true)});it("should not enable the y axis when y is 'auto' if content does not overflow vertically",function(){E({y:"auto"});expect(B.isAxisEnabled("y")).toBe(false)});it("should enable the y axis when y is 'scroll' if content overflows vertically",function(){F({y:"scroll"});expect(B.isAxisEnabled("y")).toBe(true)});it("should enable the y axis when y is 'scroll' if content does not overflow vertically",function(){E({y:"scroll"});expect(B.isAxisEnabled("y")).toBe(true)});it("should not enable the y axis when y is false if content overflows vertically",function(){F({y:false});expect(B.isAxisEnabled("y")).toBe(false)});it("should not enable the y axis when y is false if content does not overflow vertically",function(){E({y:false});expect(B.isAxisEnabled("y")).toBe(false)});it("should disable the y axis when moving from true to false",function(){F({y:true});B.setY(false);expect(B.isAxisEnabled("y")).toBe(false)});it("should enable the y axis when moving from false to true",function(){F({y:false});B.setY(true);expect(B.isAxisEnabled("y")).toBe(true)})});describe("direction",function(){it("should set x:true and y:true when direction is 'auto'",function(){A({direction:"auto"});expect(B.getX()).toBe(true);expect(B.getY()).toBe(true)});it("should set x:'scroll' and y:'scroll' direction is 'both'",function(){A({direction:"both"});expect(B.getX()).toBe("scroll");expect(B.getY()).toBe("scroll")});it("should set y:true when direction is 'vertical'",function(){A({direction:"vertical"});expect(B.getX()).toBe(false);expect(B.getY()).toBe(true)});it("should set x:true on the element when direction is 'horizontal'",function(){A({direction:"horizontal"});expect(B.getX()).toBe(true);expect(B.getY()).toBe(false)})});describe("getSize",function(){beforeEach(function(){C.appendChild({style:"height:200px;width:300px;"},true)});it("should return the content size with x:auto and y:auto",function(){A();expect(B.getSize()).toEqual({x:300,y:200})});it("should return the content size with x:scroll and y:scroll",function(){A({x:"scroll",y:"scroll"});expect(B.getSize()).toEqual({x:300,y:200})});it("should return the content size with x:false and y:false",function(){A({x:false,y:false});expect(B.getSize()).toEqual({x:300,y:200})});it("should return the content size with x:false and y:auto",function(){A({x:false,y:true});expect(B.getSize()).toEqual({x:300,y:200})});it("should return the content size with x:auto and y:false",function(){A({x:true,y:false});expect(B.getSize()).toEqual({x:300,y:200})});it("should return the content size with x:false and y:scroll",function(){A({x:false,y:"scroll"});expect(B.getSize()).toEqual({x:300,y:200})});it("should return the content size with x:scroll and y:false",function(){A({x:"scroll",y:false});expect(B.getSize()).toEqual({x:300,y:200})});it("should allow absolutely positioned elements to contribute to the size",function(){C.appendChild({style:"position:absolute;height:50px;width:50px;left:400px;top:0px;"});C.appendChild({style:"position:absolute;height:50px;width:50px;top:500px;left:0px;"});A();expect(B.getSize()).toEqual({x:450,y:550})})});describe("setSize",function(){it("should set the size",function(){A();B.setSize({x:300,y:200});expect(B.getSize()).toEqual({x:300,y:200})});it("should unset the size",function(){A();B.setSize({x:300,y:200});B.setSize(null);expect(B.getSize()).toEqual({x:100,y:100})});it("should set the size on both axes to a single number",function(){A();B.setSize(200);expect(B.getSize()).toEqual({x:200,y:200})});it("should set the x size",function(){A();B.setSize({x:200});expect(B.getSize()).toEqual({x:200,y:100})});it("should set the y size",function(){A();B.setSize({y:200});expect(B.getSize()).toEqual({x:100,y:200})})});describe("getClientSize",function(){var E=0,G=0;if(Ext.supports.touchScroll===1){E=Ext.getScrollbarSize().width;G=Ext.getScrollbarSize().height}beforeEach(function(){C.destroy();C=Ext.getBody().createChild({style:{height:"200px",width:"200px",borderColor:"red",borderStyle:"solid",borderWidth:"10px 20px",padding:"30px 40px"}})});function F(){var I=[],H;for(H=0;H<100;++H){I.push(H)}return I}it("should return the clientWidth of the element",function(){C.setHtml(F().join("<br />"));A();var H=B.getClientSize();expect(H.x).toBe(200-(20*2)-E);expect(H.y).toBe(200-(10*2))});it("should return the clientHeight of the element",function(){C.setHtml(F().join(""));A();var H=B.getClientSize();expect(H.x).toBe(200-(20*2));expect(H.y).toBe(200-(10*2)-G)});it("should read by the clientWidth and clientHeight of the element",function(){var I=F();I[0]=F().join("");C.setHtml(I.join("<br />"));A();var H=B.getClientSize();expect(H.x).toBe(200-(20*2)-E);expect(H.y).toBe(200-(10*2)-G)})});describe("scrollTo",function(){function F(G){C.appendChild({style:"height:200px;width:300px;"},true);A(G)}function E(G){C.appendChild({style:"height:100px;width:100px;"},true);A(G)}it("should scroll on the x axis",function(){F();B.scrollTo(50,0);expect(B.getPosition()).toEqual({x:50,y:0})});it("should scroll on the x axis when the x axis is disabled",function(){F({x:false});B.scrollTo(50,0);expect(B.getPosition()).toEqual({x:50,y:0})});it("should not scroll on the x axis if the content does not overflow horizontally",function(){E();B.scrollTo(50,0);expect(B.getPosition()).toEqual({x:0,y:0})});it("should constrain to the max x position",function(){F();B.scrollTo(250,0);expect(B.getPosition()).toEqual({x:200,y:0})});it("should scroll on the y axis",function(){F();B.scrollTo(0,50);expect(B.getPosition()).toEqual({x:0,y:50})});it("should scroll on the y axis when the y axis is disabled",function(){F({y:false});B.scrollTo(0,50);expect(B.getPosition()).toEqual({x:0,y:50})});it("should not scroll on the y axis if the content does not overflow vertically",function(){E();B.scrollTo(0,50);expect(B.getPosition()).toEqual({x:0,y:0})});it("should constrain to the max y position",function(){F();B.scrollTo(0,250);expect(B.getPosition()).toEqual({x:0,y:100})});it("should scroll on both axes",function(){F();B.scrollTo(50,60);expect(B.getPosition()).toEqual({x:50,y:60})});it("should constrain to max x and y",function(){F();B.scrollTo(300,300);expect(B.getPosition()).toEqual({x:200,y:100})});it("should scroll to max x using Infinity",function(){F();B.scrollTo(Infinity,0);expect(B.getPosition()).toEqual({x:200,y:0})});it("should scroll to max y using Infinity",function(){F();B.scrollTo(0,Infinity);expect(B.getPosition()).toEqual({x:0,y:100})});it("should scroll to max x and y using Infinity",function(){F();B.scrollTo(Infinity,Infinity);expect(B.getPosition()).toEqual({x:200,y:100})});it("should ignore x if null is passed",function(){F();B.scrollTo(10,10);B.scrollTo(null,20);expect(B.getPosition()).toEqual({x:10,y:20})});it("should ignore y if null is passed",function(){F();B.scrollTo(10,10);B.scrollTo(20,null);expect(B.getPosition()).toEqual({x:20,y:10})});it("should ignore x and y if both null",function(){F();B.scrollTo(10,10);B.scrollTo(null,null);expect(B.getPosition()).toEqual({x:10,y:10})});it("should scroll to negative offset from max x",function(){F();B.scrollTo(-20,0);expect(B.getPosition()).toEqual({x:180,y:0})});it("should scroll to negative offset from max y",function(){F();B.scrollTo(0,-20);expect(B.getPosition()).toEqual({x:0,y:80})});it("should scroll to negative offset from max x and y",function(){F();B.scrollTo(-20,-20);expect(B.getPosition()).toEqual({x:180,y:80})});it("should fire scrollstart and scrollend",function(){var G=0,H=0;F();B.on({scrollstart:function(){G++},scrollend:function(){H++}});B.scrollTo(20,20);waits(50);runs(function(){expect(G).toBe(1);expect(H).toBe(1)})});it("should fire scrollstart and scrollend when animated",function(){var G=0,H=0;F();B.on({scrollstart:function(){G++},scrollend:function(){H++}});B.scrollTo(20,20,true);waits(1000);runs(function(){expect(G).toBe(1);expect(H).toBe(1)})})});describe("scrollBy",function(){beforeEach(function(){C.appendChild({style:"height:200px;width:300px;"},true)});it("should set the scroll position",function(){A();B.scrollTo(20,10);expect(B.getPosition()).toEqual({x:20,y:10});B.scrollBy(-10,-5);expect(B.getPosition()).toEqual({x:10,y:5})});it("should ignore x if null is passed",function(){A();B.scrollTo(10,10);B.scrollBy(null,10);expect(B.getPosition()).toEqual({x:10,y:20})});it("should ignore y if null is passed",function(){A();B.scrollTo(10,10);B.scrollBy(10,null);expect(B.getPosition()).toEqual({x:20,y:10})});it("should ignore x and y if both null",function(){A();B.scrollTo(10,10);B.scrollBy(null,null);expect(B.getPosition()).toEqual({x:10,y:10})});it("should constrain to the max x position",function(){A();B.scrollBy(250,0);expect(B.getPosition()).toEqual({x:200,y:0})});it("should constrain to the min x position",function(){A();B.scrollBy(-10,0);expect(B.getPosition()).toEqual({x:0,y:0})});it("should constrain to the max y position",function(){A();B.scrollBy(0,250);expect(B.getPosition()).toEqual({x:0,y:100})});it("should constrain to the min y position",function(){A();B.scrollBy(0,-10);expect(B.getPosition()).toEqual({x:0,y:0})});it("should constrain to max x and y",function(){A();B.scrollBy(300,300);expect(B.getPosition()).toEqual({x:200,y:100})});it("should constrain to min x and y",function(){A();B.scrollBy(-10,-10);expect(B.getPosition()).toEqual({x:0,y:0})})});describe("getMaxPosition and getMaxUserPosition",function(){beforeEach(function(){C.appendChild({style:"height:200px;width:300px;"},true)});describe("with x:true and y:true",function(){beforeEach(function(){A()});it("should return the maxPosition",function(){expect(B.getMaxPosition()).toEqual({x:200,y:100})});it("should return the maxUserPosition",function(){expect(B.getMaxUserPosition()).toEqual({x:200,y:100})})});describe("with x:true and y:false",function(){beforeEach(function(){A({x:true,y:false})});it("should return the maxPosition",function(){expect(B.getMaxPosition()).toEqual({x:200,y:100})});it("should return the maxUserPosition",function(){expect(B.getMaxUserPosition()).toEqual({x:200,y:0})})});describe("with x:false and y:true",function(){beforeEach(function(){A({x:false,y:true})});it("should return the maxPosition",function(){expect(B.getMaxPosition()).toEqual({x:200,y:100})});it("should return the maxUserPosition",function(){expect(B.getMaxUserPosition()).toEqual({x:0,y:100})})});describe("with x:false and y:false",function(){beforeEach(function(){A({x:false,y:false})});it("should return the maxPosition",function(){expect(B.getMaxPosition()).toEqual({x:200,y:100})});it("should return the maxUserPosition",function(){expect(B.getMaxUserPosition()).toEqual({x:0,y:0})})})});describe("partnership",function(){var F,E,J,I;function H(){F=Ext.getBody().createChild({style:"height:100px;width:100px;",cn:[{style:"height:200px;width:300px;"}]});J=new Ext.scroll.TouchScroller({element:F,autoRefresh:false})}function G(){E=Ext.getBody().createChild({style:"height:100px;width:100px;",cn:[{style:"height:200px;width:300px;"}]});I=new Ext.scroll.TouchScroller({element:E,autoRefresh:false})}beforeEach(function(){C.appendChild({style:"height:200px;width:300px;"},true);A()});afterEach(function(){if(J){J.destroy();J=null}if(I){I.destroy();I=null}if(F){F.destroy();F=null}if(E){E.destroy();E=null}});describe("single partner",function(){beforeEach(function(){H()});describe("both axes enabled",function(){beforeEach(function(){B.addPartner(J)});it("should sync the partner's scroll position when the scroller is scrolled",function(){spyOn(J,"fireScrollStart").andCallThrough();spyOn(J,"fireScrollEnd").andCallThrough();B.scrollTo(10,20);expect(J.getPosition()).toEqual({x:10,y:20});expect(J.fireScrollStart.callCount).toBe(1);expect(J.fireScrollEnd.callCount).toBe(1)});it("should sync the scroller's scroll position when the partner is scrolled",function(){spyOn(B,"fireScrollStart").andCallThrough();spyOn(B,"fireScrollEnd").andCallThrough();J.scrollTo(10,20);expect(B.getPosition()).toEqual({x:10,y:20});expect(B.fireScrollStart.callCount).toBe(1);expect(B.fireScrollEnd.callCount).toBe(1)})});describe("x-axis only",function(){beforeEach(function(){B.addPartner(J,"x")});it("should sync the partner's scroll position when the scroller is scrolled",function(){B.scrollTo(10,20);expect(J.getPosition()).toEqual({x:10,y:0})});it("should sync the scroller's scroll position when the partner is scrolled",function(){J.scrollTo(10,20);expect(B.getPosition()).toEqual({x:10,y:0})})});describe("y-axis only",function(){beforeEach(function(){B.addPartner(J,"y")});it("should sync the partner's scroll position when the scroller is scrolled",function(){B.scrollTo(10,20);expect(J.getPosition()).toEqual({x:0,y:20})});it("should sync the scroller's scroll position when the partner is scrolled",function(){J.scrollTo(10,20);expect(B.getPosition()).toEqual({x:0,y:20})})});it("should remove the partner",function(){B.addPartner(J);B.removePartner(J);B.scrollTo(10,20);expect(J.getPosition()).toEqual({x:0,y:0});J.scrollTo(40,30);expect(B.getPosition()).toEqual({x:10,y:20})})});describe("multiple partners",function(){beforeEach(function(){H();G();B.addPartner(J);B.addPartner(I)});it("should sync multiple partners when the scroller is scrolled",function(){B.scrollTo(10,15);expect(J.getPosition()).toEqual({x:10,y:15});expect(I.getPosition()).toEqual({x:10,y:15})});it("should sync scroll position when a partner is scrolled",function(){J.scrollTo(50,60);expect(B.getPosition()).toEqual({x:50,y:60})});it("should remove a partner",function(){B.removePartner(J);J.scrollTo(15,20);expect(B.getPosition()).toEqual({x:0,y:0});I.scrollTo(30,45);expect(B.getPosition()).toEqual({x:30,y:45})})})});describe("refresh",function(){});describe("interaction",function(){var I=Ext.testHelper;function J(K,L){K.id=1;I.touchStart(L||C,K)}function G(K,L){K.id=1;I.touchMove(L||C,K)}function F(K,L){K.id=1;I.touchEnd(L||C,K)}function H(K,L){if(!L){L=B}var M=L.getInnerElement().getOffsetsTo(L.getElement());expect(-M[0]).toBe(K.x);expect(-M[1]).toBe(K.y)}function E(L,N){var K,M;if(N){K=Ext.getBody().createChild({style:"height:100px;width:100px;"})}else{K=C}K.appendChild({style:"height:200px;width:200px;"},true);M=new Ext.scroll.TouchScroller(Ext.apply({element:K,autoRefresh:false},L));if(!N){B=M;D=B.getInnerElement()}return M}describe("synchronizing partners",function(){var L,K;afterEach(function(){Ext.destroy(K._element,L,K)});it("should sync the partner's scroll position when the scroller is scrolled",function(){L=E();K=E(null,true);L.addPartner(K);spyOn(K,"fireScrollStart").andCallThrough();spyOn(K,"fireScrollEnd").andCallThrough();runs(function(){J({x:50,y:50},L.getElement());G({x:50,y:40},L.getElement());F({x:50,y:40},L.getElement());expect(L.isScrolling).toBe(true);expect(K.isScrolling).toBe(true)});waitsFor(function(){return !(L.isScrolling||K.isScrolling)},"both scrollers to finish scrolling");runs(function(){H({x:0,y:L.getPosition().y},K);expect(K.fireScrollStart.callCount).toBe(1);expect(K.fireScrollEnd.callCount).toBe(1)})})});describe("x:'auto' and y:'auto'",function(){it("should size to the content",function(){E();expect(B.getSize()).toEqual({x:200,y:200})});it("should allow scrolling in the vertical direction",function(){E();runs(function(){J({x:50,y:50});G({x:50,y:40})});waitsForAnimation();runs(function(){H({x:0,y:10});F({x:50,y:40})});waitsForAnimation()});it("should allow scrolling in the horizontal direction",function(){E();runs(function(){J({x:50,y:50});G({x:40,y:50})});waitsForAnimation();runs(function(){H({x:10,y:0});F({x:40,y:50})});waitsForAnimation()});it("should allow scrolling in both directions simultaneously",function(){E();runs(function(){J({x:50,y:50});G({x:40,y:40})});waitsForAnimation();runs(function(){H({x:10,y:10});F({x:40,y:40})});waitsForAnimation()});describe("stretch",function(){it("should allow stretching past the top scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:50,y:60})});waitsForAnimation();runs(function(){H({x:0,y:-5});F({x:50,y:60})});waitsForAnimation()});it("should allow stretching past the right scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:-60,y:50})});waitsForAnimation();runs(function(){H({x:105,y:0});F({x:-60,y:50})});waitsForAnimation()});it("should allow stretching past the bottom scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:50,y:-60})});waitsForAnimation();runs(function(){H({x:0,y:105});F({x:50,y:-60})});waitsForAnimation()});it("should allow stretching past the left scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:60,y:50})});waitsForAnimation();runs(function(){H({x:-5,y:0});F({x:60,y:50})});waitsForAnimation()});it("should allow stretching past the top-left scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:60,y:60})});waitsForAnimation();runs(function(){H({x:-5,y:-5});F({x:60,y:60})});waitsForAnimation()});it("should allow stretching past the top-right scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:-60,y:60})});waitsForAnimation();runs(function(){H({x:105,y:-5});F({x:-60,y:60})});waitsForAnimation()});it("should allow stretching past the bottom-right scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:-60,y:-60})});waitsForAnimation();runs(function(){H({x:105,y:105});F({x:-60,y:-60})});waitsForAnimation()});it("should allow stretching past the bottom-left scroll boundary",function(){E();runs(function(){J({x:50,y:50});G({x:60,y:-60})});waitsForAnimation();runs(function(){H({x:-5,y:105});F({x:60,y:-60})});waitsForAnimation()});describe("with outOfBoundRestrictFactor:0",function(){it("should not allow stretching past the top scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:50,y:60})});waitsForAnimation();runs(function(){H({x:0,y:0});F({x:50,y:60})});waitsForAnimation()});it("should not allow stretching past the right scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:-60,y:50})});waitsForAnimation();runs(function(){H({x:100,y:0});F({x:-60,y:50})});waitsForAnimation()});it("should not allow stretching past the bottom scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:50,y:-60})});waitsForAnimation();runs(function(){H({x:0,y:100});F({x:50,y:-60})});waitsForAnimation()});it("should not allow stretching past the left scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:60,y:50})});waitsForAnimation();runs(function(){H({x:0,y:0});F({x:60,y:50})});waitsForAnimation()});it("should not allow stretching past the top-left scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:60,y:60})});waitsForAnimation();runs(function(){H({x:0,y:0});F({x:60,y:60})});waitsForAnimation()});it("should not allow stretching past the top-right scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:-60,y:60})});waitsForAnimation();runs(function(){H({x:100,y:0});F({x:-60,y:60})});waitsForAnimation()});it("should not allow stretching past the bottom-right scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:-60,y:-60})});waitsForAnimation();runs(function(){H({x:100,y:100});F({x:-60,y:-60})});waitsForAnimation()});it("should not allow stretching past the bottom-left scroll boundary",function(){E({outOfBoundRestrictFactor:0});runs(function(){J({x:50,y:50});G({x:60,y:-60})});waitsForAnimation();runs(function(){H({x:0,y:100});F({x:60,y:-60})});waitsForAnimation()})})});describe("bounce",function(){beforeEach(function(){E()});it("should bounce back when stretched past the top scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:50,y:60})});waitsForAnimation();runs(function(){H({x:0,y:-5});B.on("scrollend",function(){K=true});F({x:50,y:60})});waitsFor(function(){return K});runs(function(){H({x:0,y:0})})});it("should bounce back when stretched past the right scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:-60,y:50})});waitsForAnimation();runs(function(){H({x:105,y:0});B.on("scrollend",function(){K=true});F({x:-60,y:50})});waitsFor(function(){return K});runs(function(){H({x:100,y:0})})});it("should bounce back when stretched past the bottom scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:50,y:-60})});waitsForAnimation();runs(function(){H({x:0,y:105});B.on("scrollend",function(){K=true});F({x:50,y:-60})});waitsFor(function(){return K});runs(function(){H({x:0,y:100})})});it("should bounce back when stretched past the left scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:60,y:50})});waitsForAnimation();runs(function(){H({x:-5,y:0});B.on("scrollend",function(){K=true});F({x:60,y:50})});waitsFor(function(){return K});runs(function(){H({x:0,y:0})})});it("should bounce back when stretched past the top-left scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:60,y:60})});waitsForAnimation();runs(function(){H({x:-5,y:-5});B.on("scrollend",function(){K=true});F({x:60,y:60})});waitsFor(function(){return K});runs(function(){H({x:0,y:0})})});it("should bounce back when stretched past the top-right scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:-60,y:60})});waitsForAnimation();runs(function(){H({x:105,y:-5});B.on("scrollend",function(){K=true});F({x:-60,y:60})});waitsFor(function(){return K});runs(function(){H({x:100,y:0})})});it("should bounce back when stretched past the bottom-right scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:-60,y:-60})});waitsForAnimation();runs(function(){H({x:105,y:105});B.on("scrollend",function(){K=true});F({x:-60,y:-60})});waitsFor(function(){return K});runs(function(){H({x:100,y:100})})});it("should bounce back when stretched past the bottom-left scroll boundary",function(){var K=false;runs(function(){J({x:50,y:50});G({x:60,y:-60})});waitsForAnimation();runs(function(){H({x:-5,y:105});B.on("scrollend",function(){K=true});F({x:60,y:-60})});waitsFor(function(){return K});runs(function(){H({x:0,y:100})})})})});describe("direction:'both'",function(){beforeEach(function(){E({direction:"both"})});it("should size to the content",function(){expect(B.getSize()).toEqual({x:200,y:200})});it("should allow scrolling in the vertical direction",function(){runs(function(){J({x:50,y:50});G({x:50,y:40})});waitsForAnimation();runs(function(){H({x:0,y:10});F({x:50,y:40})});waitsForAnimation()});it("should allow scrolling in the horizontal direction",function(){runs(function(){J({x:50,y:50});G({x:40,y:50})});waitsForAnimation();runs(function(){H({x:10,y:0});F({x:40,y:50})});waitsForAnimation()});it("should allow scrolling in both directions simultaneously",function(){runs(function(){J({x:50,y:50});G({x:40,y:40})});waitsForAnimation();runs(function(){H({x:10,y:10});F({x:40,y:40})});waitsForAnimation()})});describe("direction:'vertical'",function(){beforeEach(function(){E({direction:"vertical"})});it("should size to the content",function(){expect(B.getSize()).toEqual({x:200,y:200})});it("should allow scrolling in the vertical direction",function(){runs(function(){J({x:50,y:50});G({x:50,y:40})});waitsForAnimation();runs(function(){H({x:0,y:10});F({x:50,y:40})});waitsForAnimation()});it("should not allow scrolling in the horizontal direction",function(){runs(function(){J({x:50,y:50});G({x:40,y:50})});waitsForAnimation();runs(function(){H({x:0,y:0});F({x:40,y:50})});waitsForAnimation()});it("should not allow scrolling in both directions simultaneously",function(){runs(function(){J({x:50,y:50});G({x:40,y:40})});waitsForAnimation();runs(function(){H({x:0,y:10});F({x:40,y:40})});waitsForAnimation()})});describe("direction:'horizontal'",function(){beforeEach(function(){E({direction:"horizontal"})});it("should size to the content",function(){expect(B.getSize()).toEqual({x:200,y:200})});it("should not allow scrolling in the vertical direction",function(){runs(function(){J({x:50,y:50});G({x:50,y:40})});waitsForAnimation();runs(function(){H({x:0,y:0});F({x:50,y:40})});waitsForAnimation()});it("should allow scrolling in the horizontal direction",function(){runs(function(){J({x:50,y:50});G({x:40,y:50})});waitsForAnimation();runs(function(){H({x:10,y:0});F({x:40,y:50})});waitsForAnimation()});it("should not allow scrolling in both directions simultaneously",function(){runs(function(){J({x:50,y:50});G({x:40,y:40})});waitsForAnimation();runs(function(){H({x:10,y:0});F({x:40,y:40})});waitsForAnimation()})});it("should end scrolling when the touchend occurs outside the scroller",function(){var K=false;E();B.on("scrollend",function(){K=true});runs(function(){J({x:50,y:50});G({x:60,y:40});G({x:150,y:150});F({x:150,y:150},document.body)});waitsFor(function(){return K});runs(function(){expect(K).toBe(true)})})})})