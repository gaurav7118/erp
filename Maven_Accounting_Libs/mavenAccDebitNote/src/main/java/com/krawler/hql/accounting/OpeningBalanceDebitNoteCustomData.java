/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class OpeningBalanceDebitNoteCustomData extends AccCustomData{
    private String OpeningBalanceDebitNoteId;
    private DebitNote OpeningBalanceDebitNote;
    private String moduleId;

    public DebitNote getOpeningBalanceDebitNote() {
        return OpeningBalanceDebitNote;
    }

    public void setOpeningBalanceDebitNote(DebitNote OpeningBalanceDebitNote) {
        this.OpeningBalanceDebitNote = OpeningBalanceDebitNote;
    }

    public String getOpeningBalanceDebitNoteId() {
        return OpeningBalanceDebitNoteId;
    }

    public void setOpeningBalanceDebitNoteId(String OpeningBalanceDebitNoteId) {
        this.OpeningBalanceDebitNoteId = OpeningBalanceDebitNoteId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    
}
