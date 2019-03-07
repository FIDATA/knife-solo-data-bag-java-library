package org.fidata.chef

import groovy.transform.CompileStatic
import org.fidata.chef.exceptions.InvalidDataBagName
import java.util.regex.Pattern

@CompileStatic
class DataBag {
  static final Pattern VALID_NAME = ~/^[.-\\p{Alnum}_]+$/
  static final Pattern RESERVED_NAMES = ~/^(node|role|environment|client)$/

  static void validateName(String name) {
    if (!name =~ VALID_NAME) {
      throw new InvalidDataBagName("DataBags must have a name matching ${ VALID_NAME.inspect() }, you gave ${ name.inspect() }");
    }
    if (name =~ RESERVED_NAMES) {
      throw new InvalidDataBagName("DataBags may not have a name matching ${ RESERVED_NAMES.inspect() }, you gave ${ name.inspect() }");
    }
  }

  private String name

  def name(arg = null) {
    set_or_return(
      :name,
      arg,
      regex: VALID_NAME
    )
  }

  Map<String, String> toH() {
    [
      'name'      : @name,
      'json_class': self.class.name,
      'chef_type' : 'data_bag',
    ]
  }

  Map<String, String> toHash() {
    toH()
  }

  /**
   * Serialize this object as a hash
   */
  String toJson(*a) {
    Chef::JSONCompat.toJson(toH(), *a)
  }



  /**
   * As a string
   * @return
   */
  String toString() {
    "data_bag[${name}]"
  }

  def chef_server_rest
    @chef_server_rest ||= Chef::ServerAPI.new(Chef::Config[:chef_server_url])
  end

  def self.chef_server_rest
    Chef::ServerAPI.new(Chef::Config[:chef_server_url])
  end

  def self.from_hash(o)
    bag = new
    bag.name(o["name"])
    bag
  end

  def self.list(inflate = false)
    if Chef::Config[:solo_legacy_mode]
      paths = Array(Chef::Config[:data_bag_path])
      names = []
      paths.each do |path|
        unless File.directory?(path)
          raise Chef::Exceptions::InvalidDataBagPath, "Data bag path '#{path}' is invalid"
        end

        names += Dir.glob(File.join(
          Chef::Util::PathHelper.escape_glob_dir(path), "*")).map { |f| File.basename(f) }.sort
      end
      names.inject({}) { |h, n| h[n] = n; h }
    else
      if inflate
        # Can't search for all data bags like other objects, fall back to N+1 :(
        list(false).inject({}) do |response, bag_and_uri|
          response[bag_and_uri.first] = load(bag_and_uri.first)
          response
        end
      else
        Chef::ServerAPI.new(Chef::Config[:chef_server_url]).get("data")
      end
    end
  end

  # Load a Data Bag by name via either the RESTful API or local data_bag_path if run in solo mode
  void load(name)
    paths = Array(Chef::Config[:data_bag_path])
    data_bag = {}
    paths.each do |path|
      unless File.directory?(path)
        raise Chef::Exceptions::InvalidDataBagPath, "Data bag path '#{path}' is invalid"
      end

      Dir.glob(File.join(Chef::Util::PathHelper.escape_glob_dir(path, name.to_s), "*.json")).inject({}) do |bag, f|
        item = Chef::JSONCompat.parse(IO.read(f))

        # Check if we have multiple items with similar names (ids) and raise if their content differs
        if data_bag.key?(item["id"]) && data_bag[item["id"]] != item
          raise Chef::Exceptions::DuplicateDataBagItem, "Data bag '#{name}' has items with the same name '#{item["id"]}' but different content."
        else
          data_bag[item["id"]] = item
        end
      end
    end
    data_bag
  end

  def destroy
    chef_server_rest.delete("data/#{@name}")
  end

  # Save the Data Bag via RESTful API
  def save
    begin
      if Chef::Config[:why_run]
        Chef::Log.warn("In why-run mode, so NOT performing data bag save.")
      else
        create
      end
    rescue Net::HTTPClientException => e
      raise e unless e.response.code == "409"
    end
    self
  end

  # create a data bag via RESTful API
  def create
    chef_server_rest.post("data", self)
    self
  end
}
