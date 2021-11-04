package com.company.oauth2keycloak.builder;

import com.google.gson.annotations.Expose;

public class ResponseBuilder {
    @Expose
    private int code;

    @Expose
    private String message;

    @Expose
    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    private ResponseBuilder(Builder builder){
        this.code = builder.code;
        this.message = builder.message;;
        this.data = builder.data;
    }
    public static final class Builder{
        //requirement params
        private int code;

        //optional params
        private String message = "message from server";
        private Object data = new Object();

        public Builder(int code){
            this.code = code;
        }
        public Builder buildMessage(String message){
            this.message = message;
            return this;
        }
        public Builder buildData(Object data){
            this.data = data;
            return this;
        }
        public ResponseBuilder build(){
            return new ResponseBuilder(this);
        }

    }
}
