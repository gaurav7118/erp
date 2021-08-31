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
public class AccJECustomData extends AccCustomData {

    private String journalentryId;
    private JournalEntry journalentry;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getJournalentryId() {
        return journalentryId;
    }

    public void setJournalentryId(String journalentryId) {
        this.journalentryId = journalentryId;
    }

    public JournalEntry getJournalentry() {
        return journalentry;
    }

    public void setJournalentry(JournalEntry journalentry) {
        this.journalentry = journalentry;
    }
}
