package org.fidata.chef.gradle.tasks

import groovy.transform.CompileStatic
import org.fidata.chef.DataBagItem
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
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
  final Property<String> dataBagsDirName = project.objects.property(String).convention('data_bags')

  @Internal
  final Property<String> bagName = project.objects.property(String)

  @Internal
  final Property<String> itemName = project.objects.property(String)

  @OutputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  protected final Provider<RegularFile> outputFile =
    /*
     * WORKAROUND:
     * No way to chain dir and file calls
     * https://github.com/gradle/gradle/issues/6390
     * https://discuss.gradle.org/t/chaining-together-file-directory-providers/27980
     * <grv87 2019-03-02>
     */
    /*chefDir.dir(dataBagsDirName).map(new Transformer<Directory, Directory>() {
      @Override
      Directory transform(Directory t) {
        t.dir(bagName.get())
      }
    }).map(new Transformer<RegularFile, Directory>() {
      @Override
      RegularFile transform(Directory t) {
        t.file(itemName.map

          "${ itemName.get() }.json")
      }
    })*/
    project.providers.provider { ->
      chefDir.dir(dataBagsDirName).get().dir(bagName.get()).file("${ itemName.get() }.json")
    }

  @Input
  final MapProperty<String, Object> value = project.objects.mapProperty(String, Object)

  private final Provider<Directory> chefSystemConfigDir = project.providers.provider { ->
    project.layout.projectDirectory.dir(OperatingSystem.current().windows ? ' C:\\chef' : '/etc/chef')
  }

  @Input
  final Property<Boolean> encrypt = project.objects.property(Boolean).convention(Boolean.FALSE)

  @Internal
  final Property<String> secret = project.objects.property(String)

  @Internal
  final RegularFileProperty secretFile = project.objects.fileProperty().convention { ->
    chefSystemConfigDir.get().file('encrypted_data_bag_secret')
  }

  @Input
  @Optional
  protected final Provider<String> secretIfEncrypted = project.providers.provider { ->
    if (encrypt.get()) {
      if (!(secret.present ^ secretFile.present)) {
        throw new IllegalArgumentException('Only one of secret or secretFile should be set') // TODO
      }
      return secret.present ? secret.get() : secretFile.get().asFile.text
    }
    (String)null
  }

  @Internal
  final Property<Integer> encryptVersion = project.objects.property(Integer).convention(3) /* TODO */

  @Input
  @Optional
  protected final Provider<Integer> encryptVersionIfEncrypted = project.providers.provider { ->
    encrypt.get() ? encryptVersion.get() : null
  }


  @TaskAction
  void create() {
    DataBagItem.create().save()
  }
}
