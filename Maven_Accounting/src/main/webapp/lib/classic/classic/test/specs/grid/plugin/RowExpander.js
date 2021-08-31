describe("Ext.grid.plugin.RowExpander",function(){var B=[["3m Co",71.72,0.02,0.03,"9/1 12:00am","Manufacturing"],["Alcoa Inc",29.01,0.42,1.47,"9/1 12:00am","Manufacturing"],["Altria Group Inc",83.81,0.28,0.34,"9/1 12:00am","Manufacturing"],["American Express Company",52.55,0.01,0.02,"9/1 12:00am","Finance"],["American International Group, Inc.",64.13,0.31,0.49,"9/1 12:00am","Services"],["AT&T Inc.",31.61,-0.48,-1.54,"9/1 12:00am","Services"],["Boeing Co.",75.43,0.53,0.71,"9/1 12:00am","Manufacturing"],["Caterpillar Inc.",67.27,0.92,1.39,"9/1 12:00am","Services"],["Citigroup, Inc.",49.37,0.02,0.04,"9/1 12:00am","Finance"],["E.I. du Pont de Nemours and Company",40.48,0.51,1.28,"9/1 12:00am","Manufacturing"],["Exxon Mobil Corp",68.1,-0.43,-0.64,"9/1 12:00am","Manufacturing"],["General Electric Company",34.14,-0.08,-0.23,"9/1 12:00am","Manufacturing"],["General Motors Corporation",30.27,1.09,3.74,"9/1 12:00am","Automotive"],["Hewlett-Packard Co.",36.53,-0.03,-0.08,"9/1 12:00am","Computer"],["Honeywell Intl Inc",38.77,0.05,0.13,"9/1 12:00am","Manufacturing"],["Intel Corporation",19.88,0.31,1.58,"9/1 12:00am","Computer"],["International Business Machines",81.41,0.44,0.54,"9/1 12:00am","Computer"],["Johnson & Johnson",64.72,0.06,0.09,"9/1 12:00am","Medical"],["JP Morgan & Chase & Co",45.73,0.07,0.15,"9/1 12:00am","Finance"],["McDonald's Corporation",36.76,0.86,2.4,"9/1 12:00am","Food"],["Merck & Co., Inc.",40.96,0.41,1.01,"9/1 12:00am","Medical"],["Microsoft Corporation",25.84,0.14,0.54,"9/1 12:00am","Computer"],["Pfizer Inc",27.96,0.4,1.45,"9/1 12:00am","Services","Medical"],["The Coca-Cola Company",45.07,0.26,0.58,"9/1 12:00am","Food"],["The Home Depot, Inc.",34.64,0.35,1.02,"9/1 12:00am","Retail"],["The Procter & Gamble Company",61.91,0.01,0.02,"9/1 12:00am","Manufacturing"],["United Technologies Corporation",63.26,0.55,0.88,"9/1 12:00am","Computer"],["Verizon Communications",35.57,0.39,1.11,"9/1 12:00am","Services"],["Wal-Mart Stores, Inc.",45.45,0.73,1.63,"9/1 12:00am","Retail"],["Walt Disney Company (The) (Holding Company)",29.89,0.24,0.81,"9/1 12:00am","Services"]],G,I,A,F,H,C,D;for(D=0;D<B.length;D++){B[D].push("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Sed metus nibh, sodales a, porta at, vulputate eget, dui. Pellentesque ut nisl. Maecenas tortor turpis, interdum non, sodales non, iaculis ac, lacus. Vestibulum auctor, tortor quis iaculis malesuada, libero lectus bibendum purus, sit amet tincidunt quam turpis vel lacus. In pellentesque nisl non sem. Suspendisse nunc sem, pretium eget, cursus a, fringilla vel, urna.<br/><br/>Aliquam commodo ullamcorper erat. Nullam vel justo in neque porttitor laoreet. Aenean lacus dui, consequat eu, adipiscing eget, nonummy non, nisi. Morbi nunc est, dignissim non, ornare sed, luctus eu, massa. Vivamus eget quam. Vivamus tincidunt diam nec urna. Curabitur velit.")}function E(K,J){K=K||{};Ext.define("spec.RowExpanderCompany",{extend:"Ext.data.Model",fields:[{name:"company"},{name:"price",type:"float"},{name:"change",type:"float"},{name:"pctChange",type:"float"},{name:"lastChange",type:"date",dateFormat:"n/j h:ia"},{name:"industry"},{name:"rating",type:"int",convert:function(N,L){var M=L.get("pctChange");if(M<0){return 2}if(M<1){return 1}return 0}}]});G=new Ext.data.Store({model:"spec.RowExpanderCompany",data:B,autoDestroy:true});I=new Ext.grid.plugin.RowExpander(Ext.apply({rowBodyTpl:new Ext.XTemplate("<p><b>Company:</b> {company}</p>","<p><b>Change:</b> {change:this.formatChange}</p><br>","<p><b>Summary:</b> {desc}</p>",{formatChange:function(M){var L=M>=0?"green":"red";return'<span style="color: '+L+';">'+Ext.util.Format.usMoney(M)+"</span>"}})},J||{}));C=K.columns||[{text:"Company",flex:1,dataIndex:"company"},{text:"Price",renderer:Ext.util.Format.usMoney,dataIndex:"price"},{text:"Change",dataIndex:"change"},{text:"% Change",dataIndex:"pctChange"},{text:"Last Updated",renderer:Ext.util.Format.dateRenderer("m/d/Y"),dataIndex:"lastChange"}];A=new Ext.grid.Panel(Ext.apply({store:G,columns:C,viewConfig:{forceFit:true},width:600,height:300,plugins:I,title:"Expander Rows, Collapse and Force Fit",renderTo:document.body},K)),F=A.getView(),H=F.bufferedRenderer}afterEach(function(){Ext.destroy(A);G=I=A=C=null;Ext.undefine("spec.RowExpanderCompany");Ext.data.Model.schema.clear()});describe("RowExpander",function(){it("should not expand in response to mousedown",function(){E();jasmine.fireMouseEvent(A.view.el.query(".x-grid-row-expander")[0],"mousedown");expect(A.view.all.item(0).down("."+Ext.baseCSSPrefix+"grid-rowbody-tr").isVisible()).toBe(false)});it("should expand on click",function(){E();jasmine.fireMouseEvent(A.view.el.query(".x-grid-row-expander")[0],"click");expect(A.view.all.item(0).down("."+Ext.baseCSSPrefix+"grid-rowbody-tr").isVisible()).toBe(true)});it("should collapse on click",function(){E();I.toggleRow(0,G.getAt(0));jasmine.fireMouseEvent(A.view.el.query(".x-grid-row-expander")[0],"click");expect(A.view.all.item(0).down("."+Ext.baseCSSPrefix+"grid-rowbody-tr").isVisible()).toBe(false)});describe("with a lockedTpl",function(){beforeEach(function(){E({columns:[{text:"Company",width:200,dataIndex:"company",locked:true},{text:"Price",renderer:Ext.util.Format.usMoney,dataIndex:"price"},{text:"Change",dataIndex:"change"},{text:"% Change",dataIndex:"pctChange"},{text:"Last Updated",renderer:Ext.util.Format.dateRenderer("m/d/Y"),dataIndex:"lastChange"}]},{rowBodyTpl:new Ext.XTemplate("<p><b>Company:</b> {company}</p>","<p><b>Change:</b> {change:this.formatChange}</p><br>","<p><b>Summary:</b> {desc}</p>",{formatChange:function(K){var J=K>=0?"green":"red";return'<span style="color: '+J+';">'+Ext.util.Format.usMoney(K)+"</span>"}}),lockedTpl:new Ext.XTemplate("{industry}")})});it("should not expand in response to mousedown",function(){jasmine.fireMouseEvent(A.lockedGrid.view.el.query(".x-grid-row-expander")[0],"mousedown");expect(A.lockedGrid.view.all.item(0).down("."+Ext.baseCSSPrefix+"grid-rowbody-tr").isVisible()).toBe(false)});it("should expand on click",function(){jasmine.fireMouseEvent(A.lockedGrid.view.el.query(".x-grid-row-expander")[0],"click");expect(A.lockedGrid.view.all.item(0).down("."+Ext.baseCSSPrefix+"grid-rowbody-tr").isVisible()).toBe(true)});it("should collapse on click",function(){I.toggleRow(0,G.getAt(0));jasmine.fireMouseEvent(A.lockedGrid.view.el.query(".x-grid-row-expander")[0],"click");expect(A.lockedGrid.view.all.item(0).down("."+Ext.baseCSSPrefix+"grid-rowbody-tr").isVisible()).toBe(false);expect(A.lockedGrid.view.all.item(0).down("."+Ext.baseCSSPrefix+"grid-rowbody",true).firstChild.data).toBe(A.store.getAt(0).get("industry"));expect(A.lockedGrid.view.all.item(0).getHeight()).toBe(A.normalGrid.view.all.item(0).getHeight())})});describe("striping rows",function(){describe("normal grid",function(){it("should place the altRowCls on the view row's ancestor row",function(){E();var J=A.view.getNode(G.getAt(1));expect(Ext.fly(J).hasCls("x-grid-item-alt")).toBe(true)})});describe("locked grid",function(){it("should place the altRowCls on the view row's ancestor row",function(){E({columns:[{text:"Company",dataIndex:"company",locked:true},{text:"Price",dataIndex:"price",locked:true},{text:"Change",dataIndex:"change"},{text:"% Change",dataIndex:"pctChange"},{text:"Last Updated",dataIndex:"lastChange"}]});var J=A.view.getNode(G.getAt(1)),K=A.normalGrid.view.getNode(G.getAt(1));expect(Ext.fly(J).hasCls("x-grid-item-alt")).toBe(true);expect(Ext.fly(K).hasCls("x-grid-item-alt")).toBe(true)});it("should sync row heights when buffered renderer adds new rows during scroll",function(){E({leadingBufferZone:2,trailingBufferZone:2,height:100,columns:[{text:"Company",dataIndex:"company",locked:true},{text:"Price",dataIndex:"price",locked:true},{text:"Change",dataIndex:"change"},{text:"% Change",dataIndex:"pctChange"},{text:"Last Updated",dataIndex:"lastChange"}]});var K=A.view.el.query(".x-grid-row-expander"),M=A.lockedGrid.view,L=A.normalGrid.view,J=M.all.item(0,true).offsetHeight,N;jasmine.fireMouseEvent(K[0],"click");N=M.all.item(0,true).offsetHeight;expect(N).toBeGreaterThan(J);expect(L.all.item(0,true).offsetHeight).toBe(N);L.setScrollY(1000);waits(500);runs(function(){L.setScrollY(0)});waits(500);runs(function(){expect(M.all.item(0,true).offsetHeight).toBe(N)})})})});it("should work when defined in a subclass",function(){E({xhooks:{initComponent:function(){Ext.apply(this,{store:[],columns:[],plugins:[{ptype:"rowexpander",rowBodyTpl:new Ext.XTemplate("<p><b>Company:</b> {company}</p>","<p><b>Change:</b> {change:this.formatChange}</p><br>","<p><b>Summary:</b> {desc}</p>")}]});this.callParent(arguments)}}});expect(A.view.features.length).toBe(1)});it("should insert a colspan attribute on the rowwrap cell equal to the number of grid columns",function(){E({columns:[{text:"Company",dataIndex:"company"},{text:"Price",dataIndex:"price"},{text:"Change",dataIndex:"change"},{text:"% Change",dataIndex:"pctChange"},{text:"Last Updated",dataIndex:"lastChange"}]});expect(parseInt(A.body.down(".x-grid-cell-rowbody",true).getAttribute("colspan"),10)).toBe(6)});it("should expand the buffered rendering scroll range when at the bottom and the row is expanded",function(){E({leadingBufferZone:2,trailingBufferZone:2,height:100});expect(H).toBeDefined();waitsFor(function(){F.setScrollY(F.getScrollY()+10);return F.all.endIndex===G.getCount()-1});runs(function(){var K=F.el.query(".x-grid-row-expander"),J=F.getScrollable(),L=J.getSize().y;jasmine.fireMouseEvent(K[K.length-1],"click");expect(J.getSize().y).toBeGreaterThan(L)})});describe("locking grid",function(){describe("no initial locked columns",function(){beforeEach(function(){E({enableLocking:true})});it("should add the expander column to the normal grid",function(){expect(I.grid).toBe(A.normalGrid)});it("should hide the locked grid",function(){expect(A.lockedGrid.hidden).toBe(true)});it("should move the expander column to the locked grid when first column is locked",function(){A.lock(A.columnManager.getColumns()[1]);expect(I.grid).toBe(A.lockedGrid)})});describe("has locked columns",function(){beforeEach(function(){E({columns:[{text:"Company",locked:true,dataIndex:"company"},{text:"Price",dataIndex:"price"},{text:"Change",dataIndex:"change"},{text:"% Change",dataIndex:"pctChange"},{text:"Last Updated",dataIndex:"lastChange"}]})});it("should add the expander column to the locked grid",function(){expect(I.grid).toBe(A.lockedGrid)});it("should not hide the locked grid",function(){expect(A.lockedGrid.hidden).toBe(false)});it("should move the expander column to the normal grid when there are no locked columns",function(){A.unlock(A.columnManager.getColumns()[1]);expect(A.lockedGrid);expect(I.grid).toBe(A.normalGrid)})})})})})