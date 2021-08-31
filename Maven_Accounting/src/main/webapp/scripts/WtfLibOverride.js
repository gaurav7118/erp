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
Wtf.override(Wtf.data.Connection,{
    handleResponse : function(response){
        try{
            this.transId = false;
            var json = response.responseText.trim();
            var obj1 = eval("("+json+")")
            var options;
            var obj = obj1.data;
            if(obj.grouper!=null&&obj.grouper!=undefined){
                for(var ctr=0;ctr<obj.data.length;ctr++){
                    response.responseText= Wtf.encode(obj.data[ctr].data);
                    var callobj =  callbackmap[obj.data[ctr].no];
                    delete callbackmap[obj.data[ctr].no];
                    options = callobj.argument.options;
                    this.fireEvent("requestcomplete", this, response, options);
                    Wtf.callback(options.success, options.scope, [response, options]);
                    Wtf.callback(options.callback, options.scope, [options, true, response]);
                }

            }else{
                obj = obj1;
                options = response.argument.options;
                response.argument = options ? options.argument : null;
                this.fireEvent("requestcomplete", this, response, options);
                Wtf.callback(options.success, options.scope, [response, options]);
                Wtf.callback(options.callback, options.scope, [options, true, response]);
            }
        }catch(e){
                options = response.argument.options;
                response.argument = options ? options.argument : null;
                this.fireEvent("requestcomplete", this, response, options);
                Wtf.callback(options.success, options.scope, [response, options]);
                Wtf.callback(options.callback, options.scope, [options, true, response]);
        }

    }

})

/* 
 *  To Fix Bugs related to Chrome browser
 *  1) Link Button in htmleditor
 *  2) Select Font in htmlEditor
 *  3) Font increase and decrease
 *
 *  IE8 Bug for paging toolbar button (Disabled and enabled class).
 *
 */


function checkUA(pattern){
    ua = navigator.userAgent.toLowerCase();
    return pattern.test(ua);
}
    Wtf.isOpera = checkUA(/opera/),
    Wtf.isChrome = checkUA(/chrome/),
    Wtf.isWebKit = checkUA(/webkit/),
    Wtf.isSafari = !Wtf.isChrome && checkUA(/safari/),
    Wtf.isSafari2 =  Wtf.isSafari && checkUA(/applewebkit\/4/), // unique to Safari 2
    Wtf.isSafari3 =  Wtf.isSafari && checkUA(/version\/3/),
    Wtf.isSafari4 =  Wtf.isSafari && checkUA(/version\/4/),
    Wtf.isIE = !Wtf.isOpera && checkUA(/msie/),
    Wtf.isIE7 =  Wtf.isIE && checkUA(/msie 7/),
    Wtf.isIE8 =  Wtf.isIE && checkUA(/msie 8/),
    Wtf.isIE6 =  Wtf.isIE && !Wtf.isIE7 && !Wtf.isIE8,
    Wtf.isGecko = !Wtf.isWebKit && checkUA(/gecko/),
    Wtf.isGecko2 =  Wtf.isGecko && checkUA(/rv:1\.8/),
    Wtf.isGecko3 =  Wtf.isGecko && checkUA(/rv:1\.9/),
    Wtf.isBorderBox =  Wtf.isIE && !Wtf.isStrict,
    Wtf.isWindows = checkUA(/windows|win32/),
    Wtf.isMac = checkUA(/macintosh|mac os x/),
    Wtf.isAir = checkUA(/adobeair/),
    Wtf.isLinux = checkUA(/linux/),
    //Override onDisable and onEnable function to fix bug for paging toolbar button in IE8
    Wtf.override(Wtf.Button, {
        onDisable : function(){
            if(this.el){
                if(!Wtf.isIE6 || !this.text){
                    this.el.addClass("x-item-disabled");
                }
                this.el.dom.disabled = true;
            }
            this.disabled = true;
        },
        onEnable : function(){
            if(this.el){
                if(!Wtf.isIE6 || !this.text){
                    this.el.removeClass("x-item-disabled");
                }
                this.el.dom.disabled = false;
            }
            this.disabled = false;
        }
    });

    Wtf.override(Wtf.grid.RowSelectionModel, {
        handleMouseDown : function(g, rowIndex, e){
            if(e.button !== 0 || this.isLocked()){
                return;
            };
            var view = this.grid.getView();
            if(e.shiftKey && this.last !== false){
                var last = this.last;
                this.selectRange(last, rowIndex, e.ctrlKey);
                this.last = last; 
                view.focusRow(rowIndex);
            }else{
                var isSelected = this.isSelected(rowIndex);
                if(isSelected){
                    this.deselectRow(rowIndex);
                }else if(!isSelected || this.getCount() > 1){
                    this.selectRow(rowIndex, e.ctrlKey || e.shiftKey);
//                    view.focusRow(rowIndex);
                }
            }
        },
        selectRow : function(index, keepExisting, preventViewNotify){
            if(this.locked || (index < 0 || index >= this.grid.store.getCount())) return;
            var r = this.grid.store.getAt(index);
            if(r && this.fireEvent("beforerowselect", this, index, keepExisting, r) !== false){
                if(this.singleSelect){
                    this.clearSelections();
                }
                this.selections.add(r);
                this.last = this.lastActive = index;
                if(!preventViewNotify){
                    this.grid.getView().onRowSelect(index);
                }
                this.fireEvent("rowselect", this, index, r);
                this.fireEvent("selectionchange", this);
            }
        }
    });
    
