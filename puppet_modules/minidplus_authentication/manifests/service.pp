#service.pp
class minidplus_authentication::service inherits minidplus_authentication {

  include platform

  if ($platform::deploy_spring_boot) {
    service { $minidplus_authentication::service_name:
      ensure => running,
      enable => true,
    }
  }
}
