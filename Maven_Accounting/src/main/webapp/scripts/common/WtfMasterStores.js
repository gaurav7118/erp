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

Wtf.StoreManagerKeys = {
    autopayment : 'autopayment',
    autoreceipt : 'autoreceipt'
}
Wtf.sequenceFormatStoreRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'value'},
    {name: 'oldflag'}
]);

Wtf.PaySeqFormatStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        totalProperty: 'count',
        root: "data"
    }, Wtf.sequenceFormatStoreRec),
    url: "ACCCompanyPref/getSequenceFormatStore.do",
    baseParams: {
        mode: 'autopayment'
    }
});

Wtf.ReceiptSeqFormatStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        totalProperty: 'count',
        root: "data"
    }, Wtf.sequenceFormatStoreRec),
    url: "ACCCompanyPref/getSequenceFormatStore.do",
    baseParams: {
        mode: 'autoreceipt'
    }
});

function chkPaySeqFormatStoreLoad(){
    if(!Wtf.StoreMgr.containsKey(Wtf.StoreManagerKeys.autopayment)){
        Wtf.PaySeqFormatStore.load();
        Wtf.StoreMgr.add(Wtf.StoreManagerKeys.autopayment,Wtf.PaySeqFormatStore)
    }
}

function chkReceiptSeqFormatStoreLoad(){
    if(!Wtf.StoreMgr.containsKey(Wtf.StoreManagerKeys.autoreceipt)){
        Wtf.ReceiptSeqFormatStore.load();
        Wtf.StoreMgr.add(Wtf.StoreManagerKeys.autoreceipt,Wtf.ReceiptSeqFormatStore)
    }
}