function recycleCursor(){
    var fakeinput = Wtf.get("cursor_bin");
    fakeinput.show();
    fakeinput.focus();
    fakeinput.hide();
}

Wtf.override(Wtf.form.Field,{       
 initEvents : function(){
    this.el.on((Wtf.isIE || Wtf.isSafari || Wtf.isChrome) ? "keydown" : "keypress", this.fireKey,  this);
    this.el.on("focus", this.onFocus,  this);
    this.el.on("blur", this.onBlur,  this);
    this.originalValue = this.getValue();
 }
});

if(Wtf.isIE7){
    Wtf.TabPanel.prototype.oldSetActiveTab = Wtf.TabPanel.prototype.setActiveTab;
    Wtf.override(Wtf.TabPanel,{
        setActiveTab:function(item){
        item = this.getComponent(item);
        if(this.activeTab){
            if(this.activeTab.findByType(Wtf.newHTMLEditor).length==1 || this.activeTab.findByType(Wtf.form.HtmlEditor).length==1){
                recycleCursor();
                
            }
        }
             this.oldSetActiveTab(item);
    }
    })
}
Wtf.override(Wtf.form.HtmlEditor,{

createToolbar:function(editor){
 var tipsEnabled = Wtf.QuickTips && Wtf.QuickTips.isEnabled();
        
        function btn(id, toggle, handler){
            return {
                itemId : id,
                cls : 'x-btn-icon x-edit-'+id,
                enableToggle:toggle !== false,
                scope: editor,
                handler:handler||editor.relayBtnCmd,
                clickEvent:'mousedown',
                tooltip: tipsEnabled ? editor.buttonTips[id] || undefined : undefined,
                overflowText: editor.buttonTips[id].title || undefined,
                tabIndex:-1
            };
        }

        // build the toolbar
        var tb = new Wtf.Toolbar({
            renderTo:this.wrap.dom.firstChild
        });

        // stop form submits
        tb.on('click', function(e){
            e.preventDefault();
        });

        if(this.enableFont && !Wtf.isSafari2){
            this.fontSelect = tb.el.createChild({
                tag:'select',
                cls:'x-font-select',
                html: this.createFontOptions()
            });
            this.fontSelect.on('change', function(){
                var font = this.fontSelect.dom.value;
                this.relayCmd('fontname', font);
                this.deferFocus();
            }, this);

            tb.add(
                this.fontSelect.dom,
                '-'
            );
        }

        if(this.enableFormat){
            tb.add(
                btn('bold'),
                btn('italic'),
                btn('underline')
            );
        }

        if(this.enableFontSize){
            tb.add(
                '-',
                btn('increasefontsize', false, this.adjustFont),
                btn('decreasefontsize', false, this.adjustFont)
            );
        }

        if(this.enableColors){
            tb.add(
                '-', {
                    itemId:'forecolor',
                    cls:'x-btn-icon x-edit-forecolor',
                    clickEvent:'mousedown',
                    tooltip: tipsEnabled ? editor.buttonTips.forecolor || undefined : undefined,
                    tabIndex:-1,
                    menu : new Wtf.menu.ColorMenu({
                        allowReselect: true,
                        focus: Wtf.emptyFn,
                        value:'000000',
                        plain:true,
                        listeners: {
                            scope: this,
                            select: function(cp, color){
                                this.execCmd('forecolor', Wtf.isWebKit || Wtf.isIE ? '#'+color : color);
                                this.deferFocus();
                            }
                        },
                        clickEvent:'mousedown'
                    })
                }, {
                    itemId:'backcolor',
                    cls:'x-btn-icon x-edit-backcolor',
                    clickEvent:'mousedown',
                    tooltip: tipsEnabled ? editor.buttonTips.backcolor || undefined : undefined,
                    tabIndex:-1,
                    menu : new Wtf.menu.ColorMenu({
                        focus: Wtf.emptyFn,
                        value:'FFFFFF',
                        plain:true,
                        allowReselect: true,
                        listeners: {
                            scope: this,
                            select: function(cp, color){
                                if(Wtf.isGecko){
                                    this.execCmd('useCSS', false);
                                    this.execCmd('hilitecolor', color);
                                    this.execCmd('useCSS', true);
                                    this.deferFocus();
                                }else{
                                    this.execCmd(Wtf.isOpera ? 'hilitecolor' : 'backcolor', Wtf.isWebKit || Wtf.isIE ? '#'+color : color);
                                    this.deferFocus();
                                }
                            }
                        },
                        clickEvent:'mousedown'
                    })
                }
            );
        }

        if(this.enableAlignments){
            tb.add(
                '-',
                btn('justifyleft'),
                btn('justifycenter'),
                btn('justifyright')
            );
        }

        if(!Wtf.isSafari2){
            if(this.enableLinks){
                tb.add(
                    '-',
                    btn('createlink', false, this.createLink)
                );
            }

            if(this.enableLists){
                tb.add(
                    '-',
                    btn('insertorderedlist'),
                    btn('insertunorderedlist')
                );
            }
            if(this.enableSourceEdit){
                tb.add(
                    '-',
                    btn('sourceedit', true, function(btn){
                        this.toggleSourceEdit(!this.sourceEditMode);
                    })
                );
            }
        }

        this.tb = tb;
},
    getDoc : function(){
        return Wtf.isIE ? this.getWin().document : (this.iframe.contentDocument || this.getWin().document);
    },


    getWin : function(){
        return Wtf.isIE ? this.iframe.contentWindow : window.frames[this.iframe.name];
    },
    adjustFont: function(btn){
        var adjust = btn.getItemId() == 'increasefontsize' ? 1 : -1,
        doc = this.getDoc(),
        v = parseInt(doc.queryCommandValue('FontSize') || 2, 10);
        if((Wtf.isSafari && !Wtf.isSafari2) || Wtf.isChrome ){


            if(v <= 10){
                v = 1 + adjust;
            }else if(v <= 13){
                v = 2 + adjust;
            }else if(v <= 16){
                v = 3 + adjust;
            }else if(v <= 18){
                v = 4 + adjust;
            }else if(v <= 24){
                v = 5 + adjust;
            }else {
                v = 6 + adjust;
            }
            v = v.constrain(1, 6);
        }else{
            if(Wtf.isSafari){
                adjust *= 2;
            }
            v = Math.max(1, v+adjust) + (Wtf.isSafari ? 'px' : 0);
        }
        this.execCmd('FontSize', v);

    },
    fixKeys : function(){
        if(Wtf.isIE){
            return function(e){
                var k = e.getKey(), r;
                if(k == e.TAB){
                    e.stopEvent();
                    r = this.doc.selection.createRange();
                    if(r){
                        r.collapse(true);
                        r.pasteHTML('&nbsp;&nbsp;&nbsp;&nbsp;');
                        this.deferFocus();
                    }
                }else if(k == e.ENTER){
                    r = this.doc.selection.createRange();
                    if(r){
                        var target = r.parentElement();
                        if(!target || target.tagName.toLowerCase() != 'li'){
                            e.stopEvent();
                            r.pasteHTML('<br />');
                            r.collapse(false);
                            r.select();
                        }
                    }
                }
            };
        }else if(Wtf.isOpera){
            return function(e){
                var k = e.getKey();
                if(k == e.TAB){
                    e.stopEvent();
                    this.win.focus();
                    this.execCmd('InsertHTML','&nbsp;&nbsp;&nbsp;&nbsp;');
                    this.deferFocus();
                }
            };
        }else if(Wtf.isWebKit){
            return function(e){
                var k = e.getKey();
                if(k == e.TAB){
                    e.stopEvent();
                    this.execCmd('InsertText','\t');
                    this.deferFocus();
                }else if(k == e.ENTER){
                    e.stopEvent();
                    this.execCmd('InsertHtml','<br /><br />');
                    this.deferFocus();
                }
            };
        }
    }()
});

