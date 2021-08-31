Ext.define("Ext.panel.Pinnable",{extend:"Ext.Mixin",mixinId:"pinnable",pinnable:true,pinnedTip:"Unpin this item",unpinnedTip:"Pin this item",initPinnable:function(){var B=this,A=B.isPinned();B.addTool(B.pinTool=Ext.widget({xtype:"tool",type:A?"unpin":"pin",callback:"togglePin",scope:B,tooltip:A?B.pinnedTip:B.unpinnedTip}))},isPinned:function(){return !this.floating},setPinned:function(B){var C=this,A;if(B!==C.isPinned()){A=[C,B];if(C.fireEventArgs("beforepinchange",A)!==false){C.updatePinned(B);C.fireEventArgs("pinchange",A)}}},togglePin:function(){this.setPinned(!this.isPinned())},updatePinned:function(B){var C=this,A=C.pinTool;A.setTooltip(B?C.pinnedTip:C.unpinnedTip);A.setType(B?"unpin":"pin")}})