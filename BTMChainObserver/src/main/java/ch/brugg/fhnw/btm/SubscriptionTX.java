package ch.brugg.fhnw.btm;

import ch.brugg.fhnw.btm.command.ResetAccountsCommand;
import ch.brugg.fhnw.btm.dosAlgorithm.DoSAlgorithm;
import ch.brugg.fhnw.btm.handler.JsonAccountHandler;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Convert;
import java.math.BigInteger;

/**
 * In dieser Klasse wird die Subscription gemacht um die gemachten Transaktionen beobachten zu können
 * In dieser Klasse werden die Transaktionen gefiltert, um bei gratis Transaktionen den DoS Algorithmus zu starten
 * In dieser Klasse wird das ResetIntervall gestartet, welches alle Counters Resettet und das Speichern der daten reguliert
 * @Author Faustina Bruno, Serge-Jurij Maikoff
 */
public class SubscriptionTX {

    private Web3j web3j;
    private  Logger log = LoggerFactory.getLogger(SubscriptionTX.class);
    private JsonAccountHandler accountHandler = JsonAccountHandler.getInstance();
    private DoSAlgorithm dosAlgorithm = DoSAlgorithm.getInstance();

    /**
     * Constructor der Klasse
     * @param chainInteractions instanzertes Objekt fpr die Weitergabe
     */
    public SubscriptionTX( ChainInteractions chainInteractions) {
        this.web3j = ChainSetup.getInstance().getWeb3j();
        this.dosAlgorithm.setChainInteractions(chainInteractions);
    }


    /**
     * Mit dieser Methode wird der filter und der Intervall gestaret
     * @throws Exception
     */
    public void run() throws Exception {
        this.log.info("Filter und Intervall werden gestartet");
        this.txFilter();
        this.intervall();
    }

    /**
     * In dieser Methode wird eine Subscription gemacht und geprüft ob die getätigten Transaktionen
     * gratis Transaktionen sind.
     * Wenn ja, wird der DoS Algorithmus gestartet.
     */
    private void txFilter() {
        Disposable subscription = this.web3j.transactionFlowable().subscribe(tx -> {

          this.log.info("Eine Transaktion von folgender Adresse wurde gefunden: " + tx.getFrom());
          this.log.info("Gas price: " + tx.getGasPrice());
          this.log.info("Gas : " + tx.getGas());
          this.log.info("Gas Raw: " + tx.getGasRaw());
            this.log.info("Transferierter Ether: " + Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER));

            if (tx.getGasPrice().equals(BigInteger.ZERO)) {
               this.log.info("Transaktionskosten waren 0");
                this.dosAlgorithm.dosAlgorithm(this.accountHandler.processAccount(tx.getFrom(), tx.getGas()));
            }
        });
    }

    /**
     * In dieser Methode wird der Command um alle Counters zurückzusetzten in die Queue hinzugefügt
     */
    private void intervall()  {
        this.log.info("*************Intervall ist gestartet********************");
        DoSAlgorithm.getInstance().offerCommand(new ResetAccountsCommand());
    }

}