//On load Exception Handling
Wtf.data.Store.prototype.oldhandler = Wtf.data.Store.prototype.loadRecords;
Wtf.override(Wtf.data.Store,{
    loadRecords:function(o, options, success){
        try{
            this.oldhandler(o, options, success)
        }catch(e){
            clog(e)
        }
    }
})

//IE - setHeight() problem
Wtf.override(Wtf.Element,{
    getHeight:function(contentHeight){
        try{
            var me = this;
            var dom = me.dom;
            var display =  me.getStyle("display");
            var hidden = Wtf.isIE && (display=="none");
            var h = Math.max(dom.offsetHeight, hidden ? 0 : dom.clientHeight) || 0;
            h = !contentHeight ? h : h - me.getBorderWidth("tb") - me.getPadding("tb");
            return h < 0 ? 0 : h;
        }catch(e){
            clog(e)
        }
    }
})

Wtf.override(Wtf.Element,{
    getWidth:function(contentWidth){
        try{
            var me = this;
            var dom = me.dom;
            var display =  me.getStyle("display");
            var hidden = Wtf.isIE && (display=="none");
            var w = Math.max(dom.offsetWidth, hidden ? 0 : dom.clientWidth) || 0;
            w = !contentWidth ? w : w - me.getBorderWidth("tb") - me.getPadding("tb");
            return w < 0 ? 0 : w;
        }catch(e){
            clog(e)
        }
    }
})


