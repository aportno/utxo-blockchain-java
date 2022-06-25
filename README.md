# Learning Blockchain in Java by Hong Zhou

## Executive Summary

This repo follows the instructions in Hong Zhou's _Learning Blockchain in Java - A step-by-step approach with P2P demonstration_.
The blockchain is developed from scratch using bitcoins UTXO model.

[Purchase "Learning Blockchain in Java" on Amazon](https://www.amazon.com/Learning-Blockchain-Java-step-step/dp/1795002158)

[Reference Zhou's GitHub Repo](https://github.com/hhohho/learning-blockchain-in-java-edition-2)

## Chapter 1 :: Blockchain: A New Technology Paradigm
The fundamental requirements of a blockchain include security (cryptography), distributed (decentralized) network, and 
concurrency.

Within a blockchain network, there are different types of nodes:
* Miners - most interested in blocks to sustain the validation and growth of the blockchain
* Wallets - only utilize the blockchain system for trade, and not interested in mining
* Regional centers - providing services to wallets and some miners

Basic life cycle of a block:
1) Nodes initiate & broadcast transactions
2) Nodes collect & validate transactions
3) Each node constructs & mines a block
4) Nodes broadcast blocks
5) Only one block wins by consensus
6) Block is chained to the end of the blockchain

## Chapter 2 :: Mining Blocks and Secure Hash Algorithm

Secure hash algorithms (SHA), provide one-way hashing algorithms where it is impossible to identify the original string
from the hash value of it. Such an algorithm can accept an input of arbitrary length and output a hash of fixed-length.

SHA-256 is the most popular and outputs a 256-bit hash. This means the algorithm can generate 2<sup>256</sup> different 
hash values based on different inputs. The most critical requirement for a secure hash algorithm is to avoid a 
"collision", a case when two different inputs are given the same hash output. For SHA-256, the chance to have a collision
is practically zero

The `UtilityMethods` class uses the `MessageDigest` class in JavaCryptography Architecture (JCA). It takes in a byte 
array and returns a hash value also in the form of a byte array. The method `messageDigestSHA256_toBytes()` takes a string
as an input and applies the SHA-256 algorithm to return the hash value of the message as a byte array.
Our catch statement uses `NoSuchAlgorithmException` but given SHA256 is always available, it will be impossible to return
an error. `messageDigestSHA256_toString()` takes a string as an input, applies the Base64 class to convert a byte array
into a readable string, then turns the readable string back into the original byte array.

The `messageDigestSHA256_toString()` first converts a byte array into a string. The byte array it uses as input is 
generated from `messageDigestSHA256_toBytes()`. We use the `encodeToString` method in order to print the byte array to screen.

We test `UtilityMethods` using `TestHashing` by setting the variable `msg` as a unique string.
Using the `messageDigestSHA256_toString` method, we convert the string into a hash value. Notice that if we change any 
character in our string, the hash output will be totally different. Also note that we have no idea what the initial string was
by just looking at its hash value, nor will we ever know because SHA-256 is a one-way function!

So now that we understand how a secure hash can be obtained, how can we use the hash in a blockchain?

Structure:
* Data items in a blockchain are contained within blocks
* Each block contains a number of transactions
* Mining a block means generating a hash ID for the block based on the transactions' hash IDs and the previous block's hash ID
* This newly generated hash ID must meet the given difficulty level requirement
* The satisfactory hash ID must have a certain number of leading zeros bits (called leading zeros)

The number of leading zeros is called the difficulty level. Higher difficulty level, the more leading zeros required.
We make small changes to the block hash ID until the hash ID matches the number of leading zeros. But how do we change
our hash ID? We adjust the "nonce". A nonce is an integer that is incorporated into the input string and is constantly
changed (incremented) to obtain a different hash ID until the hash meets the requirement (of leading zeros).

In essence, mining is the process of finding the right nonce

We added the method `hashMeetsDifficultyLevel()` to the `UtilityMethods` class to examine if a hash string has at least 
a certain number of leading zeros. It converts the input string into a char array and then counts the leading zeros.

`toBinaryString()` is another method added to `UtilityMethods`. It converts a hash in bytes form into a bit string.

Finally, we need to construct a `Block` class that includes the mining process

The `Block()` constructor takes `previousBlockHashID` and `difficultyLevel` as an input. It then sets 
the class `previousBlockHashID`, timestamp (using the `getTimeStamp()` in the `UtilityMethod` class), and `difficultyLevel`.

