describe("Ext.layout.container.Anchor",function(){function A(C){var B="Ext.layout.container.Anchor";if(C){B+=" (shrinkWrap:true)"}describe(B,function(){var K="Lorem ipsum dolor sit amet",H="Lorem ipsum",I="supercalifragilisticexpialidocious",G=Ext.getScrollbarSize(),F=G.width,J=G.height,E;function D(N,M){var L=[];if(!Ext.isArray(M)){M=[M]}Ext.each(M,function(O){L.push(Ext.apply({xtype:"component",style:"margin: 4px; line-height: 20px;"},O))});E=Ext.widget(Ext.apply({renderTo:document.body,xtype:"panel",shrinkWrap:C||2,layout:"anchor",bodyPadding:"6",items:L},N))}afterEach(function(){E.destroy()});describe("configured width and height",function(){var L={height:100,width:100};describe("anchoring items using percentages",function(){beforeEach(function(){D(L,[{anchor:"100%, 50%"},{anchor:"50%, 50%"}])});it("should width the items correctly",function(){expect(E.items.getAt(0).getWidth()).toBe(78);expect(E.items.getAt(1).getWidth()).toBe(35)});it("should height the items correctly",function(){expect(E.items.getAt(0).getHeight()).toBe(35);expect(E.items.getAt(1).getHeight()).toBe(35)})});describe("anchoring items using offsets",function(){beforeEach(function(){D(L,[{anchor:"0",height:37},{anchor:"-43",height:37}])});it("should width the items correctly",function(){expect(E.items.getAt(0).getWidth()).toBe(78);expect(E.items.getAt(1).getWidth()).toBe(35)});it("should height the items correctly",function(){expect(E.items.getAt(0).getHeight()).toBe(37);expect(E.items.getAt(1).getHeight()).toBe(37)})});describe("naturally widthed child with long text",function(){beforeEach(function(){D(L,{html:K})});it("should wrap the text",function(){expect(E.child().getHeight()).toBe(40)});it("should not crush the text",function(){expect(E.child().getWidth()).toBe(78)})});describe("naturally widthed child with short text",function(){beforeEach(function(){D(L,{html:H})});it("should not wrap the text",function(){expect(E.child().getHeight()).toBe(20)});it("should naturally width the child",function(){expect(E.child().getWidth()).toBe(78)})});describe("naturally widthed child with long word",function(){beforeEach(function(){D(L,{html:H+" "+I})});it("should wrap the text",function(){expect(E.child().getHeight()).toBe(40)});it("should not allow the child's width to expand beyond the container",function(){expect(E.child().getWidth()).toBe(78)})});describe("naturally widthed child without text",function(){beforeEach(function(){D(L,{height:20})});it("should natuarally width the child",function(){expect(E.child().getWidth()).toBe(78)})});xdescribe("shrink wrapped child",function(){beforeEach(function(){D(L,{xtype:"panel",shrinkWrap:3,html:'<div style="width:20px;height:20px;"></div>'})});it("should not alter the width of the child",function(){expect(E.child().getWidth()).toBe(20)});it("should not alter the height of the child",function(){expect(E.child().getHeight()).toBe(20)})});describe("overflow",function(){describe("overflow x and y auto",function(){var M=Ext.apply({},{autoScroll:true},L);describe("large vertical, no horizontal",function(){beforeEach(function(){D(M,{anchor:"-2",height:180})});it("should have the correct scroll height",function(){expect(E.body.dom.scrollHeight).toBe(200)});it("should not have a horizontal scrollbar",function(){expect(E.body.dom.clientHeight).toBe(98)});it("should adjust anchor for scrollbar width",function(){expect(E.child().getWidth()).toBe(76-F)})});describe("small vertical, no horizontal",function(){beforeEach(function(){D(M,{anchor:"-2",height:79})});var N=Ext.isIE9m&&!C?xit:it;N("should have the correct scroll height",function(){expect(E.body.dom.scrollHeight).toBe(99)});it("should not have a horizontal scrollbar",function(){expect(E.body.dom.clientHeight).toBe(98)});it("should adjust anchor for scrollbar width",function(){expect(E.child().getWidth()).toBe(76-F)})});describe("large horizontal, no vertical",function(){beforeEach(function(){D(M,{height:20,width:180})});var N=Ext.isIE9?xit:it;it("should have the correct scroll width",function(){expect(E.body.dom.scrollWidth).toBe(200)});N("should not have a vertical scrollbar",function(){expect(E.body.dom.clientWidth).toBe(98)})});describe("small horizontal, no vertical",function(){beforeEach(function(){D(M,{height:20,width:79})});var N=Ext.isIE9m?xit:it;it("should have the correct scroll width",function(){expect(E.body.dom.scrollWidth).toBe(99)});N("should not have a vertical scrollbar",function(){expect(E.body.dom.clientWidth).toBe(98)})});describe("large vertical, large horizontal",function(){beforeEach(function(){D(M,{height:180,width:180})});it("should have the correct scroll height",function(){expect(E.body.dom.scrollHeight).toBe(200)});it("should have the correct scroll width",function(){expect(E.body.dom.scrollWidth).toBe(200)})});describe("large vertical, small horizontal",function(){beforeEach(function(){D(M,{height:180,width:79-F})});it("should have the correct scroll height",function(){expect(E.body.dom.scrollHeight).toBe(200)});it("should have the correct scroll width",function(){expect(E.body.dom.scrollWidth).toBe(99-F)})});describe("small vertical, large horizontal",function(){beforeEach(function(){D(M,{height:79-F,width:180})});it("should have the correct scroll height",function(){expect(E.body.dom.scrollHeight).toBe(99-F)});it("should have the correct scroll width",function(){expect(E.body.dom.scrollWidth).toBe(200)})})});describe("overflow x auto, overflow y scroll",function(){var M=Ext.apply({},{style:"overflow-x:auto;overflow-y:scroll;"},L)});describe("overflow x scroll, overflow y auto",function(){var M=Ext.apply({},{style:"overflow-x:scroll;overflow-y:auto;"},L)});describe("overflow x and y scroll",function(){var M=Ext.apply({},{style:"overflow:scroll;"},L)})});describe("percentage sized children",function(){describe("overflow hidden",function(){beforeEach(function(){D(L,{style:"height: 50%; width: 50%;"})});it("should width the child correctly",function(){expect(E.child().getWidth()).toBe(43)});it("should height the child correctly",function(){expect(E.child().getHeight()).toBe(43)})});describe("overflow auto",function(){var M=Ext.apply({},{style:"overflow:scroll;"},L);beforeEach(function(){D(M,{style:"height: 50%; width: 50%;"})});it("should width the child correctly",function(){expect(E.child().getWidth()).toBe(43)});it("should height the child correctly",function(){expect(E.child().getHeight()).toBe(43)})})});describe("autoScroll with no scrollbars",function(){var M=Ext.apply({},{autoScroll:true},L);beforeEach(function(){D(M,[{anchor:"100% 100%"}])});it("should not reserve space for a vertical scrollbar when sizing the child",function(){expect(E.items.getAt(0).getWidth()).toBe(78)});it("should not reserve space for a horizontal scrollbar when sizing the child",function(){expect(E.items.getAt(0).getHeight()).toBe(78)})})});describe("configured height, shrink wrap width",function(){var L={height:100,shrinkWrap:1};describe("anchoring items using percentages",function(){beforeEach(function(){D(L,[{anchor:"100%, 50%",html:'<div style="width:78px"></div>'},{anchor:"50%, 50%"}])});xit("should shrink wrap to the width of the widest child item",function(){expect(E.getWidth()).toBe(100);expect(E.items.getAt(0).getWidth()).toBe(80);expect(E.items.getAt(1).getWidth()).toBe(80)});it("should height the items correctly",function(){expect(E.items.getAt(0).getHeight()).toBe(35);expect(E.items.getAt(1).getHeight()).toBe(35)})});describe("anchoring items using offsets",function(){beforeEach(function(){D(L,[{anchor:"0",height:37,html:'<div style="width:78px"></div>'},{anchor:"-43",height:37}])});xit("should shrink wrap to the width of the widest child item",function(){expect(E.getWidth()).toBe(100);expect(E.items.getAt(0).getWidth()).toBe(78);expect(E.items.getAt(1).getWidth()).toBe(78)});it("should height the items correctly",function(){expect(E.items.getAt(0).getHeight()).toBe(37);expect(E.items.getAt(1).getHeight()).toBe(37)})});describe("auto width child with text",function(){beforeEach(function(){D(L,{html:K,height:20})});it("should not wrap the text",function(){expect(E.child().getHeight()).toBe(20)});it("should shrink wrap the width",function(){expect(E.getWidth()).toBe(E.child().getWidth()+22)})});describe("overflow",function(){var M=Ext.apply({},{bodyStyle:"overflow:auto;"},L);describe("vertical",function(){beforeEach(function(){D(M,{anchor:"0",height:180,html:'<div style="width:80px;"></div>'})});it("should have the correct scroll height",function(){expect(E.body.dom.scrollHeight).toBe(200)});xit("should shrink wrap the width",function(){expect(E.getWidth()).toBe(100)});xit("should not have horizontal overflow",function(){expect(E.body.dom.scrollWidth).toBe(E.getWidth()-F)})})})});describe("configured width, shrink wrap height",function(){var L={width:100};describe("anchoring items using percentages",function(){beforeEach(function(){D(L,[{anchor:"100%",height:37},{anchor:"50%",height:37}])});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(100)});it("should width the items correctly",function(){expect(E.items.getAt(0).getWidth()).toBe(78);expect(E.items.getAt(1).getWidth()).toBe(35)});it("should height the items correctly",function(){expect(E.items.getAt(0).getHeight()).toBe(37);expect(E.items.getAt(1).getHeight()).toBe(37)})});describe("anchoring items using offsets",function(){beforeEach(function(){D(L,[{anchor:"0",height:37},{anchor:"-43",height:37}])});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(100)});it("should width the items correctly",function(){expect(E.items.getAt(0).getWidth()).toBe(78);expect(E.items.getAt(1).getWidth()).toBe(35)});it("should height the items correctly",function(){expect(E.items.getAt(0).getHeight()).toBe(37);expect(E.items.getAt(1).getHeight()).toBe(37)})});describe("naturally widthed child with long text",function(){beforeEach(function(){D(L,{html:K})});it("should wrap the text",function(){expect(E.child().getHeight()).toBe(40)});it("should not crush the text",function(){expect(E.child().getWidth()).toBe(78)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(62)})});describe("naturally widthed child with short text",function(){beforeEach(function(){D(L,{html:H})});it("should not wrap the text",function(){expect(E.child().getHeight()).toBe(20)});it("should naturally width the child",function(){expect(E.child().getWidth()).toBe(78)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(42)})});describe("naturally widthed child with long word",function(){beforeEach(function(){D(L,{html:H+" "+I})});it("should wrap the text",function(){expect(E.child().getHeight()).toBe(40)});it("should not allow the child's width to expand beyond the container",function(){expect(E.child().getWidth()).toBe(78)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(62)})});describe("naturally widthed child without text",function(){beforeEach(function(){D(L,{height:20})});it("should naturally width the child",function(){expect(E.child().getWidth()).toBe(78)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(42)})});xdescribe("shrink wrapped child",function(){beforeEach(function(){D(L,{xtype:"panel",shrinkWrap:3,html:'<div style="width:20px;height:20px;"></div>'})});it("should not alter the width of the child",function(){expect(E.child().getWidth()).toBe(20)});it("should not alter the height of the child",function(){expect(E.child().getHeight()).toBe(20)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(40)})});describe("overflow",function(){var M=Ext.apply({},{bodyStyle:"overflow:auto;"},L);describe("horizontal",function(){beforeEach(function(){D(M,{height:78-J,width:180})});it("should have the correct scroll width",function(){expect(E.body.dom.scrollWidth).toBe(200)});xit("should shrink wrap the height",function(){expect(E.getHeight()).toBe(100)});xit("should not have vertical overflow",function(){expect(E.body.dom.scrollHeight).toBe(E.getHeight()-J)})})})});describe("shrink wrap width and height",function(){var L={shrinkWrap:3};describe("anchoring items using percentages",function(){beforeEach(function(){D(L,[{anchor:"100%, 50%",html:'<div style="width:40px;height:20px;"></div>'},{anchor:"50%, 50%",html:'<div style="width:20px;height:20px;"></div>'}])});it("should shrink wrap to the width of the widest item",function(){expect(E.getWidth()).toBe(62);expect(E.items.getAt(0).getWidth()).toBe(40);expect(E.items.getAt(1).getWidth()).toBe(40)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(66);expect(E.items.getAt(0).getHeight()).toBe(20);expect(E.items.getAt(1).getHeight()).toBe(20)})});describe("auto width child with text",function(){beforeEach(function(){D(L,{html:K,height:20})});it("should not wrap the text",function(){expect(E.child().getHeight()).toBe(20)});it("should shrink wrap the width",function(){expect(E.getWidth()).toBe(E.child().getWidth()+22)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(42)})});describe("child with configured width",function(){beforeEach(function(){D(L,{width:78,height:78})});it("should shrink wrap the width",function(){expect(E.getWidth()).toBe(100)});it("should not alter the width of the child",function(){expect(E.child().getWidth()).toBe(78)});it("should shrink wrap the height",function(){expect(E.getHeight()).toBe(100)});it("should not alter the height of the child",function(){expect(E.child().getHeight()).toBe(78)})})});xdescribe("stretching",function(){var P,M={xtype:"form",layout:"absolute",defaultType:"textfield",items:[{x:0,y:5,xtype:"label",text:"From:"},{x:55,y:0,name:"from",hideLabel:true,anchor:"100%"},{x:0,y:32,xtype:"label",text:"To:"},{x:55,y:27,xtype:"button",text:"Contacts..."},{x:127,y:27,name:"to",hideLabel:true,anchor:"100%"},{x:0,y:59,xtype:"label",text:"Subject:"},{x:55,y:54,name:"subject",hideLabel:true,anchor:"100%"}]},L={xtype:"form",layout:"anchor",defaultType:"displayfield",defaults:{style:{border:"solid red 1px"}},items:[{value:"a fairly long lable value",anchor:"100%",minWidth:150},{value:"a label",anchor:"100%"},{value:"a",anchor:"100%"}]},Q={xtype:"form",layout:"absolute",defaultType:"displayfield",defaults:{style:{border:"solid red 1px"}},items:[{x:0,y:0,value:"a fairly long lable value",minWidth:150},{x:0,y:30,value:"a label"},{x:0,y:60,value:"a"},{x:0,y:90,value:["a","b","c","d"].join("<br>"),anchor:"-30 100%"},{x:30,y:90,width:30,height:200,value:["a","b","c","d","e","f","g","h"].join("<br>"),anchor:"100% 100%"}]},R=function(S){return S.items.items},O,N=function(){return(Ext.failedLayouts||0)-P};beforeEach(function(){Ext.define("AnchorTest.StretchPanel",{extend:"Ext.container.Container",xtype:"stretchpanel",shrinkWrap:3,layout:{type:"table",columns:1},initComponent:function(){if(this.columns){this.layout=Ext.apply(this.layout,{columns:this.columns})}this.callParent()}});P=(Ext.failedLayouts||0)});afterEach(function(){Ext.undefine("AnchorTest.StretchPanel");if(O){O.destroy();O=null}});describe("shrinkWrap",function(){it("should not cause layout failures when shrinkWrapped",function(){O=Ext.ComponentManager.create({renderTo:Ext.getBody(),xtype:"stretchpanel",items:[M]});expect(N()).toBe(0)});it("should shrinkWrap horizontally",function(){O=Ext.ComponentManager.create({renderTo:Ext.getBody(),xtype:"stretchpanel",items:[L]});expect(O.getWidth()).toBe(150)});it("should stretchMax components horizontally when shrinkWrapped",function(){O=Ext.ComponentManager.create({renderTo:Ext.getBody(),xtype:"stretchpanel",items:[L]});var S=R(O);expect(N()).toBe(0);expect(O.getWidth()).toBe(150);expect(R(S[0])[0].getWidth()).toBe(150);expect(R(S[0])[1].getWidth()).toBe(150);expect(R(S[0])[2].getWidth()).toBe(150)});it("should shrinkWrap vertically",function(){O=Ext.ComponentManager.create({renderTo:Ext.getBody(),xtype:"stretchpanel",items:[Q]});expect(N()).toBe(0);expect(O.getWidth()).toBe(150);expect(O.getHeight()).toBe(290)});it("should stretchMax compnents vertically when shrinkWrapped",function(){O=Ext.ComponentManager.create({renderTo:Ext.getBody(),xtype:"stretchpanel",items:[Q]});var S=R(O);expect(N()).toBe(0);expect(O.getWidth()).toBe(150);expect(O.getHeight()).toBe(290);expect(R(S[0])[0].getWidth()).toBe(150);expect(R(S[0])[1].getWidth()).toBe(150);expect(R(S[0])[2].getWidth()).toBe(150);expect(R(S[0])[3].getHeight()).toBe(200);expect(R(S[0])[4].getHeight()).toBe(200)})})})})}A();A(true);describe("layout failures",function(){it("should work with a minHeight child",function(){var B=new Ext.container.Container({renderTo:Ext.getBody(),width:200,height:100,layout:"anchor",items:[{minHeight:70}]});expect(B.items.first().getHeight()).toBe(70);B.destroy()})});it("should shrinkwrap height correctly when it contains both liquidLayout and non-liquidLayout items",function(){var B=Ext.widget({renderTo:document.body,xtype:"form",id:"main-form",width:400,bodyPadding:5,defaults:{anchor:"100%"},items:[{xtype:"fieldcontainer",layout:"hbox",items:[{xtype:"component",flex:1,style:"height: 50px; background-color: green;",html:"&nbsp;"}]},{xtype:"textareafield",height:100,margin:"0",allowBlank:false}]});expect(B.getHeight()).toBe(167);B.destroy()})})