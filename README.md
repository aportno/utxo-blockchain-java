# Learning Blockchain in Java by Hong Zhou

## Executive Summary

This repo adapts the instructions to create a blockchain platform from Hong Zhou's _Learning Blockchain in Java - A step-by-step approach with P2P demonstration_.
The blockchain is developed from scratch using bitcoins UTXO accounting model.

[Purchase "Learning Blockchain in Java" on Amazon](https://www.amazon.com/Learning-Blockchain-Java-step-step/dp/1795002158)

[Reference Zhou's GitHub Repo](https://github.com/hhohho/learning-blockchain-in-java-edition-2)

---
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
---
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

---
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

Our `Transaction` class required some in-depth explanation. The amount to transfer will be sent from the sender to each of the
receivers (if many). So if the amount to transfer is set at 10, and there are 3 receivers, then the sender will be sending 10 * 3 =
30 total.

The `TRANSACTION_FEE` constant refers to the mining reward received by a miner. In bitcoin, transaction fees are dynamically
allocated by the transaction sender, in which case the transaction fee can increase or decrease depending on network traffic

```
    private byte[] signature = null;
    private boolean signed = false;
```

The signature is initially null. Once it is generated successfully, i.e., not null, the instance variable `signed` is set
to be true. In the `Transaction` class, only the method `signTheTransaction()` can change the valuables of variable `signature`
and variable `signed`. Moreover, once `signature` is not null or `signed` becomes true, the signature cannot be regenerated.
This is a secure coding practice to ensure that a transaction cannot be signed more than once.

`Transaction` includes two constructors. The first constructor...

```
    public Transaction(PublicKey sender, PublicKey receiver, double amountToTransfer, ArrayList<UTXO> inputs) {
        PublicKey[] publicKeys = new PublicKey[1];
        publicKeys[0] = receiver;
        double[] funds = new double[1];
        funds[0] = amountToTransfer;
        this.setUp(sender, publicKeys, funds, inputs);
    }
```

...takes a single instance of a `receiver` and `amountToTransfer`, while the second constructor takes an array of these variables:

```
    public Transaction(PublicKey sender, PublicKey[] receivers, double[] amountToTransfer, ArrayList<UTXO> inputs) {
        this.setUp(sender, receivers, amountToTransfer, inputs);
    }
```

The first constructor initializes a `PublicKey` array of length 1, and then sets `receiver` as the first value in the array.
It then creates a `double` array of length 1, and sets the `amountToTransfer` as the first value in the array. Finally, the
last step in the initialization process is to call the `setUp` method:

```
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

```
    protected void computeHashID() {
        String message = getMessageData();
        this.hashID = UtilityMethods.messageDigestSHA256_toString(message);
    }
```

It first creates a string `message` using `getMessageData()`:

```
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

```
    public void signTheTransaction(PrivateKey privateKey) {
        if(this.signature == null && !signed) {
            this.signature = UtilityMethods.generateSignature(privateKey, getMessageData());
            signed = true;
        }
    }
```

This method takes a `PrivateKey` as the input argument, but only uses it and never stores it. Any private key
should never be stored outside the key owner.

```
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

```
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
```
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
```
   for (int i = 0; i < receivers.length; i++) {
      receivers[i] = UtilityMethods.generateKeyPair().getPublic();
      amountToTransfer[i] = (i + 1) * 100;
   }
```

Initially, each position in the array contains a null value. We replace the
null values in each position with a public key generated by our `generateKeyPair().getPublic()` method in our `UtilityMethods` class.
The first account is credited with 1 * 100 = 100 funds. The second account is credited with 2 * 100 = 200 funds.

A sample of the public keys populated would like similar to:

```
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

```
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

```
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

```
   double available = 0.0;
   for (UTXO value : input) {
      available += value.getAmountTransferred();
   }
```

We loop through each `UTXO` in the `input` array and run the summation of the amounts transferred, which we've programmed
to be 1000.

```
    double totalCost = transaction.getTotalAmountToTransfer() + Transaction.TRANSACTION_FEE;
```

The `totalCost` of the transaction is going to be the amount that will be transferred from the sender to the receivers plus
the mining fee (or transaction fee) of 1. The first for loop populated that first receiver with 100 funds and the second receiver
with 200 funds, so in total the amount to be transferred is 300. This number is computed using the `getTotalAmountToTransfer()` method
in the `Transaction` class

```
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
```
   if (available < totalCost) {
      System.out.println("Fund available=" + available + ", not enough for total cost of " + totalCost);
   }
```

Now that the input `UTXO` is complete, we can create our **_output_** `UTXO`:

```
   for (int i = 0; i < receivers.length; i++) {
      UTXO outputUTXO = new UTXO(transaction.getHashID(), sender.getPublic(), receivers[i], amountToTransfer[i]);
      transaction.addOutputUTXO(utxo2);
   }
```

This for loop will iterate over each position in the original `receivers` array. At each position,
we create a new `UTXO` object referencing the current transaction, the `sender` public key, the `receivers`
public key and the amount to be transferred tied to the `receivers` address. We then use the `addOutputUTXO()` method in the
`Transaction` class to add the UTXO to our `transaction` object

```
    protected void addOutputUTXO(UTXO utxo) {
        if(!signed) {
            outputs.add(utxo);
        }
    }
```

We now need to create a `UTXO` to return any change to the `sender`.

```
   double remainingFunds = available - totalCost;
   UTXO remainingUTXO = new UTXO(transaction.getHashID(), sender.getPublic(), sender.getPublic(), remainingFunds);
   transaction.addOutputUTXO(remainingUTXO);
```

First we compute what the remaining funds will be and create a fourth `UTXO`. In this instance,
we reference the same `transaction` but the `sender` and `receiver` address will be the original `sender` account in the amount
of the remaining funds left. That is, the sender is going to pay themselves the difference between their available funds and
the cost of the transfer.

As the final step, the transaction is signed by the `sender` private key

```
    transaction.signTheTransaction(sender.getPrivate());
```

The `signTheTransaction()` method takes a `PrivateKey` instance as an input

```
    public void signTheTransaction(PrivateKey privateKey) {
        if(this.signature == null && !signed) {
            this.signature = UtilityMethods.generateSignature(privateKey, getMessageData());
            signed = true;
        }
    }
```

The `transaction` object is initiated with a `null` value and the `boolean` variable `signed` is set to false:

```
    private byte[] signature = null;
    private boolean signed = false;
```

So the `signTheTransaction()` method will then set the `signature` using the `generateSignature()` method.

Notice that once the sender signs the transaction, we are no longer able to add additional `UTXO` to the
transaction object:

```
    protected void addOutputUTXO(UTXO utxo) {
        if(!signed) {
            outputs.add(utxo);
        }
    }
```

We can test to see if the signature is verified using our `verifySignature()` method.

```
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

```
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
The `available` constant is set to `0.0` because we will use this variable to accumulate the total amount of funds we will transfer.

There are two checks in this method to safeguard that the initiation of a transaction is valid.

First, the length of receivers (or number of receivers) must match the length `amountToTransfer` exactly. I've raised this
observation earlier whereas we have a relatively insufficient mapping of accounts -> transfer value. We will look to optimize
this feature as we approach the end of our project.

Second, the `available` funds from the input `UTXO` must be enough for the `totalCost`. If at least one condition is unmet then
the method aborts and returns false.

There is one more additional safeguard we will add in the final chapter to help safeguard against some blockchain attack cases

We use `this.outputs.clear()` to ensure the `UTXO` output array is cleared before we add any `UTXOs`. This is important
to avoid overpaying the recipients in error.

Next, we create an output `UTXO` and append them to our clean `outputs` UTXO array.

The final preparation required for this method is to compute the "change" or remaining funds to pay back to the `sender`.

```
   double remainingAmount = available - totalCost;
   UTXO change = new UTXO(this.getHashID(), this.sender, this.sender, remainingAmount);
   this.outputs.add(change);
```

Note this output `UTXO` will send the remaining funds back to the `sender`

We also created our first `Wallet` class in this chapter. The `Wallet` class is one of the most complicated programs in blockchain.

The most basic requirements for our wallet are a pair of keys and a name. Wallets are represented by their public keys, but for the
sake of easier identification, we will give each wallet a name.

```
    private KeyPair keyPair;
    private String walletName;
```

The method constructor will take a `walletName` as a string and then initialize the `keyPair` and `walletNames` of the object

```
    public Wallet(String walletName) {
        this.keyPair = UtilityMethods.generateKeyPair();
        this.walletName = walletName;
    }
```

We then need to include two additional data items:
1) a password
2) a location where the keys are to be stored

The password is used only for the wallet creation and retrieval. The location for key storage should be a specific
directory added as a static field inside the `Wallet` class.

To use a password to protect our saved keys, we need a mechanism that will encrypt the keys using the password. We add in two more static
methods to the `UtilityMethods` class to make it handy to apply the bitwise exclusive XOR to encrypt and decrypt data.

```
    public static byte[] encryptionByXOR(byte[] key, String password) {
        byte[] passwordToBytes = UtilityMethods.messageDigestSHA256_toBytes(password);
        byte[] result = new byte[key.length];
        
        for (int i = 0; i < key.length; i++) {
            int j = i % passwordToBytes.length;
            result[i] = (byte)((key[i] ^ passwordToBytes[j]) & 0xFF);
        }
        return result;
    }
```

In the `encryptionByXOR()` method, we generate a hash value first based on the password. This provides an extra layer of
security. The hash value has only 32 bytes (256 bits) based on the SHA-256 algorithm we've been using, which may not be
long enough to XOR every bit in the byte array `key`. 

The bitwise operator is a binary operation that takes two bit patterns of equal length and performs the logical inclusive
OR operation on each pair of corresponding bits. The result in each position is 0 if both bits are 0, while otherwise
the result is 1. In other words, as long as one of the bits is a 1, then the operator will return a 1.

| R   | 0   | 1   | 1   | 0   |
|-----|-----|-----|-----|-----|
| A   | 0   | 1   | 0   | 1   |
| B   | 0   | 1   | 1   | 0   |

Where **R** is the return value of the bitwise operator.

The bitwise exclusive OR (also known as XOR) has an outstanding property: 
* A XOR B = C, then
* B XOR C = A

Assuming **B** is the password, **A** is the data, and **C** is the encrypted data obtained by A XOR B.

To obtain A from C, we just need to perform XOR action between B and C. In Java, the exclusive OR operator
is represented by `^`, which can only be performed between two integers.

We can reconstruct missing data if we have 2 parts of the 3 items:
* 65 XOR 22 = 87
* 87 XOR 22 = 65
* 65 XOR 87 = 22

Back to the `encryptionByXOR()` method, the code `key[i] ^ passwordToBytes[j]` performs an XOR operation between a byte in
`key` and a byte in `passwordToBytes`, the result of which is further performed an `&` i.e., AND operation with the hexadecimal
number 0xFF. 0xFF is a 4-byte integer with every bit being 1. We know that the `&` operation can only result in 1 if both bits
are 1, otherwise the result will be 0. This `&` operation here is to guarantee that the result is preserved properly. In fact,
as `key[i]` and `passwordToBytes[j]` are both of the data type byte, it is not necessary to add this `&` operation.

```
    public static byte[] decryptionByXOR(byte[] key, String password) {
        return encryptionByXOR(key, password);
    }
```

The `decryptionByXOR()` method calls `encryptionByXOR()` method to convert the encrypted data back to the original using
the same password.

We test the encryption and decryption in our `TestXOR` class. The results show our function is working as expected:

```
Our message to encrypt is: At the most beautiful place, remember the most beautiful you
The password is: blockchain

The encrypted data is: 
???U_U?5??h????8=???<???m?Q????O:f??y?????98???}??

After proper decryption, the message is:
At the most beautiful place, remember the most beautiful you

Using an incorrect password, the decrypted message looks like:
?}E?q^?^??*J??N?$?q
?]?;??F?%?77?(?

```

Alternatively, we can use the AES encryption algorithm for password-based encryption. The central ideas to AES encryption are:
1) Apply the password and a random large number called _salt_ to prepare a KeySpec object `keySpec`, then create a temporary key based on `keySpec`
2) Obtain a SecretKey instance compatible with the AES encryption algorithm by using the temp key's information
3) Create a Cipher object and initialize it with the secret key to encrypt the data. The password is incorporated into the key generation process so that only the authentic password can repeat this process to construct a correct SecretKey instance to decrypt the data
4) Data items such as _salt_ must remain constant for proper decryption, and so they must also be incorporated into the encrypted data

The `encryptionByAES()` method first creates a byte array named `salt` of size 8. Then we create an instance of a random number
generated by the `SecureRandom`:

```
byte[] salt = new byte[8];
SecureRandom rand = new SecureRandom();
```

Printing the `rand` instance to screen will not show the actual random number generated:

```
java.security.SecureRandom@3b22cdd0
```

We use `nextBytes()` to push the random number into our `salt` array:

```
rand.nextBytes(salt);
```

So now `salt` will be an array of 8 randomly generated numbers. This will provide an extra layer security. If we only relied
on the plain text password protection (like our XOR encryption), it would be relatively easy for a type of attack called a "library attack"
to breach security because there are only a finite number of character combinations.

Using a `salt` password in addition to the "plain" text password, the difficulty level for a breach to happen is dramatically increased.

```
String keyAlgo = "PBKDF2WithHmacSHA1";
SecretKeyFactory factory = SecretKeyFactory.getInstance(keyAlgo);
KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
SecretKey tempKey = factory.generateSecret(keySpec);
SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
```

The `keySpec` variable is instantiated with `PBEKeySpec` with four arguments:
1) the text password in a character array
2) `salt` password
3) the iteration count which specifies how many times the password is hashed for the derivation of the crypto key (we used 1024)
4) the key size (we used 128 because for AES algo, keys of 128-bits are the most common)

Now that we have encryption methods for our wallets, we can add a second constructor to our `Wallet` class that takes two
strings as arguments:

```
   public Wallet(String walletName, String password) {
      this.keyPair = UtilityMethods.generateKeyPair();
      this.walletName = walletName;

```

The Keys generated by the constructor are stored and encrypted. Note it populates the class variables:

```
public class Wallet {
    private KeyPair keyPair;
    private String walletName;
    private static String keyLocation = "keys";
```

The `keyLocation` variable will be used to create a folder name to store the keys created by each `Wallet` instance.

We then try to call a custom method `populateExistingWallet()`.
This method takes a `walletName` and `password` as arguments:

```
    private void populateExistingWallet(String walletName, String password) throws IOException, FileNotFoundException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(Wallet.keyLocation
                + "/"
                + walletName.replace(' ', '_') + "_keys");

        byte[] bb = new byte[4096];
        int size = fileInputStream.read(bb);

        fileInputStream.close();

        byte[] data = new byte[size];

        for (int i = 0; i < data.length; i++) {
            data[i] = bb[i];
        }

        byte[] keyBytes = UtilityMethods.decryptionByXOR(data, password);
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(keyBytes));

        this.keyPair = (KeyPair)(objectInputStream.readObject());
        this.walletName = walletName;
    }
}
```

The method will first try to load an existing `Wallet` instance by searching for a matching name and password.
If a match cannot be found, it usually means that no such wallet exists, and so the program creates a new `Wallet` instance.

More specifically, the program will search for the folder `keys\NAME_ENTERED_keys` in the current directory. If it does not
exist then a `FileNotFoundException` will be raised. Next, the program will move to our `prepareWallet()` method.

```
    private void prepareWallet(String password) throws IOException, FileNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(this.keyPair);

        byte[] keyBytes = UtilityMethods.encryptionByXOR(byteArrayOutputStream.toByteArray(), password);
        File file = new File(Wallet.keyLocation);

        if (!file.exists()) {
            file.mkdir();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(Wallet.keyLocation
                + "/"
                + this.getName().replace(' ', '_')
                + "_keys");

        fileOutputStream.write(keyBytes);
        fileOutputStream.close();
        byteArrayOutputStream.close();
    }
