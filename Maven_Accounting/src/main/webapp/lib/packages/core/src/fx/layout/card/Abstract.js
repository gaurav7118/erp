Ext.define("Ext.fx.layout.card.Abstract",{extend:"Ext.Evented",isAnimation:true,config:{direction:"left",duration:null,reverse:null,layout:null},updateLayout:function(A){if(A){this.enable()}},enable:function(){var A=this.getLayout();if(A){A.on("beforeactiveitemchange","onActiveItemChange",this)}},disable:function(){var A=this.getLayout();if(this.isAnimating){this.stopAnimation()}if(A){A.un("beforeactiveitemchange","onActiveItemChange",this)}},onActiveItemChange:Ext.emptyFn,destroy:function(){var B=this,A=B.getLayout();if(B.isAnimating){B.stopAnimation()}if(A){A.un("beforeactiveitemchange","onActiveItemChange",this)}B.setLayout(null);if(B.observableId){B.fireEvent("destroy",this)}B.callParent()}})