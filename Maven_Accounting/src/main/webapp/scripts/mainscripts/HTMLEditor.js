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
Wtf.newHTMLEditor = function(config){
    Wtf.apply(this, config);
    this.createLinkText = WtfGlobal.getLocaleText("acc.field.PleaseentertheURLforthelink");
    this.defaultLinkValue = 'http:/'+'/';
    this.smileyel = null;
    this.SmileyArray = [" ", ":)", ":(", ";)", ":D", ";;)", ">:D<", ":-/", ":x", ":>>", ":P", ":-*", "=((", ":-O", "X(", ":>", "B-)", ":-S", "#:-S", ">:)", ":((", ":))", ":|", "/:)", "=))", "O:-)", ":-B", "=;", ":-c", ":)]", "~X("];
    this.tpl = new Wtf.Template('<div id="{curid}smiley{count}" style="float:left; height:20px; width:20px; background: #ffffff;padding-left:4px;padding-top:4px;"  ><img id="{curid}smiley{count}" src="{url}" style="height:16px; width:16px"></img></div>');
    this.tbutton = new Wtf.Toolbar.Button({
        minWidth: 30,
        disabled:true,
        enableToggle: true,
        iconCls: 'smiley'
    });
    this.eventSetFlag=false;
    this.tbutton.on("click", this.handleSmiley, this);
    this.smileyWindow = new Wtf.Window({
        width: 185,
        height: 116,
        minWidth: 200,
        plain: true,
        cls: 'replyWind',
        shadow: false,
        buttonAlign: 'center',
        draggable: false,
        header: false,
        closable  : true,
        closeAction : 'hide',
        resizable: false
    });
    this.smileyWindow.on("deactivate", this.closeSmileyWindow, this);
    Wtf.newHTMLEditor.superclass.constructor.call(this, {});
    this.on("render", this.addSmiley, this);
    this.on("activate", this.enableSmiley, this);
    this.on("hide", this.hideSmiley, this);
}

Wtf.extend(Wtf.newHTMLEditor, Wtf.form.HtmlEditor, {
    enableSmiley:function(){
        this.tbutton.enable();
    },
    hideSmiley: function(){
        //        alert("hide");
        if(this.smileyWindow !== undefined && this.smileyWindow.el !== undefined)
            this.smileyWindow.hide();
    },
    addSmiley: function(editorObj){
        editorObj.getToolbar().addSeparator();
        editorObj.getToolbar().addButton(this.tbutton);

    },
    createLink : function(){
        var url = prompt(this.createLinkText, this.defaultLinkValue);
        if(url && url != 'http:/'+'/'){
            var tmpStr = url.substring(0,7);
            if(tmpStr!='http:/'+'/')
                url = 'http:/'+'/'+url;
            this.win.focus();
            var selTxt = "";
            if(Wtf.isIE == true)
                selTxt = this.doc.selection.createRange().duplicate().text;
            else
                selTxt = this.doc.getSelection().trim();
            selTxt = selTxt =="" ? url : selTxt;
            if(this.SmileyArray.join().indexOf(selTxt)==-1) {
                this.insertAtCursor("<a href = '"+url+"' target='_blank'>"+selTxt+" </a>");
                this.deferFocus();
            } else {
                msgBoxShow(170,1);
            }
        }
    },
    //  FIXME: ravi: When certain smilies are used in a pattern, the resultant from this function does not conform to regex used to decode smilies in messenger.js.

    writeSmiley: function(e){
        var obj=e;
        this.insertAtCursor(this.SmileyArray[obj.target.id.substring(this.id.length + 6)]+" ");
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    },

    handleSmiley: function(buttonObj, e){
        if(this.tbutton.pressed) {
            this.smileyWindow.setPosition(e.getPageX(), e.getPageY());
            this.smileyWindow.show();
            if(!this.eventSetFlag){
                for (var i = 1; i < 29; i++) {
                    var divObj = {
                        url: '../../images/smiley' + i + '.gif',
                        count: i,
                        curid: this.id
                    };
                    this.tpl.append(this.smileyWindow.body, divObj);
                    this.smileyel = Wtf.get(this.id + "smiley" + i);
                    this.smileyel.on("click", this.writeSmiley, this);
                    this.eventSetFlag=true;
                }
            }
        } else {
            this.smileyWindow.hide();
            this.tbutton.toggle(false);
        }
    },

    closeSmileyWindow: function(smileyWindow){
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    }
});

Wtf.newHTMLEditor.override({
    insertAtCursor : function(text){
        if(!this.activated){
            return;
        }
        if(Wtf.isIE){
            this.win.focus();
            var doc = this.doc,
                r = doc.selection.createRange();
            if(r){
                r.pasteHTML(text);
                this.syncValue();
                this.deferFocus();
            }
        }else{
            this.win.focus();
            this.execCmd('InsertHTML', text);
            this.deferFocus();
        }
    }
});/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


