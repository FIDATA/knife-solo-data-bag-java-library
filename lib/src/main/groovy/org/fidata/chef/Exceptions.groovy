package org.fidata.chef

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@CompileStatic
class Exceptions {
  @InheritConstructors
  class InvalidDataBagName extends IllegalArgumentException {}
}
