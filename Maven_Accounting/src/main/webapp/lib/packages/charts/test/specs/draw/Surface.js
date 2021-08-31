describe("Ext.draw.Surface",function(){describe("add",function(){it("should not add the same sprite to the surface twice",function(){var A=new Ext.draw.Surface({}),B=new Ext.draw.sprite.Rect({});A.add(B);A.add(B);expect(A.getItems().length).toEqual(1);A.removeAll(true);A.add([B,B]);expect(A.getItems().length).toEqual(1);A.destroy()});it("should set the sprite's 'parent' and 'surface' configs to itself",function(){var B=new Ext.draw.sprite.Rect(),A=new Ext.draw.Surface();A.add(B);expect(B.getParent()).toBe(A);expect(B.getSurface()).toBe(A);A.destroy()})});describe("get",function(){var B,A;beforeEach(function(){B=new Ext.draw.Surface({items:[{type:"rect",id:"sprite1"},{type:"text",id:"sprite2"}]})});afterEach(function(){B.destroy()});it("should be able to get a sprite by id",function(){A=B.get("sprite1");expect(A.isSprite).toBe(true);expect(A.type).toBe("rect");A=B.get("sprite2");expect(A.isSprite).toBe(true);expect(A.type).toBe("text")});it("should be able to get a sprite by index",function(){A=B.get(0);expect(A.isSprite).toBe(true);expect(A.type).toBe("rect");A=B.get(1);expect(A.isSprite).toBe(true);expect(A.type).toBe("text")})});describe("remove",function(){it("should be able to remove the sprite (instance or id), should return removed sprite",function(){var C="testing",D=new Ext.draw.sprite.Rect({}),E=new Ext.draw.sprite.Text({id:C}),B=new Ext.draw.Surface({}),A,F;B.add(D,E);expect(B.getItems().length).toBe(2);F=D.getId();A=B.remove(D);expect(A).toEqual(D);expect(D.isDestroyed).toBe(undefined);expect(B.getItems().length).toBe(1);expect(B.get(F)).toBe(undefined);A=B.remove(C);expect(A).toEqual(E);expect(E.isDestroyed).toBe(undefined);expect(B.getItems().length).toBe(0);expect(B.get(C)).toBe(undefined);B.destroy();D.destroy();E.destroy()});it("should be able to destroy the sprite (instance or id) in the process, should return destroyed sprite",function(){var C=new Ext.draw.sprite.Rect({}),D=new Ext.draw.sprite.Text({id:"testing"}),B=new Ext.draw.Surface({}),A;B.add(C,D);expect(B.getItems().length).toBe(2);A=B.remove(C,true);expect(A).toEqual(C);expect(A.isDestroyed).toBe(true);expect(B.getItems().length).toBe(1);A=B.remove("testing",true);expect(A).toEqual(D);expect(A.isDestroyed).toBe(true);expect(B.getItems().length).toBe(0);B.destroy()});it("should return null if not given a sprite",function(){var A=new Ext.draw.Surface({});function B(C){return(null===A.remove(C))&&(null===A.remove(C,true))}expect(B(0)).toBe(true);expect(B(5)).toBe(true);expect(B(true)).toBe(true);expect(B(false)).toBe(true);expect(B(undefined)).toBe(true);expect(B(null)).toBe(true);expect(B("hello")).toBe(true);expect(B("")).toBe(true);expect(B({})).toBe(true);expect(B([])).toBe(true);A.destroy()});it("if passed an already destroyed sprite, should return it without doing anything",function(){var C=new Ext.draw.sprite.Rect({}),B=new Ext.draw.Surface({}),A;C.destroy();A=B.remove(C);expect(A).toEqual(C);A=B.remove(C,true);expect(A).toEqual(C);B.destroy()});it("should be able to destroy (but not remove!) a sprite that belongs to another or no surface",function(){var B=new Ext.draw.Surface({}),E=new Ext.draw.Surface({}),D=new Ext.draw.sprite.Rect({}),C=new Ext.draw.sprite.Text({}),A;B.add(D);A=E.remove(D);expect(A).toBe(D);expect(B.getItems()[0]).toEqual(D);A=E.remove(D,true);expect(A).toEqual(D);expect(A.isDestroyed).toBe(true);expect(B.getItems().length).toBe(0);A=E.remove(C);expect(A).toEqual(C);expect(A.isDestroyed).toBe(undefined);A=E.remove(C,true);expect(A).toEqual(C);expect(A.isDestroyed).toBe(true);B.destroy();E.destroy()})});describe("destroy",function(){it("should fire the 'destroy' event",function(){var A=new Ext.draw.Surface,B;A.on("destroy",function(){B=true});A.destroy();expect(B).toBe(true)})});describe("waitFor",function(){var D,C,B,A;beforeEach(function(){D=new Ext.draw.Surface();C=new Ext.draw.Surface();B=new Ext.draw.Surface();A=new Ext.draw.Surface()});afterEach(function(){Ext.destroy(D,C,B,A)});it("should add the given surface to a list of current surface predecessors only once",function(){D.waitFor(C);expect(D.predecessors.length).toBe(1);expect(D.predecessors[0]).toEqual(C)});it("should only increase own dirty predecessor counter if the given surface is dirty",function(){D.waitFor(C);expect(D.dirtyPredecessorCount).toBe(0);B.setDirty(true);C.waitFor(B);expect(C.dirtyPredecessorCount).toBe(1)});it("should be able to wait for multiple surfaces",function(){D.waitFor(C);D.waitFor(B);D.waitFor(A);expect(D.predecessors.length).toBe(3)})});describe("'dirty' config",function(){var E,D,C,B,A;beforeEach(function(){E=new Ext.draw.Surface();D=new Ext.draw.Surface();C=new Ext.draw.Surface();B=new Ext.draw.Surface();A=new Ext.draw.Surface()});afterEach(function(){Ext.destroy(E,D,C,B,A)});it("should not be dirty upon construction",function(){expect(E.getDirty()).toBe(false)});it("should increment dirtyPredecessorCount of all successors (not just immediate) when set to true",function(){C.waitFor(D);A.waitFor(B);D.waitFor(E);B.waitFor(E);E.setDirty(true);expect(D.dirtyPredecessorCount).toBe(1);expect(C.dirtyPredecessorCount).toBe(1);expect(B.dirtyPredecessorCount).toBe(1);expect(A.dirtyPredecessorCount).toBe(1)});it("should decrement dirtyPredecessorCount of all immediate successors when set to false",function(){C.waitFor(D);A.waitFor(B);D.waitFor(E);B.waitFor(E);E.setDirty(true);E.setDirty(false);expect(D.dirtyPredecessorCount).toBe(0);expect(B.dirtyPredecessorCount).toBe(0);expect(C.dirtyPredecessorCount).toBe(1);expect(A.dirtyPredecessorCount).toBe(1)});it("should not affect dirtyPredecessorCount of successors if value hasn't changed",function(){C.waitFor(D);A.waitFor(B);D.waitFor(E);B.waitFor(E);E.setDirty(false);expect(D.dirtyPredecessorCount).toBe(0);expect(C.dirtyPredecessorCount).toBe(0);expect(B.dirtyPredecessorCount).toBe(0);expect(A.dirtyPredecessorCount).toBe(0);E.setDirty(true);E.setDirty(true);expect(D.dirtyPredecessorCount).toBe(1);expect(C.dirtyPredecessorCount).toBe(1);expect(B.dirtyPredecessorCount).toBe(1);expect(A.dirtyPredecessorCount).toBe(1)});it("should make dirtyPredecessorCount reflect the actual number of immediate dirty predecessors",function(){E.waitFor(D);E.waitFor(C);E.waitFor(B);D.setDirty(true);C.setDirty(true);B.setDirty(true);expect(E.dirtyPredecessorCount).toBe(3);C.setDirty(false);expect(E.dirtyPredecessorCount).toBe(2)})})})