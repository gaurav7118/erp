Ext.define("Ext.fx.layout.card.ScrollReveal",{extend:"Ext.fx.layout.card.Scroll",alias:"fx.layout.card.scrollreveal",onActiveItemChange:function(C,G,D,E){var H,A,J,I,F,B;this.currentEventController=E;this.outItem=D;this.inItem=G;if(G&&D){H=this.getLayout().container.innerElement;A=H.getSize();J=this.calculateXY(A);I={easing:this.getEasing(),duration:this.getDuration()};F=D.setTranslatable(true).getTranslatable();B=G.setTranslatable(true).getTranslatable();F.getWrapper().dom.style.setProperty("z-index","100","important");F.translate({x:0,y:0});B.translate({x:0,y:0});G.show();F.on({animationend:"onOutAnimationEnd",scope:this});F.translateAnimated({x:J.x,y:J.y},I);E.pause()}},onOutAnimationEnd:function(){this.outItem.getTranslatable().getWrapper().dom.style.removeProperty("z-index");this.currentEventController.resume()}})