// IE 9 Wtf.qtip dashboard tooltips not displaying problem and tree structure problem solved.  Neeraj

//if(Wtf.IE9){
	Wtf.override(Wtf.Element, {
       
        getAttributeNS : function(ns, name){
            return this.getAttribute(name, ns);
        },
       
        getAttribute: (function(){
            var test = document.createElement('table'),
                isBrokenOnTable = false,
                hasGetAttribute = 'getAttribute' in test,
                unknownRe = /undefined|unknown/;
               
            if (hasGetAttribute) {
               
                try {
                    test.getAttribute('ext:qtip');
                } catch (e) {
                    isBrokenOnTable = true;
                }
               
                return function(name, ns) {
                    var el = this.dom,
                        value;
                   
                    if (el.getAttributeNS) {
                        value  = el.getAttributeNS(ns, name) || null;
                    }
               
                    if (value == null) {
                        if (ns) {
                            if (isBrokenOnTable && el.tagName.toUpperCase() == 'TABLE') {
                                try {
                                    value = el.getAttribute(ns + ':' + name);
                                } catch (e) {
                                    value = '';
                                }
                            } else {
                                value = el.getAttribute(ns + ':' + name);
                            }
                        } else {
                            value = el.getAttribute(name) || el[name];
                        }
                    }
                    return value || '';
                };
            } else {
                return function(name, ns) {
                    var el = this.om,
                        value,
                        attribute;
                   
                    if (ns) {
                        attribute = el[ns + ':' + name];
                        value = unknownRe.test(typeof attribute) ? undefined : attribute;
                    } else {
                        value = el[name];
                    }
                    return value || '';
                };
            }
            test = null;
        })()
    });
    
/*
 *Code to override Date Picker function
 *
 *Resolved Problem : Steps to replicate:
 * 1. Open any screen (for this example Sales Invoice is used)
 * 2. Set ToDate to 31 Dec 2016.
 * 3. Select the To Date by using the Month & Year selector, select Feb-2016
 * 4. dates shown is for March instead of February
 * 
 * Solution : Before setting month and year taken from month selector I have checked that new value is valid or not and updated it accordingly in checkValidDate.
 * For more reference please refer SDP-3992.
 **/    
Wtf.override(Wtf.DatePicker,{
    onMonthClick : function(e, t){
        e.stopEvent();
        var el = new Wtf.Element(t), pn;
        
        if(el.is('button.x-date-mp-cancel')){
            this.hideMonthPicker();
        }
        else if(el.is('button.x-date-mp-ok')){
            this.selDay = (this.activeDate || this.value).getDate();
            
            /*
             *Here is the overridden change to check and update valid date. For SDP-3992
             *
             */
            this.checkValidDate();  
                
            this.update(new Date(this.mpSelYear, this.mpSelMonth, this.selDay));
            this.hideMonthPicker();
        }
        else if(pn = el.up('td.x-date-mp-month', 2)){
            this.mpMonths.removeClass('x-date-mp-sel');
            pn.addClass('x-date-mp-sel');
            this.mpSelMonth = pn.dom.xmonth;
        }
        else if(pn = el.up('td.x-date-mp-year', 2)){
            this.mpYears.removeClass('x-date-mp-sel');
            pn.addClass('x-date-mp-sel');
            this.mpSelYear = pn.dom.xyear;
        }
        else if(el.is('a.x-date-mp-prev')){
            this.updateMPYear(this.mpyear-10);
        }
        else if(el.is('a.x-date-mp-next')){
            this.updateMPYear(this.mpyear+10);
        }
    },
    
    /*
     * Checked that new value is valid or not.
     */
    checkValidDate : function(){
        var d = new Date(this.mpSelYear, this.mpSelMonth,this.selDay);
        if (!(d.getFullYear() == this.mpSelYear && d.getMonth() == this.mpSelMonth && d.getDate() == this.selDay)) {
            
            if(d.getDate() != this.selDay){
                this.selDay = this.selDay - d.getDate();
            }
        }
    }
});


