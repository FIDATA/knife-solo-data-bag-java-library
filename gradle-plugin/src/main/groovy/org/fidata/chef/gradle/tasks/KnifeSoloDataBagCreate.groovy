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
import org.gradle.api.provider.Provider
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
  final DirectoryProperty chefDir = project.objects.directoryProperty()

  @Internal
  final Property<String> bagName = project.objects.property(String)

  @Internal
  final Property<String> itemName = project.objects.property(String)

  @Input
  final MapProperty<String, String> value = project.objects.mapProperty(String, String)

  private final Provider<Directory> chefConfigDir = project.provider {
    project.layout.projectDirectory.dir(OperatingSystem.current().windows ? ' C:\\chef' : '/etc/chef')
  }

  @InputFile
  @Optional
  // TODO: no encryption by default ?
  final RegularFileProperty encryptedDataBagSecret = project.objects.fileProperty().convention {
    chefConfigDir.get().file('encrypted_data_bag_secret')
  }

  @OutputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  protected final Provider<RegularFile> outputFile = project.provider {
    chefDir.dir('data_bags').get().dir(bagName).get().file("${ itemName.get() }.json")
  }

  @TaskAction
  void create() {
    DataBagItem.create().save()
  }
}
