describe("Ext.event.gesture.Drag",function(){var A=Ext.testHelper,O=Ext.event.gesture.Drag.instance,N=O.getMinDistance(),P,D,C,H,M,Q,L,J,I;function G(S,T){A.touchStart(T||P,S)}function R(S,T){A.touchMove(T||P,S)}function E(S,T){A.touchEnd(T||P,S)}function B(S,T){A.touchCancel(T||P,S)}function F(U,T){var S;for(S in T){expect(U[S]).toBe(T[S])}}beforeEach(function(){P=Ext.getBody().createChild({});D=jasmine.createSpy();C=jasmine.createSpy();H=jasmine.createSpy();M=jasmine.createSpy();D.andCallFake(function(S){Q=S});C.andCallFake(function(S){L=S});H.andCallFake(function(S){J=S});M.andCallFake(function(S){I=S});P.on("dragstart",D);P.on("drag",C);P.on("dragend",H);P.on("dragcancel",M)});afterEach(function(){P.destroy()});it("should fire dragstart, drag, and dragend when the distance exceeds minDistance",function(){runs(function(){G({id:1,x:100,y:101});R({id:1,x:99,y:101-N})});waitsForAnimation();runs(function(){expect(D).toHaveBeenCalled();expect(C).toHaveBeenCalled();F(Q,{x:99,y:101-N,pageX:99,pageY:101-N,startX:100,startY:101,previousX:100,previousY:101,deltaX:-1,deltaY:-N,absDeltaX:1,absDeltaY:N,previousDeltaX:0,previousDeltaY:0});F(L,{x:99,y:101-N,pageX:99,pageY:101-N,startX:100,startY:101,previousX:100,previousY:101,deltaX:-1,deltaY:-N,absDeltaX:1,absDeltaY:N,previousDeltaX:0,previousDeltaY:0});R({id:1,x:97,y:100-N})});waitsForAnimation();runs(function(){expect(C.callCount).toBe(2);F(L,{x:97,y:100-N,pageX:97,pageY:100-N,startX:100,startY:101,previousX:99,previousY:101-N,deltaX:-3,deltaY:-(N+1),absDeltaX:3,absDeltaY:N+1,previousDeltaX:-1,previousDeltaY:-N});E({id:1,x:96,y:99-N})});waitsForAnimation();runs(function(){expect(H).toHaveBeenCalled();F(J,{x:96,y:99-N,pageX:96,pageY:99-N,startX:100,startY:101,previousX:97,previousY:100-N,deltaX:-4,deltaY:-(N+2),absDeltaX:4,absDeltaY:N+2,previousDeltaX:-3,previousDeltaY:-(N+1)})})});it("should not fire dragstart, drag, and dragend when the distance is less than minDistance",function(){runs(function(){G({id:1,x:100,y:101});R({id:1,x:99,y:99+N});E({id:1,x:99,y:99+N})});waitsForAnimation();runs(function(){expect(D).not.toHaveBeenCalled();expect(C).not.toHaveBeenCalled();expect(H).not.toHaveBeenCalled()})});if(Ext.supports.Touch){it("should fire dragcancel and not dragend if the touch is canceled after dragstart",function(){runs(function(){G({id:1,x:100,y:101});R({id:1,x:99,y:101-N})});waitsForAnimation();runs(function(){expect(D).toHaveBeenCalled();expect(C).toHaveBeenCalled();R({id:1,x:97,y:100-N})});waitsForAnimation();runs(function(){expect(C.callCount).toBe(2);B({id:1,x:96,y:99-N})});waitsForAnimation();runs(function(){expect(H).not.toHaveBeenCalled();expect(M).toHaveBeenCalled();F(I,{x:96,y:99-N,pageX:96,pageY:99-N,startX:100,startY:101,previousX:97,previousY:100-N,deltaX:-4,deltaY:-(N+2),absDeltaX:4,absDeltaY:N+2,previousDeltaX:-3,previousDeltaY:-(N+1)})})})}it("should have the correct e.target if the mouse is moved off of the target",function(){runs(function(){G({id:1,x:500,y:300});A.touchMove(document.body,{id:1,x:200,y:700})});waitsForAnimation();runs(function(){expect(L.target).toBe(P.dom);E({id:1,x:200,y:700})});waitsForAnimation()});function K(S){describe("when the target element is removed from the dom mid-drag "+(S?"(using removeChild)":"(using innerHTML)"),function(){var U,V,T;function W(){if(S){U.dom.removeChild(V.dom)}else{U.dom.innerHTML=""}}beforeEach(function(){U=Ext.getBody().createChild({id:"parent"});V=U.createChild({id:"child"});if(Ext.supports.TouchEvents){T=V}else{T=document.body}});afterEach(function(){U.destroy();V.destroy()});it("should recover gracefully when the listener is attached above the target",function(){runs(function(){U.on("drag",C);U.on("dragend",H);G({id:1,x:100,y:100},V);R({id:1,x:100,y:100+N},V)});waitsForAnimation();runs(function(){expect(C.callCount).toBe(1);W();R({id:1,x:120,y:150+N},T)});waitsForAnimation();runs(function(){expect(C.callCount).toBe(2);expect(L.target).toBe(V.dom);E({id:1,x:120,y:150+N},T)});waitsForAnimation();runs(function(){expect(H).toHaveBeenCalled();expect(J.target).toBe(V.dom)})});it("should recover gracefully when the listener is attached to the target",function(){runs(function(){V.on("drag",C);V.on("dragend",H);G({id:1,x:100,y:100},V);R({id:1,x:100,y:100+N},V)});waitsForAnimation();runs(function(){expect(C.callCount).toBe(1);W();R({id:1,x:120,y:150+N},T)});waitsForAnimation();runs(function(){expect(C.callCount).toBe(2);expect(L.target).toBe(V.dom);E({id:1,x:120,y:150+N},T)});waitsForAnimation();runs(function(){expect(H).toHaveBeenCalled();expect(J.target).toBe(V.dom)})})})}K(true);K(false)})