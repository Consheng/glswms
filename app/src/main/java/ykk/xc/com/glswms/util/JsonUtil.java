package ykk.xc.com.glswms.util;

import android.util.Log;

import com.solidfire.gson.Gson;
import com.solidfire.gson.JsonArray;
import com.solidfire.gson.JsonElement;
import com.solidfire.gson.JsonObject;
import com.solidfire.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 转换类
 */
public class JsonUtil {
    public static Gson mGson = new Gson();
//    对象转为字符串
//
//    String ps =gson.toJson(person);
//
//
//    字符串转为对象
//
//    Person person =gson.fromJson(json, Person.class);
//
//
//    字符串为为list
//
//    List<Person> persons =gson.fromJson(json, new TypeToken<List<Person>>() {}.getType());


    /**
     * 将json字符串转化成实体对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T stringToObject(String json, Class<T> classOfT) {
        return mGson.fromJson(json, classOfT);

    }

    /**
     * 将对象换为json字符串 或者 把list 转化成json
     *
     * @param object
     * @param <T>
     * @return
     */
    public static <T> String objectToString(T object) {
        return mGson.toJson(object);
    }

    /**
     * 把json 字符串转化成list
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> stringToList(String json, Class<T> cls) {
        // 当为一个对象的时候，返回的是json对象，而不是数组
        if(json.indexOf("[") == -1) {
            json = "[" +json + "]";
        }
//        Gson gson = new Gson();

        List<T> list = new ArrayList<T>();

        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            list.add(mGson.fromJson(elem, cls));
        }

        return list;

    }

    /**
     * 判断后台的值是成功的还是失败
     * {"code":100,"msg":"处理成功","extend":{}}
     * {"code":200,"msg":"处理失败","extend":{}}
     */
    public static boolean isSuccess(String strJson) {
        try{
            return strJson.indexOf("{\"code\":100,\"msg\":\"处理成功\"") > -1;
        }catch(Exception e) {
            Log.e("isSuccess-Exception", strJson+"\n"+e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 是否可以加载下一页数据
     */
    public static boolean isNextPage(String strJson) {
        JsonObject jsonObj = null;
        try{
            JsonObject jsonObj2 = new JsonParser().parse(strJson).getAsJsonObject();
            JsonObject extend = jsonObj2.getAsJsonObject("extend");
            if(extend.has("ykk_jsonArr")) { // 判断有没有数据集
                jsonObj = extend.getAsJsonObject("ykk_jsonArr");
            }
        }catch(Exception e) {
            Log.e("isSuccess-Exception", strJson+"\n"+e);
            e.printStackTrace();
            return false;
        }
        return jsonObj.get("hasNextPage").getAsBoolean();
    }

    /**
     * json --> list 实体类（读取自定义的json数据）无分页模块
     * {"code":100,"msg":"处理成功","extend":{"ykk_jsonArr":{"id":1,"username":"admin","password":"123456","sex":1,"turename":null,"deptId":1,"createTime":1525932994633,"createrId":0,"createrName":"","dept":null}}}
     */
    public static <T> List<T> strToList(String strJson, Class<T> cls) {
        JsonObject jsonObj = new JsonParser().parse(strJson).getAsJsonObject();

        JsonObject extend = jsonObj.getAsJsonObject("extend");
        if(extend.has("ykk_jsonArr")) { // 判断有没有数据集
            JsonParser parse = new JsonParser();
            String strDatas = extend.get("ykk_jsonArr").toString();
            if(strDatas.indexOf("[") == -1) {
                strDatas = "[" +strDatas + "]";
            }
            JsonArray array = parse.parse(strDatas).getAsJsonArray();
            List<T> list = new ArrayList<T>();

            for (final JsonElement elem : array) {
                list.add(mGson.fromJson(elem, cls));
            }
            return list;
        }
        return null;
    }
    /**
     * json --> list 实体类（读取自定义的json数据）有分页模块
     * {"code":100,"msg":"处理成功","extend":{"ykk_jsonArr":
     *      {"pageNum":1,"pageSize":1,"size":1,"startRow":1,"endRow":1,"total":3,"pages":3,"list":
     *      [{"id":1,"username":"admin","password":"123456","sex":1,"turename":null,"deptId":1,"createTime":1525932994633,"createrId":0,"createrName":"","dept":null}],
     *      "prePage":0,"nextPage":2,"isFirstPage":true,"isLastPage":false,"hasPreviousPage":false,"hasNextPage":true,"navigatePages":1,"navigatepageNums":[1],"navigateFirstPage":1,"navigateLastPage":1,"firstPage":1,"lastPage":1}}}
     */
    public static <T> List<T> strToList2(String strJson, Class<T> cls) {
        JsonObject jsonObj = new JsonParser().parse(strJson).getAsJsonObject();

        JsonObject extend = jsonObj.getAsJsonObject("extend");
        if(extend.has("ykk_jsonArr")) { // 判断有没有数据集
            JsonParser parse = new JsonParser();
            JsonArray array = extend.getAsJsonObject("ykk_jsonArr").getAsJsonArray("list");
            List<T> list = new ArrayList<T>();

            for (final JsonElement elem : array) {
                list.add(mGson.fromJson(elem, cls));
            }
            return list;
        }
        return null;
    }

    /**
     * json --> list (String)（读取自定义的json数据）无分页模块
     * {"code":100,"msg":"处理成功","extend":{"ykk_jsonArr":["124","2"]}}
     */
    public static <T> List<T> strToList(String strJson) {
        JsonObject jsonObj = new JsonParser().parse(strJson).getAsJsonObject();

        JsonObject extend = jsonObj.getAsJsonObject("extend");
        if(extend.has("ykk_jsonArr")) { // 判断有没有数据集
            JsonParser parse = new JsonParser();
            String strDatas = extend.getAsJsonArray("ykk_jsonArr").toString();
            if(strDatas.indexOf("[") == -1) {
                strDatas = "[" +strDatas + "]";
            }
            JsonArray array = parse.parse(strDatas).getAsJsonArray();
            List<T> list = new ArrayList<T>();

            for (int i=0, size=array.size(); i<size; i++) {
                list.add((T) array.get(i).getAsString());
            }
            return list;
        }
        return null;
    }
    /**
     * json --> list(String) 读取自定义的json数据）有分页模块
     * {"code":100,"msg":"处理成功","extend":{"ykk_jsonArr":{"pageNum":0,"pageSize":0,"size":5,"startRow":1,"endRow":5,"total":5,"pages":0,
     * "list":["20161201-11","20170621-12","20171012-11","20171110-24","208507"],
     * "prePage":0,"nextPage":0,"isFirstPage":false,"isLastPage":true,"hasPreviousPage":false,"hasNextPage":false,"navigatePages":1,"navigatepageNums":[],"navigateFirstPage":0,"navigateLastPage":0,"firstPage":0,"lastPage":0}}}
     */
    public static <T> List<T> strToList2(String strJson) {
        JsonObject jsonObj = new JsonParser().parse(strJson).getAsJsonObject();

        JsonObject extend = jsonObj.getAsJsonObject("extend");
        if(extend.has("ykk_jsonArr")) { // 判断有没有数据集
            JsonParser parse = new JsonParser();
            JsonArray array = extend.getAsJsonObject("ykk_jsonArr").getAsJsonArray("list");
            List<T> list = new ArrayList<T>();

            for (int i=0, size=array.size(); i<size; i++) {
                list.add((T) array.get(i).getAsString());
            }
            return list;
        }
        return null;
    }

    /**
     * json --> Object（读取自定义的json数据）无分页模块
     * {"code":100,"msg":"处理成功","extend":{"ykk_jsonObj":{"id":1,"username":"admin","password":"123456","sex":1,"turename":null,"deptId":1,"createTime":1525932994633,"createrId":0,"createrName":"","dept":null}}}
     */
    public static <T> T strToObject(String strJson, Class<T> cls) {
        JsonObject jsonObj = new JsonParser().parse(strJson).getAsJsonObject();

        JsonObject extend = jsonObj.getAsJsonObject("extend");
        if(extend.has("ykk_jsonObj")) { // 判断有没有数据集
            String strDatas = extend.getAsJsonObject("ykk_jsonObj").toString();
            return mGson.fromJson(strDatas, cls);
        }
        return null;
    }

    /**
     * json --> String（读取自定义的json数据）
     * {"code":100,"msg":"处理成功","extend":{"ykk_string":"abc"}}
     */
    public static String strToString(String strJson) {
        if(strJson == null || strJson.indexOf("ykk_string") == -1) return null;

        JsonObject jsonObj = new JsonParser().parse(strJson).getAsJsonObject();

        JsonObject extend = jsonObj.getAsJsonObject("extend");
        if(extend.has("ykk_string")) { // 判断有没有数据集
            return extend.get("ykk_string").getAsString();
        }
        return null;
    }


}
