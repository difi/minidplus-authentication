#deploy.pp
class minidplus_authentication::deploy inherits minidplus_authentication {

  difilib::spring_boot_deploy { $minidplus_authentication::application:
    package      => $minidplus_authentication::group_id,
    artifact     => $minidplus_authentication::artifact_id,
    service_name => $minidplus_authentication::service_name,
    install_dir  => "${minidplus_authentication::install_dir}${minidplus_authentication::application}",
    artifact_type => "war",
  }
}
