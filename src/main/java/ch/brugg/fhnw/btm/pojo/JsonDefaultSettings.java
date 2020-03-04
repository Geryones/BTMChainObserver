package ch.brugg.fhnw.btm.pojo;

public class JsonDefaultSettings {

    public static JsonDefaultSettings instance;

    public static JsonDefaultSettings getInstance() {

        if (JsonDefaultSettings.instance == null) {
            JsonDefaultSettings.instance = new JsonDefaultSettings();
        }
        return JsonDefaultSettings.instance;
    }
    private JsonDefaultSettings(){};

    private String connectionAddress;

    private int resetInterval;

    private int defaultTxLimit;

    private long defaultGasLimit;

    private String certifierAddress;

    private String timestampLastReset;
    private String nameRegistryAddress;

    public String getConnectionAddress() {
        return connectionAddress;
    }

    public int getResetInterval() {
        return resetInterval;
    }


    public int getDefaultTxLimit() {
        return defaultTxLimit;
    }

    public Long getDefaultGasLimit() {
        return defaultGasLimit;
    }

    public String getCertifierAddress() {
        return certifierAddress;
    }

    public String getTimestampLastReset() {
        return timestampLastReset;
    }

    public String getNameRegistryAddress() {
        return nameRegistryAddress;
    }

    public void setCertifierAddress(String certifierAddress) {
        this.certifierAddress = certifierAddress;
    }

    public void setTimestampLastReset(String timestampLastReset) {
        this.timestampLastReset = timestampLastReset;
    }
}