```

If successful, a wallet will be created and the keys stored in the `keyLocation` folder

In bitcoins blockchain, there are some fully functional nodes that are much more powerful than a wallet.
They keep the latest copy of the public blockchain (aka the ledger), respond to query, mine and broadcast blocks, and participate
in votes. This book does not focus on creating one of these fully functional nodes, but it does create a participating
role that can mine blocks and keep a local copy of the public ledger:

```
public class Miner extends Wallet{

    public Miner(String minerName, String password) {
        super(minerName, password);
    }

    public boolean mineBlock(Block block) {
        return (block.mineTheBlock());
    }
}
```
---
## Halfway Recap

We've now approached around the halfway point in the text. So far we've created a `Block`, `Miner`, `Transaction`, `UtilityMethods`,
`UTXO`, and `Wallet` class. We then built test cases such as `TestBlockMining`, `TestTransaction` and `TestWallet` using these classes
as building blocks.

From first principles, a `block` on the blockchain is composed of `transactions` that have been validated by `miners`. The transactions are between a sender `wallet` and a receiver `wallet`.
A `miner` validates the `transactions` by verifying the validity of the `utxo` involved in the `transaction`.

#### <u>The UTXO (Unspent Transaction Output)</u>
The `UTXO` class is takes a transaction ID, the sender public keys, the receivers public keys and the amount to be transferred from the senders
wallet to the receiver's wallet address. The `UTXO` object operates similar to a `block` object in the sense that the metadata
of the `UTXO` is hashed using a SHA256 algorithm. The hash value will be composed of the parent transaction ID, the senders public key,
the receivers public key, the amount transferred, the timestamp of the UTXO, and a unique number (in our case it's a counter for the number
of times we've called out `UtilityMethods` class).

In a simple transaction, there will be an input `UTXO` and an output `UTXO`. The difference between the two is trivial: there will be one
input `UTXO` constructed with the sender's public address as both the `sender` and `receiver` of the `UTXO` and two
output `UTXO` -- one to pay the `receiver` party and one to pay any change back to the `sender`.

#### <u>The Transaction</u>

The `transaction` class is initialized with a single public key address and amount so the primary constructor is called.
The `transaction` class will store all public keys involved in the `transaction` in an array. Initially, the first value in the
array will be the `receiver` public key address. The `amountToTransfer` will be initialized in a separate array but will also
replace the first value in the array with the `receivers` public keys. Similar to the `UTXO` and `Block` methods, the `transaction`
metadata is hashed, specifically the `transaction` values of a unique number, the sender address, the receiver address, amount transferred,
the `UTXO` involved, and a timestamp.

This initialization of the `transaction` object references the input `utxo` only. The output `utxo` in the `transaction` is created
by first checking if the length of the `receiver` array is equal to the length of the `amountToTransfer` array. If true,
then we check if the total amount the sender wants to transfer is less than the total amount of the input `UTXO` provided.
The next step is to create output `utxo` for each public key in the `receiver` array. The `amountToTransfer` is mapped to the index
of the `funds` array. Each output `utxo` is then added to the `output` `UTXO` array of the `transaction` object.

At this point, the `transaction` object contains one input `utxo` and two output `utxo` objects (one for each `receiver` address involved in the transaction).
There is one missing component to complete the lifecycle of the transaction; the change to return to the `sender` address.
To do so, we create one more output `UTXO` object that takes the same unique number as all other output `UTXO` but the `sender` and
`receiver` address will populate as the `sender` address (similar to the initial input `utxo`).

The preparation of our output `UTXO` is now complete. To ensure the authenticity of the proposed transaction by the `sender`, the program
attempts to sign the transaction using the `sender` private key and the transactions message data (a hash value).

We've successfully used cryptographic techniques to generate our first transaction. But how do we know if the `sender` is actually
the one who initiated the request and gave consent to transfer the expected funds? We use the method `verifySignature` to check
if the `sender` was indeed the address that signed the `transaction`. First, we retrieve the message data inside the `transaction` object.
Next, we call `verifySignature` and pass in the `sender` public key, the `signature` of the `transaction` object, and the `transaction` `message data`.

For avoidance of doubt, the `signature` of the transaction required the `sender` private key and the `transaction` `messageData`.
Now to verify the `signature`, we require the `sender` public key, `signature`, and `messageData`. How do we verify with this information?

The general idea behind this digital signature is to sign with the private key, verify with a public key. To send me a message
with proof that you are the sender, you use the private key to lock the message. You're the only person with the private key, and only a public
key can unlock messages locked by your private key. If the message was tampered then upon arrival, I would notice the message you
sent was no longer signed. If the person tampering the message wanted to conceal involvement, they could try to resign the message
using the public key (because only you have the private key). But now when I receive the tampered, resigned message, I am no longer
able to open up because the only way I can open it with your public key is if the message is signed with your private key!

So the only way to know if the signature is verified if I'm able to unlock the message with your public key. If I cannot do this,
then the message was not sent from your address and an exception is raised and the transaction fails.

You can read more about signing with a private key, verifying with a public key on this [auth0 blog post](https://auth0.com/blog/how-to-explain-public-key-cryptography-digital-signatures-to-anyone/).

#### <u>The Miner </u>
To achieve our end goal of having this `transaction` put on the ledger (included in a `block`), we need to rely on a `miner` to validate
the authenticity of the `transaction`. If the `signature` is not verified by the miner, the `transaction` fails, and it will not be included
in the next block. First the `transaction` is broadcast to all `miner`. Then the `miner` actively tries to compute a hash value
that meets the constraints of the `block` difficulty specified by the protocol. Once the miner solves for the correct
nonce, they broadcast their solution to the network. If all miners agree with the solution then the `block` is added to the
`blockchain`.

#### <u>The Block </u>
And then the process starts all over again. Our initial transaction is finally part of the blockchain. We created `UTXO` to form
a `transaction`. The `transaction` was validated by a `miner` and included in the blocks `hash` value that the network has come to
a consensus on. The next `block` will need to reference the previous `block` where the initial transaction took place. If there
is any tampering that takes place then the hash value of the previous `block` will be completely different than what the network came
to consensus on and the network will be tasked with correcting to the rightful state and handling the malicious actor

#### <u>What's next? </u>
We will introduce the dynamic of having active `wallets` carrying `UTXO` and other functions of `blocks` on the `blockchain`.

---
## Chapter 5 :: Block and Blockchain

We revisit our `Block` class and add some additional functionality. The class now contains the following class variables:
```
    public final static int TRANSACTION_UPPER_LIMIT = 2;
    private static final long serialVersionUID = 1L;
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private String hashID;
    private String previousBlockHashID;
    private long timestamp;
    private int nonce = 0;
    private int difficultyLevel;
