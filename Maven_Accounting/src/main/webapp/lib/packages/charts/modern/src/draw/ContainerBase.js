Ext.define("Ext.draw.ContainerBase",{extend:"Ext.Container",constructor:function(A){this.callParent([A]);this.initAnimator()},initialize:function(){this.callParent();this.element.on("resize","onBodyResize",this)},getElementConfig:function(){return{reference:"element",className:"x-container",children:[{reference:"innerElement",className:"x-inner"}]}},addElementListener:function(){var A=this.element;A.on.apply(A,arguments)},removeElementListener:function(){var A=this.element;A.un.apply(A,arguments)},preview:function(){Ext.Viewport.add({xtype:"panel",layout:"fit",modal:true,width:"90%",height:"90%",hideOnMaskTap:true,centered:true,scrollable:false,items:{xtype:"image",mode:"img",style:{overflow:"hidden"},src:this.getImage().data},listeners:{hide:function(){Ext.Viewport.remove(this)}}}).show()}})