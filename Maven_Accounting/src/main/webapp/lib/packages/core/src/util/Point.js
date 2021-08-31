Ext.define("Ext.util.Point",{extend:"Ext.util.Region",radianToDegreeConstant:180/Math.PI,origin:{x:0,y:0},statics:{fromEvent:function(B){var A=B.changedTouches,C=(A&&A.length>0)?A[0]:B;return this.fromTouch(C)},fromTouch:function(A){return new this(A.pageX,A.pageY)},from:function(A){if(!A){return new this(0,0)}if(!(A instanceof this)){return new this(A.x,A.y)}return A}},constructor:function(A,B){if(A==null){A=0}if(B==null){B=0}this.callParent([B,A,B,A])},clone:function(){return new this.self(this.x,this.y)},copy:function(){return this.clone.apply(this,arguments)},copyFrom:function(A){this.x=A.x;this.y=A.y;return this},toString:function(){return"Point["+this.x+","+this.y+"]"},equals:function(A){return(this.x===A.x&&this.y===A.y)},isCloseTo:function(C,B){if(typeof B=="number"){return this.getDistanceTo(C)<=B}var A=C.x,F=C.y,E=B.x,D=B.y;return(this.x<=A+E&&this.x>=A-E&&this.y<=F+D&&this.y>=F-D)},isWithin:function(){return this.isCloseTo.apply(this,arguments)},isContainedBy:function(A){if(!(A instanceof Ext.util.Region)){A=Ext.get(A.el||A).getRegion()}return A.contains(this)},roundedEquals:function(A){if(!A||typeof A!=="object"){A=this.origin}return(Math.round(this.x)===Math.round(A.x)&&Math.round(this.y)===Math.round(A.y))},getDistanceTo:function(B){if(!B||typeof B!=="object"){B=this.origin}var C=this.x-B.x,A=this.y-B.y;return Math.sqrt(C*C+A*A)},getAngleTo:function(B){if(!B||typeof B!=="object"){B=this.origin}var C=this.x-B.x,A=this.y-B.y;return Math.atan2(A,C)*this.radianToDegreeConstant}},function(){this.prototype.translate=this.prototype.translateBy})