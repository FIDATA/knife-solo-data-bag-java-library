package org.fidata.chef

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
class EncryptedDataBagItem {
  static final String ALGORITHM = 'aes-256-cbc'
  static final String AEAD_ALGORITHM = 'aes-256-gcm'

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

  def []=(key, value)
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
  end

}