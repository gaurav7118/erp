Ext.define("Ext.container.Viewport",{extend:"Ext.container.Container",requires:["Ext.plugin.Viewport"],mixins:["Ext.mixin.Responsive"],alias:"widget.viewport",alternateClassName:"Ext.Viewport",ariaRole:"application",privates:{updateResponsiveState:function(){this.handleViewportResize();this.mixins.responsive.updateResponsiveState.call(this)}}},function(){Ext.plugin.Viewport.decorate(this)})