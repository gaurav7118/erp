Ext.define("Ext.fx.layout.card.ScrollCover",{extend:"Ext.fx.layout.card.Scroll",alias:"fx.layout.card.scrollcover",onActiveItemChange:function(C,G,D,E){var H,A,J,I,B,F;this.currentEventController=E;this.inItem=G;if(G&&D){H=this.getLayout().container.innerElement;A=H.getSize();J=this.calculateXY(A);I={easing:this.getEasing(),duration:this.getDuration()};G.renderElement.dom.style.setProperty("visibility","hidden","important");B=G.setTranslatable(true).getTranslatable();F=D.setTranslatable(true).getTranslatable();F.translate({x:0,y:0});B.translate({x:J.left,y:J.top});B.getWrapper().dom.style.setProperty("z-index","100","important");G.show();B.on({animationstart:"onInAnimationStart",animationend:"onInAnimationEnd",scope:this});B.translateAnimated({x:0,y:0},I);E.pause()}},onInAnimationStart:function(){this.inItem.renderElement.dom.style.removeProperty("visibility")},onInAnimationEnd:function(){this.inItem.getTranslatable().getWrapper().dom.style.removeProperty("z-index");this.currentEventController.resume()}})