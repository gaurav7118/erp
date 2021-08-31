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

Wtf.account.PackagingContract=function(config){
    
    this.id=config.id;
    this.modeName=config.modeName;
    this.isPackagingContract=config.isPackagingContract?config.isPackagingContract:false;
    
    Wtf.apply(this, config);

    Wtf.account.PackagingContract.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.account.PackagingContract,Wtf.Panel,{
    autoScroll: true,
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:false,
    closable : false,
    autoHeight:true,
    initComponent:function(config){
        Wtf.account.PackagingContract.superclass.initComponent.call(this,config);
        
        //Create Product grid
        this.createProductGrid();
        
    },
    
    onRender:function(config){
        
        this.centerPanel=new Wtf.Panel({
            region:'center',
            border:false,
            scope:this,
//            width:"100%",
            autoHeight:true,
            id:'centerPanel'+this.id,
            items:[
                this.ProductGrid
            ]
        });
//        this.GridPanel.doLayout();
        this.centerPanel.doLayout();
        
        this.add(this.centerPanel);
                
        Wtf.account.PackagingContract.superclass.onRender.call(this, config);
    },
    
    createProductGrid: function(){
        this.ProductGrid=new Wtf.account.MRPProductDetailsGrid({
            layout:"fit",
            id:this.id+"editproductdetailsgrid",
            bodyBorder:true,
            border:false,
            bodyStyle:'padding:10px',
            height:300,
            isInitialQuatiy:false, 
            excluseDateFilters:true,
            rendermode:"productform",
            isPackagingContract:this.isPackagingContract,
            isEdit: this.isEdit,
            record: this.record
        });
        
    }
});