/*
 *  Code to override Column Model function
 *  Resolved Problem: Denied to hide last sortable column present in current visible columns
 *  Ticket--> ERP-37016
 * 
 */

Wtf.override(Wtf.grid.GridView,{
    handleHdMenuClick : function(item){
        var index = this.hdCtxIndex;
        var cm = this.cm, ds = this.ds;
        switch(item.id){
            case "asc":
                ds.sort(cm.getDataIndex(index), "ASC");
                break;
            case "desc":
                ds.sort(cm.getDataIndex(index), "DESC");
                break;
            default:
                index = cm.getIndexById(item.id.substr(4));
                if(index != -1){
                    /*
                     *changed if condition from library function for restricting user from hiding last sortable column. 
                     */
                    if(item.checked && this.getSortableColumns(cm).length == 1 ){
                        /*
                         *cm.getColumnCount(true)-> To get count of current visible columns
                         *cm.isSortable(index)-> To check current clicked column is sortable?
                         *If current visible columns are >1 but all are unsortable except the current clicked column then allow to hide other column
                         **/
                        if((cm.getColumnCount(true)-1 >=1 && !cm.isSortable(index)) ) {
                            cm.setHidden(index, item.checked);
                            return true;
                        }else{
                            this.onDenyColumnHide();
                            return false;
                        }
                    }
                    //end changes from library function.
                    cm.setHidden(index, item.checked);
                }
        }
        return true;
    },
    /*
     *New function in grid view to get array of sortable columns that are currently visible in grid.
     */
    getSortableColumns : function(cm){
        var sortableColumns = [], i = 0;
        while(i<=cm.getColumnCount()-1 ){
            if(cm.isSortable(i) && !cm.isHidden(i)){
                sortableColumns.push(cm.config[i]);
            }
            i++;
        }
        return sortableColumns;
            
    }
    
});

/**
 * To resolve empty text edit issue in combobox. When combobox blur event fires called combo.el.blur() function.
 */
Wtf.override(Wtf.form.ComboBox, {
    doForce: function () {
        if (this.el.dom.value.length > 0) {
            this.el.dom.value = this.lastSelectionText === undefined ? '' : this.lastSelectionText;
            this.applyEmptyText();
        }
        /**
         * Added call to blur() function of combo.el
         */
        if (this.el) {
            this.el.blur();
        }
    }
});

/**
 * 
 */
Wtf.override(Wtf.form.ComboBox, {
    restrictHeight: function () {
        this.innerList.dom.style.height = '';
        var inner = this.innerList.dom;
        var fw = this.list.getFrameWidth('tb');
        var h = Math.max(inner.clientHeight, inner.offsetHeight, inner.scrollHeight);
        /**
         * window.innerHeight       - Current window inner height (NOT including toolbars/scrollbars)
         * this.el.getY()           - Position of combo from top (Y coordinate)
         * this.el.getHeight()      - Combo height
         * this.footer.getHeight()  - Height of footer if present (Footer available in case of paging combo)
         * Variable 29              - To provide extra bottom margin
         * variable 37              - To provide extra top margin
         */
        var listAlign = this.listAlign;
        var height = window.innerHeight - (this.el.getY() + this.el.getHeight() + (this.footer ? this.footer.getHeight() : 0) + 29);
        if (height < 60 || listAlign === 'bl-tl' || listAlign === 'bl-tl?') {
            height = this.el.getY() - ((this.footer ? this.footer.getHeight() : 0) + 37);
            listAlign = 'bl-tl?';
        }
        this.innerList.setHeight(Math.min(h, this.maxHeight, height));
        this.list.beginUpdate();
        this.list.setHeight(this.innerList.getHeight() + fw + (this.resizable ? this.handleHeight : 0) + this.assetHeight);
        this.list.alignTo(this.el, listAlign);
        this.list.endUpdate();
    }
});
