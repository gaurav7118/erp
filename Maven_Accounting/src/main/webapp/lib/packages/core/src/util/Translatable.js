Ext.define("Ext.util.Translatable",{requires:["Ext.util.translatable.CssTransform","Ext.util.translatable.ScrollPosition","Ext.util.translatable.ScrollParent","Ext.util.translatable.CssPosition"],constructor:function(A){var B=Ext.util.translatable;switch(Ext.browser.getPreferredTranslationMethod(A)){case"scrollposition":return new B.ScrollPosition(A);case"scrollparent":return new B.ScrollParent(A);case"csstransform":return new B.CssTransform(A);case"cssposition":return new B.CssPosition(A)}}})