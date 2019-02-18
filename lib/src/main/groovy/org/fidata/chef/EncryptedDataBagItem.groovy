package org.fidata.chef

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.fidata.chef.exceptions.Exceptions

/**
 * An EncryptedDataBagItem represents a read-only data bag item where
 * all values, except for the value associated with the id key, have
 * been encrypted.
 *
 * EncryptedDataBagItem can be used in recipes to decrypt data bag item
 * members.
 *
 * Data bag item values are assumed to have been encrypted using the
 * default symmetric encryption provided by Encryptor.encrypt where
 * values are converted to YAML prior to encryption.
 *
 * If the shared secret is not specified at initialization or load,
 * then the contents of the file referred to in
 * Chef::Config[:encrypted_data_bag_secret] will be used as the
 * secret.  The default path is /etc/chef/encrypted_data_bag_secret
 *
 * EncryptedDataBagItem is intended to provide a means to avoid storing
 * data bag items in the clear on the Chef server.  This provides some
 * protection against a breach of the Chef server or of Chef server
 * backup data.  Because the secret must be stored in the clear on any
 * node needing access to an EncryptedDataBagItem, this approach
 * provides no protection of data bag items from actors with access to
 * such nodes in the infrastructure.
 */
@CompileStatic
class EncryptedDataBagItem {
  /**
   * Name of encryption algorithm for versions up to 2.
   *
   * <b>Note:</b> Java and Ruby name cipher algorithms differently,
   * and Chef uses Ruby's name without any abstraction.
   * Value of this contant in Java equals to the value
   * of the constant with the same name in Ruby
   * and represents Ruby's algorithm name.
   * For Java's algorithm name see {@link #JAVA_ALGORITHM}
   */
  static final String ALGORITHM = 'aes-256-cbc'
  /**
   * Name of encryption algorithm for versions up to 2 in Java.
   *
   * @see #ALGORITHM
   */
  static final String JAVA_ALGORITHM = 'AES/CBC/PKCS5Padding'
  /**
   * Name of encryption algorithm for version 3.
   *
   * <b>Note:</b> Java and Ruby name cipher algorithms differently,
   * and Chef uses Ruby's name without any abstraction.
   * Value of this contant in Java equals to the value
   * of the constant with the same name in Ruby
   * and represents Ruby's algorithm name.
   * For Java's algorithm name see {@link #JAVA_AEAD_ALGORITHM}
   */
  static final String AEAD_ALGORITHM = 'aes-256-gcm'
  /**
   * Name of encryption algorithm for version 3 in Java.
   *
   * @see #AEAD_ALGORITHM
   */
  static final String JAVA_AEAD_ALGORITHM = 'AES/GCM/NoPadding'

  final Map<String, String> encHash
  final secret

  /**
   * Create a new encrypted data bag item for reading (decryption)
   *
   * @param encHash
   *   The encrypted hash to be decrypted
   * @param secret
   *   The raw secret key
   */
  EncryptedDataBagItem(Map<String, String> encHash, secret) {
    this.@encHash = encHash
    this.@secret = secret
  }

  String getAt(String key) {
    String value = this.@encHash[key]
    if (key == 'id' || value == null) {
      value
    } else {
      Decryptor.for(value, this.@secret).forDecryptedItem
    }
  }

  /*def []=(key, value)
  raise ArgumentError, "assignment not supported for #{self.class}"
  end

  def to_h
  @enc_hash.keys.inject({}) { |hash, key| hash[key] = self[key]; hash }
  end

  alias_method :to_hash, :to_h

  def self.encrypt_data_bag_item(plain_hash, secret)
  plain_hash.inject({}) do |h, (key, val)|
  h[key] = if key != "id"
  Encryptor.new(val, secret).for_encrypted_item
  else
  val
  end
  h
  end
  end

  #
  # === Synopsis
  #
  #   EncryptedDataBagItem.load(data_bag, name, secret = nil)
  #
  # === Args
  #
  # +data_bag+::
  #   The name of the data bag to fetch
  # +name+::
  #   The name of the data bag item to fetch
  # +secret+::
  #   The raw secret key. If the +secret+ is nil, the value of the file at
  #   +Chef::Config[:encrypted_data_bag_secret]+ is loaded. See +load_secret+
  #   for more information.
  #
  # === Description
  #
  # Loads and decrypts the data bag item with the given name.
  #
  def self.load(data_bag, name, secret = nil)
  raw_hash = Chef::DataBagItem.load(data_bag, name)
  secret ||= load_secret
  new(raw_hash, secret)
  end

  def self.load_secret(path = nil)
  path ||= Chef::Config[:encrypted_data_bag_secret]
  if !path
  raise ArgumentError, "No secret specified and no secret found at #{Chef::Config.platform_specific_path('/etc/chef/encrypted_data_bag_secret')}"
  end
  secret = case path
  when /^\w+:\/\//
  # We have a remote key
  begin
  Kernel.open(path).read.strip
    rescue Errno::ECONNREFUSED
    raise ArgumentError, "Remote key not available from '#{path}'"
  rescue OpenURI::HTTPError
    raise ArgumentError, "Remote key not found at '#{path}'"
  end
  else
  if !File.exist?(path)
  raise Errno::ENOENT, "file not found '#{path}'"
  end
  IO.read(path).strip
  end
  if secret.size < 1
  raise ArgumentError, "invalid zero length secret in '#{path}'"
  end
  secret
  end*/

