package ch.brugg.fhnw.btm.pojo;

public class DefaultSettings {
    //TODO impl
    //intervalResetCounter in min

    private int intervalResetCounter;
    //intervalRevoke wieviel resetInervalle Account gesperrt ist
    private int intervalRevoke;
    //defaultTransaktionCount
    private String defaultTransaktionCount;
    //defaultGasCount
    private String defaultGasUsedCount;

    public DefaultSettings(String intervalResetCounter, String intervalRevoke, String defaultTransaktionCount,
            String defaultGasUsedCount) {
        this.intervalResetCounter = Integer.parseInt(intervalResetCounter);
        this.intervalRevoke = Integer.parseInt(intervalRevoke);
        this.defaultTransaktionCount =defaultTransaktionCount;
        this.defaultGasUsedCount = defaultGasUsedCount;
    }

    public int getIntervalResetCounter() {
        return intervalResetCounter;
    }

    public void setIntervalResetCounter(int intervalResetCounter) {
        this.intervalResetCounter = intervalResetCounter;
    }

    public int getIntervalRevoke() {
        return intervalRevoke;
    }

    public void setIntervalRevoke(int intervalRevoke) {
        this.intervalRevoke = intervalRevoke;
    }

    public String getDefaultTransaktionCount() {
        return defaultTransaktionCount;
    }

    public void setDefaultTransaktionCount(String defaultTransaktionCount) {
        this.defaultTransaktionCount = defaultTransaktionCount;
    }

    public String getDefaultGasUsedCount() {
        return defaultGasUsedCount;
    }

    public void setDefaultGasUsedCount(String defaultGasUsedCount) {
        this.defaultGasUsedCount = defaultGasUsedCount;
    }
}
