package org.fidata.chef

import groovy.transform.InheritConstructors

class Exceptions {
  @InheritConstructors
  class InvalidDataBagName extends IllegalArgumentException {}
}