`computeHashID` creates a new string and adds together:
1) `previousBlockHashID`
2) `this.timestamp` as a hex string
3) each transaction in the block (which are added via the `addTransaction()` method)
4) `this.difficultyLevel` as a hex string
5) the `nonce`

The string is then converted into a byte array and then returns a bit string

`mineTheBlock()` method will continue to increase the nonce of the block until the difficulty level is met. Once achieved,
the block is mined

Why does the `Block` class use `java.io.Serializable`? If objects of a class need to be transported across a network or
written into a file as bytes for later retrieval then `java.io.Serizializable` must be implemented. Simply, it acts as a
gesture to inform JBM that objects of this class should be serialized into bytes when needed.

The variable `serialVersionID` is only important when our package is used across multiple locations and there is a risk 
that different locations may have a different version of the `Block` class.

For example, say that a `Block` object is serialized at location A, JVM associates it with the `serialVersionUID`. When 
the object is deserialized at another JVM at location B, the JBM of location B needs to load `Block` class
as a template for this object to be reconstructed. The class loader checks if `Block` class at location B and the `Block`
object from location A have the same `serialVersionID` value. If the `UID` values differ, the program will throw an exception
that is easy to catch and fix. This mechanism ensures that the classes in use are consistent and compatible.

## Chapter 3 :: Transaction and Cryptography

Transactions are probably the central tenet in digital cryptocurrency. Bitcoin, the initial blockchain system, was developed
to ensure that transactions are transparent, secure and irreversible in a distributed environment. The act of transacting
is so important that bitcoin had Script specifically developed to safeguard transactions.

As an analogy of how bitcoin handles transactions, assume you go to a market to purchase a product that costs
$59.12, and you can only pay with cash. First you need to have enough cash in your pocket -- we'll delineate this as
"unspent money". Let's assume you only have a few $50 bills. You hand over two $50 bills to the seller who acknowledges 
your payment and gives you 1) the product and 2) $40.88 as the change.

Bitcoin blockchain transactions follow a similar logic, where available cash is known as unspent transaction output (UTXO).
Only UTXOs can be used as transferable funds, and these UTXOs are considered input in transactions. Payments are considered
outputs. To transfer enough funds, enough UTXOs are collected to pay the receiver. At the same time, the change is collected
as another UTXO. Continuing the analogy above, each $50 bill is considered a UTXO (i.e., the first $50 is UTXO_1 and the
second $50 is UTXO_2)

Once the transaction is completed, the original inputs have been spent and can no longer be spent again, but the change 
in the form of UTXO is available for further spending. From the buyers point of view, the change is part of the output in
the transaction and is marked as payment from and to yourself.

Throughout the transaction, only two parties are involved. The buyer and the seller.

How does this differ transactions that include third parties such as a bank? Traditionally, assuming a buyer and seller 
want to transact, then the buyers bank will get in touch with the sellers bank and initiate the transaction. If the funds
are available and the transaction is approved, then the buyer and the seller will each see the transaction in their own
private statements.

Blockchain settlement is different whereas all transactions are publicly announced to the network. To beget confidence, 
blockchain takes the following precautions:
1) Examine if the user has enough UTXO to cover the transaction cost (include any transaction fees)
2) Verify that the transaction is initiated by the right payer (i.e., only the genuine payer can make a transaction under his name)
3) Publish the transaction such that it is transparent, final and unchangeable

We can verify if a transaction is genuinely initiated by the payer using public and private key technology

To encrypt a digital message, we use a key (i.e. a password), to turn the message into a byte array via a specific algorithm
or process, and then later we can use another key, which might be the same or different from the original key, to decrypt the
byte array into the original message. The byte array is machine-readable, not human-readable.

If the key to decrypt is the same as the key to encrypt, it is called symmetric encryption (and the key is usually called the
secret key). This is the traditional encryption approach but is an inconvenient way of sharing data without giving direct
access to another user.

A workaround is achieved through the use of the public and private key algorithm, an asymmetric encryption in which the key to
decrypt and the key to encrypt are different. Public keys and private keys are pair entities. In other words, a public key
has exactly one private key to match. Using a public and private key pair, a user can publish the public key for communal
usage, keep the private key, and not worry about information leakage. As long as the private key is safe, it does not matter
who has access to the public key.

