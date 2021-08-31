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
public class OpeningBalanceCreditNoteCustomData extends AccCustomData{
    private String OpeningBalanceCreditNoteId;
    private CreditNote OpeningBalanceCreditNote;
    private String moduleId;

    public CreditNote getOpeningBalanceCreditNote() {
        return OpeningBalanceCreditNote;
    }

    public void setOpeningBalanceCreditNote(CreditNote OpeningBalanceCreditNote) {
        this.OpeningBalanceCreditNote = OpeningBalanceCreditNote;
    }

    public String getOpeningBalanceCreditNoteId() {
        return OpeningBalanceCreditNoteId;
    }

    public void setOpeningBalanceCreditNoteId(String OpeningBalanceCreditNoteId) {
        this.OpeningBalanceCreditNoteId = OpeningBalanceCreditNoteId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    
}
