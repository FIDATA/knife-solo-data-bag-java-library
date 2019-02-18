package org.fidata.chef.gradle

import groovy.transform.CompileStatic
import org.fidata.chef.gradle.tasks.KnifeSoloDataBagCreate
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class KnifeSoloDataBagPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    project.extensions.extraProperties[KnifeSoloDataBagCreate.simpleName] = KnifeSoloDataBagCreate
  }
}
