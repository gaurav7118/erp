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
Wtf.WtfWidgetComponent = function(config){
	config.draggable = true;
	Wtf.apply(this, config);
	Wtf.WtfWidgetComponent.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.WtfWidgetComponent, Wtf.Panel, {
	onRender: function(config){
		Wtf.WtfWidgetComponent.superclass.onRender.call(this, config);        
	}
});

Wtf.WtfIframeWidgetComponent = function(config){
	config.draggable = true;	
	Wtf.apply(this, config);
	Wtf.WtfIframeWidgetComponent.superclass.constructor.call(this, config);
	this.url = config.url;
}

Wtf.extend(Wtf.WtfIframeWidgetComponent, Wtf.Panel, {
	onRender: function(config){
		Wtf.WtfIframeWidgetComponent.superclass.onRender.call(this, config);
        this.html = "<iframe vscroll=false frameborder='0' height='100%' width='100%' src='"+ this.url + "' style='background-color:white'></iframe>";
	}
});
