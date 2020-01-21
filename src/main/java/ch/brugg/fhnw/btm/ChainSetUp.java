package ch.brugg.fhnw.btm;

import ch.brugg.fhnw.btm.contracts.SimpleCertifier;
import ch.brugg.fhnw.btm.contracts.SimpleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;

public class ChainSetUp {

    // Tutorial can be found here: https://kauri.io/connecting-to-an-ethereum-client-with-java-eclipse-and-web3j/b9eb647c47a546bc95693acc0be72546/a
    // Video Tutorial https://www.youtube.com/watch?v=kJ905hVbQ_E

    private final String privateKey;
    private static BigInteger REGISTRATION_FEE = BigInteger.valueOf(1000000000000000000L);
    private static Web3j web3j;
    private static SimpleCertifier simpleCertifier;
    private static SimpleRegistry simpleRegistry;
    private static TransactionManager transactionManager;
    private static byte[] hash;
    private static Logger log = LoggerFactory.getLogger(ChainSetUp.class);

    public ChainSetUp(String privateKey) {

        log.info("connecting");
        this.privateKey = privateKey;
        //Verbindungsaufbau zur Chain
        web3j = Web3j.build(new HttpService("http://192.168.99.100:8545/"));

        //TODO in Test auslagern
        //TM von dem Account mit dem PK
        transactionManager = new RawTransactionManager(web3j, getCredentialsFromPrivateKey());

        //sha3(service_transaction_checker) = 0x6d3815e6a4f3c7fcec92b83d73dda2754a69c601f07723ec5a2274bd6e81e155
        String str_hash = "6d3815e6a4f3c7fcec92b83d73dda2754a69c601f07723ec5a2274bd6e81e155";

        //Umwandeln in Bytearray Hexadecimal
        hash = new BigInteger(str_hash, 16).toByteArray();
    }

    private  boolean loadSimpleRegistry() {

        //TODO Key in Konstante auslagern
        simpleRegistry = SimpleRegistry
                .load("0x0000000000000000000000000000000000001337", web3j, getCredentialsFromPrivateKey(),
                        new DefaultGasProvider());
        try {
            log.info("Fee of Registry: " + simpleRegistry.fee().send());
            log.info("Owner of Registry: " + simpleRegistry.owner().send());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    private  boolean setUpCertifier(TransactionManager transactionManager, String accountAdressToCertfy) {

        if (deployCertifier(transactionManager) && registerCertifier() && certifyAccount(accountAdressToCertfy)) {
            log.info("deploy of Certifier worked");
            log.info("Registration of Certifier worked");
            log.info("Certifying worked");

            log.info("Address of Certifiert: " + simpleCertifier.getContractAddress());

            return true;
        }
        log.info("there was an error");
        return false;
    }

    private  Credentials getCredentialsFromPrivateKey() {
        return Credentials.create(this.privateKey);
    }

    private  boolean deployCertifier(TransactionManager transactionManager) {

        try {
            log.info("Deploying Certifier");
            simpleCertifier = SimpleCertifier.deploy(web3j, transactionManager, new DefaultGasProvider()).send();
            log.info("Deployment of Certifier done");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private  boolean registerCertifier() {

        try {
            log.info("Registering Certifier");
            TransactionReceipt receipt1 = simpleRegistry.reserve(hash, REGISTRATION_FEE).send();
            log.info("TX-Hash for reserve: " + receipt1.getTransactionHash());
            TransactionReceipt receipt2 = simpleRegistry.setAddress(hash, "A", simpleCertifier.getContractAddress())
                    .send();
            log.info("Tx-Hash for setAddress: " + receipt2.getTransactionHash());
            log.info("Done registering Certifier");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private  void getInfo(Web3j web3j) {
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().send();

            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();

            EthGasPrice gasPrice = web3j.ethGasPrice().send();

            log.info("Client version " + clientVersion.getWeb3ClientVersion());
            log.info("Block number " + blockNumber.getBlockNumber());
            log.info("Gas price " + gasPrice.getGasPrice());

        } catch (IOException e) {
            throw new RuntimeException("Error whilst sending json-rpc requests", e);
        }
    }

    //TODO evtl ChainInteractions Klasse reinnehmen
    private  boolean certifyAccount(String add) {

        try {
            log.info("Certifying Account");
            simpleCertifier.certify(add).send();
            log.info(simpleRegistry.getAddress(hash, "A").send());
            log.info("Done certifying account");
            return isCertified(add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //TODO evtl ChainInteractions Klasse reinnehmen
    private  boolean isCertified(String add) {
        try {
            return simpleCertifier.certified(add).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
