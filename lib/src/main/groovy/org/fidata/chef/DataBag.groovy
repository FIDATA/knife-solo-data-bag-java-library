package org.fidata.chef

import java.util.regex.Pattern

class DataBag {
  static final Pattern VALID_NAME = ~/^[.-\p{Alnum}_]+$/
  static final Pattern RESERVED_NAMES = ~/^(node|role|environment|client)$/

  static void validateName(String name) {
    if (!name =~ VALID_NAME) {
      throw new Exceptions.InvalidDataBagName("DataBags must have a name matching ${ VALID_NAME.inspect() }, you gave ${ name.inspect() }");
    }
    if (name =~ RESERVED_NAMES) {
        throw new Exceptions.InvalidDataBagName("DataBags may not have a name matching ${ RESERVED_NAMES.inspect() }, you gave ${ name.inspect }");
    }
  }

  private String name

  def name(arg = nil)
  set_or_return(
    :name,
    arg,
    regex: VALID_NAME
  )
  end

  def to_h
  result = {
    "name"       => @name,
    "json_class" => self.class.name,
    "chef_type"  => "data_bag",
  }
  result
  end

  alias_method :to_hash, :to_h

  # Serialize this object as a hash
  def to_json(*a)
  Chef::JSONCompat.to_json(to_h, *a)
  end

  /**
   * As a string
   * @return
   */
  String toString() {
    "data_bag[${name}]"
  }
}