```
We added a `TRANSACTION_UPPER_LIMIT` instance field to specify the maximum number of transactions allowed in a block. This 
is a targeted constraint within this blockchain and differs from bitcoins approach where block transactions are somewhat limited by its
size. Each bitcoin block could not exceed 1mb. The `transactions` array was also adjusted from an array of `string` to an array of `Transaction` class
to accommodate the dynamic nature of adding multiple transactions to a block.

The constructor remains the same:
```
    public Block(String previousBlockHashID, int difficultyLevel) {
        this.previousBlockHashID = previousBlockHashID;
        this.timestamp = UtilityMethods.getTimeStamp();
        this.difficultyLevel = difficultyLevel;
    }
```

We added a new `addTransaction()` method that takes `Transaction` as an input and returns a boolean:

```
    public boolean addTransaction (Transaction transaction) {
        if (this.getTotalNumberOfTransactions() >= Block.TRANSACTION_UPPER_LIMIT) {
            return false;
        } else {
            this.transactions.add(transaction);
            return true;
        }
    }
```

Utilizing our `TRANSACTION_UPPER_LIMIT` as an upper boundary, if the total number of transactions (or the size of the transaction array) 
is greater than the upper limit than the boolean returns `false`. Otherwise, the transaction is added to the transaction array
accordingly and the boolean returns `true`. Note that at inception, the size of the transaction array is 0, and can only increase
in size by utilizing this `addTransaction()` method.

The `computeHashID()` method is largely unchanged, except the enhanced for loop has been adjusted to accommodate the `transaction` array (previously
was an array of strings during our earlier tests)

```
    protected String computeHashID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.previousBlockHashID).append(Long.toHexString(this.timestamp));
        for (Transaction transaction : transactions) {
            sb.append(transaction.getHashID());
        }
        sb.append(Integer.toHexString(this.difficultyLevel)).append(nonce);
        byte[] b = UtilityMethods.messageDigestSHA256_toBytes(sb.toString());
        return UtilityMethods.toBinaryString(b);
    }
```

All previous getter methods are still included in the `Block` class, as well as our `mineTheBlock()` method which increments the nonce
until the required difficulty level is solved. That completes the remaining adjustments to the `Block` class.

In order to chain these blocks together to form a blockchain, we will need to use a data structure that contains a list.
The genesis block _G_ should link to the first block _B<sub>1</sub>_, linked to the second block _B<sub>2</sub>_, etc:

| G   | B<sub>1</sub> | B<sub>2</sub>    | B<sub>3</sub> | ... |
|-----|---------------|------------------|---------------|-----|

We opted to use a customized class `LedgerList` because it wraps an instance of an `ArrayList` and provides the necessary dynamic functionality for the blockchain to
add a block at the end of the chain or find a block quickly with an index. The class does not allow a block to be inserted or deleted.

The constructor will create a simple dynamic ArrayList:

```
    public LedgerList() {
        list = new ArrayList<>();
    }
```

The class has two private variables:
1) `serialVerionUSD` is used for serialization
2) `list` is an `ArrayList` that will house all blocks. Note that given it is a `private` variable, the only way to call the variable is within the class

```
    @Serial
    private static final long serialVersionUID = 1L;
    private final ArrayList<E> list;
```

The only way to edit `list` is using the `add()` method. This restricts our access to `list` and allows us to only append accordingly:

```
    public boolean add(E e) { return this.list.add(e); }
```

For the purpose of secure coding, the elements of the `LedgerList` class can only be accessed one at a time.

We then create a new `Blockchain` class that contains one constructor that uses a genesis block as the input:

```
    public Blockchain(Block genesisBlock) {
        this.blockchain = new LedgerList<>();
        this.blockchain.add(genesisBlock);
    }
