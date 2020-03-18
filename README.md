# BTMChainObserver

Die Fachhochschule Nordwestschweiz (FHNW) möchte den Studierenden und Mitarbeitenden eine kostenneutrale Blockchain anbieten. Diese soll für innovative Projekte, Experimente und Schulungszwecke genutzt werden.

Eine Blockchain schützt sich vor Denial of Service (DoS) Attacken mit Transaktionsgebühren. Dieser Schutzmechanismus ist für eine Lernumgebung nicht praktikabel. Für die Studierenden und Mitarbeitenden sollen keine Kosten anfallen. Das Ziel dieser Arbeit ist, einer definierten Benutzergruppe gratis Transaktionen zu gewähren, ohne dabei die Sicherheit der Blockchain zu komprimittieren.

Der Transaktionsmanager ist als Java-Applikation implementiert worden. Mit der Bibliothek web3j wird der jeweils neuste Block auf Transaktionen untersucht. Gefundene gratis Transaktionen werden analysiert.\newline Der Transaktionsmanager verwaltet die Benutzergruppe. Pro Benutzer wird die Anzahl getätigter gratis Transaktionen und die damit verursachten Rechenkosten der Blockchain überwacht. Die FHNW kann für jeden Benutzer individuelle Limiten definieren. Die Dauer der Suspendierung kann ebenfalls individuell festgelegt werden. Um die Datenpersistenz auch bei einem Stopp der Applikation zu gewährleisten, wird der Zustand regelmässig als Datei gespeichert. Bei einem Neustart wird dieser gelesen und wiederhergestellt.
Für die individuelle Suspendierung wird eine Priority-Queue in Kombination mit einem Command Pattern verwendet. So kann auch eine grosse Anzahl Benutzer, mit hoher Ressourceneffizienz, bearbeitet werden. Für die Entwicklung und Testung ist eine private single Node Blockchain verwendet worden.


# Betriebsanleitung

Für die Inbetriebnahme muss Docker auf dem Rechner installiert sein.

## Hochfahren der Parity Instanz

1. Die Konfiguration der Blockchain wird über die Datei  ```config.toml```
  gemacht, siehe \ref{sec_prac_config_toml}. Hier muss die Abhängigkeit der
  Pfade zum Docker-Home beachtet werden.\
  Parity stellt für die Konfiguration des Dockers diese Anleitung zur Verfügung:\
  https://github.com/parity-contracts/name-registry/tree/master/contracts

2. Die  Blockchainspezifikation ist in der Datei ```instant_seal.json```
  definiert. Der Speicherort muss mit dem Wert des Parameters ```chain``` der
  ```config.toml``` Datei übereinstimmen. Siehe \ref{sec_prac_spec} für den Aufbau der
  Datei ```instant_seal.json```.

3. Für dieses Projekt ist mti der Version 2.5.13 gearbeitet worden:\
    ``` docker pull parity/parity:2.5.13```

4. Mit follgendem Befehl wird ein neuer Party-Container erstellt:\
   ```docker run -ti -p 8545:8545 -p 8546:8546 -p 30303:30303 -p 30303:30303/udp -v ~/.local/share/io.parity.ethereum/docker/:/home/parity/.local/share/io.parity.ethereum/ parity/parity:stable --config /home/parity/.local/share/io.parity.ethereum/docker.toml --jsonrpc-interface all ```


## Deployment 

Der Transaktionsmanager kann zu einer ```.jar``` Datei gepackt werden. Um diese
neu zu erstellen muss die Methode ```package``` von Maven verwendet werden.
Dies kann über die Kommandozeile oder in der Entwicklungsumgebung gemacht
werden.\
Um alle Tests auszuführen wird bereits eine funktionierende Ethereum Blockchain
mit einem Parity-Clienten benötigt. Deshalb werden die Tests momentan nicht
automatisch ausgeführt. Das kann in der ```pom.xml``` eingestellt werden:

```{caption="pom.xml Properties" label=pom}
<properties>
    <skipTests>true</skipTests>
</properties>
```
Wenn die Zeile 2 im Listing \ref{pom}gelöscht wird, werden vor dem
Erstellen einer neuen ```.jar``` Datei, alle Tests automatisch ausgeführt.

Es werden zwei unterschiedliche ```.jar``` Dateien erstellt: 

1. ```BTMChainObserver-1.jar```
2. ```BTMChainObserver-1-jar-with-dependencies.jar```

Die Datei 1. enthält keine Dependencies und ist somit viel schlanker. Im zweiten
```.jar``` sind alle Dependencies mitgepackt. Sie ist somit unabhängiger, dafür
bedeutend grösser. 


## Konfigurieren des Transaktionsmanagers

Die Konfiguration des Transaktionsmanager geschieht über folgende Dateien:

1. ```AccountList.json```
1. ```DefaultSettings.json```
1. ```TransaktionsManagerAccount.json```

Siehe Kapitel \ref{prac_persistenz} und Kapitel \ref{sec_prac_conf} für mehr
Informationen zu deren Inhalt und Bedeutung.\
Diese Dateien müssen im selben Verzeichnis wie die ```.jar``` Datei des
Transaktionsmanagers befinden. 


## Starten der Blockchain Überwachung mit dem Blockchain Transaktionsmanager

Der generierte ```.jar``` Datei wird über die Kommandozeile angesteuert. Die
hier gezeigten Befehle setzen voraus, dass man sich im selben Verzeichnis
befindet.

Um das Programm das erste Mal nach dem Aufsetzten der Parity Instanz
laufen zu lassen, muss folgender Befehl auf der Console ausgeführt werden:

```java -cp BTMChainObserver-1-jar-with-dependencies.jar ch.brugg.fhnw.btm.Init```

Sobald die Initialisierung abgeschlossen ist, kann folgender Befehl genutzt werden:

```java -cp BTMChainObserver-1-jar-with-dependencies.jar ch.brugg.fhnw.btm.Run```

Dieser startet die Überwachung der Blockchain. 
