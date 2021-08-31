describe("Ext.String",function(){var A=Ext.String;describe("ellipsis",function(){var B="A short string",C="A somewhat longer string";it("should keep short strings intact",function(){expect(A.ellipsis(B,100)).toEqual(B)});it("should truncate a longer string",function(){expect(A.ellipsis(C,10)).toEqual("A somew...")});describe("word break",function(){var F="www.sencha.com",E="Yeah!Yeah!Yeah!",D="Who?When?What?";it("should find a word break on ' '",function(){expect(A.ellipsis(C,10,true)).toEqual("A...")});it("should be able to break on '.'",function(){expect(A.ellipsis(F,9,true)).toEqual("www...")});it("should be able to break on '!'",function(){expect(A.ellipsis(E,9,true)).toEqual("Yeah...")});it("should be able to break on '?'",function(){expect(A.ellipsis(D,8,true)).toEqual("Who...")})})});describe("escapeRegex",function(){var B;it("should escape minus",function(){B="12 - 175";expect(A.escapeRegex(B)).toEqual("12 \\- 175")});it("should escape dot",function(){B="Brian is in the kitchen.";expect(A.escapeRegex(B)).toEqual("Brian is in the kitchen\\.")});it("should escape asterisk",function(){B="12 * 175";expect(A.escapeRegex(B)).toEqual("12 \\* 175")});it("should escape plus",function(){B="12 + 175";expect(A.escapeRegex(B)).toEqual("12 \\+ 175")});it("should escape question mark",function(){B="What else ?";expect(A.escapeRegex(B)).toEqual("What else \\?")});it("should escape caret",function(){B="^^";expect(A.escapeRegex(B)).toEqual("\\^\\^")});it("should escape dollar",function(){B="500$";expect(A.escapeRegex(B)).toEqual("500\\$")});it("should escape open brace",function(){B="something{stupid";expect(A.escapeRegex(B)).toEqual("something\\{stupid")});it("should escape close brace",function(){B="something}stupid";expect(A.escapeRegex(B)).toEqual("something\\}stupid")});it("should escape open bracket",function(){B="something[stupid";expect(A.escapeRegex(B)).toEqual("something\\[stupid")});it("should escape close bracket",function(){B="something]stupid";expect(A.escapeRegex(B)).toEqual("something\\]stupid")});it("should escape open parenthesis",function(){B="something(stupid";expect(A.escapeRegex(B)).toEqual("something\\(stupid")});it("should escape close parenthesis",function(){B="something)stupid";expect(A.escapeRegex(B)).toEqual("something\\)stupid")});it("should escape vertival bar",function(){B="something|stupid";expect(A.escapeRegex(B)).toEqual("something\\|stupid")});it("should escape forward slash",function(){B="something/stupid";expect(A.escapeRegex(B)).toEqual("something\\/stupid")});it("should escape backslash",function(){B="something\\stupid";expect(A.escapeRegex(B)).toEqual("something\\\\stupid")})});describe("htmlEncode",function(){var B;it("should replace ampersands",function(){B="Fish & Chips";expect(A.htmlEncode(B)).toEqual("Fish &amp; Chips")});it("should replace less than",function(){B="Fish > Chips";expect(A.htmlEncode(B)).toEqual("Fish &gt; Chips")});it("should replace greater than",function(){B="Fish < Chips";expect(A.htmlEncode(B)).toEqual("Fish &lt; Chips")});it("should replace double quote",function(){B='Fish " Chips';expect(A.htmlEncode(B)).toEqual("Fish &quot; Chips")});it("should replace apostraphes",function(){B="Fish ' Chips";expect(A.htmlEncode(B)).toEqual("Fish &#39; Chips")});describe("adding character entities",function(){var D="A string with entities: \u00e9\u00dc\u00e7\u00f1\u00b6",C="A string with entities: &egrave;&Uuml;&ccedil;&ntilde;&para;";beforeEach(function(){A.addCharacterEntities({"&Uuml;":"\u00dc","&ccedil;":"\u00e7","&ntilde;":"\u00f1","&egrave;":"\u00e9","&para;":"\u00b6"})});afterEach(function(){A.resetCharacterEntities()});it("should allow extending the character entity set",function(){expect(A.htmlEncode(D)).toBe(C)})})});describe("htmlDecode",function(){var B;it("should replace ampersands",function(){B="Fish &amp; Chips";expect(A.htmlDecode(B)).toEqual("Fish & Chips")});it("should replace less than",function(){B="Fish &gt; Chips";expect(A.htmlDecode(B)).toEqual("Fish > Chips")});it("should replace greater than",function(){B="Fish &lt; Chips";expect(A.htmlDecode(B)).toEqual("Fish < Chips")});it("should replace double quote",function(){B="Fish &quot; Chips";expect(A.htmlDecode(B)).toEqual('Fish " Chips')});it("should replace apostraphes",function(){B="Fish &#39; Chips";expect(A.htmlDecode(B)).toEqual("Fish ' Chips")});describe("adding character entities",function(){var D="A string with entities: \u00e9\u00dc\u00e7\u00f1\u00b6",C="A string with entities: &egrave;&Uuml;&ccedil;&ntilde;&para;";beforeEach(function(){A.addCharacterEntities({"&Uuml;":"\u00dc","&ccedil;":"\u00e7","&ntilde;":"\u00f1","&egrave;":"\u00e9","&para;":"\u00b6"})});afterEach(function(){A.resetCharacterEntities()});it("should allow extending the character entity set",function(){expect(A.htmlDecode(C)).toBe(D)})})});describe("escaping",function(){it("should leave an empty string alone",function(){expect(A.escape("")).toEqual("")});it("should leave a non-empty string without escapable characters alone",function(){expect(A.escape("Ed")).toEqual("Ed")});it("should correctly escape a double backslash",function(){expect(A.escape("\\")).toEqual("\\\\")});it("should correctly escape a single backslash",function(){expect(A.escape("'")).toEqual("\\'")});it("should correctly escape a mixture of escape and non-escape characters",function(){expect(A.escape("'foo\\")).toEqual("\\'foo\\\\")})});describe("formatting",function(){it("should leave a string without format parameters alone",function(){expect(A.format("Ed")).toEqual("Ed")});it("should ignore arguments that don't map to format params",function(){expect(A.format("{0} person",1,123)).toEqual("1 person")});it("should accept several format parameters",function(){expect(A.format("{0} person {1}",1,"came")).toEqual("1 person came")});it("should ignore nonexistent format functions",function(){expect(A.format("{0:foo} {0} person {1}",1,"came")).toEqual("{0:foo} 1 person came")});it("should ignore alphabetic format tokens which end with numerals",function(){expect(A.format("{foo:0} {0} person {1}",1,"came")).toEqual("{foo:0} 1 person came")})});describe("leftPad",function(){it("should pad the left side of an empty string",function(){expect(A.leftPad("",5)).toEqual("     ")});it("should pad the left side of a non-empty string",function(){expect(A.leftPad("Ed",5)).toEqual("   Ed")});it("should not pad a string where the character count already exceeds the pad count",function(){expect(A.leftPad("Abraham",5)).toEqual("Abraham")});it("should allow a custom padding character",function(){expect(A.leftPad("Ed",5,"0")).toEqual("000Ed")})});describe("when toggling between two values",function(){it("should use the first toggle value if the string is not already one of the toggle values",function(){expect(A.toggle("Aaron","Ed","Abe")).toEqual("Ed")});it("should toggle to the second toggle value if the string is currently the first",function(){expect(A.toggle("Ed","Ed","Abe")).toEqual("Abe")});it("should toggle to the first toggle value if the string is currently the second",function(){expect(A.toggle("Abe","Ed","Abe")).toEqual("Ed")})});describe("trimming",function(){it("should not modify an empty string",function(){expect(A.trim("")).toEqual("")});it("should not modify a string with no whitespace",function(){expect(A.trim("Abe")).toEqual("Abe")});it("should trim a whitespace-only string",function(){expect(A.trim("     ")).toEqual("")});it("should trim leading whitespace",function(){expect(A.trim("  Ed")).toEqual("Ed")});it("should trim trailing whitespace",function(){expect(A.trim("Ed   ")).toEqual("Ed")});it("should trim leading and trailing whitespace",function(){expect(A.trim("   Ed  ")).toEqual("Ed")});it("should not trim whitespace between words",function(){expect(A.trim("Fish and chips")).toEqual("Fish and chips");expect(A.trim("   Fish and chips  ")).toEqual("Fish and chips")});it("should trim tabs",function(){expect(A.trim("\tEd")).toEqual("Ed")});it("should trim a mixture of tabs and whitespace",function(){expect(A.trim("\tEd   ")).toEqual("Ed")})});describe("urlAppend",function(){it("should leave the string untouched if the second argument is empty",function(){expect(A.urlAppend("sencha.com")).toEqual("sencha.com")});it("should append a ? if one doesn't exist",function(){expect(A.urlAppend("sencha.com","foo=bar")).toEqual("sencha.com?foo=bar")});it("should append any new values with & if a ? exists",function(){expect(A.urlAppend("sencha.com?x=y","foo=bar")).toEqual("sencha.com?x=y&foo=bar")})});describe("capitalize",function(){it("should handle an empty string",function(){expect(A.capitalize("")).toEqual("")});it("should capitalize the first letter of the string",function(){expect(A.capitalize("open")).toEqual("Open")});it("should leave the first letter capitalized if it is already capitalized",function(){expect(A.capitalize("Closed")).toEqual("Closed")});it("should capitalize a single letter",function(){expect(A.capitalize("a")).toEqual("A")});it("should capitalize even when spaces are included",function(){expect(A.capitalize("this is a sentence")).toEqual("This is a sentence")})});describe("uncapitalize",function(){it("should handle an empty string",function(){expect(A.uncapitalize("")).toEqual("")});it("should uncapitalize the first letter of the string",function(){expect(A.uncapitalize("Foo")).toEqual("foo")});it("should ignore case in the rest of the string",function(){expect(A.uncapitalize("FooBar")).toEqual("fooBar")});it("should leave the first letter uncapitalized if it is already uncapitalized",function(){expect(A.uncapitalize("fooBar")).toEqual("fooBar")});it("should uncapitalize a single letter",function(){expect(A.uncapitalize("F")).toEqual("f")});it("should uncapitalize even when spaces are included",function(){expect(A.uncapitalize("This is a sentence")).toEqual("this is a sentence")})});describe("repeat",function(){it("should return an empty string if count == 0",function(){expect(A.repeat("an ordinary string",0)).toBe("")});it("should return an empty string if the count is < 0",function(){expect(A.repeat("an ordinary string",-1)).toBe("")});it("should repeat the pattern as many times as required using the specified separator",function(){expect(A.repeat("an ordinary string",1,"/")).toBe("an ordinary string");expect(A.repeat("an ordinary string",2,"&")).toBe("an ordinary string&an ordinary string");expect(A.repeat("an ordinary string",3,"%")).toBe("an ordinary string%an ordinary string%an ordinary string")});it("should concatenate the repetitions if no separator is specified",function(){expect(A.repeat("foo",3)).toBe("foofoofoo");expect(A.repeat("bar baz",3)).toBe("bar bazbar bazbar baz")})});describe("splitWords",function(){it("should handle no args",function(){var B=A.splitWords();expect(Ext.encode(B)).toEqual("[]")});it("should handle null",function(){var B=A.splitWords(null);expect(Ext.encode(B)).toEqual("[]")});it("should handle an empty string",function(){var B=A.splitWords("");expect(Ext.encode(B)).toEqual("[]")});it("should handle one trimmed word",function(){var B=A.splitWords("foo");expect(Ext.encode(B)).toEqual('["foo"]')});it("should handle one word with spaces around it",function(){var B=A.splitWords(" foo ");expect(Ext.encode(B)).toEqual('["foo"]')});it("should handle two trimmed words",function(){var B=A.splitWords("foo bar");expect(Ext.encode(B)).toEqual('["foo","bar"]')});it("should handle two untrimmed words",function(){var B=A.splitWords("  foo  bar  ");expect(Ext.encode(B)).toEqual('["foo","bar"]')});it("should handle five trimmed words",function(){var B=A.splitWords("foo bar bif boo foobar");expect(Ext.encode(B)).toEqual('["foo","bar","bif","boo","foobar"]')});it("should handle five untrimmed words",function(){var B=A.splitWords(" foo   bar   bif   boo  foobar    \t");expect(Ext.encode(B)).toEqual('["foo","bar","bif","boo","foobar"]')})});describe("insert",function(){describe("undefined/null/empty values",function(){it("should handle an undefined original string",function(){expect(A.insert(undefined,"foo",0)).toBe("foo")});it("should handle a null original string",function(){expect(A.insert(null,"foo",0)).toBe("foo")});it("should handle an empty original string",function(){expect(A.insert("","foo",0)).toBe("foo")});it("should handle an undefined substring",function(){expect(A.insert("foo",undefined)).toBe("foo")});it("should handle a null substring",function(){expect(A.insert("foo",null)).toBe("foo")});it("should handle an empty substring",function(){expect(A.insert("foo","")).toBe("foo")})});describe("index",function(){describe("invalid indexes",function(){it("should default the index to the end of the string",function(){expect(A.insert("foo","bar")).toBe("foobar")});it("should put any negative index greater than the length at the start",function(){expect(A.insert("foo","bar",-100)).toBe("barfoo")});it("should put any index greater than the string length at the end",function(){expect(A.insert("foo","bar",100)).toBe("foobar")})});describe("valid index",function(){it("should insert at the start with 0 index",function(){expect(A.insert("foo","bar",0)).toBe("barfoo")});it("should insert at the end with index = len",function(){expect(A.insert("foo","bar",3)).toBe("foobar")});it("should insert at the start with index = -len",function(){expect(A.insert("foo","bar",-3)).toBe("barfoo")});it("should insert at the before the last character",function(){expect(A.insert("foo","bar",2)).toBe("fobaro")});it("should insert after the first character",function(){expect(A.insert("foo","bar",1)).toBe("fbaroo")});it("should insert before the last character (negative index)",function(){expect(A.insert("foo","bar",-1)).toBe("fobaro")});it("should insert after the first character (negative index)",function(){expect(A.insert("foo","bar",-2)).toBe("fbaroo")})})})});describe("startsWith",function(){describe("invalid params",function(){it("should return false when the original string is null",function(){expect(A.startsWith(null,"")).toBe(false)});it("should return false when the original string is undefined",function(){expect(A.startsWith(undefined,"")).toBe(false)});it("should return false when the substring is null",function(){expect(A.startsWith("",null)).toBe(false)});it("should return false when the substring is longer than the string",function(){expect(A.startsWith("a","foo")).toBe(false)})});describe("mixed strings",function(){it("should return true when both strings are empty",function(){expect(A.startsWith("","")).toBe(true)});it("should return true when the substring is empty",function(){expect(A.endsWith("foo","")).toBe(true)});it("should return true when both strings are the same",function(){expect(A.startsWith("foo","foo")).toBe(true)});it("should return true when the substring is at the start of the string",function(){expect(A.startsWith("foobar","foo")).toBe(true)});it("should return true when the substring is at the start and in other places",function(){expect(A.startsWith("foobarfoo","foo")).toBe(true)});it("should return false when the substring appears in the middle of the string",function(){expect(A.startsWith("foobarbaz","bar")).toBe(false)});it("should return false when the substring appears at the end of the string",function(){expect(A.startsWith("foobarbaz","baz")).toBe(false)})});describe("ignoreCase",function(){it("should match when both are lower case",function(){expect(A.startsWith("foobarbaz","foo",true)).toBe(true)});it("should match when both are upper case",function(){expect(A.startsWith("FOOBARBAZ","FOO",true)).toBe(true)});it("should match when the original is upper, substring is lower",function(){expect(A.startsWith("FOOBARBAZ","foo",true)).toBe(true)});it("should match when the original is lower, substring is upper",function(){expect(A.startsWith("foobarbaz","FOO",true)).toBe(true)});it("should match with mixed case",function(){expect(A.startsWith("fOobarbaz","FoO",true)).toBe(true)})})});describe("endsWith",function(){describe("invalid params",function(){it("should return false when the original string is null",function(){expect(A.endsWith(null,"")).toBe(false)});it("should return false when the original string is undefined",function(){expect(A.endsWith(undefined,"")).toBe(false)});it("should return false when the substring is null",function(){expect(A.endsWith("",null)).toBe(false)});it("should return false when the substring is longer than the string",function(){expect(A.endsWith("a","foo")).toBe(false)})});describe("mixed strings",function(){it("should return true when both strings are empty",function(){expect(A.endsWith("","")).toBe(true)});it("should return true when the substring is empty",function(){expect(A.endsWith("foo","")).toBe(true)});it("should return true when both strings are the same",function(){expect(A.endsWith("foo","foo")).toBe(true)});it("should return true when the substring is at the end of the string",function(){expect(A.endsWith("foobar","bar")).toBe(true)});it("should return true when the substring is at the end and in other places",function(){expect(A.endsWith("foobarfoo","foo")).toBe(true)});it("should return false when the substring appears in the middle of the string",function(){expect(A.endsWith("foobarbaz","bar")).toBe(false)});it("should return false when the substring appears at the start of the string",function(){expect(A.endsWith("foobarbaz","foo")).toBe(false)})});describe("ignoreCase",function(){it("should match when both are lower case",function(){expect(A.endsWith("foobarbaz","baz",true)).toBe(true)});it("should match when both are upper case",function(){expect(A.endsWith("FOOBARBAZ","BAZ",true)).toBe(true)});it("should match when the original is upper, substring is lower",function(){expect(A.endsWith("FOOBARBAZ","baz",true)).toBe(true)});it("should match when the original is lower, substring is upper",function(){expect(A.endsWith("foobarbaz","BAZ",true)).toBe(true)});it("should match with mixed case",function(){expect(A.endsWith("foobarbAz","BaZ",true)).toBe(true)})})})})