```
As well as three class variables initialized:

```
    @Serial
    private static final long serialVersionUID = 1L;
    public static final double MINING_REWARD = 100.0;
    private final LedgerList<Block> blockchain;
```

We utilize our new `LedgerList` class that will store objects from our `Block` class. In practice, each additional block will be
"chained" in this array, forming the blockchain. We've set the `MINING_REWARD` with an arbitrary value of 100.0. At the moment,
this parameter is not being used in our program.

The genesis (or very first) block is very special in the history of a blockchain. Every blockchain starts from a genesis block, therefore
it would be a good idea to request the genesis block in order to construct a blockchain.

The `addBlock()` method is `synchronized` because we must guarantee that only one calling method can append a block to the
blockchain at a time. This will avoid potential collision or forks in the future.

```
    public synchronized void addBlock(Block block) {
        if (block.getPreviousBlockHashID().equals(this.getLastBlock().getHashID())) {
            this.blockchain.add(block);
        }
    }
```

To help assist in finding the balance for a given public key, we created the method `findRelatedUTXOs()`. Later we will see
why it's necessary to overload the function. The function is computationally expensive because it searches through the entire blockchain
looking for UTXOs related to the public key. In reality, a complete search process is only necessary when validating a blockchain.
A wallet is only concerned with its own ledger, so searching through the entire blockchain to find its balance is unnecessary.

```
    public double findRelatedUTXOs(PublicKey publicKey, ArrayList<UTXO> all, ArrayList<UTXO> spent, ArrayList<UTXO> unspent, ArrayList<Transaction> sentTransactions) {
        double gain = 0.0, spending = 0.0;
        HashMap<String, UTXO> map = new HashMap<>();
        int limit = this.getBlockchainSize();
        for (int i = 0; i < limit; i++) {
            Block block = this.blockchain.findByIndex(i);
            int blockSize = block.getTotalNumberOfTransactions();
            for (int j = 0; j < blockSize; j++) {
                Transaction transaction = block.getTransaction(j);
                int n;
                if (i != 0 && transaction.getSender().equals(publicKey)) {
                    n = transaction.getNumberOfInputUTXOs();
                    for (int k = 0; k < n; k++) {
                        UTXO inputUTXO = transaction.getInputUTXO(k);
                        spent.add(inputUTXO);
                        map.put(inputUTXO.getHashID(), inputUTXO);
                        spending += inputUTXO.getAmountTransferred();
                    }
                    sentTransactions.add(transaction);
                }
                n = transaction.getNumberOfOutputUTXOs();
                for (int k = 0; k < n; k++) {
                    UTXO outputUTXO = transaction.getOutputUTXO(k);
                    if (outputUTXO.getReceiver().equals(publicKey)) {
                        all.add(outputUTXO);
                        gain += outputUTXO.getAmountTransferred();
                    }
                }
            }
        }
        for (UTXO utxo : all) {
            if (!map.containsKey(utxo.getHashID())) {
                unspent.add(utxo);
            }
        }

        return (gain - spending);
    }
```

The method takes 5 arguments:
1) `PublicKey publicKey` is the public key to the wallet you are searching for on the blockchain
2) `ArrayList<UTXO> all` collects all output `UTXO` where wallet is the receiver
3) `ArrayList<UTXO> spent` collects all spent input `UTXO` where wallet is the sender
4) `ArrayList<UTXO> unspent` collects all unspent input `UTXO`
5) `ArrayList<Transaction> sentTransactions` collects are transactions affiliated with a `UTXO`

The logic inside the method could be improved, but essentially it follows the below steps:
1) Create a counter to track (output) UTXOs received and (input) UTXOs sent related to the public key (wallet address) specified
2) Create a hash map containing all spent input UTXOs from wallet
3) Create a for-loop boundary to cycle through every block in the blockchain
4) Loop through each block in the blockchain
5) Select the block in the blockchain
6) Find the number of transactions in the block
7) Loop through each transaction
8) Check if the sender address in the transaction is from the public key, skipping the genesis block
9) Count the number of input UTXOs involved in the transaction -- these will be spent UTXOs
10) Loop through each spent input UTXO is the transaction
11) Add the input UTXO to the spent array -- we know this input UTXO was sent from the wallet (spent)
12) Add the input UTXO hash ID to the hash map containing all spent UTXOs from the wallet
13) Increment the counter tracking the input UTXOs spent by the wallet
14) Add the transaction to the list of historical transactions (`sentTransactions`)
15) Count the number of output UTXOs involved in the transaction -- these will be received UTXOs from wallet
16) Loop through each output UTXO in the transaction
17) Check if the receiver address in the transaction is from the public key
18) Add the output UTXO to the `all` array containing output UTXO where the wallet is the receiver
19) Increment the counter tracking the output UTXOs received by the wallet
20) Loop through each UTXO in the `all` array containing the output UTXO where the wallet is the receiver
21) Check if the output UTXO is inside the hash map containing all spent input UTXOs
22) If the output UTXO is also inside the hash map containing all spent input UTXOs then it is a spent UTXO
23) If the output UTXO is not in the hash map of spent UTXOs, then it is an unspent UTXO
24) Return the difference between UTXOs received and UTXOs spent

Next, we updated our `Wallet` class so that it can transfer funds on the `Blockchain`

First, we added a new instance variable `localLedger` to let each wallet have a local blockchain:

```
    private Blockchain localLedger;
```

The constructor remains the same. The wallet is initialized with a `keyPair` and `walletName`. If the `walletName` exists
then it is loaded accordingly, otherwise we create a new `wallet`.

We can then use `getLocalLedger()` to return the local blockchain of this wallet. Essentially each wallet will have it's own
"local" blockchain which will then connect to the larger "main" or "global" chain:

```
    public synchronized Blockchain getLocalLedger() {
        return this.localLedger;
    }
```

We added the new method `transferFund()`, which consumes a public key array `receivers` and a double array `amountToTransfer`.

```
    public Transaction transferFund(PublicKey[] receivers, double[] amountToTransfer) {
        ArrayList<UTXO> unspent = new ArrayList<>();
        double available = this.getLocalLedger().findUnspentUTXOs(this.getPublicKey(), unspent);
        double totalNeeded = Transaction.TRANSACTION_FEE;
        for (double fund : amountToTransfer) {
            totalNeeded += fund;
        }

        if (available < totalNeeded) {
            System.out.println(this.walletName + "balance=" + available + ", not enough to make the transfer of " + totalNeeded);
            return null;
        }

        // Create input for the transaction
        ArrayList<UTXO> inputUTXOs = new ArrayList<>();
        available = 0;
        for (int i = 0; i < unspent.size() && available < totalNeeded; i++) {
            UTXO unspentUTXO = unspent.get(i);
            available += unspentUTXO.getAmountTransferred();
            inputUTXOs.add(unspentUTXO);
        }

        // Create the transaction
        Transaction transaction = new Transaction(this.getPublicKey(), receivers, amountToTransfer, inputUTXOs);

        // Prepare output UTXO
        boolean isPreparedOutputUTXO = transaction.prepareOutputUTXOs();
        if (isPreparedOutputUTXO) {
            transaction.signTheTransaction(this.getPrivateKey());
            return transaction;
        } else {
            return null;
        }
    }
```

The method initializes an ArrayList of `UTXO` objects. It's important to check the available funds for the wallet. We do so
by calling `getLocalLedger()` to reference the `Blockchain` object initialized for this `wallet` and then by calling
the `findUnspentUTXOs()` method explained previously. This will return all available UTXOs that can be spent by this wallet.

Any transaction on our blockchain requires some transaction fee that is paid to the miners. We initialize `totalNeeded` with
this value as a baseline amount as it is the bare minimum amount required to effectuate a transfer. The method goes on to check
to make sure the available UTXOs will cover the transfer amount. Once confirmed, the wallet needs to create UTXOs to complete
the requested transaction

We create an ArrayList of `UTXO` to collect our `inputUTXO`, then we loop through our `unspent` utxo array and sum all
available funds in the `UTXO`. The end of this process will result in an ArrayList of all available unspent `UTXO` associated
with the wallet, a baseline requirement before initiating a transaction.

Next we create a `Transaction` object using the `wallet` public key, the designated `receivers`, `amountToTransfer` and the 
`inputUTXOs` that will be used to process the transfer. The `transcaction` object calls the `prepareOutputUTXOs()` method returning
a boolean value. If the output returns true, then the `output` arrayList in transaction object will be populated with
`outputUTXOs`.

If the `outputUTXOs` are prepared, then by definition that means there was sufficient `inputUTXOs` to effectuate the transaction.
The next step is for the wallet to sign the transaction with the wallets `privateKey`, and then return the signed `transaction` object.

We've now built enough functionality to test our blockchain using a `BlockchainPlatform` class. At the moment,
our `wallets` are not distributed and all share the same copy of the blockchain. The system will be distributed when we
apply networking architecture into our system.

The `BlockchainPlatform` class is primitive. This version lacks many necessary features:
* Miners cannot collect mining rewards or transaction fees
* When adding a block, currently we only examine if it is sequentially the correct block to use. There should be some other types of verification
  * For example, we need to make sure that no UTXO is double spent, and no transaction is added twice

However, it does highlight some key features we've built. The `BlockchainPlatform` class is initialized with two variables:

```
    private static Blockchain blockchain;
    private static double transactionFee = 0.0;
