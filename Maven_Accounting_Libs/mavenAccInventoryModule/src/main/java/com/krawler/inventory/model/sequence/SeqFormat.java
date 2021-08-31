/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence;

import com.krawler.common.admin.Company;
import com.krawler.common.util.StringUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Vipin Gupta
 */
public class SeqFormat {

    private String id;
    private SeqModule seqModule;
    private Company company;
    private String suffix;
    private String prefix;
    private int numberOfDigits;
    private long startFrom;
    private boolean defaultFormat;
    private boolean active;
    private SeqDateFormat prefixDateFormat;
    private SeqDateFormat suffixDateFormat;
    private String separator;

    public SeqFormat() {
        this.numberOfDigits = 10;
        this.startFrom = 0;
        this.active = true;
    }

    public SeqDateFormat getPrefixDateFormat() {
        return prefixDateFormat;
    }

    public void setPrefixDateFormat(SeqDateFormat prefixDateFormat) {
        this.prefixDateFormat = prefixDateFormat;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public SeqDateFormat getSuffixDateFormat() {
        return suffixDateFormat;
    }

    public void setSuffixDateFormat(SeqDateFormat suffixDateFormat) {
        this.suffixDateFormat = suffixDateFormat;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumberOfDigits() {
        return numberOfDigits;
    }

    public void setNumberOfDigits(int numberOfDigits) {
        this.numberOfDigits = numberOfDigits;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public SeqModule getSeqModule() {
        return seqModule;
    }

    public void setSeqModule(SeqModule seqModule) {
        this.seqModule = seqModule;
    }

    public long getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(long startFrom) {
        this.startFrom = startFrom;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isDefaultFormat() {
        return defaultFormat;
    }

    public void setDefaultFormat(boolean defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValidData() {
        boolean valid = false;
        if (seqModule != null && numberOfDigits > 0) {
            valid = true;
        }
        return valid;
    }

    public long getMaxSeqNumber() {
        return (long) Math.pow(10, numberOfDigits) - 1;
    }

    public String getFormat() {
        return getFormat(startFrom);
    }

    public String getFormat(long sequenceNumber) { // prefix-prefixdate-seqnumber-suffixdate-suffix
        String sep = "";
        if (!StringUtil.isNullOrEmpty(this.separator)) {
            sep = this.separator;
        }
        StringBuilder formatedSeqNumber = new StringBuilder();
        if (sequenceNumber < 0) {
            sequenceNumber = -1 * sequenceNumber;
        }
//        if (isValidData()) {
        String formatedDigits = formatDigits(sequenceNumber);
        if (!StringUtil.isNullOrEmpty(prefix)) {
            formatedSeqNumber.append(prefix).append(sep);
        }
        if (prefixDateFormat != null) {
            DateFormat dfPrefix = new SimpleDateFormat(prefixDateFormat.getStringName());
            String dateString = dfPrefix.format(new Date());
            formatedSeqNumber.append(dateString).append(sep);
        }
        formatedSeqNumber.append(formatedDigits);
        if (suffixDateFormat != null) {
            DateFormat dfSuffix = new SimpleDateFormat(suffixDateFormat.getStringName());
            String dateString = dfSuffix.format(new Date());
            formatedSeqNumber.append(sep).append(dateString);
        }
        if (!StringUtil.isNullOrEmpty(suffix)) {
            formatedSeqNumber.append(sep).append(suffix);
        }
//        }
        return formatedSeqNumber.toString();
    }

    public String formatWithoutPrefixSuffix(long sequenceNumber) {
        String formatedSeqNumber = "";
        if (isValidData()) {
            formatedSeqNumber = formatDigits(sequenceNumber);
        }
        return formatedSeqNumber;
    }

    private String formatDigits(long seqNumber) {
        String formatedDigits = String.valueOf(seqNumber).toString();
        int currentLen = formatedDigits.length();
        if (currentLen <= numberOfDigits) {
            int extraZerosLen = numberOfDigits - currentLen;
            String prefixZeros = "";
            for (int i = 0; i < extraZerosLen; i++) {
                prefixZeros += "0";
            }
            formatedDigits = prefixZeros + formatedDigits;
        }
        return formatedDigits;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj != null && obj instanceof SeqFormat) {
            SeqFormat seq = (SeqFormat) obj;
            if (this.id.equals(seq.getId())) {
                equal = true;
            }
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
