describe("Ext.app.bind.Formula",function(){var B;beforeEach(function(){B=new Ext.app.ViewModel({scheduler:{tickDelay:1000000}})});afterEach(function(){B.destroy();B=null;expect(Ext.util.Scheduler.instances.length).toBe(0)});function D(F,E){var G={};G[E||"fn"]=F;B.setFormulas(G);return B.getRoot().children.fn.formula}function C(E){return{toString:function(){return E}}}function A(G,E){var F=D(Ext.emptyFn),H;H=F.parseFormula({toString:function(){return G}});delete H.$literal;expect(Ext.Object.getKeys(H)).toEqual(E)}describe("argument parsing",function(){it("should work with a simple function definition",function(){A("function (get) { return get('foo'); };",["foo"])});it("should parse a var with numbers in the name",function(){A("function (g2et) { return g2et('foo'); };",["foo"])});it("should parse a var starting with an underscore",function(){A("function (_get) { return _get('foo'); };",["foo"])});it("should parse a with multi vars",function(){A("function (get, some, other, stuff) { return get('foo'); };",["foo"])});it("should parse with no spaces between function and parens",function(){A("function(get) { return get('foo'); };",["foo"])});it("should parse with no spaces between parens and curly",function(){A("function (get){ return get('foo'); };",["foo"])});it("should parse without an ending semi colon",function(){A("function (get) { return get('foo'); }",["foo"])});it("should parse with leading spaces",function(){A("    function (get) { return get('foo'); }",["foo"])});it("should parse with trailing spaces",function(){A("function (get) { return get('foo'); };                ",["foo"])})});describe("recognizing bindings",function(){it("should parse a simple variable",function(){A("function (get) { return get('foo'); };",["foo"])});it("should only match a variable once",function(){A("function (get) { return get('foo') + get('foo') + get('foo'); };",["foo"])});it("should match multiple expressions",function(){A("function (get) { return get('foo') + get('bar') + get('baz'); };",["foo","bar","baz"])});it("should match an expression with a number in it",function(){A("function (get) { return get('foo1'); };",["foo1"])});it("should match an expression that starts with an underscore",function(){A("function (get) { return get('_foo'); };",["_foo"])});it("should match as a dynamic property",function(){A("function (get) { return someObj[get('foo')]; };",["foo"])});it("should match inside parens",function(){A("function (get) { return (get('foo') + 1 + get('bar')); };",["foo","bar"])});it("should match an expression with double quotes",function(){A('function (get) { return get("foo"); };',["foo"])});describe("spacing",function(){it("should match leading spaces",function(){A('function (get) { return get(        "foo"); };',["foo"])});it("should match trailing spaces",function(){A('function (get) { return get("foo"   ); };',["foo"])});it("should match leading & trailing spaces",function(){A('function (get) { return get( "foo" ); };',["foo"])})});describe("non-matches",function(){it("should not match when the identifier has a prefix",function(){A("function (get) { return get('foo') + forget('bar'); };",["foo"])});it("should not match when the identifier has a suffix",function(){A("function (get) { return get('foo') + getfor('bar'); };",["foo"])});it("should not match when the identifier is a property of another object",function(){A("function (get) { return get('foo') + something.get('bar'); };",["foo"])})});describe("functions",function(){it("should match only the expression part",function(){A("function (get) { return get('foo').substring(0, 3); };",["foo"])});it("should match as the sole param to a function",function(){A("function (get) { return someFn(get('foo')) };",["foo"])});it("should match as the first param to a function",function(){A("function (get) { return someFn(get('foo'), 1, 2) };",["foo"])});it("should match as a middle param to a function",function(){A("function (get) { return someFn(1, get('foo'), 2) };",["foo"])});it("should match as the last param to a function",function(){A("function (get) { return someFn(1, 2, get('foo')) };",["foo"])});it("should match multiple params to a function",function(){A("function (get) { return someFn(get('foo'), get('bar')) };",["foo","bar"])})});describe("nesting",function(){it("should match a nested expression",function(){A("function (get) { return get('foo.bar.baz'); };",["foo.bar.baz"])});it("should match multiple nested subpaths",function(){A("function (get) { return get('foo.bar.baz.a') + get('foo.bar.baz.b'); };",["foo.bar.baz.a","foo.bar.baz.b"])});it("should match paths at different depths",function(){A("function (get) { return get('foo') + get('bar.baz.a.b') + get('some.other.path.x.y'); };",["foo","bar.baz.a.b","some.other.path.x.y"])});it("should match get calls inside get calls",function(){A("function (get) { return (get(get('foo') + get('bar')); };",["foo","bar"])})})})})