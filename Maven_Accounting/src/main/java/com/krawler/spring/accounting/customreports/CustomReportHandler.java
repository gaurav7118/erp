/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.util.Constants;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.udojava.evalex.Expression;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mvel2.MVEL;

/**
 *
 * @author krawler
 */
public class CustomReportHandler {
    
      
    public static JSONObject executeRegularExpression(JSONArray requestArr, String expression, String key, int precision) throws IOException, JSONException {
        JSONObject responseObj = new JSONObject();
        Expression exp = new Expression(expression);
        List<String> vars = exp.getUsedVariables();
        Map<String, Map> varMap = new HashMap<String, Map>();
        try {
            for (int j = 0; j < requestArr.length(); j++) {
                Map<String, Object> variableMapForFormula = new HashMap<String, Object>();
                JSONObject responseCustomData = (JSONObject) requestArr.get(j);

                for (int i = 0; i < vars.size(); i++) {
                    String id = vars.get(i);
                    if (!variableMapForFormula.containsKey(id)) {
                        String toBeReplaced = id.substring(8, id.length());
                        String idNew = toBeReplaced.replaceAll("_", "-"); //replacing underscores with hyphens
                        Double value = responseCustomData.optDouble(idNew, 0.0);
                        variableMapForFormula.put(id, value);
                    }
                }
                varMap.put(responseCustomData.optString(Constants.billid), variableMapForFormula);
            }
            evaluateExpression(expression, key, varMap, precision, requestArr);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            responseObj.put(Constants.data, requestArr);
        }
        return responseObj;
    }

    public static void evaluateExpression(String expression, String key, Map<String, Map> variables, Integer precisonFormat, JSONArray responseArr) throws JSONException {
        Object result = null;
        if (variables != null && !variables.isEmpty()) {
            Object compiledExpression = MVEL.compileExpression(expression.replaceAll(" ",""));
            for (int i = 0; i < responseArr.length(); i++) {
                JSONObject jobj = (JSONObject) responseArr.get(i);
                Map variable = variables.get(jobj.optString(Constants.billid));
                try {
                    result = MVEL.executeExpression(compiledExpression, variable);
                    if (result instanceof Double) {
                        Double d = (Double) result;
                        if (d == null || d.isInfinite() || d.isNaN()) {
                            result = 0.0;
                        } else if (d == -0.0) {
                            result = 0.0;
                        } else {
                            DecimalFormat decimal = getPrecisonFormat(precisonFormat);
                            result = decimal.format(result);
                        }
                    }
                    jobj.put(key, result);
                } catch (Exception e) {
                    jobj.put(key, "0.00");
                    e.printStackTrace();
                }
            }
        }
    }

    public static DecimalFormat getPrecisonFormat(Integer precisonFormat) {

        DecimalFormat decimal = new DecimalFormat("#.##");

        switch (precisonFormat) {
            case 1:
                decimal = new DecimalFormat("#.#");
                break;
            case 2:
                decimal = new DecimalFormat("#.##");
                break;
            case 3:
                decimal = new DecimalFormat("#.###");
                break;
            case 4:
                decimal = new DecimalFormat("#.####");
                break;
        }
        return decimal;
    }
}