  @InheritConstructors
  static class DecryptionFailure extends RuntimeException {
  }

  @InheritConstructors
  static class UnacceptableEncryptedDataBagItemFormat extends RuntimeException {
  }

  @InheritConstructors
  static class UnsupportedEncryptedDataBagItemFormat extends RuntimeException {
  }

  @InheritConstructors
  static class UnsupportedCipher extends RuntimeException {
  }

  @InheritConstructors
  static class EncryptedDataBagRequirementsFailure extends RuntimeException {
  }

  static class Assertions {
    static void assertFormatVersionAcceptable(Object formatVersion) {
      if (!Integer.instanceOf(formatVersion) /* || TODO: formatVersion < Chef::Config[: data_bag_decrypt_minimum_version]*/) {
        throw new UnacceptableEncryptedDataBagItemFormat(
          "The encrypted data bag item has format version `${formatVersion}', " +
            "but the config setting 'data_bag_decrypt_minimum_version' requires version `#{Chef::Config[:data_bag_decrypt_minimum_version]}'"
        )
      }
    }

    static void assertValidCipher(requestedCipher, algorithm) {
      /*
       * In the future, chef may support configurable ciphers. For now, only
       * aes-256-cbc and aes-256-gcm are supported.
       */
      if (requestedCipher != algorithm) {
        throw new UnsupportedCipher(
          "Cipher '${requestedCipher}' is not supported by this version of Chef. Available ciphers: ['${ALGORITHM}', '${AEAD_ALGORITHM}']"
        )
      }
    }

    static void assertAeadRequirementsMet(algorithm) {
      /* TODO:
      if (!OpenSSL::Cipher.ciphers.include?(algorithm)) {
        throw new EncryptedDataBagRequirementsFailure(
          "The used Encrypted Data Bags version requires an OpenSSL version with \"${algorithm}\" algorithm support"
        )
      }*/
    }
  }

  /**
   * For backwards compatibility, Chef implements decryption/deserialization for
   * older encrypted data bag item formats in addition to the current version.
   * Each decryption/deserialization strategy is implemented as a class in this
   * namespace. For convenience the factory method +Decryptor.for()+ can be used
   * to create an instance of the appropriate strategy for the given encrypted
   * data bag value.
   */
  static class Decryptor extends Assertions {

    /**
     * Detects the encrypted data bag item format version and instantiates a
     * decryptor object for that version. Call {@link #forDecryptedItem} on the
     * resulting object to decrypt and deserialize it.
     */
    static Decryptor for1 /* TODO */(encryptedValue, key) {
      Object formatVersion = formatVersionOf(encryptedValue)

      assertFormatVersionAcceptable(formatVersion)
      switch (formatVersion) {
        case 3:
          new Version3Decryptor(encryptedValue, key)
          break
        case 2:
          new Version2Decryptor(encryptedValue, key)
          break
        case 1:
          new Version1Decryptor(encryptedValue, key)
          break
        case 0:
          new Version0Decryptor(encryptedValue, key)
          break
        default:
          throw new UnsupportedEncryptedDataBagItemFormat(
            "This version of chef does not support encrypted data bag item format version '${format_version}'"
          )
      }
    }

    Object formatVersionOf(encryptedValue) {
      if (encryptedValue.respondTo ? ( : key ?) ) {
        encryptedValue['version']
      } else {
        0
      }
    }

    static class Version0Decryptor extends Assertions {
      final encryptedData
      final key

      Version0Decryptor(encryptedData, key) {
        this.@encryptedData = encryptedData
        this.@key = key
      }

      #
      Returns the
      used decryption
      algorithm
      def algorithm
      ALGORITHM
        end

      def forDecryptedItem() {
        YAML.load(decrypted_data)
      }

