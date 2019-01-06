package org.fidata.chef

import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class DataBag {
  static final Pattern VALID_NAME = ~/^[.-\p{Alnum}_]+$/
  static final Pattern RESERVED_NAMES = ~/^(node|role|environment|client)$/

  static void validateName(String name) {
    if (!name =~ VALID_NAME) {
      throw new Exceptions.InvalidDataBagName("DataBags must have a name matching ${ VALID_NAME.inspect() }, you gave ${ name.inspect() }");
    }
    if (name =~ RESERVED_NAMES) {
      throw new Exceptions.InvalidDataBagName("DataBags may not have a name matching ${ RESERVED_NAMES.inspect() }, you gave ${ name.inspect() }");
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
}