The limitations to this approach is that public and private key pairs are not suitable for encrypting and decrypting large
amounts of data. For example, if we use RSA algorithm to generate public and private key pairs with key size of 2048 bits
(256 bytes), then the public and private keys can only encrypt and decrypt a block of data no larger
than 256 bytes.

To put that into perspective, eight bits are called a byte. One byte character sets can contain 256 characters. So the 
maximum number of plain text characters that can be encrypted is 256 * 256 = 65,536 characters long

A sample of public and private key flows between two users:
1) User B generates a key pair (public :: private key) and keeps private key safe (hidden)
2) User B sends their public key to User A
3) User A applies User B's public key to encrypt data
4) User A sends User B the encrypted data
5) User B applies their private key to decrypt the data

Another scenario where public and private key techniques can be utilized is between a client and server that are starting up
a secure network connection for data transportation:
1) the client sends his public key to the server
2) The server randomly generates a secret key, utilizes the client's public key to encrypt the secret key and sends it back to the client
3) The client uses his own private key to decrypt the message to see the secret key
4) After this exchange, a secure connection has been established so that all messages between the server and the client are encrypted using the secret key

We need to add a few additional methods to our `UtilityMethods` class...

`getUniqueNumber()` is used to increment the `uniqueNumber` class variable

`generateKeyPair()` initializes a 2048 bit instance of RSA and returns a key pair

`generateSignature()` takes a `privateKey` and `message` as an input and returns a signature using SHA256 with RSA

`verifySignature()` takes a `publicKey`, `signature` and `message` as an input

`getKeyString()` is used because when we compute the hash ID for UTXO or a transaction, we need to include the sender's public
key and the receiver's public key as strings

A UTXO represents a spendable fund. It should include the following data:
1) Where it comes from
   1) A UTXO is generated inside a transaction so we need to know inside which transaction this UTXO is
   2) It is a good idea to record who sends the fund (technically this is optional)
2) The owner of the UTXO. The receiver (in the form on a public key) is the owner of the UTXO
3) The amount of funds available inside the UTXO
4) A timestamp
5) A unique ID identifying the UTXO
   1) This is another hash based on the above data items (where the UTXO is from, the sender, the receiver, amount of funds and a timestamp)
   2) We also need a unique sequential number that is different for each UTXO to make the ID absolutely unique

Our `UTXO` class packages a number of variables and methods to help us retrieve this metadata.

We serialized the class using `java.io.Serializable` to ensure the network is referencing the same `UTXO` objects. When 
a UTXO object at location A is serialized into bytes and transported to another location B, the JVM at location B determines
that the incoming bytes are for an instance of class `UTXO`. The JVM's class loader uploads the class structure
of the `UTXO` class to reconstruct the UTXO object based on the incoming bytes.

It is possible that location A and location B are using different, incompatible version of UTXO classes, which can cause
unexpected errors. It is a good programming convention to specify a `serialVersionUID` and update it whenever the class is
modified in an effort to avoid these unexpected errors.

`parentTransactionID` is the unique hash value of the transaction in which the UTXO is created as an element. This allows us to 
always track back to the transaction owning the UTXO via this hash value

The constructor the `UTXO` class takes the `parentTransactionID` as a string, `sender` as a public key, `receiver` as a public key
and the `amountToTransfer` as a double as inputs. We initialize the object with `"O"` as the `parentTransactionID`.

`PublicKey` is an interface that groups all public key interfaces. First we need to create a `KeyPair` object using our `generateKeyPair()`
method in our `UtilityMethods` class. This will produce an address that we can assign to a sender or receiver. We can then
pass these objects into the constructor.

The `UTXO` object is instantiated with the below parameters:
* `this.sequentialNumber` is referenced in our `UtilityMethods` class and is incremented after every call
* `this.parentTransactionID` is described above
* `this.receiver` is a `PublicKey` address
* `this.sender` is a `PublicKey` address
* `this.amountTransferred` is the amount the sender will send to the receiver
* `this.timestamp` is the timestamp for when the UTXO is created (i.e., the transaction occurs)
* `this.hashID` calls the `computeHashID` method, which references all variables declared above

The `computeHashID` returns a hash value that is generated by appending `parentTransactionID` + the senders address +
the receivers address + the amount transferred + the timestamp + the sequential number. If any of these values changes then 
the hash value will be completely different

