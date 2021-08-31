/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.overrides.util.Collection', {
    override: 'Ext.util.Collection',
    _aggregators: {
        average: function(items, begin, end, property, root) {
//            console.log('*** In average method of ReportBuilder.overrides.util.Collection ***');
            var n = end - begin;
            return n && this._aggregators.sum.call(this, items, begin, end, property, root) / n;
        },
        bounds: function(items, begin, end, property, root) {
//            console.log('*** In bounds method of ReportBuilder.overrides.util.Collection ***');
            for (var value, max, min,
                i = Number(begin); i < end; ++i) {                       //here is the change converting "begin" from string type to number type
                value = items[i];
                value = Number((root ? value[root] : value)[property]);  //here is the change converting "value" from string type to number type
                // First pass max and min are undefined and since nothing is less than
                // or greater than undefined we always evaluate these "if" statements as
                // true to pick up the first value as both max and min.
                if (!(value < max)) {
                    // jshint ignore:line
                    max = value;
                }
                if (!(value > min)) {
                    // jshint ignore:line
                    min = value;
                }
            }
            return [
            min,
            max
            ];
        },
        count: function(items) {
//            console.log('*** In count method of ReportBuilder.overrides.util.Collection ***');
            return items.length;
        },
        extremes: function(items, begin, end, property, root) {
//            console.log('*** In extremes method of ReportBuilder.overrides.util.Collection ***');
            var most = null,
            least = null,
            i, item, max, min, value;
            for (i = begin; i < end; ++i) {
                item = items[i];
                value = Number((root ? item[root] : item)[property]);        //here is the change converting "value" from string type to number type
                // Same trick as "bounds"
                if (!(value < max)) {
                    // jshint ignore:line
                    max = value;
                    most = item;
                }
                if (!(value > min)) {
                    // jshint ignore:line
                    min = value;
                    least = item;
                }
            }
            return [
            least,
            most
            ];
        },
        max: function(items, begin, end, property, root) {
//            console.log('*** In max method of ReportBuilder.overrides.util.Collection ***');
            var b = this._aggregators.bounds.call(this, items, begin, end, property, root);
            return b[1];
        },
        maxItem: function(items, begin, end, property, root) {
//            console.log('*** In maxItem method of ReportBuilder.overrides.util.Collection ***');
            var b = this._aggregators.extremes.call(this, items, begin, end, property, root);
            return b[1];
        },
        min: function(items, begin, end, property, root) {
//            console.log('*** In min method of ReportBuilder.overrides.util.Collection ***');
            var b = this._aggregators.bounds.call(this, items, begin, end, property, root);
            return b[0];
        },
        minItem: function(items, begin, end, property, root) {
//            console.log('*** In minItem method of ReportBuilder.overrides.util.Collection ***');
            var b = this._aggregators.extremes.call(this, items, begin, end, property, root);
            return b[0];
        },
        sum: function(items, begin, end, property, root) {
//            console.log('*** In sum method of ReportBuilder.overrides.util.Collection ***');
            for (var value,
                sum = 0,
                i = begin; i < end; ++i) {
                value = items[i];
                value = Number((root ? value[root] : value)[property]);     //here is the change converting "value" from string type to number type
                sum += value;
            }
            return sum;
        }
    }
});