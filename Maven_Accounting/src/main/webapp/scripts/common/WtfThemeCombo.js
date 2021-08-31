/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */


Wtf.ux.ThemeCombo = Wtf.extend(Wtf.form.ComboBox, {
     themeBlueText: 'Default Blue Theme',
     themeGrayText: 'Gray Theme',
     themeBlackText: 'Black Theme',
     themeOliveText: 'Olive Theme',
     themePurpleText: 'Purple Theme',
     themeDarkGrayText: 'Dark Gray Theme',
     themeSlateText: 'Slate Theme',
     themeVistaText: 'Vista Theme',
     themePeppermintText: 'Peppermint Theme',
     themePinkText: 'Pink Theme',
     themeChocolateText: 'Chocolate Theme',
     themeGreenText: 'Green Theme',
     themeIndigoText: 'Indigo Theme',
     themeMidnightText: 'Midnight Theme',
     themeSilverCherryText: 'Silver Cherry Theme',
     themeSlicknessText: 'Slickness Theme',
     themeVar:'theme',
     selectThemeText: 'Select Theme',
     themeGrayExtndText:'Gray-Extended Theme',
     lazyRender:true,
     lazyInit:true,
     cssPath:'lib/resources/css/', 
    initComponent:function() {

        Wtf.apply(this, {
            store: new Wtf.data.SimpleStore({
                fields: ['themeFile', {name:'themeName', type:'string'}]
                ,data: [
                     ['xtheme-default.css', this.themeBlueText]
                    ,['xtheme-gray.css', this.themeGrayText]
                    ,['xtheme-darkgray.css', this.themeDarkGrayText]
                    ,['xtheme-black.css', this.themeBlackText]
                    ,['xtheme-olive.css', this.themeOliveText]
                    ,['xtheme-purple.css', this.themePurpleText]
                    ,['xtheme-slate.css', this.themeSlateText]
//                    ,['xtheme-peppermint.css', this.themePeppermintText]
//                    ,['xtheme-chocolate.css', this.themeChocolateText]
                    ,['xtheme-green.css', this.themeGreenText]
                    ,['xtheme-indigo.css', this.themeIndigoText]
                    ,['xtheme-midnight.css', this.themeMidnightText]
                    ,['xtheme-silverCherry.css', this.themeSilverCherryText]
                    ,['xtheme-slickness.css', this.themeSlicknessText]
                    ,['xtheme-gray-extend.css', this.themeGrayExtndText]
                    ,['xtheme-vista.css', this.themeVistaText]
//                    ,['xtheme-pink.css', this.themePinkText]
                ]
            })
            ,valueField: 'themeFile'
            ,displayField: 'themeName'
            ,triggerAction:'all'
            ,mode: 'local'
            ,forceSelection:true
            ,editable:false
            ,fieldLabel: this.selectThemeText
        }); 

        this.store.sort('themeName');

        // call parent
        Wtf.ux.ThemeCombo.superclass.initComponent.apply(this, arguments);

//        if(false !== this.stateful && Wtf.state.Manager.getProvider()) {
//            this.setValue(Wtf.state.Manager.get(this.themeVar) || 'xtheme-default.css');
//        }
//        if(Wtf.theme) {
//            this.setValue(Wtf.theme || 'xtheme-default.css');
//        }
//        else {
//            this.setValue('xtheme-default.css');
//        }

    }, // end of function initComponent
    setValue:function(val) {
        Wtf.ux.ThemeCombo.superclass.setValue.apply(this, arguments);

        // set theme
        Wtf.util.CSS.swapStyleSheet(this.themeVar, this.cssPath + val);

        if(false !== this.stateful && Wtf.state.Manager.getProvider()) {
            Wtf.state.Manager.set(this.themeVar, val);
        }
    } // eo function setValue

}); // end of extend

// register xtype
Wtf.reg('themecombo', Wtf.ux.ThemeCombo);

