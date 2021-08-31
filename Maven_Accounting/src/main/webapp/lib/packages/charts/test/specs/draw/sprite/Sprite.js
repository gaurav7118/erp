describe("Ext.draw.sprite.Sprite",function(){describe("transformation matrix calculation",function(){describe("default centers of scaling and rotation",function(){it("should apply transformation in the following order: scale, rotate, translate",function(){var B=Math.PI/2,I=Math.sin(B),N=Math.cos(B),C=100,L=100,A=100,O=100,M=2,K=0.5,H=100,G=50,F=C+A/2,E=L+O/2;var J=new Ext.draw.sprite.Rect({x:C,y:L,width:A,height:O,rotationRads:B,scalingX:M,scalingY:K,translationX:H,translationY:G});var D=[N*M,I*M,-I*K,N*K,N*(F*(1-M)-F)-I*(E*(1-K)-E)+F+H,I*(F*(1-M)-F)+N*(E*(1-K)-E)+E+G];J.applyTransformations(true);expect(J.attr.matrix.elements).toEqual(D)})});describe("custom centers of scaling and rotation",function(){it("should apply transformation in the following order: scale, rotate, translate",function(){var D=Math.PI/2,K=Math.sin(D),P=Math.cos(D),E=100,N=100,C=100,Q=100,O=2,M=0.5,H=100,G=50,J=50,I=50,B=150,A=150;var L=new Ext.draw.sprite.Rect({x:E,y:N,width:C,height:Q,rotationRads:D,scalingX:O,scalingY:M,translationX:H,translationY:G,rotationCenterX:B,rotationCenterY:A,scalingCenterX:J,scalingCenterY:I});var F=[P*O,K*O,-K*M,P*M,P*(J*(1-O)-B)-K*(I*(1-M)-A)+B+H,K*(J*(1-O)-B)+P*(I*(1-M)-A)+A+G];L.applyTransformations(true);expect(L.attr.matrix.elements).toEqual(F)})})});describe("setTransform",function(){var B=[1.76776695,1.76776695,-5.30330086,5.30330086,3,4],A;beforeEach(function(){A=new Ext.draw.sprite.Rect()});afterEach(function(){Ext.destroy(A)});it("should use the given elements for the transformation matrix of the sprite",function(){A.setTransform(B);var C=A.attr.matrix.elements;expect(C).toEqual(B)});it("should mark the sprite and its parent as dirty",function(){var D=new Ext.draw.Container({renderTo:Ext.getBody(),width:200,height:200});var C=D.getSurface();expect(C.getDirty()).toBe(false);C.add(A);expect(C.getDirty()).toBe(true);C.renderFrame();expect(C.getDirty()).toBe(false);A.setTransform(B);expect(A.attr.dirty).toBe(true);expect(A.getParent().getDirty()).toBe(true);D.destroy()});it("should properly calculate the inverse matrix from the given matrix",function(){A.setTransform(B);var D=A.attr.inverseMatrix.elements,C=8;expect(D[0]).toBeCloseTo(0.28284271,C);expect(D[1]).toBeCloseTo(-0.0942809,C);expect(D[2]).toBeCloseTo(0.28284271,C);expect(D[3]).toBeCloseTo(0.0942809,C);expect(D[4]).toBeCloseTo(-1.97989899,C);expect(D[5]).toBeCloseTo(-0.0942809,C)});it("should mark bbox transform as dirty",function(){A.setTransform(B);expect(A.attr.bbox.transform.dirty).toBe(true)});it("should not update the transformation attributes by default",function(){var K=A.attr,L=K.rotationRads,D=K.rotationCenterX,C=K.rotationCenterY,F=K.scalingX,E=K.scalingY,J=K.scalingCenterX,I=K.scalingCenterY,H=K.translationX,G=K.translationY;A.setTransform(B);expect(K.rotationRads).toEqual(L);expect(K.rotationCenterX).toEqual(D);expect(K.rotationCenterY).toEqual(C);expect(K.scalingX).toEqual(F);expect(K.scalingY).toEqual(E);expect(K.scalingCenterX).toEqual(J);expect(K.scalingCenterY).toEqual(I);expect(K.translationX).toEqual(H);expect(K.translationY).toEqual(G)});it("should update the transformation attributes, if explicitly asked",function(){var C=A.attr,D=8;A.setTransform(B,true);expect(C.rotationRads).toBeCloseTo(Math.PI/4,D);expect(C.rotationCenterX).toEqual(0);expect(C.rotationCenterY).toEqual(0);expect(C.scalingX).toBeCloseTo(2.5,D);expect(C.scalingY).toBeCloseTo(7.5,D);expect(C.scalingCenterX).toEqual(0);expect(C.scalingCenterY).toEqual(0);expect(C.translationX).toEqual(3);expect(C.translationY).toEqual(4)});it("should not modify the given array",function(){A.setTransform(B);A.attr.matrix.rotate(Math.PI/4);expect(B).toEqual([1.76776695,1.76776695,-5.30330086,5.30330086,3,4])});it("should return the sprite itself",function(){var C=A.transform([1,0,0,1,100,100]);expect(C).toEqual(A)})});describe("resetTransform",function(){var A={type:"rect",x:0,y:0,width:100,height:100,rotationCenterX:0,rotationCenterY:0,rotationRads:Math.PI/3,scalingCenterX:0,scalingCenterY:0,scalingX:2,scalingY:3,translationX:50,translationY:50};it("should mark the sprite and its parent as dirty",function(){var C=new Ext.draw.Container({renderTo:Ext.getBody(),width:200,height:200});var B=C.getSurface();expect(B.getDirty()).toBe(false);var D=B.add(A);expect(B.getDirty()).toBe(true);B.renderFrame();expect(B.getDirty()).toBe(false);D.resetTransform();expect(D.attr.dirty).toBe(true);expect(D.getParent().getDirty()).toBe(true);C.destroy()});it("should reset the transformation matrix and its reverse to the identity matrix",function(){var B=new Ext.draw.sprite.Rect(A),C=[1,0,0,1,0,0];B.applyTransformations(true);expect(B.attr.matrix.elements).toNotEqual(C);B.resetTransform();expect(B.attr.matrix.elements).toEqual(C);expect(B.attr.inverseMatrix.elements).toEqual(C);B.destroy()});it("should return the sprite itself",function(){var C=new Ext.draw.sprite.Rect(),B=C.transform([1,0,0,1,100,100]);expect(B).toEqual(C);C.destroy()})});describe("transform",function(){it("should multiply the given matrix with the current transformation matrix",function(){var B=new Ext.draw.sprite.Rect(),A=12;B.attr.matrix.elements=[1,2,3,4,5,6];B.transform([1,2,3,4,5,6]);expect(B.attr.matrix.elements).toEqual([7,10,15,22,28,40]);var C=B.attr.inverseMatrix.elements;expect(C[0]).toBeCloseTo(5.5,A);expect(C[1]).toBeCloseTo(-2.5,A);expect(C[2]).toBeCloseTo(-3.75,A);expect(C[3]).toBeCloseTo(1.75,A);expect(C[4]).toBeCloseTo(-4,A);expect(C[5]).toBeCloseTo(0,A)});it("should pre-multiply the current matrix with the given matrix",function(){var A=new Ext.draw.sprite.Rect(),E=[2,0,0,3,0,0],D=[1,0,0,1,100,100],B=[2,4],C;A.transform(D).transform(E);expect(A.attr.matrix.elements).toEqual([2,0,0,3,200,300]);C=A.attr.matrix.transformPoint(B);expect(C).toEqual([204,312]);A.resetTransform();A.transform(E).transform(D);expect(A.attr.matrix.elements).toEqual([2,0,0,3,100,100]);C=A.attr.matrix.transformPoint(B);expect(C).toEqual([104,112]);A.destroy()});it("should return the sprite itself",function(){var B=new Ext.draw.sprite.Rect(),A=B.transform([1,0,0,1,100,100]);expect(A).toEqual(B);B.destroy()})});describe("remove",function(){it("should remove itself from the surface, returning itself or null (if already removed)",function(){var B=new Ext.draw.Surface({}),C=new Ext.draw.sprite.Rect({}),D=C.getId(),A;B.add(C);A=C.remove();expect(B.getItems().length).toBe(0);expect(B.get(D)).toBe(undefined);expect(A).toEqual(C);A=C.remove();expect(A).toBe(null);C.destroy();B.destroy()})});describe("destroy",function(){it("should remove itself from the surface",function(){var A=new Ext.draw.Surface({}),B=new Ext.draw.sprite.Rect({}),C=B.getId();A.add(B);B.destroy();expect(A.getItems().length).toBe(0);expect(A.get(C)).toBe(undefined);A.destroy()})});describe("isVisible",function(){var E="none",C="rgba(0,0,0,0)",D,A,B;beforeEach(function(){B=new Ext.draw.Container({renderTo:Ext.getBody()});A=new Ext.draw.Surface();D=new Ext.draw.sprite.Rect({hidden:false,globalAlpha:1,fillOpacity:1,strokeOpacity:1,fillStyle:"red",strokeStyle:"red"});A.add(D);B.add(A)});afterEach(function(){Ext.destroy(D,A,B)});it("should return true if the sprite belongs to a visible parent, false otherwise",function(){expect(D.isVisible()).toBe(true);A.remove(D);expect(D.isVisible()).toBe(false);var F=new Ext.draw.sprite.Instancing({template:D});A.add(F);expect(D.isVisible()).toBe(true);F.destroy()});it("should return false if the sprite belongs to a parent that doesn't belong to a surface",function(){var F=new Ext.draw.sprite.Instancing({template:D});expect(D.isVisible()).toBe(false)});it("should return false in case the sprite is hidden",function(){D.hide();expect(D.isVisible()).toBe(false)});it("should return false in case the sprite has no fillStyle and strokeStyle, true otherwise",function(){D.setAttributes({fillStyle:E});expect(D.isVisible()).toBe(true);D.setAttributes({fillStyle:C});expect(D.isVisible()).toBe(true);D.setAttributes({fillStyle:"red",strokeStyle:E});expect(D.isVisible()).toBe(true);D.setAttributes({strokeStyle:C});expect(D.isVisible()).toBe(true);D.setAttributes({fillStyle:E,strokeStyle:E});expect(D.isVisible()).toBe(false);D.setAttributes({fillStyle:E,strokeStyle:C});expect(D.isVisible()).toBe(false);D.setAttributes({fillStyle:C,strokeStyle:E});expect(D.isVisible()).toBe(false);D.setAttributes({fillStyle:C,strokeStyle:C});expect(D.isVisible()).toBe(false)});it("should return false if the globalAlpha attribute is zero",function(){D.setAttributes({globalAlpha:0});expect(D.isVisible()).toBe(false)});it("should return false if both fill and stroke are completely transparent, true otherwise",function(){D.setAttributes({fillOpacity:0,strokeOpacity:0});expect(D.isVisible()).toBe(false);D.setAttributes({fillOpacity:0,strokeOpacity:0.01});expect(D.isVisible()).toBe(true);D.setAttributes({fillOpacity:0.01,strokeOpacity:0});expect(D.isVisible()).toBe(true)})});describe("hitTest",function(){var C,A,B;beforeEach(function(){B=new Ext.draw.Container({renderTo:Ext.getBody()});A=new Ext.draw.Surface();C=new Ext.draw.sprite.Circle({hidden:false,globalAlpha:1,fillOpacity:1,strokeOpacity:1,fillStyle:"red",strokeStyle:"red",r:100,cx:100,cy:100});A.add(C);B.add(A)});afterEach(function(){Ext.destroy(C,A,B)});it("should return an object with the 'sprite' property set to the sprite itself, if the sprite is visible and its bounding box is hit",function(){var D=Ext.draw.sprite.Sprite.prototype.hitTest.call(C,[10,10]);expect(D&&D.sprite).toBe(C)});it("should return null, if the sprite's bounding box is hit, but the sprite is not visible",function(){var E=C.isVisible;C.isVisible=function(){return false};var D=Ext.draw.sprite.Sprite.prototype.hitTest.call(C,[10,10]);expect(D).toBe(null);C.isVisible=E});it("should return null, if the sprite is visible, but it's bounding box is not hit",function(){var D=Ext.draw.sprite.Sprite.prototype.hitTest.call(C,[210,210]);expect(D).toBe(null)})});describe("getAnimation",function(){it("should return the stored reference to the sprite's animation modifier",function(){var A=new Ext.draw.sprite.Rect();expect(A.getAnimation()).toEqual(A.fx)})});describe("setAnimation",function(){it("should set the config of the Animation modifier of a sprite",function(){var C=new Ext.draw.sprite.Rect();var B={duration:2000,easing:"bounceOut",customEasings:{x:"linear"},customDurations:{y:1000}};C.setAnimation(B);var A=C.fx.getInitialConfig();expect(A.duration).toEqual(B.duration);expect(A.easing).toEqual(B.easing);expect(A.customEasings).toEqual(B.customEasings);expect(A.customDurations).toEqual(B.customDurations)})})})