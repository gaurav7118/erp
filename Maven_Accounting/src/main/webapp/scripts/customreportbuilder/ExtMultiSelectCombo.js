Ext.define('Ext.form.field.MultiSelectCombo', {
    extend: 'Ext.form.field.Tag',
    xtype: 'multiselectcombo',
    grow: false,
    autoSelect: false,
    hideTrigger: true,
    isInputFieldExpanded: false,
    triggers: {
        expandListTrigger: {
            weight: -1,
            cls: 'x-form-default-trigger-custom',
            hidden: false
        },
        clearTrigger: {
            weight: -2,
            cls: 'x-form-clear-trigger-custom',
            hidden: false
        }
    },
    onRender: function () {
        var me = this;
        me.callParent();
        
        if (me.getTrigger('expandListTrigger')) {
            me.getTrigger('expandListTrigger').handler = me.onExpandListTriggerHandler;
        }
        
        if (me.getTrigger('clearTrigger')) {
            me.getTrigger('clearTrigger').handler = me.clearTriggerHandler;
        }
        
        if (me.el) {
            me.el.on('mousedown', me.onInputFieldMouseDown, me);
        }
        if (me.inputEl) {
            me.inputEl.on('blur', me.onInputFieldBlur, me);
        }
        
        me.on('beforeselect', me.onListItemSelect, me);
        me.on('beforedeselect', me.onListItemDeselect, me);
        me.on('change', me.onChangeHandler, me);
    },
    onInputFieldMouseDown: function (e) {
        var me = this;
        if (me.emptyEl) {
            me.emptyEl.setStyle('display', 'none');
        }
        if (me.inputEl) {
            me.inputEl.setStyle('display', 'block');
        }
        me.focus();
    },
    onInputFieldBlur: function () {
        var me = this;
        if (me.getValue().length == 0) {
            if (me.emptyEl) {
                me.emptyEl.setStyle('display', 'block');
            }
            if (me.inputEl) {
                me.inputEl.setStyle('display', 'none');
            }
        }
    },
    onListItemSelect: function (me, record, eOpts) {
        if (me.listWrapper) {
            me.listWrapper.addCls('multiselect-list-wrapper-max-height');
        }
        if (me.getTrigger('clearTrigger')) {
            me.getTrigger('clearTrigger').show();
        }
        if (me.getTrigger('expandListTrigger')) {
            me.getTrigger('expandListTrigger').show();
        }
        
        me.expandInputField();
    },
    onListItemDeselect: function (me, record, eOpts) {
        if (me.getValue().length < 2) {
            if (me.listWrapper) {
                me.listWrapper.removeCls('multiselect-list-wrapper-max-height');
            }
            if (me.getTrigger('clearTrigger')) {
                me.getTrigger('clearTrigger').hide();
            }
        }
        
        me.collapseInputField();
    },
    clearTriggerHandler: function () {
        var me = this;
        me.reset();
        if (me.listWrapper) {
            me.listWrapper.removeCls('multiselect-list-wrapper-max-height');
        }
        if (me.getTrigger('clearTrigger')) {
            me.getTrigger('clearTrigger').hide();
        }
        if (me.getTrigger('expandListTrigger')) {
            me.getTrigger('expandListTrigger').hide();
        }
        
        me.collapseInputField();
    },
    onChangeHandler: function (me, newVal, oldVal) {
        if (me.getValue().length == 0) {
            if (me.getTrigger('clearTrigger')) {
                me.getTrigger('clearTrigger').hide();
            }
            if (me.listWrapper) {
                me.listWrapper.removeCls('multiselect-list-wrapper-max-height');
            }
            if (me.emptyEl) {
                me.emptyEl.setStyle('display', 'block');
            }
            if (me.inputEl) {
                me.inputEl.setStyle('display', 'none');
            }
        }
    },
    onExpandListTriggerHandler: function () {
        var me = this;
        if (!me.isInputFieldExpanded) {
            me.expandInputField();
        } else {
            me.collapseInputField();
        }
    },
    expandInputField: function () {
        var me = this;
        me.setHeight(70);
        if (me.listWrapper) {
            me.listWrapper.addCls('multiselect-list-wrapper-height');
        }
        if (me.getTrigger('expandListTrigger')) {
            me.getTrigger('expandListTrigger').el.addCls('x-form-trigger-flip-vertically');
        }
        me.isInputFieldExpanded = true;
    },
    collapseInputField: function () {
        var me = this;
        me.setHeight(23);
        if (me.listWrapper) {
            me.listWrapper.removeCls('multiselect-list-wrapper-height');
        }
        if (me.getTrigger('expandListTrigger')) {
            me.getTrigger('expandListTrigger').el.removeCls('x-form-trigger-flip-vertically');
        }
        me.isInputFieldExpanded = false;
    }
});