```

The `blockchain` variable will serve as the object connecting all of the `block` we generate and the `transactionFee` is the amount
miners will receive (this feature is yet to be built).

Before diving into the `main()` method, the class has one additional method called `displayBalanceAfterBlock()` which essentially
shows the balances of all participants involved on-chain and then some metadata about each block produced.

Now inside our `main()` method, we initialize the level of mining difficulty:

```
     int difficultyLevel = 25;
```

Given this is the start of our blockchain, the very first block with be coined the `genesis` block. The `genesis` block can only
be hashed if there are `miners` available to validate the transactions on the block.

So we create a `Miner` instance called `genesisMiner`. Remember the `Miner` class extends the `Wallet` class so a `miner` by definition
is a `wallet`:

```
     Miner genesisMiner = new Miner("genesis", "genesis");
```

Next, we create our very first `Block`, the `genesisBlock`:

```
     Block genesisBlock = new Block("0", difficultyLevel);
```

We generate two UTXOs to fund the `genesisMiner` account accordingly. They will later be used as inputs for the genesis transaction:

```
    UTXO firstInputUTXO = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10001.0);
    UTXO secondInputUTXO = new UTXO("0", genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0);
```

Note these initial `UTXO` objects do not verify where the "amountToTransfer" comes from. They simply receive the inputs and populate
the `UTXO` class variable with the arbitrary amount, the `sender` of the arbitrary amount, and the `receiver` of the arbitrary amount.
Creating `UTXO` does not fund the `wallet`. To add funds, a `wallet` needs to `transact` `UTXO`. Creating the `UTXO` is the first
step in being able to `transact`.

So we create an ArrayList of `UTXO` to store out input `UTXO`:

```
     ArrayList<UTXO> inputUTXO = new ArrayList<>();
     inputUTXO.add(firstUTXO);
     inputUTXO.add(secondUTXO);
```

Now we have enough data to create our `genesisTransaction` and fund our `genesisMiner` wallet:

```
     Transaction genesisTransaction = new Transaction(genesisMiner.getPublicKey(), genesisMiner.getPublicKey(), 10000.0, inputUTXO);
```

The `genesisTransaction` will use the `genesisMiner` as both the `sender` and `receiver` in the `transaction`. The amount of `10000.0`
was arbitrarily chosen. The `transaction` consumed our arrayList of input `UTXO`, so will have 2 input `UTXO` and 0 output `UTXO`
stored in the object. It should be highlighted that the `amountToTransfer` is the key numerical variable. The value of the input `UTXO`
is only relevant in confirming there is enough funds to effectuate the `amountToTransfer`. So in this case, we passed in 2
input `UTXO` equaling a combined value of `10000.0` + `10001.0` = `20001.0`. The `transaction` object does not store this amount
anywhere. The only value it stores, and functions around, is the `amountToTransfer` which is `10000.0`.

At this point in time the `genesisTransaction` has an array of input `UTXO` to reference but has not produced any output `UTXO` required
to complete the `transaction`

```
     boolean isPreparedOutputUTXO = genesisTransaction.prepareOutputUTXOs();
     if (!isPreparedOutputUTXO) {
         System.out.println("Genesis transaction failed");
         System.exit(1);
     }
```

Utilizing the `prepareOutputUTXOs()` method, we can begin the process of completing the transaction.

The `transaction` class has a hardcoded `TRANSACTION_FEE` of `1.0`, so the total cost of the `transaction` is going to be
`amountToTransfer` + `TRANSACTION_FEE` = `10000.0` + `1.0` = `10001.0`. The method loops through our input `UTXO` array and sums
all available funds and stores it in a variable `available`. This is a local variable and is destroyed once the method exits. As long as
`available` is greater than the total cost then the method will continue.

To recap what our `transaction` "knows" at the moment, there are 2 input `UTXO` equaling `20001.0` in total value. The `transaction` is looking
to transfer `10000` from the `sender` to the `receiver`. There is 1 sender, and 1 receiver so there should be 1 output `UTXO` created. The `prepareOutputUTXOs()` method creates local `UTXO` variables so each `receiver` will receive the `amountToTransfer`.  

Since we are looking to transfer `10000.0` (plus a `1.0` fee) with `20001.0` in input `UTXO` available, there will be `20001.0` - `10001.0` = `10000`
remaining as "change" that will need to be remitted back to the `sender`. This additional output `UTXO` is added to the 
`outputs` array.

The `miner` wallet will record 2 output `UTXO` as the outcome of this `transaction`. It will receive `10000.0` as the `amountToTransfer`
and then `10000.0` as the `change` for a total of `20000.0`

We now have a `transaction` that's received a sufficient number of input `UTXO` from the `sender` to transfer an amount to
the `receiver`. The `sender` will now need to provide a signature to authenticate the transaction, so it can be added to the block:

```
    genesisTransaction.signTheTransaction(genesisMiner.getPrivateKey());
    genesisBlock.addTransaction(genesisTransaction);
```

*Note: surprisingly the program did not check to see if the transaction is verified before adding it to the block.

As per protocol, the miner looks to mine the block. If complete, the genesis block will be finalized and ready to be added
to the blockchain:

```
   boolean isMinedBlock = genesisMiner.mineBlock(genesisBlock);
   if (isMinedBlock) {
      System.out.println("Genesis block successfully mined");
      System.out.println("Hash ID: " + genesisBlock.getHashID());
   } else {
      System.out.println("Failed to mine genesis block. System exit");
      System.exit(1);
   }
```

Bringing us to the next task of creating our `Blockchain` object:

```
   blockchain = new Blockchain(genesisBlock);
```

Followed by creating a copy of the ledger and localizing it in our `genesisMiner` object:

```
   genesisMiner.setLocalLedger(blockchain);
```

Now that the genesis block has been mined, we added additional users to start working on the next block on the chain:

```
   Miner userA = new Miner("Miner A", "Miner A");
   Wallet userB = new Wallet("Wallet A", "Wallet A");
   Miner userC = new Miner("Miner B", "Miner B");
```

All users should reference the same chain as their local ledgers:

```
   userA.setLocalLedger(blockchain);
   userB.setLocalLedger(blockchain);
   userC.setLocalLedger(blockchain);
```

Following our previous process, we build a block so transactions can be stored accordingly:

```
   Block block2 = new Block(blockchain.getLastBlock().getHashID(), difficultyLevel);
```

Our second transaction was a test for a much smaller amount. The genesis miner currently has a balance of `20000.0`. The looked
to transfer `100.0` from the `genesisMiner` to `userA`:

```
   Transaction transaction2 = genesisMiner.transferFund(userA.getPublicKey(), 100);
```

I'm not particularly impressed with the logic of this line because it embeds crucial functions complicating the logic rather
than simplifying the process. Specifically, the `transferFund()` method requires a signature to effectuate. However, in the setting
of our test case, it looks as if the transfer can occur without signature. Maybe it will make more sense down the road but at the
moment I'd rather the signature show some presence before triggering the verification boolean:

```
   if (transaction2 != null) {
      if (transaction2.verifySignature() && block2.addTransaction(transaction2)) {
          System.out.println("Current balances");
          double total = genesisMiner.getCurrentBalance(blockchain)
                  + userA.getCurrentBalance(blockchain)
                  + userB.getCurrentBalance(blockchain)
                  + userC.getCurrentBalance(blockchain);
          System.out.println("Genesis miner: " + genesisMiner.getCurrentBalance(blockchain));
          System.out.println("User A: " + userA.getCurrentBalance(blockchain));
          System.out.println("User B: " + userB.getCurrentBalance(blockchain));
          System.out.println("User C: " + userC.getCurrentBalance(blockchain));
          System.out.println("Transaction added to second block...");
      } else {
          System.out.println("Failed to create transaction");
      }
   }
