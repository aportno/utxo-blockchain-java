# Learning Blockchain in Java by Hong Zhou

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

We wrote a test class that initiates a transaction.