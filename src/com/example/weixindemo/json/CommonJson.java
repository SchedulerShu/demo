package com.example.weixindemo.json;

/**
 * Created by franc on 2016/7/30.
 */
public class CommonJson {


    /**
     * rc 应答码: 0操作成功  1无效请求  2服务器内部错误  3业务操作失败
     * operation : 服务的细分操作编码
     * service : 服务
     * answer : 对结果内容的最简化文本
     * text ：用户的输入
     */

    private int rc;
    private String operation;
    private String service;
    
    private AnswerBean answer;
    private String text;

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    
    public AnswerBean getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerBean answer) {
        this.answer = answer;
    }
    
    public static class AnswerBean {
        private String type;
        private String text;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
