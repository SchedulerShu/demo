package com.example.weixindemo.json;

/**
 * Created by franc on 2016/7/30.
 */
public class CommonJson {


    /**
     * rc Ӧ����: 0�����ɹ�  1��Ч����  2�������ڲ�����  3ҵ�����ʧ��
     * operation : �����ϸ�ֲ�������
     * service : ����
     * answer : �Խ�����ݵ�����ı�
     * text ���û�������
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
