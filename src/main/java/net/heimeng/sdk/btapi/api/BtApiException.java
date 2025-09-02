package net.heimeng.sdk.btapi.api;

public class BtApiException extends RuntimeException {
    private final boolean status;
    private final String msg;

    public BtApiException(boolean status, String msg) {
        super(msg);
        this.status = status;
        this.msg = msg;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "BtApiException{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }
}
