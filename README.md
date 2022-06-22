# Learning Blockchain in Java by Hong Zhou

[Purchase on Amazon](https://www.amazon.com/Learning-Blockchain-Java-step-step/dp/1795002158)

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