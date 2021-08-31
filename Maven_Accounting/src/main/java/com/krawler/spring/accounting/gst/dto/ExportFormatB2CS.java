/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.gst.dto;

import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class ExportFormatB2CS {

    public static void main(String[] args) {
        ExportFormatB2CS obj = new ExportFormatB2CS();
        obj.loadData();
    }

    private void loadData() {
        List<B2CS> list = new ArrayList<B2CS>();
        B2CS b2cs = new B2CS();
        b2cs.setTyp("E");
        b2cs.setPos("37-Andhra Pradesh");
        b2cs.setRt(5.00f);
        b2cs.setTxval(500000.00);
        b2cs.setEtin("01ABCDE1234E1CF");
        list.add(b2cs);

        b2cs = new B2CS();
        b2cs.setTyp("OE");
        b2cs.setPos("37-Andhra Pradesh");
        b2cs.setRt(28.00f);
        b2cs.setTxval(50000);
        b2cs.setCamt(20756);
        list.add(b2cs);

        b2cs = new B2CS();
        b2cs.setTyp("E");
        b2cs.setPos("32-Kerala");
        b2cs.setRt(12.00f);
        b2cs.setTxval(250000.00);
        b2cs.setEtin("01ABCDE1234E1CF");
        list.add(b2cs);

        b2cs = new B2CS();
        b2cs.setTyp("OE");
        b2cs.setPos("37-Andhra Pradesh");
        b2cs.setRt(5.00f);
        b2cs.setTxval(76000.45);
        list.add(b2cs);

        b2cs = new B2CS();
        b2cs.setTyp("E");
        b2cs.setPos("36-Telengana");
        b2cs.setRt(12.00f);
        b2cs.setTxval(350004.56);
        b2cs.setEtin("01ABCDE1234E1CF");
        list.add(b2cs);

        b2cs = new B2CS();
        b2cs.setTyp("E");
        b2cs.setPos("36-Telengana");
        b2cs.setRt(12.00f);
        b2cs.setTxval(10000.00);
        b2cs.setEtin("01ABCDE1234E1CF");
        list.add(b2cs);

        exportDataFormat(list);
        
    }

    public Map<UniqueParam, Map<String, Double>> exportDataFormat(List<B2CS> list) {
        Map<UniqueParam, Map<String, Double>> collection = new HashMap<UniqueParam, Map<String, Double>>();
        Set<UniqueParam> set = new HashSet<UniqueParam>();
        for (B2CS list1 : list) {
            String pos = String.format("%02d", list1.getStcode()) + "-" + list1.getPos();
            UniqueParam innerClass = new UniqueParam(list1.getEtin() != null ? list1.getEtin() : "",
                    list1.getTyp(), pos, list1.getRt());
            Map<String, Double> map = new HashMap<String, Double>();
            if(set.add(innerClass)){
                map.put("txval", list1.getTxval());
                map.put("cess", list1.getCsamt());
                collection.put(innerClass, map);                
            }else{
                Map<String, Double> oldMap = collection.get(innerClass);
                Double txval = oldMap.get("txval");
                Double cess = oldMap.get("cess");

                map.put("txval", txval + list1.getTxval());
                map.put("cess", cess + list1.getCsamt());                
                collection.put(innerClass, map);
            }
        }
        return collection;
    }
    
    public Map<UniqueParam, Map<String, Double>> exportDataFormat(JSONArray b2clArray) throws JSONException {
        Map<UniqueParam, Map<String, Double>> collection = new HashMap<UniqueParam, Map<String, Double>>();
        Set<UniqueParam> set = new HashSet<UniqueParam>();
        for (int i = 0; i < b2clArray.length(); i++) {
            JSONObject b2b = b2clArray.getJSONObject(i);
            
            String pos = String.format("%02d", b2b.getInt("stcode")) + "-" + b2b.getString("pos");
            
            UniqueParam innerClass = new UniqueParam(b2b.optString("etin"),
                    b2b.getString("typ"), pos, b2b.getDouble("rt"));
            
            Map<String, Double> map = new HashMap<String, Double>();
            if(set.add(innerClass)){
                map.put("txval", b2b.getDouble("txval"));
                map.put("cess", b2b.getDouble("csamt"));
                collection.put(innerClass, map);                
            }else{
                Map<String, Double> oldMap = collection.get(innerClass);
                Double txval = oldMap.get("txval");
                Double cess = oldMap.get("cess");

                map.put("txval", txval + b2b.getDouble("txval"));
                map.put("cess", cess + b2b.getDouble("csamt"));                
                collection.put(innerClass, map);
            }
        }
        
//        for (Entry<UniqueParam, Map<String, Double>> entry : collection.entrySet()) {
//            System.out.println("entry : "+entry.getKey().toString());
//            System.out.println("value : "+entry.getValue().toString());
//            
//        }
//        System.out.println("collection : " + collection);   
        return collection;
    }

    public class UniqueParam {

        private String eComNumber;
        private String type;
        private String pos;
        private double rate;

        public UniqueParam(String ecomno, String type, String pos, double rate) {
            this.eComNumber = ecomno;
            this.type = type;
            this.pos = pos;
            this.rate = rate;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eComNumber, type, pos, rate);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof UniqueParam) {
                UniqueParam obj = (UniqueParam) o;
                return Objects.equals(eComNumber, obj.eComNumber)
                        && Objects.equals(type, obj.type)
                        && Objects.equals(pos, obj.pos)
                        && rate == obj.rate;
            } else {
                return false;
            }
        }

        public String toString(){
            return type + " " + pos + " " + rate + " "+ eComNumber;
        }
        
        public String geteComNumber() {
            return eComNumber;
        }

        public void seteComNumber(String eComNumber) {
            this.eComNumber = eComNumber;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

    }
}