package org.fidata.chef.gradle.tasks

import groovy.transform.CompileStatic
import org.fidata.chef.DataBagItem
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

@CompileStatic
final class KnifeSoloDataBagCreate extends DefaultTask {
  @Internal
  DirectoryProperty chefDir = project.objects.directoryProperty()

  @Internal
  Property<String> bagName = project.objects.property(String)

  @Internal
  Property<String> itemName = project.objects.property(String)

  @Input
  MapProperty<String, String> value = project.objects.mapProperty(String, String)

  private Directory getChefConfigDir() {
    project.layout.projectDirectory.dir(OperatingSystem.current().windows ? ' C:\\chef' : '/etc/chef')
  }

  @InputFile
  @Optional
  RegularFileProperty encryptedDataBagSecret = project.objects.fileProperty().convention {
    chefConfigDir.file('encrypted_data_bag_secret')
  }

  @OutputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  // TODO: Provider ?
  protected RegularFile getOutputFile() {
    chefDir.dir('data_bags').get().dir(bagName).get().file("${ itemName.get() }.json")
  }

  @TaskAction
  void create() {
    DataBagItem.create().save()
  }
}