```

`transaction2` is complete and ready to be included in `block2`. The balances being printed in the code block above are related
to the current balances on the `blockchain`, which at the moment only contains the `genesisBlock`. It won't be until after `block2`
is mined and added to the `blockchain` until the balances are updated

We add another test transaction, where the `genesisMiner` transfers 200 to `userB`:

```
   Transaction transaction3 = genesisMiner.transferFund(userB.getPublicKey(), 200);
   if (transaction3 != null) {
      if (transaction3.verifySignature() && block2.addTransaction(transaction3)) {
          System.out.println("Balances on the BLOCKCHAIN prior to block 2 addition");
          double total = genesisMiner.getCurrentBalance(blockchain)
                  + userA.getCurrentBalance(blockchain)
                  + userB.getCurrentBalance(blockchain)
                  + userC.getCurrentBalance(blockchain);
          System.out.println("Genesis miner: " + genesisMiner.getCurrentBalance(blockchain));
          System.out.println("User A: " + userA.getCurrentBalance(blockchain));
          System.out.println("User B: " + userB.getCurrentBalance(blockchain));
          System.out.println("User C: " + userC.getCurrentBalance(blockchain));
          System.out.println("Transaction added to second block...");
      } else {
          System.out.println("Failed to add transaction to second block");
      }
   } else {
      System.out.println("Failed to create transaction");
   }
```

`transaction3` will produce a successful result. We can now mine `block2` and add it to the `blockchain`:

```
   if (userA.mineBlock(block2)) {
      blockchain.addBlock(block2);
      System.out.println("User A mined second block");
      System.out.println("Hash ID: " + block2.getHashID());
      System.out.println("After second block is added to the chain, the balances are: ");
      displayBalanceAfterBlock(block2, genesisMiner, userA, userB, userC);
   }
```

`userA` is a `miner` object and will continue to compute hashes until the correct hash has been found. Once this occurs, 
`block2` is added to the `blockchain` and the updated balances will reflect `transaction2` and `transaction3`"

```
Current balances on the blockchain
Genesis miner: 19698.0
User A: 100.0
User B: 200.0
User C: 0.0

Blockchain data:
Total Cash: 19998.0
Transaction fee: 2.0
Number of blocks: 2
```

It should be pointed out that the miner balance was initially 20,000. It then transferred 100 + 1 transaction fee = 101,
to user A followed by 200 + 1 transaction fee = 201 to user B for a total of 302 transferred. The mining balance is now
20,000 - 302 = 19,698.

For our 3rd block, we test our failure functions. We first try to transfer 200 from `userA` to `userB` in our 4th transaction.
The `transaction` fails because `userA` only has a balance of `100.0`:

```
   Transaction transaction4 = userA.transferFund(userB.getPublicKey(), 200.0);
   if (transaction4 != null) {
      if (transaction4.verifySignature() && block3.addTransaction(transaction4)) {
          System.out.println("Transaction 4 added to block 3");
      } else {
          System.out.println("Failed to add transaction to block 3");
      }
   } else {
      System.out.println("Failed to create transaction");
   }
```

Failure message:
```
Miner A balance=100.0
Not enough funds available to complete the transfer of 201.0
Failed to create transaction 4
```

We then try to transfer 300 from `userA` to `userC` for our 5th transaction, generating the same error:

```
   Transaction transaction5 = userA.transferFund(userC.getPublicKey(), 300.0);
   if (transaction5 != null) {
      if (transaction5.verifySignature() && block3.addTransaction(transaction5)) {
          System.out.println("Transaction 5 added to block 3");
      } else {
          System.out.println("Failed to add transaction 5 to block\5");
      }
   } else {
      System.out.println("Failed to create transaction 5\n");
   }
```

Failure message:
```
Miner A balance=100.0
Not enough funds available to complete the transfer of 301.0
Failed to create transaction 5
```

The 6th transaction will be successful because we're transferring an amount of `20` from `userA` that is less than their balance of
`100`:

```
   Transaction transaction6 = userA.transferFund(userC.getPublicKey(), 20.0);
   if (transaction6 != null) {
      if (transaction6.verifySignature() && block3.addTransaction(transaction6)) {
          System.out.println("Transaction 6 added to block 3...");
      } else {
          System.out.println("Failed to add transaction to block");
      }
   } else {
      System.out.println("Failed to create transaction");
   }
```

Success message:
```
Transaction 6 added to block 3
```

Our final test is to try and successfully transfer funds from `userB` to `userC`:

```
   Transaction transaction7 = userB.transferFund(userC.getPublicKey(), 80.0);
   if (transaction7 != null) {
      if (transaction7.verifySignature() && block3.addTransaction(transaction7)) {
          System.out.println("Transaction 7 added to block 3...\n");
      } else {
          System.out.println("Failed to add transaction 7 to block\n");
      }
   } else {
      System.out.println("Failed to create transaction 7\n");
   }
```

Success message:

```
Transaction 7 added to block 3
```

Our test program is complete once we confirm `block3` is added on-chain:

```
   if (userC.mineBlock(block3)) {
      blockchain.addBlock(block3);
      System.out.println("User C mined block 3");
      System.out.println("Block hash ID: " + block3.getHashID());
      System.out.println("Current balances on the blockchain");
      displayBalanceAfterBlock(block3, genesisMiner, userA, userB, userC);
   }
```

Success message:
```
Transaction 7 added to block 3...
```

Test results:
```
Starting blockchain platform...
Created genesis miner
Created genesis block

Genesis transaction added to the genesis block successfully
Attempting to mine genesis block...
Genesis block successfully mined

Block hash ID: 0000000000110011001010011111111111111111111111111100111011111111111111111111111110000111011001101111111111111111111111111000000111111111111111111111111111001001111111111111111111111111110110101111111111111111111111111100000100010010010101001111111111111111111111111100111111111111111111111111111110100000111111111111111111111111111110101111111111111111111111111000011111111111111111111111111110101110111111111111111111111111100111111111111111111111111111111101000000111011111111111111111111111111110111101111111111111111111111111010111111111111111111111111111111101000011100110100110100011000111111111111111111111111110001001111111111111111111111111000111111111111111111111111111111000000000110010000100011111111111111111111111111000111

Initializing blockchain...
Blockchain initialized with genesis block

Genesis miner balance: 20000.0

Creating block 2...
Block 2 created successfully

Balances on the BLOCKCHAIN prior to block 2 addition
Genesis miner: 20000.0
User A: 0.0
User B: 0.0
User C: 0.0
Transaction 2 added to block 2...

Balances on the BLOCKCHAIN prior to block 2 addition
Genesis miner: 20000.0
User A: 0.0
User B: 0.0
User C: 0.0
Transaction 3 added to block 2...

Attempting to mine block2...
User A mined block2
Block hash ID: 0000000000100000111111111111111111111111111111010100001011111111111111111111111110110011111111111111111111111111110110100111000011111111111111111111111111100001000011100100000111111111111111111111111111011100111111111111111111111111100000010111100101011001000011100000110100101101011000010010101100010110111111111111111111111111101011101111111111111111111111111010100100101010000010111111111111111111111111111001001001110010011001110100100111111111111111111111111110011110111111111111111111111111110010010011110111111111111111111111111110101001

Current balances on the blockchain
Genesis miner: 19698.0
User A: 100.0
User B: 200.0
User C: 0.0

Blockchain data:
Total Cash: 19998.0
Transaction fee: 2.0
Number of blocks: 2

Creating block...
Block created successfully

Miner A balance=100.0
Not enough funds available to complete the transfer of 201.0
Failed to create transaction 4

Miner A balance=100.0
Not enough funds available to complete the transfer of 301.0
Failed to create transaction 5

Transaction 6 added to block 3...

Transaction 7 added to block 3...

Attempting to mine block3...
User C mined block 3
Block hash ID: 0000000000100110111111111111111111111111110010000111001011111111111111111111111111011001000010110100010001001000111111111111111111111111100110110000110001101001001101011111111111111111111111111010100001000110111111111111111111111111101011100001011011111111111111111111111111110100111111111111111111111111111110101111111111111111111111111011100001001111111111111111111111111111100101011111111111111111111111111110011011111111111111111111111110001011111111111111111111111111101010101111111111111111111111111001000000110001111111111111111111111111111010000111011011111111111111111111111110000011111111111111111111111111110011010111110011111111111111111111111111101100

Current balances on the blockchain
Genesis miner: 19698.0
User A: 79.0
User B: 119.0
User C: 100.0

Blockchain data:
Total Cash: 19996.0
Transaction fee: 4.0
Number of blocks: 3

==========Blockchain Platform Shutting Down==========

