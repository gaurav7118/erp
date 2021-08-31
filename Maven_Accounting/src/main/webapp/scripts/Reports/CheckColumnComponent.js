/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


// -------------- CheckColumn Plugin for grid -------------------------//
/*
 * Please make sure that while creating check column you should assign id to it otherwise it will create problems.
 */
Wtf.CheckColumnComponent = function(config){ 
    Wtf.apply(this, config);
    this.renderer = this.renderer.createDelegate(this);
};
Wtf.CheckColumnComponent.prototype ={     
    init : function(grid){},
    renderer : function(v, p, record){
        if (record.data.POCode != undefined && record.data.POCode != ""  && record.data.POCode != "") {
            return record.data.POCode;   
        } else if (record.data.periodtype == 1 || record.data.periodtype == 2 || record.data.genpo == "no") {
            return '';
        } else {
            p.css += ' x-grid3-check-col-td';
            return '<div class="x-grid3-check-col' + (v ? '-on' : '') + ' x-grid3-cc-' + this.id + '">&#160;</div>';//on check/uncheck we are just changing image in css image - css available in grid.css file 
        }
    }
};