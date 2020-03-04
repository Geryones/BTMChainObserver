package ch.brugg.fhnw.btm;

import ch.brugg.fhnw.btm.handler.JsonAccountHandler;
import ch.brugg.fhnw.btm.handler.old.AccountLoader;
import ch.brugg.fhnw.btm.handler.old.DefaultSettingsLoader;
import ch.brugg.fhnw.btm.pojo.Account;
import ch.brugg.fhnw.btm.writer.old.AccountWriter;
import ch.brugg.fhnw.btm.writer.old.DefaultSettingsWriter;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//TODO Beschreischbung was diese Klasse macht
public class SubscriptionTX {

    private Web3j web3j;
    private Subscription subscription;
    private static Logger log = LoggerFactory.getLogger(SubscriptionTX.class);
    private AccountLoader accountLoader;
    private DefaultSettingsLoader defaultSettingsLoader;
    private ChainInteractions chainInteractions;
    private JsonAccountHandler accountHandler = JsonAccountHandler.getInstance();
    private DefaultSettingsWriter defaultSettingsWriter;

    public SubscriptionTX(Web3j web3j, ChainInteractions chainInteractions) {
        this.web3j = web3j;
        this.accountLoader = AccountLoader.getInstance();
        this.defaultSettingsLoader = DefaultSettingsLoader.getInstance();
        this.chainInteractions = chainInteractions;
        defaultSettingsWriter = DefaultSettingsWriter.getInstance();
    }

    //TODO JAVADOC
    public void run(int min) throws Exception {
        log.info("Doing simple subscription");
        this.txFilter();
        this.intervall(min);

    }

    void txFilter() throws Exception {
        Disposable subscription = web3j.transactionFlowable().subscribe(tx -> {
            //TODO Gas pro Zeiteinheit anzeigen
            log.info("Eine Transaktion von folgender Adresse wurde gefunden: " + tx.getFrom());
            log.info("Gas price used: " + tx.getGasPrice());
            log.info("Gas : " + tx.getGas());
            log.info("Gas : " + tx.getGasRaw());
            log.info("Transferierter Ether: " + Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER));

            //TODO increaseCounter wieder einkommentieren und hier die DOS Algo implemeniteren
            if (tx.getGasPrice().equals(BigInteger.ZERO)) {
                // FileLoader.getInstance().increaseCounter(tx.getFrom().trim().toLowerCase());
                log.info("Transaktionskosten waren 0");
                log.info("Anzahl Accounts:" + accountLoader.getAccountArrayList().size());

                for (Account account : accountLoader.getAccountArrayList()) {
                    if (account.getAddress().equals(tx.getFrom())) {
                        account.decraseTransactionCounter();
                        account.decreaseGasUsedCounter(Integer.parseInt(tx.getGas().toString()));

                        //TODO DCREASE GASUSED COUNTER
                        //account.decraseGasUsedCounter(tx.getGas());
                        log.info("Account: " + account.getAddress() + " hat noch " + account.getTransactionCounter()
                                + " Transaktionen auf dem Counter und noch so viel Gas zum verbauchen " + account
                                .getGasUsedCounter());
                        this.dosAlgorithmus(account);
                        break;
                    }
                }

            }
        }, Throwable::printStackTrace);
    }

    //TODO Admin Account darf unendlich viele Transaktionen machen (ID von Account == dann nicht revoked)
    //TODO Algo anhand Gasprice und nicht Anzahl Transaktionen
    //TODO Counter runterzählen

    /**
     * In dieser Methode wird geschaut, wieviel Transaktionen von dem Account gemacht wurden.
     * Wenn dieser Account mehr Transaktionen als die Limite gemacht hat, wird er gesperrt.
     *
     * @param account Account der inspiziert/kontrolliert wird
     */
    private void dosAlgorithmus(Account account) {
        if (account.getTransactionCounter() == 0) {
            this.chainInteractions.revokeAccount(account.getAddress());

            this.accountLoader.getRevokedAccountArrayList().add(account);
            this.accountLoader.getAccountArrayList().remove(account);
            //TODO fixe Revoke Zeit

            log.info("Der Acccount hat zu viele Transaktionen verbruacht und " + account.getAddress()
                    + " wurde   gesperrt. Die Revoke Periode ist: " + defaultSettingsLoader.getDefaultSettings()
                    .getRevokeMultiplier());
            account.setRevokeTime(defaultSettingsLoader.getDefaultSettings().getRevokeMultiplier() - 1);

                accountHandler.writeAccountList();

        } else if (account.getGasUsedCounter() < 0) {
            this.chainInteractions.revokeAccount(account.getAddress());
            this.accountLoader.getRevokedAccountArrayList().add(account);
            this.accountLoader.getAccountArrayList().remove(account);
            //TODO fixe Zeit eingeben

            log.info("Der Acccount hat zu viel Gas verbraucht " + account.getAddress()
                    + " wurde gesperrt. Die Revoke Periode ist: " + defaultSettingsLoader.getDefaultSettings()
                    .getRevokeMultiplier());
            account.setRevokeTime(defaultSettingsLoader.getDefaultSettings().getRevokeMultiplier() - 1);
        }
    }

    //TODO Intervall Methode wo alle Counter von Accounts raufzählt
    //TODO javadoc
    /*

     */
    private void setAllCountersToMax() {

        for (Account account : accountLoader.getAccountArrayList()) {
            account.setTransactionCounter(account.getTxLimit().intValue());
            account.setGasUsedCounter(account.getGasLimit().intValue());

        }

    }

    /*
    Alle Accounts die genug lange gesperrt waren werden wieder entsperrt
     */
    private void certifyRevokedAccounts() {
        List<Account> removeFromRevokeList = new ArrayList<>();

        for (Account account : this.accountLoader.getRevokedAccountArrayList()) {
            if (this.controlRevokePeriode(account)) {
                this.chainInteractions.certifyAccount(account.getAddress());
                log.info("Account wurde wieder certifiziert: " + account.getAddress());
                account.setRevokeTime(defaultSettingsLoader.getDefaultSettings().getRevokeMultiplier());
                account.setTransactionCounter(Integer.parseInt(account.getTxLimit().toString()));
                account.setGasUsedCounter(Integer.parseInt(account.getGasLimit().toString()));
                this.accountLoader.getAccountArrayList().add(account);
                removeFromRevokeList.add(account);
            } else {
                account.setRevokeTime(account.getRevokeTime() - 1);

            }
        }

        //TODO in eigene Methode auslagern
        for (Account removeAccount : removeFromRevokeList) {
            this.accountLoader.getRevokedAccountArrayList().remove(removeAccount);
        }

    }

    //TODO evtl unnötige methode da einzeiler
    private boolean controlRevokePeriode(Account account) {
        return account.getRevokeTime() < 0;

    }

    //TODO JAVADOV
    private void intervall(int min) throws InterruptedException, IOException {
        System.out.println("*************Intervall ist gestartet********************");
        while (true) {
            System.out.println(new Date());
            Thread.sleep(min * 60 * 1000);
            this.setAllCountersToMax();
            this.certifyRevokedAccounts();
            this.defaultSettingsLoader.getDefaultSettings()
                    .setTimestampLastReset(new Timestamp(System.currentTimeMillis()).getTime());
           accountHandler.writeAccountList();
            this.defaultSettingsWriter.writeDefaultSettingsInFile();
        }
    }

}