Process finished with exit code 0
```
___
## Chapter 6 :: Blockchain Improved

Currently, our current blockchain system lacks a few necessary features:
* Mining rewards cannot be collected
* There is no validation process when a transaction is added to a block
* Adding a block into the chain does not go through a complete validation process
* Wallets currently share the same public blockchain instead of keeping local copies

The bitcoin white paper discusses memory management when blockchain grows enormously. One solution involves pruning
spent transactions from blocks buried deep in the chain, but the caveat is that discarding spent transactions can break 
the block's hash. To bypass this complication, the algorithm merkle tree is applied to hash transactions.

A Merkle tree can be a binary tree, a triple tree, or another type of tree. In a binary merkle hash tree, a node's hash is
always constructed from hash values of the two tree nodes directly below it, the exception being the bottom nodes.

Eventually, there will be only one hash left at the root, while the root hash is related to every bottom node. A block's hash
can be computed from the previous block's hash, nonce, timestamp, and transactions' root hash which is computed via a merkle
tree algorithm from all transactions' hashes.

---
## Chapter 7 :: Network and Network Messaging

Our blockchain system is still not distributed despite the significant improvements made in the prior chapter. We must 
incorporate network components into our system to make it distributed. Java provides a network package with classes supporting
TCP and UDP protocols. 

TCP (Transmission Control Protocol) requires establishing a connection before two entities can communicate. Once the connection
is set up, communication is bidirectional and messages between the two entities are guaranteed to arrive and arrive in order.

UDP (User Datagram Protocol) requires no such connection and therefore is more efficient. It does not guarantee the arrival of messages,
however. UDP wraps data into datagrams, and these datagrams can take different routes and arrive in different orders. If the
order is important, then the application layer must take care of it instead.

Our new `TestUDPServer` class:

 `private int serverPort;` - this is the port number that the server will be listening (expecting incoming messages) at it.
 It's akin to a TV chanel. Information targeting a specific port is sorted by the network card to the proper port, the 
 same way a signal for a specific TV channel can be viewed only when you switch that chanel. Port numbers 1-1024 are 
 reserved for special purposes, so applications should use other port numbers.

` private DatagramSocket serverUDPSocket;` - in Java UDP, both the server and the client make sure of `DatagramSocket` to send and receive
network messages. The only difference is that, a server `DatagramSocket` is always bound to a specific port, while a client
`DatagramSocket` is not.

 `private boolean isServerRunning = true;` is self-explanatory. It is a variable used to terminate the server when needed

` private Scanner userInput;` is self-explanatory. It stores a users input

**What is a socket?**

A socket is one end-point of a two-way communication link between two programs running on the network. Socket classes 
are used to represent the connection between a client program and a server program.

**What is a DatagramPacket and DatagramSocket?**

A `DatagramPacket` object is a data container. A `DatagramSocket` object is a type of network socket which provides 
connection-less point for sending and receiving `DatagramPacket` -- it is the mechanism used to send or receive `DatagramPackets`.

We use `DatagramPacket` to accept incoming messages. Upon receiving a message from the client `serverUDPSocket.receive(datagramPacket)`,
we display the client's information including client socket's IP address and listening port number, and fetch the data from
the `DatagramPacket` object.

The new `TestUDPClient` class is very similar to `TestUDPServer` but with two major differences. First, the client program's
`DatagramSocket` does not need to bind to a port. If we wanted to bind it to a port, the port should be different from the
one the server is bound to. The second difference lies in the `main()` method. This method requires the server's IP address
because the client needs to locate the server.

A network server starts automatically on the local computer with IP address "localhost". Therefore, when a server starts, it does
not need to be given an IP address.

Executing a test case is relatively straight forward. To execute on the same computer:
1) Start the program `TestUDPServer` on computer A. The server should be up and running before the client to prevent the client's messages from getting lost
2) Start the program `TestUDPClient` on computer A

The user will receive the following prompt in the client terminal. Enter `localhost`
```
What is the IP address of the server?:
localhost
```

Then enter a message to send to the server:

```
IP: localhost
UDP client starting now, server is listening at port 8888
Please enter your response: 
That's one small step for man. One giant leap for mankind
```

Toggle to the server side terminal, and you will see the incoming message!

```
Client:
port=
IP=127.0.0.1
Client:
 That's one small step for man. One giant leap for mankind
Please enter your response: 
Roger that
Server listening for incoming messages...
```

If you want to test this message system between two different computers, then:
1) Run the server side on computer A like explained above
2) Run the client side on computer B. When prompted to enter the IP address, enter the IP address of computer A instead of `localhost`

Java differentiates the socket for a server from the socket for a client in TCP. Servers run full time but client sockets
can be on and off. When a server socket is on, it keeps listening for connection requests from clients and upon obtaining a request,
the server socket forks a socket dedicated to that particular connection. From then on, all communication between the server
and the client is facilitated through this socket-to-socket connection.

Our new `TestTCPServer` class:

```
    public static final int port = 8888;
```

The server will be listening in at port 8888.

**What is a stream?**

A stream can be defined as a sequence of data. The `InputStream` is used to read data from a source and the `OutputStream`
is used for writing data to a destination

Some key code blocks within our `TestTCPServer` class:

```
   MessageManagerTCP messageManagerTCP = new MessageManagerTCP(socket, userTo);
   messageManagerTCP.start();
   System.out.println("Message manager started");
```

The `MessageManagerTCP` instance is dedicated to sending and receiving messages to and from the client

```
     CommunicationChannel communicationChannel = new CommunicationChannel(messageManagerTCP);
     communicationChannel.start();
```

The `CommunicationChannel` object is a thread dedicated to receiving inputs from `System.in`. The inputs are messages entered
by users.

**What is a thread?**

A thread enables multiprocessing in Java. For example, in our `UDP` network, messaging operated in a walkie-talkie like function
where one user sends a message and must wait for an incoming message before the user can send another. This wouldn't be the case if we utilized threads
like we do in `TCP`. Using `TCP`, the client or server can send consecutive messages without having to wait for a response. In other
words, `UDP` allows one message at a time (i.e., message from A -> message from B -> message from A) whereas `TCP` allows for sequential messages (i.e,
message from user A -> message from user A -> message from user B). This closely replicates the traditional "chat" applications


All Java programs have at least one thread, known as the main thread which is created by the JVM at the programs start. 
A single-threaded application has only one Java thread and can handle only one task at a time. To handle multiple tasks in parallel,
multi-threading is used: multiple Java threads are created, each performing a different task.

**Our network going forward**

TCP will our chosen protocol because it is the better choice when conducting business transactions with zero fault tolerance.
Blockchain can implement both TCP and UDP, just for different functions. Bitcoin, for example, started with TCP, but bitcoin's Fast
Internet Relay Engine (FIBRE) is a UDP-based relay network

**Building a P2P Environment**

In a P2P environment, each peer is a server and a client. In our first example, we will have a 2 user P2P chat program, each server
is dedicated to message receiving and each client is dedicated to sending messages.

The class `PeerTCP` contains three parts:
* the driver class `PeerTCP` itself
* `PeerTCPIncomingMessageManager`
* `PeerTCPOutgoingMessageManager`

`PeerTCP` starts an instance of `PeerTCPOutgoingMessageManager` before creating a TCP server socket. This is to avoid being blocked
by the server socket. `PeerTCPOutgoingMessageManager` wraps around a client socket and is dedicated to sending messages. `PeerTCPIncomingMessageManager`
wraps a server side socket connection to manage incoming messages. Not that `PeerTCPIncomingMessageManager` does not
send messages and `PeerTCPOutgoingMessageManager` does not receive messages.

**UDP and TCP chat programs**

The two chat programs we created above have the disadvantage of only allowing messaging between two users. Since blockchain has
a prominent broadcasting feature, we need to write another chat program in which more than two users can share their message on
a discussion board. For this multiuser chat program, we will have a central server that acts like a message routing center, accepting multiple connections
simultaneously and forwarding messages to all users. The central server is critical. Without it, no communication among the
users is available.

We create `SimpleTextMessage` class to unify the messages in the network. Every message received and sent is a `SimpleTextMessage`
containing the name of the sender and the message from the sender.

There's 3 classes needed on the server side:
1) a driver class which is also responsible for accepting incoming connection requests
2) a `UserChannelInfo` class to wrap the socket from the driver class and manage all socket-to-socket communication
3) a `ServerMessageManager` polls the queue from time to time, forwards messages to participating clients i.e., broadcasting

To test the TCP multiple-users chat program:
1) Compile the `TestTCPServerCenter` and `TestTCPClientFrame` classes
2) Execute the `TestTCPServerCenter` on computer A
3) Execute the `TestTCPClientFrame` on any computer
4) Start chatting

---
## Chapter 8 :: Distributed Blockchain System

In an ideal peer-to-peer (P2P) network, all nodes are equal. The crucial concept of P2P is to decentralize, i.e., there 
should not be a central server that controls everything. In an ideal environment, each peer is a server and a client.
Individuals can share their resources as a "server" and access the resources of others as a "client". We can essentially
abstract away the difference between these two terms as their definitions are implicit within the idea of a "peer". Instead,
the proper terminologies would be "outgoing/outbound" connections and "incoming/inbound" connections.

Depending on the architecture and implementation of the P2P network, an incoming connection could be a TCP client socket connection,
or it could be the responding socket connection from the server socket. The latter choice is not very common, however.

Bitcoin network is built upon a collection of nodes running the bitcoin P2P protocol. These peer nodes maintain the network routing
function. Each node is supposed to have 8 outgoing connections and can accept around 100 incoming connections. These nodes
and their connections make up the "backbone" of bitcoin, but the extended bitcoin network also involves nodes running the
P2P protocol and other specialized protocols.

Our next step is to adopt the star network model. Once we understand how a blockchain system can be implemented via the network
star model, we can then start implementing our P2P network.

The core of the star model is the server. Our server starts with 2 components:
* a genesis miner
* a message routing service provider

The genesis miner begins byb constructing a genesis block and jumpstarting the blockchain. Having formed the genesis blockchain, 
the genesis miner hands it over to the message routing service provider and ceases direct contract. From now on the genesis
miner behaves like any other wallet, forming a normal network connection with the message routing service provider.

By now, the message routing service provider becomes the only component in the server. The message routing service provider
(or just message service provider for short) is in charge of managing all incoming messages. It is composed of a network server
a collection of network connections and a message task manager.

Every node in our blockchain system is either a wallet or a miner and each connects to the message service provider (similar to
our `TestTCPServerCenter` class) through the network.

Except for the genesis miner, each wallet has 3 major components:
1) an agent dedicated to network connection
2) a task manager dedicated to processing messages
3) a GUI that facilitates contact between the user and the connection agent or the message task manager

Both the network connection agent and the message processing task manager are independent working threads. For a blockchain with
minimum functionalities, the network messages should include:
* Private and broadcast chat messages
* Block broadcast messages (broadcast a mined block)
* Transaction broadcast messages (broadcast a transaction)
* Private blockchain messages upon request
* Private messages paging available participants (addresses)
* Private messages containing available addresses upon request
* Private messages asking to close network connection

**Figure: the architecture of our blockchain application system**

<a href="https://ibb.co/KssdjLH"><img src="https://i.ibb.co/X551Z3T/PlsFix.png" alt="PlsFix" border="0"></a>

All messages in our system will be a subclass of the `Message` class. They all need to implement the 3 abstract methods:
* `getMessageType()`
* `getMessageBody()`
* `isForBroadcast()`

The `Message` class is an abstract class, meaning the objects cannot be directly created from it, and instead they can only be
constructed from its subclasses. The `Message` class is to define a standard applying to all message types used within the
system. 

**Why aren't we using interface instead?**

Both the abstract class and the interface allow polymorphisms in OOO states or other implemented methods. An abstract class,
on the other hand, is a perfect choice for the super class which is partially completed except for the part that different
subclasses can have different implementations.

For example, an equity option contract in finance is characterized by 5 parameters (strike, spot, vol, interest rate, time).
All equity options must contain these attributes, therefor `EquityOption` could be an abstract class, which is then extended
by say a `Put` class or a `Call` class.

For a private chat, a class named `MessageTextPrivate` is defined. When the message service provider (i.e., server) forwards
a private chat message, it needs to know who is the receiver, and the receiver needs to know the identity of the sender.
`MessageTextPrivate` must include:
* the text message
* the sender's public key and name
* the receiver's public key

The class `MessageTextBroadcast` is for public chat. It is similar to the `MessageTextPrivate` class, but since `MessageTextBroadcast`
is for public chat, it only needs to be broadcast. We can clone our `MessageTextPrivate` class and have the `isForBroadcast()`
method set to `true` while also adjusting our message type:

```
   public int getMessageType() {
     return Message.TEXT_BROADCAST;
   }
