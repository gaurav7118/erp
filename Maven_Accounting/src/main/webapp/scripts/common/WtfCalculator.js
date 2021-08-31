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

Wtf.namespace("Wtf.ux");

Wtf.ux.Calculator = function(config) {
	Wtf.ux.Calculator.superclass.constructor.call(this, config);
	this.id = this.id || Wtf.id();
};


Wtf.extend(Wtf.ux.Calculator, Wtf.Component, {
	number: '0',
	num1: '',
	num2: '',
	operator: '',
	memValue: '0',
	addToNum: 'no', // yes, no, reset
	showOkBtn: true,
	showTips: false,

	onRender : function() {
		//var elDom = Wtf.get(document.body).createChild({tag: 'div', cls: 'ux-calc'});
        var elDom = document.getElementById(this.renderToId);
		el = Wtf.get(elDom);

		this.standardDiv = el.createChild({tag: 'div', id: 'standardCalc_' + this.id, style: 'float: left;'});
		this.stTable = this.standardDiv.createChild({tag: 'table', cellspacing: 0, cellpadding: 0, width: 150, cls: 'ux-calc-container'});

		var maxCols = 5;

		var stBtns =
		[
			[{label: WtfGlobal.getLocaleText("acc.field.&nbsp;"), func: 'memStore', id: 'memStore_' + this.id}, {label: WtfGlobal.getLocaleText("acc.field.C"), func: 'clear', keys: [27], tip: WtfGlobal.getLocaleText("acc.field.ClearAll")}, {label: WtfGlobal.getLocaleText("acc.field.CE"), func: 'clear', tip: WtfGlobal.getLocaleText("acc.field.ClearEntry")}, {label: WtfGlobal.getLocaleText("acc.field.BS"), func: 'clear', keys: [22], tip: WtfGlobal.getLocaleText("acc.field.Backspace")}, {label: '/', func: 'operation', keys: [111, 191]}],
			[{label: WtfGlobal.getLocaleText("acc.field.MC"), func: 'memory', tip: WtfGlobal.getLocaleText("acc.field.MemoryClear")}, {label:WtfGlobal.getLocaleText("acc.field.7"), func: 'enterDigit', keys: [55, 103]}, {label: WtfGlobal.getLocaleText("acc.field.8"), func: 'enterDigit', keys: [56, 104]}, {label:WtfGlobal.getLocaleText("acc.field.9"), func: 'enterDigit', keys: [57, 105]}, {label: WtfGlobal.getLocaleText("acc.field.*"), func: 'operation', keys: [106]}],
			[{label: WtfGlobal.getLocaleText("acc.field.MR"), func: 'memory', tip: WtfGlobal.getLocaleText("acc.field.MemoryRecall")}, {label: WtfGlobal.getLocaleText("acc.field.4"), func: 'enterDigit', keys: [52, 100]}, {label: WtfGlobal.getLocaleText("acc.field.5"), func: 'enterDigit', keys: [53, 101]}, {label:WtfGlobal.getLocaleText("acc.field.6"), func: 'enterDigit', keys: [54, 102]}, {label: WtfGlobal.getLocaleText("acc.field.-"), func: 'operation', keys: [109]}],
			[{label: WtfGlobal.getLocaleText("acc.field.MS"), func: 'memory', tip: WtfGlobal.getLocaleText("acc.field.MemoryStore")}, {label: WtfGlobal.getLocaleText("acc.field.1"), func: 'enterDigit', keys: [49, 97]}, {label: WtfGlobal.getLocaleText("acc.field.2"), func: 'enterDigit', keys: [50, 98]}, {label:WtfGlobal.getLocaleText("acc.field.3"), func: 'enterDigit', keys: [51, 99]}, {label: '+', func: 'operation', keys: [107]}],
			[{label: WtfGlobal.getLocaleText("acc.field.M+"), func: 'memory', tip: WtfGlobal.getLocaleText("acc.field.MemoryAdd")}, {label: WtfGlobal.getLocaleText("acc.field.+/-"), func: 'plusminus'}, {label:WtfGlobal.getLocaleText("acc.field.0"), func: 'enterDigit', keys: [48, 96]}, {label: WtfGlobal.getLocaleText("acc.field.."), func: 'enterDot', keys: [110, 190]}, {label: WtfGlobal.getLocaleText("acc.field.equalto"), func: 'equals', keys: [10, 13]}],
			[{label: WtfGlobal.getLocaleText("acc.OK"), func: 'ok'}]
		];

		this.keyMap = new Wtf.KeyMap(el, {});

		var row = this.stTable.createChild({tag: 'tr'}).child('tr');
		var cell = Wtf.get(row.dom.appendChild(document.createElement('td')));
		cell.dom.colSpan = maxCols;

		this.inputBox = new Wtf.form.TextField({
			id: this.id,
			name: this.id,
			width: 150,
			readOnly: true,
			cls: 'ux-calc-input',
			value: '0'
		});
		this.inputBox.render(cell);

		for (i = 0; i < stBtns.length; i++) {
			if (!this.showOkBtn && i == stBtns.length - 1) {
				break;
			}

			var btn = stBtns[i];
			var row = this.stTable.createChild({tag:'tr'}).child('tr');

			for (j = 0; j < btn.length; j++) {
				var cell = Wtf.get(row.dom.appendChild(document.createElement('td')));
				cell.dom.id = btn[j].id || Wtf.id();
				cell.dom.innerHTML = btn[j].label;
				cell.dom.width = '30';
				cell.dom.align = 'center';
				cell.dom.valign = 'center';

				switch (btn[j].func) {
					case 'enterDigit':
						var cls = 'ux-calc-digit';
						break;
					case 'operation':
						var cls = 'ux-calc-operator';
						break;
					case 'equals':
						var cls = 'ux-calc-equals';
						break;
					case 'clear':
						var cls = 'ux-calc-memory';
						break;
					case 'memory':
						var cls = 'ux-calc-memory';
						break;
					case 'memStore':
						var cls = 'ux-calc-memstore';
						break;
					case 'ok':
						var cls = 'ux-calc-ok';
						break;
					default:
						cls = 'ux-calc-misc';

				}

				cell.dom.className = cls;

				if (j == btn.length - 1 && j < maxCols - 1) {
					cell.dom.colSpan = (maxCols - j+1);
				}

				if (btn[j].func != 'memStore') {
					cell.addClassOnOver('ux-calc-btn-hover');
					cell.on('click', this.onClick, this, {button: btn[j]});
				}

				if (btn[j].keys) {
					this.keyMap.addBinding({
						key: btn[j].keys,
						fn: this.onClick.createDelegate(this, [null, this, {button: btn[j], viaKbd: true, cell: cell}]),
						scope: this
					});
				}

				if (this.showTips && btn[j].tip) {
					Wtf.QuickTips.register({
						target: cell,
						text: btn[j].tip
					});
				}
			}
		}

		this.keyMap.enable();
		this.el = el;
	},

	getValue : function() {
		return this.inputBox.getValue();
	},

	setValue : function(value) {
		this.number = value;
		this.inputBox.setValue(this.number);
	},

	onClick : function(e, el, opt) {
		if (opt.viaKbd) {
			Wtf.get(opt.cell).highlight('FF0000', {attr: 'color', duration: .3});
		}

		var s = 'this.' + opt.button.func + '(\'' + opt.button.label + '\');';
		eval(s);
	},

	updateDisplay : function() {
		if (this.number == 'Infinity') {
			this.number = '0';
		}

		this.inputBox.setValue(this.number);
	},

	enterDigit : function(n) {
		if (this.addToNum == 'yes') {
			this.number += n;

			if (this.number.charAt(0) == 0 && this.number.indexOf('.') == -1) {
				this.number = this.number.substring(1);
			}
		}
		else {
			if (this.addToNum == 'reset') {
				this.reset();
			}

			this.number = n;
			this.addToNum = 'yes';
		}
		this.updateDisplay();
	},

	enterDot : function() {
		if (this.addToNum == 'yes') {
			if (this.number.indexOf('.') != -1) {
				return;
			}

			this.number += '.';
		}
		else {
			if (this.addToNum == 'reset') {
				this.reset();
			}

			this.number = '0.';
			this.addToNum = 'yes';
		}

		this.updateDisplay();
	},

	plusminus : function() {
		if (this.number == '0') {
			return;
		}

		this.number = (this.number.charAt(0) == '-') ? this.number.substring(1) : '-' + this.number;
		this.updateDisplay();
	},

	reset : function() {
		this.number = '0';
		this.addToNum = 'no';
		this.num1 = '';
		this.num2 = '';
		this.operator = '';
	},

	clear : function(o) {
		switch(o) {
			case 'C':
				this.clearAll();
				break;
			case 'CE':
				this.clearEntry();
				break;
			case 'BS':
				this.backspace();
				break;
			default:
				break;
		}
	},

	clearAll : function() {
		this.reset();
		this.updateDisplay();
	},

	clearEntry : function() {
		this.number = '0';
		this.addToNum = 'no';
		this.updateDisplay();
	},

	backspace : function() {
		var n = this.number + '';

		if (n == '0') {
			return;
		}

		this.number = n.substring(0, n.length-1);
		this.updateDisplay();
	},

	memory : function(o) {
		switch(o) {
			case 'M+':
				this.memStore(true);
				break;
			case 'MS':
				this.memStore();
				break;
			case 'MR':
				this.memRecall();
				break;
			case 'MC':
				this.memClear();
				break;
			default:
				break;
		}
	},

	memStore : function(add) {
		if (!this.number || this.number == '0') {
			return;
		}
		else {
			this.memValue = (add === true) ? this.calculate(this.number, this.memValue, '+') : this.number;

			var memDiv = Wtf.get('memStore_' + this.id);
			memDiv.dom.innerHTML = 'M';

			if (this.showTips) {
				Wtf.QuickTips.register({
					target: memDiv,
					text: 'Memory: <b>' + this.memValue + '</b>'
				});
			}
		}
	},

	memRecall : function() {
		if (this.memValue != '0') {
			this.number = this.memValue;

			if (this.num1) {
				this.num2 = this.memValue;
			}

			this.updateDisplay();
		}
	},

	memClear : function() {
		this.memValue = '0';
		var memDiv = Wtf.get('memStore_' + this.id);
		memDiv.dom.innerHTML = '&nbsp;';

		if (this.showTips) {
			Wtf.QuickTips.unregister(memDiv);
		}
	},

	accuracyCheck : function(result) {
		var i, n, j, k;
		var check;

		for (i = 0; i < 9; i++) {
			check = result * Math.pow(10, i);
			k = i + 1;
			n = Math.abs(Math.round(check) - check);
			j = Math.pow(10, -(12-i));

			if (n < j) {
				return Math.round(check) * Math.pow(10, -i);
			}
		}

		return result;
	},

	calculate : function(o1, o2, op) {
		var result;

		if (op == '=') {
			result = o1 = o2;
			o2 = '';
		}
		else {
			result = eval('o1 + op + o2');
			result = eval(result);
		}

		return result;
	},

	operation : function(op) {
		if (this.num1 == '' && typeof(this.num1) == 'string') {
			this.num1 = parseFloat(this.number);
			this.operator = op;
			this.addToNum = 'no';
		}
		else {
			if (this.addToNum == 'yes') {
				this.num2 = parseFloat(this.number);
				this.num1 = this.calculate(this.num1, this.num2, this.operator);
				this.number = this.accuracyCheck(this.num1) + '';
				this.updateDisplay();
				this.operator = op;
				this.addToNum = 'no';
			}
			else {
				this.operator = op;
				this.addToNum = 'no';
			}
		}
	},

	equals : function() {
		if (this.addToNum == 'yes') {
			if (this.num1 == '' && typeof(this.num1) == 'string') {
				this.operator = '=';
				this.num1 = parseFloat(this.number);
				this.addToNum = 'no';
			}
			else {
				this.num2 = parseFloat(this.number);
				this.num1 = this.calculate(this.num1, this.num2, this.operator);
				this.number = this.accuracyCheck(this.num1) + '';
				this.updateDisplay();
				this.addToNum = 'reset';
			}
		}
		else {
			if (this.num1 == '' && typeof(this.num1) == 'string') {
				return;
			}
			else {
				if (this.num2 == '' && typeof(this.num2) == 'string') {
					this.num2 = this.num1;
				}

				this.num1 = this.calculate(this.num1, this.num2, this.operator);
				this.number = this.accuracyCheck(this.num1) + '';
				this.updateDisplay();
				this.addToNum = 'reset';
			}
		}
	},

	alignTo : function(el, pos) {
		if (this.el) {
			this.el.alignTo(el, pos);
		}
	},

	ok : function() {
		this.fireEvent('hide', this);
	},

	show : function() {
		if (this.el) {
			this.el.show();
			this.inputBox.el.dom.focus();
		}
	},

	hide : function() {
		if (this.el && this.el.isVisible()) {
			this.el.hide();
		}
	}
});


Wtf.calculatorWindow = function(config){
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.field.Calculator")
    },config);

    this.calculator = new Wtf.ux.Calculator({showOkBtn: false, showTips: true, renderToId:"staticCalc"});

    this.items = [this.headerCalTemp=new Wtf.Panel({
            border: false,
            baseCls:'bckgroundcolor',
            html:"<div id=\"staticCalc\" style=\"height:150px\">",
            items:this.calculator
        })]
    Wtf.calculatorWindow.superclass.constructor.call(this, config);
}
Wtf.extend( Wtf.calculatorWindow, Wtf.Window, {
    afterRender: function(config){
        Wtf.calculatorWindow.superclass.afterRender.call(this, config);
        this.calculator.show();
    }
});