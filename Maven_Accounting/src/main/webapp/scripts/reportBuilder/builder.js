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
Wtf.reportBuilder.label = function(conf){
    Wtf.apply(this, conf);
    Wtf.reportBuilder.label.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.reportBuilder.label, Wtf.BoxComponent, {
    cls: "reportTitle",
    onRender: function(conf){
        Wtf.reportBuilder.label.superclass.onRender.call(this, conf);
        this.labelDiv = document.createElement("div");
        if(this.text)
            this.labelDiv.innerHTML = this.text;
        this.editLink = document.createElement("a");
        this.editLink.className = "edit";
        this.editLink.href = "#";
        this.editLink.innerHTML = "Edit";
        this.editLink.onclick = this.editLinkClicked.createDelegate(this, [this]);
        this.labelDiv.appendChild(this.editLink);
        this.applyToMarkup(this.labelDiv);
    },

    editLinkClicked: function(){
        var newHtml = "Save"
        if(this.editLink.innerHTML != "Edit")
            newHtml = "Edit";
        this.editLink.innerHTML = newHtml;
    }
});


Wtf.reportBuilder.containerPanel = function(conf){
    Wtf.apply(this, conf);
    this.border = true;
    Wtf.reportBuilder.containerPanel.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.reportBuilder.containerPanel, Wtf.Panel, {
    baseCls: "reportContainerDiv",
    autoHeight : true,
    onRender: function(conf){
        Wtf.reportBuilder.containerPanel.superclass.onRender.call(this, conf);
        if(this.header){
            this.el.dom.removeChild(this.header);
        }
    }
});


Wtf.reportBuilder.builderPanel = function(conf) {
    Wtf.apply(this, conf);
    Wtf.reportBuilder.builderPanel.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.reportBuilder.builderPanel, Wtf.Panel, {
    baseCls: "reportBuilder",
    autoScroll: true,
    onRender: function(conf){
        Wtf.reportBuilder.builderPanel.superclass.onRender.call(this, conf);
        this.cPanel = new Wtf.reportBuilder.containerPanel();
        if(this.formCont !== undefined)
            this.cPanel.add(this.formCont);
        this.add(this.cPanel);
    }
});