The remaining methods are self-explanatory getter methods other than `isMiningReward()`. In a blockchain, a miner is rewarded
an incentive for mining a block successfully. The reward is in the form of a UTXO. To track where the fund is from, it would
be a good idea to tell the difference between a general UTXO (a sender and a receiver) and a UTXO rewarded for
mining.

For avoidance of doubt, the construction of a UTXO is simply the hash value of the metadata generated by a transaction

Our `Transaction` class required some in-depth explanation.

The `TRANSACTION_FEE` constant refers to the mining reward received by a miner. In bitcoin, transaction fees are dynamically
allocated by the transaction sender, in which case the transaction fee can increase or decrease depending on network traffic

```aidl
    private byte[] signature = null;
    private boolean signed = false;
```

The signature is initially null. Once it is generated successfully, i.e., not null, the instance variable `signed` is set
to be true. In the `Transaction` class, only the method `signTheTransaction()` can change the valuables of variable `signature`
and variable `signed`. Moreover, once `signature` is not null or `signed` becomes true, the signature cannot be regenerated.
This is a secure coding practice to ensure that a transaction cannot be signed more than once.

`Transaction` includes two constructors. The first constructor...

```aidl
    public Transaction(PublicKey sender, PublicKey receiver, double amountToTransfer, ArrayList<UTXO> inputs) {
        PublicKey[] publicKeys = new PublicKey[1];
        publicKeys[0] = receiver;
        double[] funds = new double[1];
        funds[0] = amountToTransfer;
        this.setUp(sender, publicKeys, funds, inputs);
    }
```

...takes a single instance of a `receiver` and `amountToTransfer`, while the second constructor takes an array of these variables:

```aidl
    public Transaction(PublicKey sender, PublicKey[] receivers, double[] amountToTransfer, ArrayList<UTXO> inputs) {
        this.setUp(sender, receivers, amountToTransfer, inputs);
    }
```

The first constructor initializes a `PublicKey` array of length 1, and then sets `receiver` as the first value in the array.
It then creates a `double` array of length 1, and sets the `amountToTransfer` as the first value in the array. Finally, the
last step in the initialization process is to call the `setUp` method:

```aidl
    private void setUp(PublicKey sender, PublicKey[] receivers, double[] amountToTransfer, ArrayList<UTXO> inputs) {
        this.mySequentialNumber = UtilityMethods.getUniqueNumber();
        this.sender = sender;
        this.receivers = new PublicKey[1];
        this.receivers = receivers;
        this.amountToTransfer = amountToTransfer;
        this.inputs = inputs;
        this.timestamp = java.util.Calendar.getInstance().getTimeInMillis();

        computeHashID();
    }
```

Note there can be one `sender` that can transact with multiple `receivers` which requires the method to consume an array 
of UTXOs. Observe that we also need to guarantee that the number of recipients matches the number of funds.

The `computeHashID()` method is different from the `computeHashID()` in the `UTXO` class:

```aidl
    protected void computeHashID() {
        String message = getMessageData();
        this.hashID = UtilityMethods.messageDigestSHA256_toString(message);
    }
```

It first creates a string `message` using `getMessageData()`:

```aidl
    private String getMessageData() {
        StringBuilder sb = new StringBuilder();
        sb.append(UtilityMethods.getKeyString(sender))
                .append(Long.toHexString(this.timestamp))
                .append(Long.toString(this.mySequentialNumber));
        for (int i = 0; i < this.receivers.length; i++) {
            sb.append(UtilityMethods.getKeyString(this.receivers[i])).append(Double.toHexString(this.amountToTransfer[i]));
        }
        for (int i = 0; i < this.getNumberOfInputUTXOs(); i++) {
            UTXO ut = this.getInputUTXO(i);
            sb.append(ut.getHashID());
        }
        return sb.toString();
    }
```

Another secure coding practice is used in the method `signTheTransaction()`

```aidl
    public void signTheTransaction(PrivateKey privateKey) {
        if(this.signature == null && !signed) {
            this.signature = UtilityMethods.generateSignature(privateKey, getMessageData());
            signed = true;
        }
    }
```

This method takes a `PrivateKey` as the input argument, but only uses it and never stores it. Any private key
should never be stored outside the key owner.

```aidl
    public double getTotalAmountToTransfer() {
        double f = 0;
        for (int i = 0; i < this.amountToTransfer.length; i++) {
            f += this.amountToTransfer[i];
        }
        return f;
    }
```

