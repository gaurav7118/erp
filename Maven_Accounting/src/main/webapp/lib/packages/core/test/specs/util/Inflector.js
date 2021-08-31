describe("Ext.util.Inflector",function(){var B=Ext.util.Inflector;var A={bus:"buses",word:"words",tomato:"tomatoes",potato:"potatoes",person:"people",alumnus:"alumni",cactus:"cacti",focus:"foci",nucleus:"nuclei",radius:"radii",stimulus:"stimuli",axis:"axes",analysis:"analyses",basis:"bases",crisis:"crises",diagnosis:"diagnoses",ellipsis:"ellipses",hypothesis:"hypotheses",oasis:"oases",paralysis:"paralyses",parenthesis:"parentheses",synthesis:"syntheses",synopsis:"synopses",thesis:"theses",appendix:"appendices",index:"indexes",matrix:"matrices",beau:"beaux",bureau:"bureaux",tableau:"tableaux",child:"children",man:"men",ox:"oxen",woman:"women",bacterium:"bacteria",corpus:"corpora",criterion:"criteria",curriculum:"curricula",datum:"data",genus:"genera",medium:"media",memorandum:"memoranda",phenomenon:"phenomena",stratum:"strata",deer:"deer",fish:"fish",means:"means",offspring:"offspring",series:"series",sheep:"sheep",species:"species",foot:"feet",goose:"geese",tooth:"teeth",antenna:"antennae",formula:"formulae",nebula:"nebulae",vertebra:"vertebrae",vita:"vitae",louse:"lice",mouse:"mice"};describe("pluralizing words",function(){describe("normal words",function(){it("should pluralize the word correctly",function(){for(var C in A){expect(B.pluralize(C)).toEqual(A[C])}})});describe("uncountable words",function(){it("should return the same word",function(){expect(B.pluralize("sheep")).toEqual("sheep")})})});describe("clearing existing pluralizations",function(){var C;beforeEach(function(){C=B.plurals});afterEach(function(){B.plurals=C});it("should remove all singular rule definitions",function(){B.clearPlurals();expect(B.plurals.length).toEqual(0)})});describe("clearing existing singularizations",function(){var C;beforeEach(function(){C=B.singulars});afterEach(function(){B.singulars=C});it("should remove all singular rule definitions",function(){B.clearSingulars();expect(B.singulars.length).toEqual(0)})});describe("adding pluralizations",function(){it("should add to the plurals array",function(){var C=B.plurals.length;B.plural(/^(ox)$/,"$1");expect(B.plurals.length).toEqual(C+1)});it("should recognize the new pluralization correctly",function(){var C=B.plurals;B.plurals=[];expect(B.pluralize("ox")).toEqual("ox");B.plural(/^(ox)$/,"$1en");expect(B.pluralize("ox")).toEqual("oxen");B.plurals=C})});describe("adding singularizations",function(){it("should add to the singulars array",function(){var C=B.singulars.length;B.singular(/^(ox)en$/,"$1");expect(B.singulars.length).toEqual(C+1)});it("should recognize the new singularization correctly",function(){var C=B.singulars;B.singulars=[];expect(B.singularize("oxen")).toEqual("oxen");B.singular(/^(ox)en$/,"$1");expect(B.singularize("oxen")).toEqual("ox");B.singulars=C})});describe("singularizing words",function(){describe("normal words",function(){it("should singularize the word correctly",function(){for(var C in A){expect(B.singularize(A[C])).toEqual(C)}})});describe("uncountable words",function(){it("should return the same word",function(){expect(B.singularize("sheep")).toEqual("sheep")})})});describe("classifying words",function(){var C;beforeEach(function(){C=["user","users","User","Users"]});it("should correctly classify",function(){Ext.each(C,function(D){expect(B.classify(D)).toEqual("User")},this)})});describe("uncountable words",function(){it("should be detected",function(){expect(B.isTransnumeral("sheep")).toEqual(true)});it("should not return false positives",function(){expect(B.isTransnumeral("person")).toEqual(false)})});describe("ordinalizing numbers",function(){it("should add st to numbers ending in 1",function(){expect(B.ordinalize(21)).toEqual("21st")});it("should add nd to numbers ending in 2",function(){expect(B.ordinalize(22)).toEqual("22nd")});it("should add rd to numbers ending in 3",function(){expect(B.ordinalize(23)).toEqual("23rd")});it("should add th to all other numbers",function(){expect(B.ordinalize(24)).toEqual("24th")});it("should add th to all early teens",function(){expect(B.ordinalize(11)).toEqual("11th");expect(B.ordinalize(12)).toEqual("12th");expect(B.ordinalize(13)).toEqual("13th")})})})