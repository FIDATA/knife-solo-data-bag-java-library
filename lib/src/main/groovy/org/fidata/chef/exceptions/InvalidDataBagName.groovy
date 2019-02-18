package org.fidata.chef.exceptions

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@InheritConstructors
@CompileStatic
class InvalidDataBagName extends IllegalArgumentException {}