```

Every node in the blockchain is a wallet. When a wallet starts a transaction, this transaction is broadcast to all other nodes as
a public message. The class `MessageTransactionBroadcast` implements this functionality.

A transaction message does not need to be signed because the transaction itself is signed, and it is broadcast
publicly across the network. The same logic applies to block messages in our `MessageBlockBroadcast` class. 

The blockchain, the ledger itself, should not be broadcast, but only sent to wallets who request it. Broadcasting a blockchain
is actually very inefficient and difficult because the chain can grow to fairly large sizes.

Our last message type is `MessageAddressPrivate`. A wallet that wants a list of all participating wallets (including the genesis miner)
in the blockchain system can send a private query for this message. In most cases this isn't necessary because each wallet is constantly
and automatically updating its local wallet list whenever it receives a message from other wallets.

A wallet node is composed of 3 classes:
1) `WalletSimulator` for the GUI
2) `WalletConnectionAgent` for network connection including message receiving and sending
3) `WalletMessageTaskManager` that processes incoming messages including updating local blockchain and displaying chat messages

The class `WalletConnectionAgent` controls a wallet's network connection. It is responsible for:
1) Constructing a network connection between the wallet and the message service provider
2) Accepting messages from the message service provider and placing them in a queue for the `WalletMessageTaskManager` to process
3) Sending messages out
4) Properly closing the network connection

The `WalletMessageTaskManager` class major task is to poll the queue for available messages. If messages exist, the task manager
will process them based on the message types. There is a method written specifically for every message type except for
`MessageBlockchainBroadcast`. The reason is that `WalletMessageTaskManager` is a super class with two subclasses:
`MinerMessageTaskManager` and `MinerGenesisMessageTaskManager`. These two subclasses need to take different actions upon some
messages, which require them to override corresponding methods.

The `WalletSimulator` is the driver class for a wallet. It makes use of the `WalletConnectionAgent` and `WalletMessageTaskManager`
to mimic a wallets' functions. If it has the `MinerMessageTaskManager` instance, it will simulate a miner instead. The `WalletSimulator`
class contains four inner classes, each displaying a unique GUI.

The `WalletSimulator` class references the `MinerMessageTaskManager` class which is a subclass of the `WalletMessageTaskManager` class.
The `MinerMessageTaskManager` delineates the actions a miner should take upon receiving various message types -- actions different
from those of the wallets. However, both miners and wallets use the same `WalletSimulator` to start the application.
When mining a block, the miner should simultaneously be able to manage incoming messages, for example, continue with collecting transactions.
Thus, the mining process should be an independent thread, and therefore we need to have the `MinerTheWorker` class that is dedicated
to block mining only.

The class `MinerTheWorker` has two check points to decide whether to continue or terminate the mining. These check points are to
simulate the following scenario:
* in the middle of mining, the miner finds out that someone else has already successfully mined a block
* at this point, the mining process should stop right away so that the miner can move on and start mining the next block

Class `MinerGenesisMessageTaskManager` is a subclass of `MinerMessageTaskManager` (it could be designed as a subclass of
`MinerMessageTaskManager` instead). It overrides some methods so that a genesis miner can act differently than a common miner.
For example, a genesis miner does not chat, collect public transactions, or participate in mining competition. The genesis miner
has the added function of sending out sign-in bonuses.

The message routing services are provided in the program `BlockchainMessageServiceProvider`. The services include:
* accepting network connection requests
* relaying messages to proper receivers
* keeping a list of wallets (both name and public key)
* rendering each new, incoming wallet a genesis blockchain
* providing a name discovery service

It includes 3 classes:
1) `BlockchainMessageServiceProvider` provides the network server.
2) `ConnectionChannelTaskManager` manages a connection channel and the messages received and sent through this connection channel
3) `MessageCheckingTaskManager` constantly scrutinizes the message queue to process messages based on their origins, destinations, and types

The `BlockchainMessageServiceProvider` accepts incoming connections, and then creates a `ConnectionChannelTaskManager` thread to
specifically manage the connection between the server and the client. It also provides some storage and lookup services. There
is only one instance of `BlockchainMessageServiceProvider` on the server side.

Upon receiving a message, the `ConnectionChannelTaskManger` places the message into the designated queue. Every network connection has a dedicated instance
of `ConnectionChannelTaskManager` so there are multiple instances of it on the server side.

---
## Chapter 9 :: Peer-to-Peer Blockchain System

In our P2P implementation, a peer finds other peers through its directly-connected neighbor(s). A peer sends out messages 
periodically to search for other peers in the network. The receiving peers are required to forward such messages to their
directly-connected peers. Clearly, messages will be sent and received repeatedly.

For example, assume peer A has direct connection with peers B, C, and D, while peer X has direct connection with peers
A, C, and Y, and peer C has direct connection to A, X, and Y:

<a href="https://imgbb.com/"><img src="https://i.ibb.co/f2kLKR7/p2p-network.png" alt="p2p-network" border="0"></a>

When X sends a message to A, A will forward it to B, C, D and X. Then C will forward the same message to A, X, Y. Now Y would
forward it to X and C. The same message is repeatedly sent and received, and this becomes a serious unnecessary overheard.

A solution to this problem is to tag every message with a unique ID, timestamp, and a sender. Upon receiving, messages too
old will be discarded, messages that have already been processed by a peer will be discarded, and by default, the sender 
of a message will discard the same message forwarded back to him.

---
## Running the Application
* Run `BlockchainPlatform` on computer 1
* Run `WalletSimulator` on computer 2
* Run `WalletSimulator` on computer n

TODO:
1) Outline how to use the application
2) Refactor where necessary
3) Add additional functionality
   1) Display all messages on the message board (including the sender)