The `Transaction` class does not provide any access to the instance variable `double[] amountToTransfer` for two reasons.
One reason is that the content of the double array can be obtained from the output UTXOs. A more import reason is for secure
coding practice. We cannot have a method that returns an array of double because arrays are accessed by reference. If we provide
a method `double[] getAmountToTransfer()` which returns the original array `amountToTransfer`, then when the returned array is
modified outside the `Transaction` object, the content of the `Transaction` object is altered.

Such a scenario should never be allowed to happen.

We wrote the test class `TestTransaction` that initiates a transaction.

```aidl
   KeyPair sender = UtilityMethods.generateKeyPair();
   PublicKey[] receivers = new PublicKey[2];
   double[] amountToTransfer = new double[receivers.length];
   ```

First we generate a key pair for the `sender` address. We then create a `PublicKey` array to store the `receivers` in the transaction.
The array will initially populate the array with null values which will be replaced with key-pairs later in the script. 
The size of the `receiver` array is of length two because there will be two receivers on the other side of the transaction.
In total, there will be 3 parties involved - 1 one sender, 2 receivers. The `amountToTransfer` array must be the same 
exact size as the `receivers` array to ensure all funds are dispensed accordingly.

Three sample addresses involved in the transaction are below
```aidl
   Sender: Sun RSA public key, 2048 bits
   params: null
   modulus: 19340388782177945236365016871001458330256161166593780182058800290781697561634874354722647434402065625360099888083228457128712720112542483050327347387767996889181601365641065355272287432228765124112214498704745874637706775822274789575206678923276209605744024988227938057410005795123842748469529920823072671362776387111543882314252991193737732684804962956601580962041435488602347496094553457691915166826727389950315618707896157669875506555876984517384830221513917277471195999786511527019587541015284620191858644609815843723222171625719670541653868542224819860970106577563803643071644323773007288249022830424929497454899
   public exponent: 65537
   
    Receiver 0: Sun RSA public key, 2048 bits
   params: null
   modulus: 25854584702315955630829213931710888723247966552751878331727433834957432786258266190359666661930646784948593861135716056640215543709746304508528849240884363913609385224372364665224872276747014209690426893324127226840559776642729301334268517888026073714744793684808154394505951289368609551850207948852394104818248141838898266980224310238356407538851854800274023316314047731705083846294689942475135469270887159767292087571414265309753576102060516187242769827832132189712678122434877359522763885421280909108205105905649847043946752113818741403674985093642042184161401830915652272576649115561170393769609025273092681349001
   public exponent: 65537
   
    Receiver 1: Sun RSA public key, 2048 bits
   params: null
   modulus: 23708687768946429146930118955823134100704672869800741876789192642196484725392469284937755768294275702219165460724517131448245362157379013913136245469286395713187888282437861562722148810574065795799454279248761793945193091617540151430606380749172369373962759898770250059980931402580849259787407846868210888158658070174327146303174163688387954541699913215165396433032855091228867706702697753004436973823383008623514974308220582969535568143018048633258864216900813546285739086645657596476543799793909361767662225612754065747908724194338475389819896952288008369918141503019834080390741320236798594844823743201459469460957
   public exponent: 65537
```
Next, a loop iterates over the `receivers` array.
```aidl
   for (int i = 0; i < receivers.length; i++) {
      receivers[i] = UtilityMethods.generateKeyPair().getPublic();
      amountToTransfer[i] = (i + 1) * 100;
   }
```

Initially, each position in the array contains a null value. We replace the
null values in each position with a public key generated by our `generateKeyPair().getPublic()` method in our `UtilityMethods` class.
The first account is credited with 1 * 100 = 100 funds. The second account is credited with 2 * 100 = 200 funds.

A sample of the public keys populated would like similar to:

```aidl
Sun RSA public key, 2048 bits
  params: null
  modulus: 23280617649686657869694767207020301177370369689502418432636947250717027703849368037149499666863497359217169863501719566521819037545414856621102722566100111317227198919226477314340792336226128171634713206541004833936727851904260807506050578982705948078331817623568462738016243250069470120979247317684807711150844279532335531162157229170023400118041175455671308158383132115005139054486917436883393634987163294071869004175357439268023191893152356439699408347964515561674221688779738805127508994984186459267776988996661404301103260926510840231205996588625995779882620351596918697192121778729561080542859757051835810445779
  public exponent: 65537
  
Sun RSA public key, 2048 bits
  params: null
  modulus: 22364356471416170247965853372417723082981632508036267478945079681412984113182647409325653706169122978303295201101157192186291815483828996170418461952857135623423214531357147717561474689658796295406713617593350487659734407674974001812200396858479013567073200628721910216142054495047815647239028510507888016766255101242412584251448503455353358528960698363369744635988546974309408293931899322849832422812022532701221860487108858944170933915101027882284736175601003026819860183235863929789084900728793832414763363098022922641198897815019258735390567520176590027282157181024962701375143390738942515665723828728703196974193
  public exponent: 65537
```

