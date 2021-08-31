Ext.define("Ext.util.Region",{requires:["Ext.util.Offset"],isRegion:true,statics:{getRegion:function(A){return Ext.fly(A).getRegion()},from:function(A){return new this(A.top,A.right,A.bottom,A.left)}},constructor:function(E,B,A,D){var C=this;C.y=C.top=C[1]=E;C.right=B;C.bottom=A;C.x=C.left=C[0]=D},contains:function(B){var A=this;return(B.x>=A.x&&B.right<=A.right&&B.y>=A.y&&B.bottom<=A.bottom)},intersect:function(F){var E=this,C=Math.max(E.y,F.y),D=Math.min(E.right,F.right),A=Math.min(E.bottom,F.bottom),B=Math.max(E.x,F.x);if(A>C&&D>B){return new this.self(C,D,A,B)}else{return false}},union:function(F){var E=this,C=Math.min(E.y,F.y),D=Math.max(E.right,F.right),A=Math.max(E.bottom,F.bottom),B=Math.min(E.x,F.x);return new this.self(C,D,A,B)},constrainTo:function(B){var A=this,C=Ext.Number.constrain;A.top=A.y=C(A.top,B.y,B.bottom);A.bottom=C(A.bottom,B.y,B.bottom);A.left=A.x=C(A.left,B.x,B.right);A.right=C(A.right,B.x,B.right);return A},adjust:function(E,B,A,D){var C=this;C.top=C.y+=E;C.left=C.x+=D;C.right+=B;C.bottom+=A;return C},getOutOfBoundOffset:function(A,B){if(!Ext.isObject(A)){if(A=="x"){return this.getOutOfBoundOffsetX(B)}else{return this.getOutOfBoundOffsetY(B)}}else{B=A;var C=new Ext.util.Offset();C.x=this.getOutOfBoundOffsetX(B.x);C.y=this.getOutOfBoundOffsetY(B.y);return C}},getOutOfBoundOffsetX:function(A){if(A<=this.x){return this.x-A}else{if(A>=this.right){return this.right-A}}return 0},getOutOfBoundOffsetY:function(A){if(A<=this.y){return this.y-A}else{if(A>=this.bottom){return this.bottom-A}}return 0},isOutOfBound:function(A,B){if(!Ext.isObject(A)){if(A=="x"){return this.isOutOfBoundX(B)}else{return this.isOutOfBoundY(B)}}else{B=A;return(this.isOutOfBoundX(B.x)||this.isOutOfBoundY(B.y))}},isOutOfBoundX:function(A){return(A<this.x||A>this.right)},isOutOfBoundY:function(A){return(A<this.y||A>this.bottom)},restrict:function(B,D,A){if(Ext.isObject(B)){var C;A=D;D=B;if(D.copy){C=D.copy()}else{C={x:D.x,y:D.y}}C.x=this.restrictX(D.x,A);C.y=this.restrictY(D.y,A);return C}else{if(B=="x"){return this.restrictX(D,A)}else{return this.restrictY(D,A)}}},restrictX:function(B,A){if(!A){A=1}if(B<=this.x){B-=(B-this.x)*A}else{if(B>=this.right){B-=(B-this.right)*A}}return B},restrictY:function(B,A){if(!A){A=1}if(B<=this.y){B-=(B-this.y)*A}else{if(B>=this.bottom){B-=(B-this.bottom)*A}}return B},getSize:function(){return{width:this.right-this.x,height:this.bottom-this.y}},copy:function(){return new this.self(this.y,this.right,this.bottom,this.x)},copyFrom:function(B){var A=this;A.top=A.y=A[1]=B.y;A.right=B.right;A.bottom=B.bottom;A.left=A.x=A[0]=B.x;return this},toString:function(){return"Region["+this.top+","+this.right+","+this.bottom+","+this.left+"]"},translateBy:function(A,C){if(arguments.length==1){C=A.y;A=A.x}var B=this;B.top=B.y+=C;B.right+=A;B.bottom+=C;B.left=B.x+=A;return B},round:function(){var A=this;A.top=A.y=Math.round(A.y);A.right=Math.round(A.right);A.bottom=Math.round(A.bottom);A.left=A.x=Math.round(A.x);return A},equals:function(A){return(this.top===A.top&&this.right===A.right&&this.bottom===A.bottom&&this.left===A.left)}})