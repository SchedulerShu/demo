package com.example.weixindemo.json;

/**
 * Created by franc on 2016/7/30.
 */
public class FqaJson {


    /**
     * rc : 0
     * operation : ANSWER
     * service : faq
     * answer : 
     * text : 
     */

    private int rc;
    private String operation;
    private String service;
    /**
     * text : 
     * type : T
     */

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

    public AnswerBean getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerBean answer) {
        this.answer = answer;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static class AnswerBean {
        private String text;
        private String type;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