As a small tangent, the `generateKeyPair()` method can produce public keys (using `getPublic()`) or private keys (using
`getPrivate()`). This was generated by the same `KeyPair`. Note the modulus is the same, however the public exponent and 
the private exponent differ. A sample of the values are below:

```aidl
Sun RSA public key, 2048 bits
  params: null
  modulus: 21733588452862127161893467390477728307228815719676859254871484782954575528080519803389735440568191163004846485707480209518796347098897854987716620912924805164437584440122477085055664357522547284251323617384942669532951391501657223891200081415777199253193021680951534207408186208895176139186444671662820818677130888245931485568718972362381178163049707290792185605319121000546040246128936908622237048356864127818452122412257344707013376717553577866336893883505414671335452090787712698384561289621719811806916616006438681062908197451907778403439880591936661463304792288479783618172913632980740900520111261312636626029951
  public exponent: 65537 SunRsaSign RSA private CRT key, 2048 bits
  
  params: null
  modulus: 21733588452862127161893467390477728307228815719676859254871484782954575528080519803389735440568191163004846485707480209518796347098897854987716620912924805164437584440122477085055664357522547284251323617384942669532951391501657223891200081415777199253193021680951534207408186208895176139186444671662820818677130888245931485568718972362381178163049707290792185605319121000546040246128936908622237048356864127818452122412257344707013376717553577866336893883505414671335452090787712698384561289621719811806916616006438681062908197451907778403439880591936661463304792288479783618172913632980740900520111261312636626029951
  private exponent: 2603297246131343239829776074479280103856814850675953399768328844169307571672288333967733059152850989946369415554846944851877124740490582591453277185965361165067505543215305331805460647938491487341810971162468490261662733751213199929046225884524573553933006073400008678820444885141654564932330160774296767170955801608638204471638643985433389648752594909104534308492132196310321722260511910216252789267549798366472252825142648284688059477044256076395849367183932344958567465142001805426905362046938144962033904771789375938857226449517042265000481724691817420645894528564569681126949355702468015681242040623783885525069
```

A more eloquent way to improve this class would be to use a hash table. At the moment, we reference two different, disconnected
arrays (one for the keys, and one for the amounts). We assume the position integrity of the arrays will not be tampered with.
If it is, then the account balances will not map to the correct amounts.

Our next step is to create an **_input_** `UTXO`

```aidl
   UTXO inputUTXO = new UTXO("0", sender.getPublic(), sender.getPublic(), 1000);
   ArrayList<UTXO> input = new ArrayList<UTXO>();
   input.add(utxo1);
   Transaction transaction = new Transaction(sender.getPublic(), receivers, amountToTransfer, input);
```

As this is a demo, the first UTXO sends an amount of 1000 to themselves. We then add the UTXO to our array
of UTXOs. Finally, we create a transaction using the `sender` public key, the `receivers` in our array, and a transfer
amount of 1000 (which we initiated the senders account with). The size of the `input` array is 1 because we've only
added one `UTXO` object.

To refresh how the current metadata stands, our first `UTXO` had the same `sender` and `receiver`. We want to ensure the amount
of funds available at the `sender` address is sufficient

```aidl
   double available = 0.0;
   for (UTXO value : input) {
      available += value.getAmountTransferred();
   }
```

We loop through each `UTXO` in the `input` array and run the summation of the amounts transferred, which we've programmed
to be 1000.

```aidl
    double totalCost = transaction.getTotalAmountToTransfer() + Transaction.TRANSACTION_FEE;
```

The `totalCost` of the transaction is going to be the amount that will be transferred from the sender to the receivers plus
the mining fee (or transaction fee) of 1. The first for loop populated that first receiver with 100 funds and the second receiver
with 200 funds, so in total the amount to be transferred is 300. This number is computed using the `getTotalAmountToTransfer()` method
in the `Transaction` class

