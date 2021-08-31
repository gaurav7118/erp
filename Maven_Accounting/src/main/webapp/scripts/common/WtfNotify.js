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
Wtf.notify = function() {
    var msgCt;
    function createBox(t, s) {
        return ['<div style="position:absolute;" id="mainDiv" class="msg">','<img class="hideMessageImg" src="../../images/cancel-button-icon.png" alt="Close" onclick="hideMessage()" /> ', '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>', '<div class="x-box-ml"> <div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>', '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>', '</div>'].join('');
    }
    return {
        msg: function(title, format) {
            if (!msgCt) {
                msgCt = Wtf.DomHelper.insertFirst(document.body, {
                    id: 'msg-div'
                }, true);
            }
            msgCt.alignTo(document, 't-t');
            var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
            var m = Wtf.DomHelper.append(msgCt, {
                html: createBox(title, s)
            }, true);
            m.slideIn('t').pause(5.0).ghost("t", {
                remove: true
            });
        },

        init: function() {
            var t = Wtf.get('exttheme');
            if (!t) { // run locally?
                return;
            }
            var theme = Cookies.get('exttheme') || 'aero';
            if (theme) {
                t.dom.value = theme;
                Wtf.getBody().addClass('x-' + theme);
            }
            t.on('change', function() {
                Cookies.set('exttheme', t.getValue());
                setTimeout(function() {
                    window.location.reload();
                }, 250);
            });

            var lb = Wtf.get('lib-bar');
            if (lb) {
                lb.show();
            }
        }
    };
}();


function hideMessage() {
    var mainDiv = document.getElementById("mainDiv");
    if (mainDiv != undefined) {
        mainDiv.remove();
    }
}

//Wtf.ux.NotificationMgr = {
//    positions: []
//};
//
//Wtf.ux.Notification = Wtf.extend(Wtf.Window, {
//    initComponent: function(){
//        Wtf.apply(this, {
//            iconCls: this.iconCls || 'x-icon-information',
//            cls: 'x-notification',
//            width: 200,
//            autoHeight: true,
//            plain: false,
//            draggable: false,
//            bodyStyle: 'text-align:center'
//        });
//        if (this.autoDestroy) {
//            this.task = new Wtf.util.DelayedTask(this.hide, this);
//        } else {
//            this.closable = true;
//        }
//        Wtf.ux.Notification.superclass.initComponent.call(this);
//    },
//    
//    setMessage: function(msg) {
//        this.body.update(msg);
//    },
//    
//    setTitle: function(title, iconCls) {
//        Wtf.ux.Notification.superclass.setTitle.call(this, title, iconCls||this.iconCls);
//    },
//    
//    onRender: function(ct, position) {
//        Wtf.ux.Notification.superclass.onRender.call(this, ct, position);
//    },
//    
//    onDestroy: function() {
//        Wtf.ux.NotificationMgr.positions.remove(this.pos);
//        Wtf.ux.Notification.superclass.onDestroy.call(this);
//    },
//    
//    cancelHiding: function() {
//        this.addClass('fixed');
//        if(this.autoDestroy) {
//            this.task.cancel();
//        }
//    },
//    
//    afterShow: function() {
//        Wtf.ux.Notification.superclass.afterShow.call(this);
//        Wtf.fly(this.body.dom).on('click', this.cancelHiding, this);
//        if (this.autoDestroy) {
//            this.task.delay(this.hideDelay || 5000);
//        }
//    },
//    
//    animShow: function() {
//        this.pos = 0;
//        while (Wtf.ux.NotificationMgr.positions.indexOf(this.pos)>-1) {
//            this.pos++;
//        }
//        Wtf.ux.NotificationMgr.positions.push(this.pos);
//        this.setSize(200,100);
//        this.el.alignTo(document, "br-br", [ -20, -20-((this.getSize().height+10)*this.pos) ]);
//        this.el.slideIn('b', {
//            duration: 1,
//            callback: this.afterShow,
//            scope: this
//        });
//    },
//    
//    animHide: function() {
//        Wtf.ux.NotificationMgr.positions.remove(this.pos);
//        this.el.ghost("b", {
//            duration: 1,
//            remove: true
//        });
//    },
//
//    focus: Wtf.emptyFn
//
//});