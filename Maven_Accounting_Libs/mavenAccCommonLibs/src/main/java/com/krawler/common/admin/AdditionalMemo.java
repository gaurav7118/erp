/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.common.admin;

/**
 * This POJO created to store "ID KETERANGAN TAMBAHAN"(Additional Memo) default
 * value if Customer/Vendor Tax Type is 07 or 08 for INDONESIA country ERP-41891
 * If 07 = 
 * 1 = Kawasan Bebas
 * 2 = Tempat Penimbunan Berikat
 * 3 = Hibah dan Bantuan Luar Negeri
 * 4 = Avtur
 * 5 = Lainnya
 * 6 = Kontraktor Perjanjian Karya Pengusahaan Pertambangan Batubara Generasi I
 * 
 * If 08 = 
 * 1 = BKP dan JKP Tertentu 
 * 2 = BKP Tertentu yang Bersifat Strategis 
 * 3 = Jasa Kebandarudaraan 
 * 4 = Lainnya
 *
 * @author Rahul A. Bhawar
 */
public class AdditionalMemo {

    private String id;
    private String name;
    private String code;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