```aidl
    public double getTotalAmountToTransfer() {
        double f = 0;
        for (int i = 0; i < this.amountToTransfer.length; i++) {
            f += this.amountToTransfer[i];
        }
        return f;
    }
```

The transaction will need to abort if there is insufficient funds; where the available funds are less than the total cost of
the transaction:
```aidl
   if (available < totalCost) {
      System.out.println("Fund available=" + available + ", not enough for total cost of " + totalCost);
   }
```

Now that the input `UTXO` is complete, we can create our **_output_** `UTXO`:

```aidl
   for (int i = 0; i < receivers.length; i++) {
      UTXO outputUTXO = new UTXO(transaction.getHashID(), sender.getPublic(), receivers[i], amountToTransfer[i]);
      transaction.addOutputUTXO(utxo2);
   }
```

This for loop will iterate over each position in the original `receivers` array. At each position,
we create a new `UTXO` object referencing the current transaction, the `sender` public key, the `receivers`
public key and the amount to be transferred tied to the `receivers` address. We then use the `addOutputUTXO()` method in the
`Transaction` class to add the UTXO to our `transaction` object

```aidl
    protected void addOutputUTXO(UTXO utxo) {
        if(!signed) {
            outputs.add(utxo);
        }
    }
```

We now need to create a `UTXO` to return any change to the `sender`.

```aidl
   double remainingFunds = available - totalCost;
   UTXO remainingUTXO = new UTXO(transaction.getHashID(), sender.getPublic(), sender.getPublic(), remainingFunds);
   transaction.addOutputUTXO(remainingUTXO);
```

First we compute what the remaining funds will be and create a fourth `UTXO`. In this instance,
we reference the same `transaction` but the `sender` and `receiver` address will be the original `sender` account in the amount
of the remaining funds left. That is, the sender is going to pay themselves the difference between their available funds and
the cost of the transfer.

As the final step, the transaction is signed by the `sender` private key

```aidl
    transaction.signTheTransaction(sender.getPrivate());
```

The `signTheTransaction()` method takes a `PrivateKey` instance as an input

```aidl
    public void signTheTransaction(PrivateKey privateKey) {
        if(this.signature == null && !signed) {
            this.signature = UtilityMethods.generateSignature(privateKey, getMessageData());
            signed = true;
        }
    }
```

The `transaction` object is initiated with a `null` value and the `boolean` variable `signed` is set to false:

```aidl
    private byte[] signature = null;
    private boolean signed = false;
```

So the `signTheTransaction()` method will then set the `signature` using the `generateSignature()` method.

Notice that once the sender signs the transaction, we are no longer able to add additional `UTXO` to the
transaction object:

```aidl
    protected void addOutputUTXO(UTXO utxo) {
        if(!signed) {
            outputs.add(utxo);
        }
    }
```

We can test to see if the signature is verified using our `verifySignature()` method.

```aidl
    public boolean verifySignature() {
        String message = getMessageData();
        return UtilityMethods.verifySignature(this.sender, this.signature, message);
    }

```

We first request the message data from our `transaction` object and then check to see if the `sender` who signed
the `transaction` is indeed the same address.

## Chapter 4 :: Transaction, Wallet and Miner

In blockchain, transactions are initiated by wallets. The bitcoin white paper suggests that users should utilize a different
pair of public/private keys for each transaction to ensure security. If this suggestion is followed, a wallet must be capable
of storing a large number of keys belonging to the same user.

By the end of this chapter, we should be able to code:
* A wallet capable of initiating transactions
* Checking balances
* Storing a pair of public and private keys

We start by adding another method called `prepareOutputUTXOs()` to our `Transaction` class:

```aidl
    public boolean prepareOutputUTXOs() {
        if (this.receivers.length != this.amountToTransfer.length) {
            return false;
        }
        double totalCost = this.getTotalAmountToTransfer() + Transaction.TRANSACTION_FEE;
        double available = 0.0;
        for (UTXO input : this.inputs) {
            available += input.getAmountTransferred();
        }
        if (available < totalCost) {
            return false;
        }
        this.outputs.clear();
        for (int i = 0; i < receivers.length; i++) {
            UTXO utxo = new UTXO(this.getHashID(), this.sender, receivers[i], this.amountToTransfer[i]);
            this.outputs.add(utxo);
        }
        double remainingAmount = available - totalCost;
        UTXO change = new UTXO(this.getHashID(), this.sender, this.sender, remainingAmount);
        this.outputs.add(change);
        return true;
    }
```

