describe("Ext.util.HashMap",function(){var A;beforeEach(function(){A=new Ext.util.HashMap()});afterEach(function(){A.clearListeners();A=null});describe("after construction",function(){it("should be empty",function(){expect(A.getCount()).toEqual(0)});it("should not have any keys",function(){expect(A.getKeys()).toEqual([])});it("should not have any values",function(){expect(A.getValues()).toEqual([])})});describe("keyFn",function(){it("should have a default keyFn that returns the id of the object",function(){var B={id:"foo"};A.add(B);expect(A.get("foo")).toBe(B)});it("should accept a custom getKey function",function(){A=new Ext.util.HashMap({keyFn:function(){return B.key}});var B={key:"foo"};A.add(B);expect(A.get("foo")).toBe(B)})});describe("adding",function(){it("should add simple values",function(){A.add("key","a");expect(A.get("key")).toBe("a")});it("should preserve the type",function(){A.add("key",3);expect((typeof A.get("key")).toLowerCase()).toBe("number")});it("should be able to add complex types",function(){var C={foo:"bar"},B=[C];A.add("key1",C);A.add("key2",B);expect(A.get("key1")).toBe(C);expect(A.get("key2")).toEqual([C])});it("should be able to be called multiple times",function(){A.add("key1","a");A.add("key2","b");expect(A.get("key1")).toBe("a");expect(A.get("key2")).toBe("b")});it("should support adding undefined values",function(){A.add("key1",undefined);expect(A.get("key1")).toBeUndefined()});it("should support adding null values",function(){A.add("key1",null);expect(A.get("key1")).toBeNull()});it("should support taking a single param",function(){var B={id:"key",foo:"bar"};A.add(B);expect(A.get("key")).toBe(B)});it("should fire the add event",function(){var B={fn:Ext.emptyFn};spyOn(B,"fn");A.on("add",B.fn);A.add("key","val");expect(B.fn).toHaveBeenCalled()})});describe("replace",function(){it("should add the value if it doesn't exist",function(){A.replace("key","val");expect(A.get("key")).toBe("val")});it("should replace any old value",function(){A.add("key","val1");A.replace("key","val2");expect(A.get("key")).toBe("val2")});it("should replace an old value with undefined",function(){A.add("key","val1");A.replace("key",undefined);expect(A.get("key")).toBeUndefined()});it("should replace an old value with null",function(){A.add("key","val1");A.replace("key",null);expect(A.get("key")).toBeNull()});it("should fire the replace event",function(){A.add("key","val1");var B={fn:Ext.emptyFn};spyOn(B,"fn");A.on("replace",B.fn);A.replace("key","val2");expect(B.fn).toHaveBeenCalled()})});describe("counting",function(){it("should return 0 when empty",function(){expect(A.getCount()).toBe(0)});it("should return the correct count",function(){A.add("key1",1);A.add("key2",2);A.add("key3",3);A.add("key4",4);A.add("key5",5);expect(A.getCount()).toBe(5)});it("should increase the count when adding a new item",function(){expect(A.getCount()).toBe(0);A.add("key1",1);expect(A.getCount()).toBe(1)});it("should decrease the count when removing an item",function(){A.add("key1",1);expect(A.getCount()).toBe(1);A.removeAtKey("key1");expect(A.getCount()).toBe(0)});it("should keep the same count when replacing an item",function(){A.add("key1",1);expect(A.getCount()).toBe(1);A.replace("key1",2);expect(A.getCount()).toBe(1)});it("should keep the same count when adding an existing item",function(){A.add("key1",1);expect(A.getCount()).toBe(1);A.add("key1",2);expect(A.getCount()).toBe(1)})});describe("removing",function(){describe("by key",function(){it("should return false if the key doesn't exist",function(){expect(A.removeAtKey("key")).toBe(false)});it("should remove the item if found",function(){A.add("key","val");A.removeAtKey("key");expect(A.get("key")).toBeUndefined()});it("should return true on a  successful remove",function(){A.add("key","val");expect(A.removeAtKey("key")).toBe(true)});it("should fire the remove event",function(){A.add("key","val");var B={fn:Ext.emptyFn};spyOn(B,"fn");A.on("remove",B.fn);A.removeAtKey("key");expect(B.fn).toHaveBeenCalled()});it("should modify the count",function(){A.add("key","val");A.removeAtKey("key");expect(A.getCount()).toBe(0)});describe("by value",function(){it("should return false if the key doesn't exist",function(){expect(A.remove(1)).toBe(false)});it("should remove the item if found",function(){A.add("key","val");A.remove("val");expect(A.get("key")).toBeUndefined()});it("should only remove the first matched value",function(){A.add("key1","val");A.add("key2","val");A.remove("val");expect(A.get("key1")).toBeUndefined();expect(A.get("key2")).toBe("val")});it("should return true on a  successful remove",function(){A.add("key","val");expect(A.remove("val")).toBe(true)});it("should fire the remove event",function(){A.add("key","val");var B={fn:Ext.emptyFn};spyOn(B,"fn");A.on("remove",B.fn);A.remove("val");expect(B.fn).toHaveBeenCalled()})})})});describe("each",function(){var D,C,B;beforeEach(function(){D={fn:function(E){C=this;if(E=="drop"){return false}}};B=spyOn(D,"fn").andCallThrough()});afterEach(function(){D=B=C=null});it("should not iterate if the hash is empty",function(){A.each(D.fn);expect(D.fn).not.toHaveBeenCalled()});it("should iterate over every item",function(){A.add("key1","val1");A.add("key2","val2");A.add("key3","val3");A.each(D.fn);expect(B.callCount).toBe(3)});it("should pass in arguments",function(){A.add("key1","val1");A.each(D.fn);expect(B.argsForCall[0]).toEqual(["key1","val1",1])});it("should default to the hash as scope",function(){A.add("key","val");A.each(D.fn);expect(C).toBe(A)});it("should use scope if one is passed",function(){A.add("key","val");A.each(D.fn,D);expect(C).toBe(D)});it("should stop iterating if false is returned",function(){A.add("key1","a");A.add("key2","b");A.add("drop",true);A.add("key3","c");A.add("key4","d");A.each(D.fn);expect(B.callCount).toBe(3)})});describe("checking contains",function(){describe("containsKey",function(){it("should return false if the hash is empty",function(){expect(A.containsKey("key")).toBe(false)});it("should return false if the key doesn't exist",function(){A.add("key1","a");expect(A.containsKey("key")).toBe(false)});it("should return true if the key is matched",function(){A.add("key","val");expect(A.containsKey("key")).toBe(true)});it("should use a hasOwnProperty check",function(){expect(A.containsKey("toString")).toBe(false)})});describe("contains",function(){it("should return false if the hash is empty",function(){expect(A.contains("val")).toBe(false)});it("should return false if the value doesn't exist",function(){A.add("key","v");expect(A.contains("val")).toBe(false)});it("should return true if the value exists",function(){A.add("key","val");expect(A.contains("val")).toBe(true)})})});describe("get",function(){it("should return undefined if the item doesn't exist",function(){expect(A.get("key")).toBeUndefined()});it("should return the value if found",function(){A.add("key","val");expect(A.get("key")).toBe("val")});it("should preserve the type",function(){var C={},B={},D={};A.add("key",[C,B,D]);expect(A.get("key")).toEqual([C,B,D])})});describe("clear",function(){it("should do nothing if the hash is empty",function(){A.clear();expect(A.getCount()).toBe(0)});it("should remove all items from the hash",function(){A.add("key1","a");A.add("key2","b");A.add("key3","c");A.add("key4","d");A.add("key5","e");A.clear();expect(A.getCount()).toBe(0)});it("should fire the clear event",function(){var B={fn:Ext.emptyFn};spyOn(B,"fn");A.on("clear",B.fn);A.clear();expect(B.fn).toHaveBeenCalled()})});describe("getKeys/getValues",function(){it("should return an empty array if there are no keys",function(){expect(A.getKeys()).toEqual([])});it("should return all of the keys",function(){A.add("a","a");A.add("b","b");A.add("c","c");var B=A.getKeys();expect(B).toContain("a");expect(B).toContain("b");expect(B).toContain("c")});it("should return an empty array if there are no values",function(){expect(A.getValues()).toEqual([])});it("should return all of the values",function(){var C={};A.add("a",1);A.add("b",C);var B=A.getValues();expect(B).toContain(1);expect(B).toContain(C)})});describe("clone",function(){it("should be empty when cloning an empty hash",function(){var B=A.clone();expect(B.getCount()).toBe(0)});it("should clone all items",function(){A.add("a",1);A.add("b",2);var B=A.clone();expect(A.get("a")).toBe(1);expect(A.get("b")).toBe(2)});it("should only do a shallow clone",function(){var C={foo:"bar"},B;A.add("key",C);B=A.clone();expect(B.get("key")).toBe(C)})})})