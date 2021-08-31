/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

Wtf.ux.TextField = function(config) {
    Wtf.apply(this,config);
    Wtf.ux.TextField.superclass.constructor.call(this);
}

Wtf.extend(Wtf.ux.TextField, Wtf.form.TextField, {
    initComponent:function(config){
        Wtf.ux.TextField.superclass.initComponent.call(this,config);
        this.on('change',this.mychange,this);
    },
    mychange:function(field,newval){
        var retVal = Wtf.util.Format.stripTags(newval).trim();
        field.setValue(retVal);
        return retVal;
    }
});

Wtf.reg('striptextfield',Wtf.ux.TextField);


Wtf.form.ExtendedTextField=function(config){
    Wtf.form.ExtendedTextField.superclass.constructor.call(this, config);
}

Wtf.reg('striptextfield',Wtf.form.ExtendedTextField)

Wtf.extend(Wtf.form.ExtendedTextField,Wtf.form.TextField,{

	initComponent:function(config){
	    Wtf.form.ExtendedTextField.superclass.initComponent.call(this,config);
	    this.on('change',this.mychange,this);
	},
	mychange:function(field,newval){
	   var retVal = Wtf.util.Format.stripTags(newval).trim();
	   field.setValue(retVal);
	   return retVal;
	},    

	initEvents:function(){
        Wtf.form.ExtendedTextField.superclass.initEvents.call(this);
        this.el.on('keypress', this.formatValue, this);
        if(this.enableKeyEvents){
                this.el.on('keyup', this.onKeyUp, this);
                this.el.on('keydown', this.onKeyDown, this);
                this.el.on('keypress', this.onKeyPress, this);
        }
    },

    onKeyUp : function(e){
        this.fireEvent('keyup', this, e);
    },

    onKeyDown : function(e){
        this.fireEvent('keydown', this, e);
    },

    onKeyPress : function(e){
        this.fireEvent('keypress', this, e);
    },

    formatValue:function(e){
        if(String.fromCharCode(e.getCharCode())==" "){
            var val = this.getRawValue();
            var newVal=this.titleCaps(val);
            if(newVal!=val)
                this.setRawValue(newVal);
        }
    },

	titleCaps: function(title){
        var small = "(a|an|and|as|at|but|by|en|for|if|in|of|on|or|the|to|v[.]?|via|vs[.]?)";
        var punct = "([!\"#$%&'()*+,./:;<=>?@[\\\\\\]^_`{|}~-]*)";
		var parts = [], split = /[:.;?!] |(?: |^)["Ò]/g, index = 0;

		while (true) {
			var m = split.exec(title);

			parts.push( title.substring(index, m ? m.index : title.length)
				.replace(/\b([A-Za-z][a-z.'Õ]*)\b/g, function(all){
					return /[A-Za-z]\.[A-Za-z]/.test(all) ? all : this.upper(all);
				}.createDelegate(this))
				.replace(RegExp("\\b" + small + "\\b", "ig"), this.lower)
				.replace(RegExp("^" + punct + small + "\\b", "ig"), function(all, punct, word){
					return punct + this.upper(word);
				}.createDelegate(this))
				.replace(RegExp("\\b" + small + punct + "$", "ig"), this.upper));

			index = split.lastIndex;

			if ( m ) parts.push( m[0] );
			else break;
		}

		return parts.join("").replace(/ V(s?)\. /ig, " v$1. ")
			.replace(/(['Õ])S\b/ig, "$1s")
			.replace(/\b(AT&T|Q&A)\b/ig, function(all){
				return all.toUpperCase();
			});
	},
	lower:function(word){
		return word.toLowerCase();
	},

	upper:function(word){
	  return word.substr(0,1).toUpperCase() + word.substr(1);
	}
});

