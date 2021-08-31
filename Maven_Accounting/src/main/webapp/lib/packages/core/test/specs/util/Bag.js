describe("Ext.util.Bag",function(){var D,B,A,F,E;function C(){return new Ext.util.Bag()}beforeEach(function(){D=C();B={id:"a"};A={id:"b"};F={id:"c"};E={id:"d"}});afterEach(function(){D=Ext.destroy(D)});describe("at construction",function(){it("should have no items",function(){expect(D.length).toBe(0);expect(D.getCount()).toBe(0);expect(D.getAt(0)).toBeNull()});it("should be at generation 0",function(){expect(D.generation).toBe(0)})});describe("adding",function(){describe("a new item",function(){it("should add an item to an empty collection",function(){D.add(B);expect(D.length).toBe(1);expect(D.getCount()).toBe(1);expect(D.getAt(0)).toBe(B);expect(D.getByKey("a")).toBe(B);expect(D.generation).toBe(1)});it("should add to the end of a filled collection",function(){D.add(B);D.add(A);D.add(F);expect(D.length).toBe(3);expect(D.getCount()).toBe(3);expect(D.getAt(0)).toBe(B);expect(D.getByKey("a")).toBe(B);expect(D.getAt(1)).toBe(A);expect(D.getByKey("b")).toBe(A);expect(D.getAt(2)).toBe(F);expect(D.getByKey("c")).toBe(F);expect(D.generation).toBe(3)});it("should return the added item",function(){expect(D.add(B)).toBe(B)})});describe("an existing item",function(){beforeEach(function(){D.add(B);D.add(A);D.add(F)});it("should leave the item in place",function(){D.add(A);expect(D.length).toBe(3);expect(D.getCount()).toBe(3);expect(D.getAt(1)).toBe(A);expect(D.getByKey("b")).toBe(A);expect(D.generation).toBe(4)});it("should replace an item with the same key",function(){var G={id:"b"};D.add(G);expect(D.length).toBe(3);expect(D.getCount()).toBe(3);expect(D.getAt(1)).toBe(G);expect(D.getByKey("b")).toBe(G);expect(D.generation).toBe(4)});it("should return the old item",function(){var G={id:"b"};expect(D.add(A)).toBe(A);expect(D.add(G)).toBe(A)})})});describe("clear",function(){describe("empty collection",function(){it("should be empty",function(){D.clear();expect(D.length).toBe(0);expect(D.getCount()).toBe(0);expect(D.getAt(0)).toBeNull();expect(D.getByKey("a")).toBeNull()});it("should not increment the generation if there have been no items",function(){D.clear();expect(D.generation).toBe(0)});it("should increment the generation if there have been items",function(){D.add(B);D.remove(B);D.clear();expect(D.generation).toBe(3)})});describe("filled collection",function(){beforeEach(function(){D.add(B);D.add(A);D.add(F)});it("should be empty",function(){D.clear();expect(D.length).toBe(0);expect(D.getCount()).toBe(0);expect(D.getAt(0)).toBeNull();expect(D.getByKey("a")).toBeNull()});it("should increment the generation",function(){D.clear();expect(D.generation).toBe(4)})});describe("cloned collection",function(){it("should increment the generation",function(){D.add(B);var G=D.clone();expect(G.generation).toBe(0);G.clear();expect(G.generation).toBe(1);G.destroy()})})});describe("clone",function(){var G;afterEach(function(){G=Ext.destroy(G)});describe("empty collection",function(){beforeEach(function(){G=D.clone()});it("should have generation 0",function(){G=D.clone();expect(G.generation).toBe(0)});it("should be empty",function(){G=D.clone();expect(G.length).toBe(0);expect(G.getCount()).toBe(0)});it("should not share changes",function(){G=D.clone();G.add(B);expect(G.getAt(0)).toBe(B);expect(G.getByKey("a")).toBe(B);expect(D.getAt(0)).toBeNull();expect(D.getByKey("a")).toBeNull()})});describe("filled collection",function(){beforeEach(function(){D.add(B);D.add(A);D.add(F);G=D.clone()});it("should have generation 0",function(){expect(G.generation).toBe(0)});it("should copy over items",function(){expect(G.length).toBe(3);expect(G.getCount(3));expect(G.getAt(0)).toBe(D.getAt(0));expect(G.getAt(1)).toBe(D.getAt(1));expect(G.getAt(2)).toBe(D.getAt(2));expect(G.getByKey("a")).toBe(D.getByKey("a"));expect(G.getByKey("b")).toBe(D.getByKey("b"));expect(G.getByKey("c")).toBe(D.getByKey("c"))});it("should not share changes made to the clone",function(){G.add(E);expect(G.length).toBe(4);expect(G.getCount()).toBe(4);expect(G.getAt(3)).toBe(E);expect(G.getByKey("d")).toBe(E);expect(D.length).toBe(3);expect(D.getCount()).toBe(3);expect(D.getAt(3)).toBeNull();expect(D.getByKey("d")).toBeNull()});it("should not share changes made to the original",function(){D.add(E);expect(D.length).toBe(4);expect(D.getCount()).toBe(4);expect(D.getAt(3)).toBe(E);expect(D.getByKey("d")).toBe(E);expect(G.length).toBe(3);expect(G.getCount()).toBe(3);expect(G.getAt(3)).toBeNull();expect(G.getByKey("d")).toBeNull()})})});describe("contains",function(){describe("empty collection",function(){it("should always be false",function(){expect(D.contains(B)).toBe(false)})});describe("filled collection",function(){beforeEach(function(){D.add(B);D.add(A)});it("should be false if the value is null/undefined",function(){expect(D.contains(null)).toBe(false);expect(D.contains(undefined)).toBe(false)});it("should return true if the item is in the collection",function(){expect(D.contains(B)).toBe(true)});it("should return false for an item not in the collection",function(){expect(D.contains(F)).toBe(false)});it("should return false for an item with a matching key but not the same reference",function(){expect(D.contains({id:"b"})).toBe(false)})})});describe("containsKey",function(){describe("empty collection",function(){it("should always be false",function(){expect(D.containsKey("a")).toBe(false)})});describe("filled collection",function(){beforeEach(function(){D.add(B)});it("should return false when the key is not in the collection",function(){expect(D.containsKey("b")).toBe(false)});it("should return true when the key is in the collection",function(){expect(D.containsKey("a")).toBe(true)})})});describe("getAt",function(){describe("empty collection",function(){it("should always return null",function(){expect(D.getAt(0)).toBeNull()})});describe("filled collection",function(){beforeEach(function(){D.add(B);D.add(A);D.add(F);D.add(E)});it("should return the item at the specified index",function(){expect(D.getAt(0)).toBe(B);expect(D.getAt(1)).toBe(A);expect(D.getAt(2)).toBe(F);expect(D.getAt(3)).toBe(E)});it("should return null when the index is larger than the collection bounds",function(){expect(D.getAt(200)).toBeNull()})})});describe("getByKey",function(){describe("empty collection",function(){it("should always return null",function(){expect(D.getByKey("a")).toBeNull()})});describe("filled collection",function(){beforeEach(function(){D.add(B);D.add(A);D.add(F);D.add(E)});it("should return the item with the matching key",function(){expect(D.getByKey("c")).toBe(F)});it("should return null when when the key doesn't exist in the collection",function(){expect(D.getByKey("z")).toBeNull()})})});describe("remove",function(){describe("empty collection",function(){it("should not modify the generation",function(){D.remove(B);expect(D.generation).toBe(0)});it("should remain empty",function(){D.remove(B);expect(D.length).toBe(0);expect(D.getCount()).toBe(0)});it("should return null",function(){expect(D.remove(B)).toBeNull()})});describe("filled collection",function(){beforeEach(function(){D.add(B);D.add(A);D.add(F);D.add(E)});describe("item exists in the collection",function(){it("should return the removed item",function(){expect(D.remove(B)).toBe(B)});it("should decrement the length",function(){D.remove(B);expect(D.length).toBe(3);expect(D.getCount()).toBe(3)});it("should increment the generation",function(){D.remove(B);expect(D.generation).toBe(5)});it("should be able to remove the last item",function(){D.remove(E);expect(D.getAt(0)).toBe(B);expect(D.getAt(1)).toBe(A);expect(D.getAt(2)).toBe(F);expect(D.getAt(3)).toBeNull()});it("should move the last item in place of the removed item",function(){D.remove(B);expect(D.getAt(0)).toBe(E);expect(D.getAt(1)).toBe(A);expect(D.getAt(2)).toBe(F);expect(D.getAt(3)).toBeNull()});it("should be able to remove the last remaining item",function(){D.remove(B);D.remove(A);D.remove(F);D.remove(E);expect(D.length).toBe(0);expect(D.getCount()).toBe(0);expect(D.getAt(0)).toBeNull();expect(D.getAt(1)).toBeNull();expect(D.getAt(2)).toBeNull();expect(D.getAt(3)).toBeNull()})});describe("item not in the collection",function(){it("should return null",function(){expect(D.remove({id:"z"})).toBeNull()});it("should not modify the length",function(){D.remove({id:"z"});expect(D.length).toBe(4);expect(D.getCount()).toBe(4)});it("should not modify the generation",function(){D.remove({id:"z"});expect(D.generation).toBe(4)})})})});describe("removeByKey",function(){describe("empty collection",function(){it("should not modify the generation",function(){D.removeByKey("a");expect(D.generation).toBe(0)});it("should remain empty",function(){D.removeByKey("a");expect(D.length).toBe(0);expect(D.getCount()).toBe(0)});it("should return null",function(){expect(D.removeByKey("a")).toBeNull()})});describe("filled collection",function(){beforeEach(function(){D.add(B);D.add(A);D.add(F);D.add(E)});describe("item exists in the collection",function(){it("should return the removed item",function(){expect(D.removeByKey("a")).toBe(B)});it("should decrement the length",function(){D.removeByKey("a");expect(D.length).toBe(3);expect(D.getCount()).toBe(3)});it("should increment the generation",function(){D.removeByKey("a");expect(D.generation).toBe(5)});it("should be able to remove the last item",function(){D.removeByKey("d");expect(D.getAt(0)).toBe(B);expect(D.getAt(1)).toBe(A);expect(D.getAt(2)).toBe(F);expect(D.getAt(3)).toBeNull()});it("should move the last item in place of the removed item",function(){D.removeByKey("a");expect(D.getAt(0)).toBe(E);expect(D.getAt(1)).toBe(A);expect(D.getAt(2)).toBe(F)});it("should be able to remove the last remaining item",function(){D.removeByKey("a");D.removeByKey("b");D.removeByKey("c");D.removeByKey("d");expect(D.length).toBe(0);expect(D.getCount()).toBe(0);expect(D.getAt(0)).toBeNull();expect(D.getAt(1)).toBeNull();expect(D.getAt(2)).toBeNull();expect(D.getAt(3)).toBeNull()})});describe("item not in the collection",function(){it("should return null",function(){expect(D.removeByKey({id:"z"})).toBeNull()});it("should not modify the length",function(){D.removeByKey({id:"z"});expect(D.length).toBe(4);expect(D.getCount()).toBe(4)});it("should not modify the generation",function(){D.removeByKey({id:"z"});expect(D.generation).toBe(4)})})})});describe("sort",function(){function G(I,H){I=I.id;H=H.id;if(I===H){return 0}return I<H?-1:1}describe("empty collection",function(){it("should not increase the generation",function(){D.sort(G);expect(D.generation).toBe(0)})});describe("filled collection",function(){beforeEach(function(){D.add(A);D.add(B);D.add(E);D.add(F)});it("should sort by function",function(){D.sort(G);expect(D.getAt(0)).toBe(B);expect(D.getAt(1)).toBe(A);expect(D.getAt(2)).toBe(F);expect(D.getAt(3)).toBe(E);expect(D.getByKey("a")).toBe(B);expect(D.getByKey("b")).toBe(A);expect(D.getByKey("c")).toBe(F);expect(D.getByKey("d")).toBe(E);expect(D.length).toBe(4);expect(D.getCount()).toBe(4)});it("should increase the generation",function(){D.sort(G);expect(D.generation).toBe(5)});it("should not attempt to maintain the sort",function(){var H={id:"e"};D.sort(function(J,I){J=J.id;I=I.id;if(J===I){return 0}return J<I?1:-1});D.add(H);expect(D.getAt(0)).toBe(E);expect(D.getAt(1)).toBe(F);expect(D.getAt(2)).toBe(A);expect(D.getAt(3)).toBe(B);expect(D.getAt(4)).toBe(H)})})})})