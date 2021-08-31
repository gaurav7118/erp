/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


  $('#calendar').fullCalendar({
    // put your options and callbacks here
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'month,basicWeek,basicDay'
        },
        defaultView: 'month',
        //                   height:'auto',
        aspectRatio:1.9,
        eventSources: [{
            url: 'ACCInvoiceCMN/getCustInvoiceDueEvents.do',
            type: 'POST',
            color: '#c5eff7',   // an option!
            textColor: 'black' // an option!

        },{
            url: 'ACCGoodsReceiptCMN/getVendorInvoiceDueEvents.do',
            type: 'POST',
            color: '#c8f7c5',   // an option!
            textColor: 'black' // an option!

        }],
        eventLimit:{
            'default':2
        }
});
          