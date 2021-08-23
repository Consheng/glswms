package ykk.xc.com.glswms.bean;

/**
 * 日期：2019-12-10 10:33
 * 描述：用于EnentBus 数据传输
 * 作者：ykk
 */
public class EventBusEntity {
    private int caseId;
    private Object obj;
    private Object obj2;

    public EventBusEntity(int caseId) {
        this.caseId = caseId;
    }

    public EventBusEntity(int caseId, Object obj) {
        this.caseId = caseId;
        this.obj = obj;
    }

    public EventBusEntity(int caseId, Object obj, Object obj2) {
        this.caseId = caseId;
        this.obj = obj;
        this.obj2 = obj2;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Object getObj2() {
        return obj2;
    }

    public void setObj2(Object obj2) {
        this.obj2 = obj2;
    }

}