      def decryptedData() {
        @decrypted_data || = begin
        plaintext = openssl_decryptor.update(encrypted_bytes)
        plaintext << openssl_decryptor.
          final
        rescue OpenSSL
          ::Cipher::CipherError = > e
        # if
        the key
        length is
        less than
        255 characters,
          and it
        contains slashes, we
        think it
        may be
        a path
          .
          raise DecryptionFailure
        ,
        "Error decrypting data bag value: '#{e.message}'. Most likely the provided key is incorrect. #{(@key.length < 255 && @key.include?('/')) ? 'You may need to use --secret-file rather than --secret.' : ''}"
        end
      }

      def encryptedBytes() {
        Base64.decode64(@ encrypted_data)
      }

      def opensslDecryptor() {
        @openssl_decryptor || = begin
        d = OpenSSL::Cipher.new(algorithm)
        d.decrypt
        d
          .

          pkcs5_keyivgen(key)

        d
        end
      }
    }

    static class Version1Decryptor extends Version0Decryptor {
      final encryptedData
      final key

      Version1Decryptor(encryptedData, key) {
        this.@encryptedData = encryptedData
        this.@key = key
      }

      def forDecryptedItem() {
        try {
          Chef::JSONCompat.parse(decrypted_data)["json_wrapper"]
        } catch (Chef::Exceptions::JSON::ParseError ) {
          /*
         * convert to a DecryptionFailure error because the most likely scenario
         * here is that the decryption step was unsuccessful but returned bad
         * data rather than raising an error.
         */
          throw new DecryptionFailure(
            "Error decrypting data bag value. Most likely the provided key is incorrect"
          )
        }
      }

      def encryptedBytes() {
        Base64.decode64(@ encrypted_data["encrypted_data"])
      }

      def iv() {
        Base64.decode64(@ encrypted_data["iv"])
      }

      def decryptedData() {
        @decrypted_data || = begin
        plaintext = openssl_decryptor.update(encrypted_bytes)
        plaintext << openssl_decryptor.
          final
        rescue OpenSSL
          ::Cipher::CipherError = > e
        # if
        the key
        length is
        less than
        255 characters,
          and it
        contains slashes, we
        think it
        may be
        a path
          .
          raise DecryptionFailure
        ,
        "Error decrypting data bag value: '#{e.message}'. Most likely the provided key is incorrect. #{( @key.length < 255 && @key.include?('/')) ? 'You may need to use --secret-file rather than --secret.' : ''}"
        end
      }

      def opensslDecryptor() {
        @openssl_decryptor || = begin
        assert_valid_cipher !( @encrypted_data ["cipher"] , algorithm )
        d = OpenSSL::Cipher.new(algorithm)
        d.decrypt
        #
        We must
        set key
        before iv
        : https://bugs.ruby-lang.org/issues/8221
        d.key = OpenSSL::Digest::SHA256.digest(key)
        d.iv = iv
        d
        end
      }
    }

    static class Version2Decryptor extends Version1Decryptor {

      def decrypted_data() {
        validate_hmac !unless @decrypted_data
        super
      }

      def validate_hmac() {
        digest = OpenSSL::Digest.new("sha256")
        raw_hmac = OpenSSL::HMAC.digest(digest, key, @ encrypted_data["encrypted_data"])

        if (candidate_hmac_matches ? (raw_hmac)) {
          true
        } else {
          throw new DecryptionFailure(
            'Error decrypting data bag value: invalid hmac. Most likely the provided key is incorrect'
          )
        }
      }


      private def candidate_hmac_matches() {
        ? (expected_hmac)
        return false
        unless @encrypted_data ["hmac"]
        expected_bytes =
          expected_hmac.bytes.to_a
        candidate_hmac_bytes = Base64.decode64(@ encrypted_data["hmac"]).bytes.to_a
        valid = expected_bytes.size ^
          candidate_hmac_bytes.size
        expected_bytes
          .

          zip(candidate_hmac_bytes) { | x , y | valid |= x ^ y.to_i }

        valid == 0
        end
      }
    }

    static class Version3Decryptor extends Version1Decryptor {

      Version3Decryptor(encryptedData, key) {
        super()
        assertAeadRequirementsMet(algorithm)
      }

      /**
       * Returns the used decryption algorithm
       */
      String algorithm() {
        AEAD_ALGORITHM
      }

      def authTag() {
        auth_tag_b64 = @encrypted_data ["auth_tag"]
        if
        auth_tag_b64.nil ?
          raise DecryptionFailure
        , "Error decrypting data bag value: invalid authentication tag. Most likely the data is corrupted"
        end
        Base64.decode64(auth_tag_b64)
      }

      def openssl_decryptor() {
        @openssl_decryptor || = begin
        d = super
        d.auth_tag = auth_tag
        d.auth_data = ""
        d
        end
      }

    }

  }
}
